package com.ventas.eCommerce.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ventas.eCommerce.TestData;
import com.ventas.eCommerce.dto.OrderResponse;
import com.ventas.eCommerce.entities.Product;
import com.ventas.eCommerce.entities.User;
import com.ventas.eCommerce.enums.OrderStatus;
import com.ventas.eCommerce.enums.Rol;
import com.ventas.eCommerce.exceptions.BusinessException;
import com.ventas.eCommerce.exceptions.InsufficientStockException;
import com.ventas.eCommerce.repositories.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OrderServiceTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private CartService cartService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private TestData data;

    @Test
    void checkoutReservesStockSnapshotsPriceAndClearsCart() {
        User user = data.user(Rol.USER);
        Product product = data.product("1500.50", 10);
        cartService.addItem(user.getId(), product.getId(), 3);

        OrderResponse order = orderService.checkout(user.getId(), TestData.checkoutForm());

        assertThat(order.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.total()).isEqualByComparingTo("4501.50");
        assertThat(order.items()).singleElement()
                .satisfies(item -> {
                    assertThat(item.quantity()).isEqualTo(3);
                    assertThat(item.unitPrice()).isEqualByComparingTo("1500.50");
                });
        assertThat(stockOf(product)).isEqualTo(7);
        assertThat(cartService.getCart(user.getId()).isEmpty()).isTrue();
    }

    @Test
    void checkoutFailsAndRollsBackWhenStockIsInsufficient() {
        User user = data.user(Rol.USER);
        Product plenty = data.product("100", 10);
        Product scarce = data.product("100", 2);
        cartService.addItem(user.getId(), plenty.getId(), 5);
        cartService.addItem(user.getId(), scarce.getId(), 2);
        // Otro cliente compra una unidad del producto escaso mientras tanto.
        scarce.setStock(1);
        productRepository.save(scarce);

        assertThatThrownBy(() -> orderService.checkout(user.getId(), TestData.checkoutForm()))
                .isInstanceOf(InsufficientStockException.class);

        // El descuento del otro producto se revirtió y el carrito sigue intacto.
        assertThat(stockOf(plenty)).isEqualTo(10);
        assertThat(cartService.getCart(user.getId()).totalQuantity()).isEqualTo(7);
    }

    @Test
    void onlyOneOfTwoConcurrentBuyersGetsTheLastUnit() throws Exception {
        Product product = data.product("999", 1);
        User first = data.user(Rol.USER);
        User second = data.user(Rol.USER);
        cartService.addItem(first.getId(), product.getId(), 1);
        cartService.addItem(second.getId(), product.getId(), 1);

        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            List<Callable<Boolean>> buyers = List.of(buyer(first, start), buyer(second, start));
            List<Future<Boolean>> results = buyers.stream().map(executor::submit).toList();
            start.countDown();

            long successes = 0;
            for (Future<Boolean> result : results) {
                if (result.get()) {
                    successes++;
                }
            }
            assertThat(successes).isEqualTo(1);
            assertThat(stockOf(product)).isZero();
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void cancellingReturnsStock() {
        User user = data.user(Rol.USER);
        Product product = data.product("100", 5);
        cartService.addItem(user.getId(), product.getId(), 4);
        OrderResponse order = orderService.checkout(user.getId(), TestData.checkoutForm());
        assertThat(stockOf(product)).isEqualTo(1);

        orderService.cancelByUser(order.id(), user.getId());

        assertThat(orderService.get(order.id()).status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(stockOf(product)).isEqualTo(5);
        assertThatThrownBy(() -> orderService.cancelByUser(order.id(), user.getId()))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void markPaidIsIdempotentAndChecksAmount() {
        User user = data.user(Rol.USER);
        Product product = data.product("250", 5);
        cartService.addItem(user.getId(), product.getId(), 2);
        OrderResponse order = orderService.checkout(user.getId(), TestData.checkoutForm());

        orderService.markPaid(order.id(), "PAY-WRONG", new BigDecimal("1"));
        assertThat(orderService.get(order.id()).status()).isEqualTo(OrderStatus.PENDING);

        orderService.markPaid(order.id(), "PAY-1", new BigDecimal("500.00"));
        orderService.markPaid(order.id(), "PAY-1", new BigDecimal("500.00"));

        OrderResponse paid = orderService.get(order.id());
        assertThat(paid.status()).isEqualTo(OrderStatus.PAID);
        assertThat(paid.paymentId()).isEqualTo("PAY-1");
    }

    @Test
    void adminStatusTransitionsAreValidated() {
        User user = data.user(Rol.USER);
        Product product = data.product("100", 5);
        cartService.addItem(user.getId(), product.getId(), 1);
        OrderResponse order = orderService.checkout(user.getId(), TestData.checkoutForm());

        assertThatThrownBy(() -> orderService.changeStatus(order.id(), OrderStatus.SHIPPED))
                .isInstanceOf(BusinessException.class);

        orderService.changeStatus(order.id(), OrderStatus.PAID);
        orderService.changeStatus(order.id(), OrderStatus.SHIPPED);
        assertThat(orderService.changeStatus(order.id(), OrderStatus.DELIVERED).status())
                .isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    void cartRejectsQuantitiesAboveStock() {
        User user = data.user(Rol.USER);
        Product product = data.product("100", 3);

        assertThatThrownBy(() -> cartService.addItem(user.getId(), product.getId(), 4))
                .isInstanceOf(BusinessException.class);
        cartService.addItem(user.getId(), product.getId(), 2);
        assertThatThrownBy(() -> cartService.addItem(user.getId(), product.getId(), 2))
                .isInstanceOf(BusinessException.class);
        assertThat(cartService.updateQuantity(user.getId(), product.getId(), 3).totalQuantity()).isEqualTo(3);
        assertThat(cartService.updateQuantity(user.getId(), product.getId(), 0).isEmpty()).isTrue();
    }

    private Callable<Boolean> buyer(User user, CountDownLatch start) {
        return () -> {
            start.await();
            try {
                orderService.checkout(user.getId(), TestData.checkoutForm());
                return true;
            } catch (InsufficientStockException e) {
                return false;
            }
        };
    }

    private int stockOf(Product product) {
        return productRepository.findById(product.getId()).orElseThrow().getStock();
    }
}
