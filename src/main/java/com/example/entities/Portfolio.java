package com.example.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "portfolio", uniqueConstraints = @UniqueConstraint(columnNames = { "trader_id", "stock" }))
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Portfolio {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "trader_id", nullable = false, length = 10)
    private String traderId;

    @Column(nullable = false, length = 10)
    private String stock;

    @Column(nullable = false, length = 20)
    private String sector;

    @Column(nullable = false)
    private int quantity;

    @Version
    private int version;
}
