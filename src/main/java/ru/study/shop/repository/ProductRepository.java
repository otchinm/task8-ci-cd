package ru.study.shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.study.shop.model.Product;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByNameIgnoreCase(String name);

    /** Поиск товаров по части названия без учёта регистра. */
    @Query("select p from Product p where lower(p.name) like lower(concat('%', :name, '%')) order by p.price")
    List<Product> search(@Param("name") String name);
}
