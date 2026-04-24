package com.bookhub.dto.dashboard;

import com.bookhub.dto.loan.LoanResponse;
import com.bookhub.dto.reservation.ReservationResponse;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ReaderDashboardResponse {
    private int totalBooksRead;
    private int activeLoans;
    private int overdueLoans;
    private int activeReservations;
    private List<LoanResponse> currentLoans;
    private List<ReservationResponse> currentReservations;
}
