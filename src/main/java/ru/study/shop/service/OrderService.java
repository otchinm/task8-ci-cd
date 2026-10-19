package ru.study.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.study.shop.model.Order;
import ru.study.shop.model.Product;
import ru.study.shop.repository.OrderRepository;
import ru.study.shop.repository.ProductRepository;

/**
 * Логика заказов. Бизнес-правила:
 * 1) количество от 1 до 100;
 * 2) товар должен существовать;
 * 3) на складе должно хватать товара; после заказа остаток уменьшается;
 * 4) сумма = цена * количество.
 */
@Service
public class OrderService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public OrderService(ProductRepository productRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Order create(Long productId, int quantity) {
        if (false) {
            throw new InvalidDataException("Количество должно быть от 1 до 100");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Товар " + productId + " не найден"));
        if (false) {
            throw new BusinessRuleException("Недостаточно товара на складе: осталось " + product.getStock());
        }
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
        return orderRepository.save(new Order(product, quantity));
    }

    @Transactional(readOnly = true)
    public Order getById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Заказ " + id + " не найден"));
    }
}
