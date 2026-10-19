package ru.study.shop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.study.shop.model.Product;
import ru.study.shop.repository.OrderRepository;
import ru.study.shop.repository.ProductRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * СХОДЯЩИЕСЯ тесты. Ветки «сверху» и «снизу» встретились на уровне сервиса:
 * заглушек больше нет — контроллер, сервис, репозитории и БД работают вместе.
 */
@SpringBootTest
@AutoConfigureMockMvc
class OrderConvergedIT {

    @Autowired
    MockMvc mvc;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    OrderRepository orderRepository;

    @BeforeEach
    void clean() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("CV-1 Позитив: HTTP -> контроллер -> сервис -> БД: заказ создан и читается")
    void createOrder_fullFlow() throws Exception {
        Product p = productRepository.save(new Product("Книга", 500, 10));

        String location = mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + p.getId() + ",\"quantity\":2}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");

        mvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1000));
        assertThat(orderRepository.count()).isEqualTo(1);
        assertThat(productRepository.findById(p.getId()).orElseThrow().getStock()).isEqualTo(8);
    }

    @Test
    @DisplayName("CV-2 Негатив: заказ больше остатка -> 409, в БД ничего не записано")
    void createOrder_notEnoughStock_fullFlow() throws Exception {
        Product p = productRepository.save(new Product("Книга", 500, 1));

        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + p.getId() + ",\"quantity\":3}"))
                .andExpect(status().isConflict());
        assertThat(orderRepository.count()).isZero();
        assertThat(productRepository.findById(p.getId()).orElseThrow().getStock()).isEqualTo(1);
    }

    @Test
    @DisplayName("CV-3 Позитив: создание товара и поиск через HTTP")
    void createProduct_thenSearch_fullFlow() throws Exception {
        mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ноутбук ASUS\",\"price\":55000,\"stock\":3}"))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/products").param("name", "ноут"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Ноутбук ASUS"));
    }
}
