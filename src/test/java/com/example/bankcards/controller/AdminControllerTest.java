package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardRequestDto;
import com.example.bankcards.dto.card.CardResponseDto;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.service.CardService;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {
    @Mock
    private CardService cardService;

    @InjectMocks
    private AdminController adminController;

    private CardResponseDto cardResponseDto;
    private CardRequestDto cardRequestDto;
    private SoftAssertions softly;

    @BeforeEach
    void setUp() {
        softly = new SoftAssertions();
        cardResponseDto = new CardResponseDto();
        cardResponseDto.setId(1L);
        cardResponseDto.setMaskedCardNumber("**** **** **** 3456");
        cardResponseDto.setHolderName("Admin");
        cardResponseDto.setExpiryDate(LocalDate.now().plusYears(1));
        cardResponseDto.setStatus(CardStatus.ACTIVE);
        cardResponseDto.setBalance(1000.0);

        cardRequestDto = new CardRequestDto();
        cardRequestDto.setCardNumber("1234567890123456");
        cardRequestDto.setHolderName("Admin");
        cardRequestDto.setExpiryDate(LocalDate.now().plusYears(1));
        cardRequestDto.setBalance(1000.0);
    }

    @Test
    void getAllCards() {
        when(cardService.getAllCards())
            .thenReturn(Collections.singletonList(cardResponseDto));

        ResponseEntity<List<CardResponseDto>> response = adminController.getAllCards();

        softly.assertThat(response.getBody())
            .isNotNull()
            .satisfies(body -> {
                softly.assertThat(body.size()).isEqualTo(1);
                softly.assertThat(body.get(0).getId()).isEqualTo(1L);
            });
        verify(cardService).getAllCards();
        softly.assertAll();
    }

    @Test
    void createCardForUser() {
        when(cardService.createCard(any(CardRequestDto.class), anyString()))
            .thenReturn(cardResponseDto);

        ResponseEntity<CardResponseDto> response =
            adminController.createCardForUser(cardRequestDto, "admin");

        softly.assertThat(response.getBody())
            .isNotNull()
            .extracting(CardResponseDto::getId)
            .isEqualTo(1L);

        verify(cardService).createCard(cardRequestDto, "admin");
        softly.assertAll();
    }

    @Test
    void updateAnyCardStatus() {
        cardResponseDto.setStatus(CardStatus.BLOCKED);
        when(cardService.updateCardStatus(anyLong(), any(CardStatus.class), any()))
            .thenReturn(cardResponseDto);

        ResponseEntity<CardResponseDto> response =
            adminController.updateAnyCardStatus(1L, CardStatus.BLOCKED);

        softly.assertThat(response.getBody())
            .isNotNull()
            .extracting(CardResponseDto::getStatus)
            .isEqualTo(CardStatus.BLOCKED);
        verify(cardService).updateCardStatus(1L, CardStatus.BLOCKED, null);
        softly.assertAll();
    }

    @Test
    void getAllCardsWithFilters() {
        List<CardResponseDto> content = Collections.singletonList(cardResponseDto);
        Page<CardResponseDto> page = new PageImpl<>(
            content,
            Pageable.unpaged(),
            content.size()
        );

        when(cardService.getAllCardsWithFilters(
            any(), any(), any(), any(), any(), any()
        )).thenReturn(page);

        ResponseEntity<Page<CardResponseDto>> response = adminController.getAllCardsWithFilters(
            CardStatus.ACTIVE,
            LocalDate.now(),
            LocalDate.now().plusYears(1),
            0.0,
            1000.0,
            Pageable.unpaged()
        );

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Optional.ofNullable(response.getBody())
            .ifPresentOrElse(
                body -> {
                    softly.assertThat(body.getContent())
                        .isNotNull()
                        .hasSize(1)
                        .containsExactly(cardResponseDto);
                },
                () -> softly.fail("Тело ответа имеет значение null")
            );

        verify(cardService).getAllCardsWithFilters(
            CardStatus.ACTIVE,
            LocalDate.now(),
            LocalDate.now().plusYears(1),
            0.0,
            1000.0,
            Pageable.unpaged()
        );

        softly.assertAll();
    }
}