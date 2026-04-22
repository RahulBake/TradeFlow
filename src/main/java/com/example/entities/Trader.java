package com.example.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "traders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trader {

    @Id
    @Column(nullable = false, length = 10)
    private String id;
}
