package com.example.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.example.entities.Trader;
import com.example.repository.TraderRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TraderService {

    private final TraderRepository traderRepository;

    @Transactional
    public Trader lockOrCreate(String traderId) {
        return traderRepository.findByIdForUpdate(traderId)
                .orElseGet(() -> createThenLock(traderId));
    }

    @Transactional
    public Trader ensureExists(String traderId) {
        return traderRepository.findById(traderId)
                .orElseGet(() -> traderRepository.save(new Trader(traderId)));
    }

    private Trader createThenLock(String traderId) {
        try {
            traderRepository.saveAndFlush(new Trader(traderId));
        } catch (DataIntegrityViolationException ignored) {
            // Another concurrent request created the trader first.
        }

        return traderRepository.findByIdForUpdate(traderId)
                .orElseThrow(() -> new IllegalStateException("Failed to lock trader " + traderId));
    }
}
