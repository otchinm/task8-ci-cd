package ru.study.shop;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import ru.study.shop.model.Order;
import ru.study.shop.model.Product;
import ru.study.shop.repository.OrderRepository;
import ru.study.shop.repository.ProductRepository;
import ru.study.shop.service.BusinessRuleException;
import ru.study.shop.service.OrderService;
import ru.study.shop.service.ProductService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ВОСХОДЯЩИЕ тесты (снизу вверх).
 * Реальные: БД H2 + репозитории + сервисы. Контроллера нет:
 * тест сам вызывает сервис, то есть выступает ДРАЙВЕРОМ.
 * Проверяем сохранение, поиск, бизнес-правила и ограничения БД.
 */
@DataJpaTest
@Import({OrderService.class, ProductService.class})
class OrderBottomUpIT {

    @Autowired
    OrderService orderService;
    @Autowired
    ProductService productService;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    OrderRepository orderRepository;

    @Test
    @DisplayName("BU-1 Позитив: заказ сохраняется в БД, остаток уменьшается")
    void createOrder_savesToDatabase() {
        Product p = productRepository.save(new Product("Книга", 500, 10));

        Order order = orderService.create(p.getId(), 2);

        assertThat(order.getId()).isNotNull();
        assertThat(order.getTotal()).isEqualTo(1000);
        assertThat(orderRepository.findById(order.getId())).isPresent();
        assertThat(productRepository.findById(p.getId()).orElseThrow().getStock()).isEqualTo(8);
    }

    @Test
    @DisplayName("BU-2 Позитив: поиск по части названия без учёта регистра (запрос репозитория)")
    void search_byPartOfName() {
        productRepository.save(new Product("Книга «Java»", 900));
        productRepository.save(new Product("Электронная книга", 7000));
        productRepository.save(new Product("Ручка", 50));

        assertThat(productService.search("КНИГ"))
                .extracting(Product::getName)
                .containsExactly("Книга «Java»", "Электронная книга"); // по цене
    }

    @Test
    @DisplayName("BU-3 Негатив: заказ больше остатка отклонён, остаток и заказы не изменились")
    void createOrder_moreThanStock_rejected() {
        Product p = productRepository.save(new Product("Книга", 500, 3));

        assertThatThrownBy(() -> orderService.create(p.getId(), 4))
                .isInstanceOf(BusinessRuleException.class);
        assertThat(productRepository.findById(p.getId()).orElseThrow().getStock()).isEqualTo(3);
        assertThat(orderRepository.count()).isZero();
    }

    @Test
    @DisplayName("BU-4 Негатив: бизнес-правило — дубль названия товара запрещён")
    void createProduct_duplicateName_rejected() {
        productService.create("Мышь", 1000, 5);

        assertThatThrownBy(() -> productService.create("мышь", 1200, 1))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("уже существует");
    }

    @Test
    @DisplayName("BU-5 Негатив: ограничение БД — уникальное название (в обход сервиса)")
    void database_uniqueNameConstraint() {
        productRepository.saveAndFlush(new Product("Клавиатура", 3000));

        assertThatThrownBy(() -> productRepository.saveAndFlush(new Product("Клавиатура", 3500)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
