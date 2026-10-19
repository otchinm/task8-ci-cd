package ru.study.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.study.shop.model.Product;
import ru.study.shop.repository.ProductRepository;

import java.util.List;

/** Логика товаров. */
@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public Product create(String name, int price, int stock) {
        if (name == null || name.isBlank()) {
            throw new InvalidDataException("Название обязательно");
        }
        if (price <= 0) {
            throw new InvalidDataException("Цена должна быть больше 0");
        }
        if (stock < 0) {
            throw new InvalidDataException("Остаток не может быть отрицательным");
        }
        // Правило: два товара с одинаковым названием быть не может
        if (productRepository.existsByNameIgnoreCase(name.trim())) {
            throw new BusinessRuleException("Товар «" + name + "» уже существует");
        }
        return productRepository.save(new Product(name.trim(), price, stock));
    }

    @Transactional(readOnly = true)
    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Товар " + id + " не найден"));
    }

    @Transactional(readOnly = true)
    public List<Product> search(String name) {
        return productRepository.search(name == null ? "" : name.trim());
    }
}
