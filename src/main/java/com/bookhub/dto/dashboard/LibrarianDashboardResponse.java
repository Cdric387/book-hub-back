package com.bookhub.dto.dashboard;

import com.bookhub.dto.loan.LoanResponse;
import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class LibrarianDashboardResponse {
    private long totalBooks;
    private long totalUsers;
    private long activeLoans;
    private long overdueLoans;
    private List<LoanResponse> overdueList;
    private List<Map<String, Object>> topBooks;   // titre + nb emprunts
}
