package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    Page<Card> findByUser(User user, Pageable pageable);
    Optional<Card> findByIdAndUser(Long id, User user);
    List<Card> findByUser(User user);
    boolean existsByCardNumber(String cardNumber);
    boolean existsByCardNumberAndUser(String cardNumber, User user);

    @Query("SELECT c FROM Card c ORDER BY c.id")
    List<Card> findAllOrderedById();

    @Query("SELECT c FROM Card c WHERE " +
        "(:status IS NULL OR c.status = :status) AND " +
        "(:expiryDateFrom IS NULL OR c.expiryDate >= :expiryDateFrom) AND " +
        "(:expiryDateTo IS NULL OR c.expiryDate <= :expiryDateTo) AND " +
        "(:minBalance IS NULL OR c.balance >= :minBalance) AND " +
        "(:maxBalance IS NULL OR c.balance <= :maxBalance) AND " +
        "(:userId IS NULL OR c.user.id = :userId)")
    Page<Card> findWithFilters(
        @Param("status") CardStatus status,
        @Param("expiryDateFrom") LocalDate expiryDateFrom,
        @Param("expiryDateTo") LocalDate expiryDateTo,
        @Param("minBalance") Double minBalance,
        @Param("maxBalance") Double maxBalance,
        @Param("userId") Long userId,
        Pageable pageable);

}