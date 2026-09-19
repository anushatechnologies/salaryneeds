package com.salaryneeds.repository;

import com.salaryneeds.entity.BookingOffer;
import com.salaryneeds.entity.enums.BookingOfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingOfferRepository extends JpaRepository<BookingOffer, Long> {

    List<BookingOffer> findByWorkerIdAndStatusOrderByCreatedAtDesc(String workerId, BookingOfferStatus status);

    List<BookingOffer> findByBookingId(Long bookingId);

    List<BookingOffer> findByBookingIdAndStatus(Long bookingId, BookingOfferStatus status);

    Optional<BookingOffer> findByIdAndWorkerId(Long id, String workerId);

    Optional<BookingOffer> findByBookingIdAndWorkerId(Long bookingId, String workerId);

    boolean existsByBookingIdAndWorkerId(Long bookingId, String workerId);

    @Modifying
    @Query("UPDATE BookingOffer o SET o.status = :newStatus, o.respondedAt = :now " +
           "WHERE o.booking.id = :bookingId AND o.id != :winningOfferId AND o.status = 'PENDING'")
    int cancelCompetingOffers(
            @Param("bookingId") Long bookingId,
            @Param("winningOfferId") Long winningOfferId,
            @Param("newStatus") BookingOfferStatus newStatus,
            @Param("now") LocalDateTime now
    );

    @Modifying
    @Query("UPDATE BookingOffer o SET o.status = 'EXPIRED' " +
           "WHERE o.status = 'PENDING' AND o.expiresAt < :now")
    int expireStaleOffers(@Param("now") LocalDateTime now);
}
