package ru.study.shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.study.shop.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
