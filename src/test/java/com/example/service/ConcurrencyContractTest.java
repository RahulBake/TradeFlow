package com.example.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import com.example.entities.Order;
import com.example.entities.Portfolio;

import jakarta.persistence.Version;

class ConcurrencyContractTest {

    @Test
    void orderEntityUsesOptimisticLocking() throws Exception {
        Field versionField = Order.class.getDeclaredField("version");
        assertNotNull(versionField.getAnnotation(Version.class));
    }

    @Test
    void portfolioEntityUsesOptimisticLocking() throws Exception {
        Field versionField = Portfolio.class.getDeclaredField("version");
        assertNotNull(versionField.getAnnotation(Version.class));
    }
}
