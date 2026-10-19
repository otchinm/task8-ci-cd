package ru.study.shop.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.study.shop.model.Product;
import ru.study.shop.service.ProductService;

import java.net.URI;
import java.util.List;

/**
 * POST /api/products          {"name":"Книга","price":500,"stock":10} -> 201
 * GET  /api/products/{id}                                            -> 200
 * GET  /api/products?name=кни                                        -> поиск
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    public record CreateProductRequest(@NotBlank(message = "название обязательно") String name,
                                       @Min(value = 1, message = "цена должна быть больше 0") int price,
                                       @Min(value = 0, message = "остаток не может быть отрицательным") int stock) {
    }

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<Product> create(@Valid @RequestBody CreateProductRequest request) {
        Product product = productService.create(request.name(), request.price(), request.stock());
        return ResponseEntity.created(URI.create("/api/products/" + product.getId())).body(product);
    }

    @GetMapping("/{id}")
    public Product get(@PathVariable Long id) {
        return productService.getById(id);
    }

    @GetMapping
    public List<Product> search(@RequestParam(defaultValue = "") String name) {
        return productService.search(name);
    }
}
