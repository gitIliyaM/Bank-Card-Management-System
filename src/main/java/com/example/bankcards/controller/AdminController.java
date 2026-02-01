package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardRequestDto;
import com.example.bankcards.dto.card.CardResponseDto;
import com.example.bankcards.dto.user.UserRequestDto;
import com.example.bankcards.dto.user.UserResponseDto;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@Tag(name = "Admin Management", description = "Endpoints for administrators")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {
    private final CardService cardService;
    private final UserService userService;

    public AdminController(CardService cardService, UserService userService) {
        this.cardService = cardService;
        this.userService = userService;
    }

    @GetMapping("/cards")
    @Operation(summary = "Get all cards (admin only)")
    public ResponseEntity<List<CardResponseDto>> getAllCards() {
        return ResponseEntity.ok(cardService.getAllCards());
    }

    @PostMapping("/cards")
    @Operation(summary = "Create card for any user (admin only)")
    public ResponseEntity<CardResponseDto> createCardForUser(
        @Valid @RequestBody CardRequestDto cardRequestDto,
        @RequestParam String username) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(cardService.createCard(cardRequestDto, username));
    }

    @PatchMapping("/cards/{id}/status")
    @Operation(summary = "Update any card status (admin only)")
    public ResponseEntity<CardResponseDto> updateAnyCardStatus(
        @PathVariable Long id,
        @RequestParam CardStatus status) {
        return ResponseEntity.ok(cardService.updateCardStatus(id, status, null));
    }

    @DeleteMapping("/cards/{id}")
    @Operation(summary = "Delete any card (admin only)")
    public ResponseEntity<Void> deleteAnyCard(@PathVariable Long id) {
        cardService.deleteCard(id, null);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users")
    @Operation(summary = "Create new user (admin only)")
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserRequestDto userRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(userService.createUser(userRequestDto));
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Delete user (admin only)")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cards/filter")
    public ResponseEntity<Page<CardResponseDto>> getAllCardsWithFilters(
        @RequestParam(required = false) CardStatus status,
        @RequestParam(required = false) LocalDate expiryDateFrom,
        @RequestParam(required = false) LocalDate expiryDateTo,
        @RequestParam(required = false) Double minBalance,
        @RequestParam(required = false) Double maxBalance,
        Pageable pageable) {
        return ResponseEntity.ok(
            cardService.getAllCardsWithFilters(
                status,
                expiryDateFrom,
                expiryDateTo,
                minBalance,
                maxBalance,
                pageable
            )
        );
    }
}