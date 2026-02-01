package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.repository.CardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
public class CardExpirationService {
    private final CardRepository cardRepository;

    public CardExpirationService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @Transactional
    public void checkAndUpdateExpiredCards() {
        List<Card> cards = cardRepository.findAll();
        LocalDate today = LocalDate.now();
        cards.forEach(card -> {
            if (card.getExpiryDate().isBefore(today) && card.getStatus() != CardStatus.EXPIRED) {
                card.setStatus(CardStatus.EXPIRED);
                cardRepository.save(card);
            }
        });
    }
}