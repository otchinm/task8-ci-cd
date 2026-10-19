package ru.study.shop.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** Товар: название (уникальное), цена в рублях, остаток на складе. */
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int stock;

    protected Product() {
    }

    public Product(String name, int price, int stock) {
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public Product(String name, int price) {
        this(name, price, 10);
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public int getPrice() { return price; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
}
