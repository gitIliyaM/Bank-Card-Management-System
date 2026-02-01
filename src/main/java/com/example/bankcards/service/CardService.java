package com.example.bankcards.service;

import com.example.bankcards.dto.card.CardRequestDto;
import com.example.bankcards.dto.card.CardResponseDto;
import com.example.bankcards.dto.transfer.TransferRequestDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.CardNullException;
import com.example.bankcards.exception.CardNumberExistsException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.InvalidCardOperationException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.CardUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CardService {
    private final CardRepository cardRepository;
    private final UserService userService;
    private final CardUtils cardUtils;
    private final CardExpirationService cardExpirationService;

    public CardService(CardRepository cardRepository, UserService userService, CardUtils cardUtils, CardExpirationService cardExpirationService) {
        this.cardRepository = cardRepository;
        this.userService = userService;
        this.cardUtils = cardUtils;
        this.cardExpirationService = cardExpirationService;
    }

    @Transactional(readOnly = true)
    public Page<CardResponseDto> getUserCards(Pageable pageable, String username) {
        User user = userService.getUserByUsername(username);
        return cardRepository.findByUser(user, pageable)
            .map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public List<CardResponseDto> getAllUserCards(String username) {
        User user = userService.getUserByUsername(username);
        return cardRepository.findByUser(user).stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CardResponseDto> getAllCards() {
        return cardRepository.findAllOrderedById().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CardResponseDto getCardById(Long id, String username) {
        User user = userService.getUserByUsername(username);
        Card card = cardRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new CardNotFoundException("Карта с идентификатором не найдена: " + id));
        return convertToDto(card);
    }

    @Transactional
    public CardResponseDto createCard(CardRequestDto cardRequestDto, String username) {
        if (cardRepository.existsByCardNumber(cardRequestDto.getCardNumber())) {
            throw new CardNumberExistsException("Номер карты уже существует");
        }
        User user = userService.getUserByUsername(username);

        if (cardRepository.existsByCardNumberAndUser(cardRequestDto.getCardNumber(), user)) {
            throw new CardNumberExistsException("Номер карты уже существует для этого пользователя");
        }

        Card card = new Card();
        card.setCardNumber(cardRequestDto.getCardNumber());
        card.setHolderName(cardRequestDto.getHolderName());
        card.setExpiryDate(cardRequestDto.getExpiryDate());
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(cardRequestDto.getBalance());
        card.setUser(user);

        Card savedCard = cardRepository.save(card);
        return convertToDto(savedCard);
    }

    @Transactional
    public CardResponseDto updateCardStatus(Long id, CardStatus status, String username) {
        User user = userService.getUserByUsername(username);
        Card card = cardRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new CardNotFoundException("Карта с идентификатором не найдена: " + id));

        if (card.getStatus() == CardStatus.EXPIRED) {
            throw new InvalidCardOperationException("Невозможно изменить статус карты с истекшим сроком действия");
        }

        card.setStatus(status);
        Card updatedCard = cardRepository.save(card);
        return convertToDto(updatedCard);
    }

    @Transactional
    public void deleteCard(Long id, String username) {
        User user = userService.getUserByUsername(username);
        Card card = cardRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new CardNotFoundException("Карта с идентификатором не найдена: " + id));
        cardRepository.delete(card);
    }

    @Transactional
    public void transferBetweenCards(TransferRequestDto transferRequest, String username) {
        User user = userService.getUserByUsername(username);

        Card sourceCard = cardRepository.findByIdAndUser(transferRequest.getSourceCardId(), user)
            .orElseThrow(() -> new CardNotFoundException("Исходная карта не найдена"));

        Card destinationCard = cardRepository.findByIdAndUser(transferRequest.getDestinationCardId(), user)
            .orElseThrow(() -> new CardNotFoundException("Карта назначения не найдена"));

        if (sourceCard.getStatus() != CardStatus.ACTIVE || destinationCard.getStatus() != CardStatus.ACTIVE) {
            throw new InvalidCardOperationException("Обе карты должны быть активны для перевода");
        }

        if (sourceCard.getBalance() < transferRequest.getAmount()) {
            throw new InsufficientFundsException("Недостаточно средств для перевода");
        }

        validateCardForOperations(sourceCard);
        validateCardForOperations(destinationCard);

        sourceCard.setBalance(sourceCard.getBalance() - transferRequest.getAmount());
        destinationCard.setBalance(destinationCard.getBalance() + transferRequest.getAmount());

        cardRepository.save(sourceCard);
        cardRepository.save(destinationCard);
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

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void scheduleCheckAndUpdateExpiredCards() {
        cardExpirationService.checkAndUpdateExpiredCards();
    }

    private void validateCardForOperations(Card card) {
        if (card == null) {
            throw new InvalidCardOperationException("Карта не может быть null");
        }
        if (card.getExpiryDate() == null) {
            throw new InvalidCardOperationException("Дата окончания действия карты не может быть null");
        }
        if (card.getStatus() == CardStatus.EXPIRED) {
            throw new InvalidCardOperationException("Срок действия карты истёк");
        }
        if (card.getExpiryDate().isBefore(LocalDate.now())) {
            card.setStatus(CardStatus.EXPIRED);
            cardRepository.save(card);
            throw new InvalidCardOperationException("Карта просрочена");
        }
    }

    CardResponseDto convertToDto(Card card) {
        if (card == null) {
            throw new CardNullException("Карта не может быть null");
        }

        CardResponseDto dto = new CardResponseDto();
        dto.setId(card.getId());
        dto.setMaskedCardNumber(cardUtils.maskCardNumber(card.getCardNumber()));
        dto.setHolderName(card.getHolderName());
        dto.setExpiryDate(card.getExpiryDate());
        dto.setStatus(card.getStatus());
        dto.setBalance(card.getBalance());
        return dto;
    }

    @Transactional(readOnly = true)
    public Page<CardResponseDto> filterUserCards(
        CardStatus status,
        LocalDate expiryDateFrom,
        LocalDate expiryDateTo,
        Double minBalance,
        Double maxBalance,
        String username,
        Pageable pageable
    ) {
        User user = userService.getUserByUsername(username);
        return cardRepository.findWithFilters(
            status,
            expiryDateFrom,
            expiryDateTo,
            minBalance,
            maxBalance,
            user.getId(),
            pageable
        ).map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public Page<CardResponseDto> getAllCardsWithFilters(
        CardStatus status,
        LocalDate expiryDateFrom,
        LocalDate expiryDateTo,
        Double minBalance,
        Double maxBalance,
        Pageable pageable
    ) {
        return cardRepository.findWithFilters(
            status,
            expiryDateFrom,
            expiryDateTo,
            minBalance,
            maxBalance,
            null,
            pageable
        ).map(this::convertToDto);
    }
}