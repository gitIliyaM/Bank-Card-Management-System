package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardRequestDto;
import com.example.bankcards.dto.card.CardResponseDto;
import com.example.bankcards.dto.transfer.TransferRequestDto;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/cards")
@Tag(name = "Управление карточками", description = "Конечные точки для управления банковскими картами")
@SecurityRequirement(name = "bearerAuth")
public class CardController {
    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    @Operation(summary = "Получите карточки пользователей с разбивкой по страницам")
    public ResponseEntity<Page<CardResponseDto>> getUserCards(
        Pageable pageable,
        @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cardService.getUserCards(pageable, userDetails.getUsername()));
    }

    @GetMapping("/all")
    @Operation(summary = "Получите все карточки пользователей без разбивки на страницы")
    public ResponseEntity<List<CardResponseDto>> getAllUserCards(
        @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cardService.getAllUserCards(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить карту по идентификатору")
    public ResponseEntity<CardResponseDto> getCardById(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cardService.getCardById(id, userDetails.getUsername()));
    }

    @PostMapping
    @Operation(summary = "Создайте новую карточку")
    public ResponseEntity<CardResponseDto> createCard(
        @Valid @RequestBody CardRequestDto cardRequestDto,
        @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cardService.createCard(cardRequestDto, userDetails.getUsername()));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Обновить статус карты")
    public ResponseEntity<CardResponseDto> updateCardStatus(
        @PathVariable Long id,
        @RequestParam CardStatus status,
        @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cardService.updateCardStatus(id, status, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить карточку")
    public ResponseEntity<Void> deleteCard(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails) {
        cardService.deleteCard(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/transfer")
    @Operation(summary = "Перевод между картами пользователя")
    public ResponseEntity<Void> transferBetweenCards(
        @Valid @RequestBody TransferRequestDto transferRequest,
        @AuthenticationPrincipal UserDetails userDetails) {
        cardService.transferBetweenCards(transferRequest, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<CardResponseDto>> filterCards(
        @RequestParam(required = false) CardStatus status,
        @RequestParam(required = false) LocalDate expiryDateFrom,
        @RequestParam(required = false) LocalDate expiryDateTo,
        @RequestParam(required = false) Double minBalance,
        @RequestParam(required = false) Double maxBalance,
        Pageable pageable,
        @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cardService.filterUserCards(
            status, expiryDateFrom, expiryDateTo,
            minBalance, maxBalance,
            userDetails.getUsername(), pageable));
    }
}