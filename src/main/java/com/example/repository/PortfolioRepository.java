package com.example.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.entities.Portfolio;

import jakarta.persistence.LockModeType;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long>
{ 
	Optional<Portfolio> findByTraderIdAndStock(String traderId, String stock);
    List<Portfolio> findByTraderId(String traderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Portfolio p where p.traderId = :traderId and p.stock = :stock")
    Optional<Portfolio> findByTraderIdAndStockForUpdate(@Param("traderId") String traderId, @Param("stock") String stock);
}
