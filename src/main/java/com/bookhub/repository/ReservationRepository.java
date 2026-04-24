package com.bookhub.repository;

import com.bookhub.entity.Reservation;
import com.bookhub.entity.ReservationStatus;
import com.bookhub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUser(User user);
    List<Reservation> findByUserAndStatus(User user, ReservationStatus status);
    long countByUserAndStatus(User user, ReservationStatus status);
    boolean existsByUserIdAndBookIdAndStatus(Long userId, Long bookId, ReservationStatus status);
    List<Reservation> findByBookIdAndStatusOrderByQueuePosition(Long bookId, ReservationStatus status);
}
