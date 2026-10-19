package ru.study.shop;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.study.shop.model.Product;
import ru.study.shop.repository.OrderRepository;
import ru.study.shop.repository.ProductRepository;
import ru.study.shop.service.OrderService;
import ru.study.shop.web.OrderController;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * НИСХОДЯЩИЕ тесты (сверху вниз).
 * Реальные: контроллер + сервис. Заглушки (@MockitoBean): оба репозитория (БД нет).
 * Проверяем HTTP-коды, JSON, валидацию и реакцию на ошибки сервиса.
 */
@WebMvcTest(OrderController.class)
@Import(OrderService.class)
class OrderTopDownIT {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    ProductRepository productRepository;

    @MockitoBean
    OrderRepository orderRepository;

    private static String order(long productId, int quantity) {
        return "{\"productId\":" + productId + ",\"quantity\":" + quantity + "}";
    }

    @Test
    @DisplayName("TD-1 Позитив: заказ создан -> 201, Location, сумма = цена * количество")
    void createOrder_valid_returns201() throws Exception {
        when(productRepository.findById(1L)).thenReturn(Optional.of(new Product("Книга", 500, 10)));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(order(1, 2)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.productName").value("Книга"))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.total").value(1000));
    }

    @Test
    @DisplayName("TD-2 Негатив: товара нет -> 404")
    void createOrder_unknownProduct_returns404() throws Exception {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(order(99, 1)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Товар 99 не найден"));
    }

    @Test
    @DisplayName("TD-3 Негатив: на складе мало -> 409, заказ не сохраняется")
    void createOrder_notEnoughStock_returns409() throws Exception {
        when(productRepository.findById(1L)).thenReturn(Optional.of(new Product("Книга", 500, 1)));

        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(order(1, 5)))
                .andExpect(status().isConflict());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("TD-4 Негатив: количество 0 -> 400 (валидация контроллера), сервис не вызывается")
    void createOrder_zeroQuantity_returns400() throws Exception {
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(order(1, 0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("quantity: минимум 1 шт."));
        verify(productRepository, never()).findById(any());
    }

    @Test
    @DisplayName("TD-5 Негатив: заказа нет -> 404")
    void getOrder_unknown_returns404() throws Exception {
        when(orderRepository.findById(7L)).thenReturn(Optional.empty());

        mvc.perform(get("/api/orders/7"))
                .andExpect(status().isNotFound());
    }
}
