package com.ventas.eCommerce.api;

import com.ventas.eCommerce.config.AppProperties;
import com.ventas.eCommerce.dto.ApiDtos.CheckoutResponse;
import com.ventas.eCommerce.dto.CheckoutForm;
import com.ventas.eCommerce.dto.OrderResponse;
import com.ventas.eCommerce.security.AuthUser;
import com.ventas.eCommerce.services.OrderService;
import com.ventas.eCommerce.services.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderService orderService;
    private final PaymentService paymentService;
    private final AppProperties appProperties;

    @GetMapping
    public Page<OrderResponse> list(@AuthenticationPrincipal AuthUser user,
                                    @PageableDefault(size = 10) Pageable pageable) {
        return orderService.findByUser(user.id(), pageable);
    }

    @GetMapping("/{id}")
    public OrderResponse get(@AuthenticationPrincipal AuthUser user, @PathVariable Integer id) {
        return orderService.getForUser(id, user.id());
    }

    /** Convierte el carrito en un pedido y devuelve la URL del checkout de la pasarela. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CheckoutResponse checkout(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody CheckoutForm form) {
        OrderResponse order = orderService.checkout(user.id(), form);
        return new CheckoutResponse(order.id(), absolute(paymentService.startPayment(order.id(), user.id())));
    }

    /** Reintenta el pago de un pedido pendiente. */
    @PostMapping("/{id}/payment")
    public CheckoutResponse pay(@AuthenticationPrincipal AuthUser user, @PathVariable Integer id) {
        return new CheckoutResponse(id, absolute(paymentService.startPayment(id, user.id())));
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancel(@AuthenticationPrincipal AuthUser user, @PathVariable Integer id) {
        orderService.cancelByUser(id, user.id());
        return orderService.getForUser(id, user.id());
    }

    private String absolute(String url) {
        return url.startsWith("/") ? appProperties.baseUrl() + url : url;
    }
}
