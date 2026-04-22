package com.example.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.entities.Trader;

import jakarta.persistence.LockModeType;

public interface TraderRepository extends JpaRepository<Trader, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Trader t where t.id = :id")
    Optional<Trader> findByIdForUpdate(@Param("id") String id);
}
