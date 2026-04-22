package com.bookhub.dto.loan;

import com.bookhub.entity.LoanStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class LoanResponse {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private String bookAuthor;
    private String bookCoverUrl;
    private LocalDate borrowedAt;
    private LocalDate dueDate;
    private LocalDate returnedAt;
    private LoanStatus status;
    private boolean overdue;
    private long daysOverdue;
}
