package com.example.bankcards.service;

import com.example.bankcards.dto.card.CardRequestDto;
import com.example.bankcards.dto.card.CardResponseDto;
import com.example.bankcards.dto.transfer.TransferRequestDto;
import com.example.bankcards.dto.user.UserRequestDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.InvalidCardOperationException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.CardUtils;
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
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {
    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserService userService;

    @Mock
    private CardUtils cardUtils;

    @InjectMocks
    private CardService cardService;

    private User user;
    private Card card;
    private CardRequestDto cardRequestDto;
    private SoftAssertions softly;

    @BeforeEach
    void setUp() {
        softly = new SoftAssertions();
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        card = new Card();
        card.setId(1L);
        card.setCardNumber("1234567890123456");
        card.setHolderName("Test User");
        card.setExpiryDate(LocalDate.now().plusYears(1));
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(1000.0);
        card.setUser(user);

        cardRequestDto = new CardRequestDto();
        cardRequestDto.setCardNumber("1234567890123456");
        cardRequestDto.setHolderName("Test User");
        cardRequestDto.setExpiryDate(LocalDate.now().plusYears(1));
        cardRequestDto.setBalance(1000.0);

        UserRequestDto userRequestDto = new UserRequestDto();
        userRequestDto.setUsername("testuser");
        userRequestDto.setPassword("password");
        userRequestDto.setRole("ROLE_USER");
    }

    @Test
    void getUserCardsPageOfCards() {
        when(userService.getUserByUsername(anyString())).thenReturn(user);
        when(cardRepository.findByUser(any(User.class), any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.singletonList(card)));

        Page<CardResponseDto> result = cardService.getUserCards(Pageable.unpaged(), "testuser");

        softly.assertThat(result.getTotalElements()).isEqualTo(1);
        verify(cardRepository).findByUser(user, Pageable.unpaged());
        softly.assertAll();
    }

    @Test
    void getCardById() {
        when(userService.getUserByUsername(anyString())).thenReturn(user);
        when(cardRepository.findByIdAndUser(anyLong(), any(User.class))).thenReturn(Optional.of(card));
        when(cardUtils.maskCardNumber(anyString())).thenReturn("**** **** **** 3456");

        CardResponseDto result = cardService.getCardById(1L, "testuser");

        softly.assertThat(result).isNotNull();
        softly.assertThat(result.getId()).isEqualTo(1L);
        softly.assertThat(result.getMaskedCardNumber()).isEqualTo("**** **** **** 3456");
        softly.assertAll();
    }

    @Test
    void getCardByIdNotFoundException() {
        when(userService.getUserByUsername(anyString())).thenReturn(user);
        when(cardRepository.findByIdAndUser(anyLong(), any(User.class))).thenReturn(Optional.empty());

        softly.assertThatThrownBy(() -> cardService.getCardById(1L, "testuser"))
            .isInstanceOf(CardNotFoundException.class);
        softly.assertAll();
    }

    @Test
    void createCard() {
        when(userService.getUserByUsername(anyString())).thenReturn(user);
        when(cardRepository.save(any(Card.class))).thenReturn(card);
        when(cardUtils.maskCardNumber(anyString())).thenReturn("**** **** **** 3456");

        CardResponseDto result = cardService.createCard(cardRequestDto, "testuser");

        softly.assertThat(result).isNotNull();
        softly.assertThat(result.getId()).isEqualTo(1L);
        verify(cardRepository).save(any(Card.class));
        softly.assertAll();
    }

    @Test
    void updateCardStatus() {
        when(userService.getUserByUsername(anyString())).thenReturn(user);
        when(cardRepository.findByIdAndUser(anyLong(), any(User.class)))
            .thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);
        when(cardUtils.maskCardNumber(anyString())).thenReturn("**** **** **** 3456");

        CardResponseDto result = cardService.updateCardStatus(1L, CardStatus.BLOCKED, "testuser");

        softly.assertThat(result.getStatus()).isEqualTo(CardStatus.BLOCKED);
        verify(cardRepository).save(card);
        softly.assertAll();
    }
    @Test
    void updateCardStatusExpiredException() {
        card.setStatus(CardStatus.EXPIRED);
        when(userService.getUserByUsername(anyString())).thenReturn(user);
        when(cardRepository.findByIdAndUser(anyLong(), any(User.class))).thenReturn(Optional.of(card));

        softly.assertThatThrownBy(() -> cardService.updateCardStatus(1L, CardStatus.ACTIVE, "testuser"))
            .isInstanceOf(InvalidCardOperationException.class);
        softly.assertAll();
    }

    @Test
    void transferBetweenCardsFunds() {
        Card sourceCard = new Card();
        sourceCard.setId(1L);
        sourceCard.setBalance(1000.0);
        sourceCard.setStatus(CardStatus.ACTIVE);
        sourceCard.setExpiryDate(LocalDate.now().plusYears(1));
        sourceCard.setUser(user);

        Card destCard = new Card();
        destCard.setId(2L);
        destCard.setBalance(500.0);
        destCard.setStatus(CardStatus.ACTIVE);
        destCard.setExpiryDate(LocalDate.now().plusYears(1));
        destCard.setUser(user);

        when(userService.getUserByUsername(anyString())).thenReturn(user);
        when(cardRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(sourceCard));
        when(cardRepository.findByIdAndUser(2L, user)).thenReturn(Optional.of(destCard));

        TransferRequestDto transferRequest = new TransferRequestDto();
        transferRequest.setSourceCardId(1L);
        transferRequest.setDestinationCardId(2L);
        transferRequest.setAmount(200.0);

        cardService.transferBetweenCards(transferRequest, "testuser");

        softly.assertThat(sourceCard.getBalance()).isEqualTo(800.0);
        softly.assertThat(destCard.getBalance()).isEqualTo(700.0);
        verify(cardRepository, times(2)).save(any(Card.class));
        softly.assertAll();
    }

    @Test
    void transferBetweenCardsInsufficientFundsException() {
        Card sourceCard = new Card();
        sourceCard.setId(1L);
        sourceCard.setBalance(100.0);
        sourceCard.setStatus(CardStatus.ACTIVE);
        sourceCard.setUser(user);

        Card destCard = new Card();
        destCard.setId(2L);
        destCard.setBalance(500.0);
        destCard.setStatus(CardStatus.ACTIVE);
        destCard.setUser(user);

        when(userService.getUserByUsername(anyString())).thenReturn(user);
        when(cardRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(sourceCard));
        when(cardRepository.findByIdAndUser(2L, user)).thenReturn(Optional.of(destCard));

        TransferRequestDto transferRequest = new TransferRequestDto();
        transferRequest.setSourceCardId(1L);
        transferRequest.setDestinationCardId(2L);
        transferRequest.setAmount(200.0);

        softly.assertThatThrownBy(() -> cardService.transferBetweenCards(transferRequest, "testuser"))
            .isInstanceOf(InsufficientFundsException.class);
        softly.assertAll();
    }

    @Test
    void getAllCards() {
        when(cardRepository.findAllOrderedById()).thenReturn(Collections.singletonList(card));
        when(cardUtils.maskCardNumber(anyString())).thenReturn("**** **** **** 3456");

        List<CardResponseDto> result = cardService.getAllCards();

        softly.assertThat(result.size()).isEqualTo(1);
        verify(cardRepository).findAllOrderedById();
        softly.assertAll();
    }

    @Test
    void checkAndUpdateExpiredCards() {
        Card expiredCard = new Card();
        expiredCard.setId(2L);
        expiredCard.setExpiryDate(LocalDate.now().minusDays(1));
        expiredCard.setStatus(CardStatus.ACTIVE);

        when(cardRepository.findAll()).thenReturn(Arrays.asList(card, expiredCard));

        cardService.checkAndUpdateExpiredCards();

        softly.assertThat(expiredCard.getStatus()).isEqualTo(CardStatus.EXPIRED);
        verify(cardRepository, times(1)).save(expiredCard);
        softly.assertAll();
    }

    @Test
    void filterUserCards() {
        User user = new User();
        user.setId(1L);

        when(userService.getUserByUsername(anyString())).thenReturn(user);
        when(cardRepository.findWithFilters(
            any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(new PageImpl<>(Collections.singletonList(card)));

        Page<CardResponseDto> result = cardService.filterUserCards(
            CardStatus.ACTIVE,
            LocalDate.now(),
            LocalDate.now().plusYears(1),
            0.0,
            1000.0,
            "testuser",
            Pageable.unpaged());

        softly.assertThat(result.getTotalElements()).isEqualTo(1);
        softly.assertAll();
    }
}