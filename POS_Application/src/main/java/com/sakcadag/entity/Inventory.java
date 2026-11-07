package com.sakcadag.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "inventory")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Product is required")
    @OneToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @PositiveOrZero(message = "Stock quantity cannot be negative")
    private int stockQuantity;
}
