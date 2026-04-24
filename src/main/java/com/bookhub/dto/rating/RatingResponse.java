package com.bookhub.dto.rating;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class RatingResponse {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private Long userId;
    private String userFirstName;
    private String userLastName;
    private int stars;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
