package com.ventas.eCommerce.services;

import com.ventas.eCommerce.config.AppProperties;
import com.ventas.eCommerce.dto.CheckoutForm;
import com.ventas.eCommerce.dto.DashboardStats;
import com.ventas.eCommerce.dto.OrderResponse;
import com.ventas.eCommerce.dto.ProductResponse;
import com.ventas.eCommerce.entities.Cart;
import com.ventas.eCommerce.entities.CartItem;
import com.ventas.eCommerce.entities.Order;
import com.ventas.eCommerce.entities.OrderItem;
import com.ventas.eCommerce.entities.Product;
import com.ventas.eCommerce.enums.OrderStatus;
import com.ventas.eCommerce.enums.Rol;
import com.ventas.eCommerce.exceptions.BusinessException;
import com.ventas.eCommerce.exceptions.InsufficientStockException;
import com.ventas.eCommerce.exceptions.ResourceNotFoundException;
import com.ventas.eCommerce.repositories.OrderRepository;
import com.ventas.eCommerce.repositories.ProductRepository;
import com.ventas.eCommerce.repositories.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartService cartService;
    private final AppProperties appProperties;

    /**
     * Convierte el carrito en un pedido PENDIENTE y reserva el stock.
     *
     * <p>El stock se descuenta con un UPDATE condicional ({@code stock >= cantidad}) por producto,
     * dentro de una única transacción: si algún producto no alcanza, se lanza una excepción y se
     * revierte todo, incluidos los descuentos ya hechos. Los productos se procesan ordenados por id
     * para que dos compras simultáneas bloqueen las filas en el mismo orden y no generen deadlocks.
     */
    @Transactional
    public OrderResponse checkout(Integer userId, CheckoutForm form) {
        Cart cart = cartService.findCart(userId);
        if (cart.isEmpty()) {
            throw new BusinessException("Tu carrito está vacío");
        }

        Order order = new Order();
        order.setUser(userRepository.getReferenceById(userId));
        order.setShippingAddress(form.toShippingAddress());

        List<CartItem> items = cart.getItems().stream()
                .sorted(Comparator.comparing(item -> item.getProduct().getId()))
                .toList();

        for (CartItem item : items) {
            Product product = item.getProduct();
            if (!product.isActive()) {
                throw new BusinessException("\"" + product.getName() + "\" ya no está disponible. Quitalo del carrito para continuar.");
            }
            if (productRepository.decrementStock(product.getId(), item.getQuantity()) == 0) {
                throw new InsufficientStockException(product.getName());
            }
            order.addItem(new OrderItem(product, item.getQuantity()));
        }

        orderRepository.save(order);
        cart.clear();
        log.info("Pedido {} creado para el usuario {} por {}", order.getId(), userId, order.getTotal());
        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> findByUser(Integer userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, withDefaultSort(pageable)).map(OrderResponse::summary);
    }

    @Transactional(readOnly = true)
    public OrderResponse getForUser(Integer orderId, Integer userId) {
        return OrderResponse.from(findOwned(orderId, userId));
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> search(OrderStatus status, Pageable pageable) {
        return orderRepository.search(status, withDefaultSort(pageable)).map(OrderResponse::summary);
    }

    @Transactional(readOnly = true)
    public OrderResponse get(Integer orderId) {
        return OrderResponse.from(findDetailed(orderId));
    }

    /** Devuelve la entidad completa (usuario e items cargados) de un pedido pendiente del usuario. */
    @Transactional(readOnly = true)
    public Order getPayableOrder(Integer orderId, Integer userId) {
        Order order = findOwned(orderId, userId);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("El pedido #" + orderId + " ya no está pendiente de pago");
        }
        return order;
    }

    @Transactional
    public void registerPaymentAttempt(Integer orderId, String provider, String reference) {
        Order order = findDetailed(orderId);
        order.setPaymentProvider(provider);
        order.setPaymentReference(reference);
    }

    /**
     * Marca el pedido como pagado. Es idempotente: la pasarela puede notificar el mismo pago
     * varias veces (webhook + redirección de vuelta).
     */
    @Transactional
    public void markPaid(Integer orderId, String paymentId, BigDecimal amount) {
        Order order = findDetailed(orderId);

        if (paymentId.equals(order.getPaymentId())) {
            return;
        }
        if (amount != null && order.getTotal().compareTo(amount) != 0) {
            log.error("Pago {} del pedido {} por {} no coincide con el total {}. Requiere revisión manual.",
                    paymentId, orderId, amount, order.getTotal());
            return;
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            // Ej.: el pedido se canceló por vencimiento y el pago llegó tarde. El stock ya se liberó,
            // así que no se confirma automáticamente: queda registrado para reembolsar o resolver a mano.
            log.error("Pago aprobado {} para el pedido {} en estado {}. Requiere revisión manual.",
                    paymentId, orderId, order.getStatus());
            order.setPaymentId(paymentId);
            return;
        }

        order.setPaymentId(paymentId);
        order.changeStatus(OrderStatus.PAID);
        log.info("Pedido {} pagado (pago {})", orderId, paymentId);
    }

    @Transactional
    public void cancelByUser(Integer orderId, Integer userId) {
        Order order = findOwned(orderId, userId);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("Solo se pueden cancelar pedidos pendientes de pago");
        }
        cancel(order);
    }

    @Transactional
    public OrderResponse changeStatus(Integer orderId, OrderStatus newStatus) {
        Order order = findDetailed(orderId);
        if (!order.getStatus().canTransitionTo(newStatus)) {
            throw new BusinessException("No se puede pasar un pedido de \"" + order.getStatus().getLabel()
                    + "\" a \"" + newStatus.getLabel() + "\"");
        }
        if (newStatus == OrderStatus.CANCELLED) {
            cancel(order);
        } else {
            order.changeStatus(newStatus);
        }
        return OrderResponse.from(order);
    }

    /** Cancela los pedidos pendientes cuya reserva de stock venció. */
    @Transactional
    public int expirePendingOrders() {
        LocalDateTime limit = LocalDateTime.now().minus(appProperties.orders().reservationTime());
        List<Order> expired = orderRepository.findByStatusAndCreatedAtBefore(OrderStatus.PENDING, limit);
        expired.forEach(this::cancel);
        if (!expired.isEmpty()) {
            log.info("Se cancelaron {} pedido(s) pendientes vencidos", expired.size());
        }
        return expired.size();
    }

    @Transactional(readOnly = true)
    public DashboardStats dashboardStats() {
        BigDecimal revenue = orderRepository.sumTotalByStatusIn(
                EnumSet.of(OrderStatus.PAID, OrderStatus.SHIPPED, OrderStatus.DELIVERED));
        List<OrderResponse> latest = orderRepository.search(null,
                        PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(OrderResponse::summary).getContent();
        List<ProductResponse> lowStock = productRepository
                .findByActiveTrueAndStockLessThanEqualOrderByStockAsc(ProductService.LOW_STOCK_THRESHOLD)
                .stream().limit(5).map(ProductResponse::from).toList();

        return new DashboardStats(
                productRepository.countByActiveTrue(),
                productRepository.countByActiveTrueAndStockLessThanEqual(ProductService.LOW_STOCK_THRESHOLD),
                userRepository.countByRol(Rol.USER),
                orderRepository.countByStatus(OrderStatus.PENDING),
                orderRepository.countByStatus(OrderStatus.PAID),
                orderRepository.countByStatus(OrderStatus.SHIPPED),
                revenue,
                latest,
                lowStock);
    }

    private void cancel(Order order) {
        for (OrderItem item : order.getItems()) {
            if (item.getProduct() != null) {
                productRepository.incrementStock(item.getProduct().getId(), item.getQuantity());
            }
        }
        order.changeStatus(OrderStatus.CANCELLED);
        log.info("Pedido {} cancelado; stock devuelto", order.getId());
    }

    private Order findDetailed(Integer orderId) {
        return orderRepository.findDetailedById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
    }

    private Order findOwned(Integer orderId, Integer userId) {
        Order order = findDetailed(orderId);
        if (!order.getUser().getId().equals(userId)) {
            // Mismo mensaje que si no existiera, para no revelar pedidos de otros usuarios.
            throw new ResourceNotFoundException("Pedido no encontrado");
        }
        return order;
    }

    private static Pageable withDefaultSort(Pageable pageable) {
        return pageable.getSort().isSorted()
                ? pageable
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}
