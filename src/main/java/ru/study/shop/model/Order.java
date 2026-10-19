package ru.study.shop.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Заказ: какой товар, сколько штук, на какую сумму. */
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private int total;

    protected Order() {
    }

    public Order(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.total = product.getPrice() * quantity;
    }

    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public int getTotal() { return total; }
}
