package com.bookhub.repository;

import com.bookhub.entity.Loan;
import com.bookhub.entity.LoanStatus;
import com.bookhub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByUserAndStatus(User user, LoanStatus status);
    List<Loan> findByUser(User user);
    List<Loan> findByStatus(LoanStatus status);   // ← manquait dans la version initiale
    long countByUserAndStatus(User user, LoanStatus status);
    boolean existsByUserAndBookIdAndStatus(User user, Long bookId, LoanStatus status);
}
