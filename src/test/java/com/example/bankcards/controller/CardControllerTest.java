package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardRequestDto;
import com.example.bankcards.dto.card.CardResponseDto;
import com.example.bankcards.dto.transfer.TransferRequestDto;
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
import org.springframework.security.core.userdetails.UserDetails;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardControllerTest {
    @Mock
    private CardService cardService;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private CardController cardController;

    private CardResponseDto cardResponseDto;
    private CardRequestDto cardRequestDto;
    private TransferRequestDto transferRequestDto;
    private SoftAssertions softly;

    @BeforeEach
    void setUp() {
        softly = new SoftAssertions();
        cardResponseDto = new CardResponseDto();
        cardResponseDto.setId(1L);
        cardResponseDto.setMaskedCardNumber("**** **** **** 3456");
        cardResponseDto.setHolderName("Test User");
        cardResponseDto.setExpiryDate(LocalDate.now().plusYears(1));
        cardResponseDto.setStatus(CardStatus.ACTIVE);
        cardResponseDto.setBalance(1000.0);

        cardRequestDto = new CardRequestDto();
        cardRequestDto.setCardNumber("1234567890123456");
        cardRequestDto.setHolderName("Test User");
        cardRequestDto.setExpiryDate(LocalDate.now().plusYears(1));
        cardRequestDto.setBalance(1000.0);

        transferRequestDto = new TransferRequestDto();
        transferRequestDto.setSourceCardId(1L);
        transferRequestDto.setDestinationCardId(2L);
        transferRequestDto.setAmount(100.0);

        when(userDetails.getUsername()).thenReturn("testuser");
    }

    @Test
    void getUserCards() {
        Page<CardResponseDto> page = new PageImpl<>(Collections.singletonList(cardResponseDto));
        when(cardService.getUserCards(any(Pageable.class), anyString())).thenReturn(page);

        ResponseEntity<Page<CardResponseDto>> response =
            cardController.getUserCards(Pageable.unpaged(), userDetails);

        Optional.ofNullable(response.getBody())
            .ifPresent(body -> {
                softly.assertThat(body.getContent()).hasSize(1);
            });
        verify(cardService).getUserCards(Pageable.unpaged(), "testuser");
        softly.assertAll();
    }

    @Test
    void getAllUserCards() {
        List<CardResponseDto> cards = Collections.singletonList(cardResponseDto);
        when(cardService.getAllUserCards(anyString())).thenReturn(cards);

        ResponseEntity<List<CardResponseDto>> response =
            cardController.getAllUserCards(userDetails);

        softly.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        softly.assertThat(response.getBody()).isNotNull();
        softly.assertThat(response.getBody()).hasSize(1);
        verify(cardService).getAllUserCards("testuser");
        softly.assertAll();
    }

    @Test
    void getCardById() {
        when(cardService.getCardById(anyLong(), anyString())).thenReturn(cardResponseDto);

        ResponseEntity<CardResponseDto> response =
            cardController.getCardById(1L, userDetails);

        softly.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        softly.assertThat(response.getBody()).isNotNull();
        softly.assertThat(Optional.ofNullable(response.getBody())
                .map(body -> body.getId())
                .orElse(null))
            .isEqualTo(1L);
        verify(cardService).getCardById(1L, "testuser");
        softly.assertAll();
    }

    @Test
    void createCard() {
        when(cardService.createCard(any(CardRequestDto.class), anyString())).thenReturn(cardResponseDto);

        ResponseEntity<CardResponseDto> response =
            cardController.createCard(cardRequestDto, userDetails);

        softly.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        softly.assertThat(response.getBody()).isNotNull();
        softly.assertThat(Optional.ofNullable(response.getBody())
                .map(body -> body.getId())
                .orElse(null))
            .isEqualTo(1L);
        verify(cardService).createCard(cardRequestDto, "testuser");
        softly.assertAll();
    }

    @Test
    void updateCardStatus() {
        cardResponseDto.setStatus(CardStatus.BLOCKED);
        when(cardService.updateCardStatus(anyLong(), any(CardStatus.class), anyString()))
            .thenReturn(cardResponseDto);

        ResponseEntity<CardResponseDto> response =
            cardController.updateCardStatus(1L, CardStatus.BLOCKED, userDetails);

        softly.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        softly.assertThat(response.getBody()).isNotNull();
        softly.assertThat(Optional.ofNullable(response.getBody())
                .map(body -> body.getStatus())
                .orElse(null))
            .isEqualTo(CardStatus.BLOCKED);
        verify(cardService).updateCardStatus(1L, CardStatus.BLOCKED, "testuser");
        softly.assertAll();
    }

    @Test
    void deleteCardNoContent() {
        doNothing().when(cardService).deleteCard(anyLong(), anyString());

        ResponseEntity<Void> response =
            cardController.deleteCard(1L, userDetails);

        softly.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(cardService).deleteCard(1L, "testuser");
        softly.assertAll();
    }

    @Test
    void transferBetweenCardsNoContent() {
        doNothing().when(cardService).transferBetweenCards(any(TransferRequestDto.class), anyString());

        ResponseEntity<Void> response =
            cardController.transferBetweenCards(transferRequestDto, userDetails);

        softly.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(cardService).transferBetweenCards(transferRequestDto, "testuser");
        softly.assertAll();
    }

    @Test
    void filterCards() {
        Page<CardResponseDto> page = new PageImpl<>(Collections.singletonList(cardResponseDto));
        when(cardService.filterUserCards(
            any(),
            any(),
            any(),
            any(),
            any(),
            anyString(),
            any(Pageable.class)
        )).thenReturn(page);

        ResponseEntity<Page<CardResponseDto>> response =
            cardController.filterCards(
                CardStatus.ACTIVE,
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                0.0,
                1000.0,
                Pageable.unpaged(),
                userDetails
            );

        softly.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        softly.assertThat(response.getBody()).isNotNull();
        softly.assertThat(Optional.ofNullable(response.getBody())
                .map(body -> body.getContent())
                .orElse(Collections.emptyList()))
            .hasSize(1);
        verify(cardService).filterUserCards(
            CardStatus.ACTIVE,
            LocalDate.now(),
            LocalDate.now().plusYears(1),
            0.0,
            1000.0,
            "testuser",
            Pageable.unpaged()
        );
        softly.assertAll();
    }
}