package com.bookhub.dto.reservation;

import com.bookhub.entity.ReservationStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ReservationResponse {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private String bookAuthor;
    private String bookCoverUrl;
    private ReservationStatus status;
    private int queuePosition;
    private LocalDateTime createdAt;
}
