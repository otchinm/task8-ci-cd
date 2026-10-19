package ru.study.shop.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.study.shop.model.Order;
import ru.study.shop.service.OrderService;

import java.net.URI;

/**
 * POST /api/orders      {"productId":1,"quantity":2}  -> 201 + заказ
 * GET  /api/orders/{id}                               -> 200 + заказ
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    public record CreateOrderRequest(@NotNull(message = "productId обязателен") Long productId,
                                     @Min(value = 1, message = "минимум 1 шт.")
                                     @Max(value = 100, message = "максимум 100 шт.") int quantity) {
    }

    public record OrderResponse(Long id, Long productId, String productName, int quantity, int total) {
        static OrderResponse of(Order o) {
            return new OrderResponse(o.getId(), o.getProduct().getId(), o.getProduct().getName(), o.getQuantity(), o.getTotal());
        }
    }

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        Order order = orderService.create(request.productId(), request.quantity());
        return ResponseEntity.created(URI.create("/api/orders/" + order.getId())).body(OrderResponse.of(order));
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable Long id) {
        return OrderResponse.of(orderService.getById(id));
    }
}
