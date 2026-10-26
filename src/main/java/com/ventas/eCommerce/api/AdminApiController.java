package com.ventas.eCommerce.api;

import com.ventas.eCommerce.dto.ApiDtos.StatusUpdateRequest;
import com.ventas.eCommerce.dto.DashboardStats;
import com.ventas.eCommerce.dto.OrderResponse;
import com.ventas.eCommerce.enums.OrderStatus;
import com.ventas.eCommerce.services.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminApiController {

    private final OrderService orderService;

    @GetMapping("/stats")
    public DashboardStats stats() {
        return orderService.dashboardStats();
    }

    @GetMapping("/orders")
    public Page<OrderResponse> orders(@RequestParam(required = false) OrderStatus status,
                                      @PageableDefault(size = 20) Pageable pageable) {
        return orderService.search(status, pageable);
    }

    @GetMapping("/orders/{id}")
    public OrderResponse order(@PathVariable Integer id) {
        return orderService.get(id);
    }

    @PatchMapping("/orders/{id}/status")
    public OrderResponse changeStatus(@PathVariable Integer id, @Valid @RequestBody StatusUpdateRequest request) {
        return orderService.changeStatus(id, request.status());
    }
}
