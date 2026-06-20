package com.pos.service;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.pos.dto.request.DailyClosingRequest;
import com.pos.dto.response.DailyClosingResponse;
import com.pos.dto.response.ReportSummaryResponse;
import com.pos.entity.DailyClosing;
import com.pos.entity.User;
import com.pos.enums.UserRole;
import com.pos.repository.DailyClosingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class DailyClosingService {
    private final DailyClosingRepository dailyClosingRepository;
    private final CurrentUserService currentUserService;
    private final ReportService reportService;
    private final ObjectMapper objectMapper;

    public DailyClosingService(
            DailyClosingRepository dailyClosingRepository,
            CurrentUserService currentUserService,
            ReportService reportService,
            ObjectMapper objectMapper
    ) {
        this.dailyClosingRepository = dailyClosingRepository;
        this.currentUserService = currentUserService;
        this.reportService = reportService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public DailyClosingResponse close(LocalDate date, String username) {
        return close(date, username, null);
    }

    @Transactional
    public DailyClosingResponse close(LocalDate date, String username, DailyClosingRequest request) {
        User user = requireManager(username);
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Future dates cannot be closed");
        }
        if (dailyClosingRepository.findByShopIdAndBusinessDate(user.getShop().getId(), date).isPresent()) {
            throw new IllegalArgumentException("This business date is already closed");
        }

        ReportSummaryResponse summary = reportService.summary(username, date, date);
        BigDecimal cashExpected = paymentTotal(summary, "CASH");
        BigDecimal digitalPayTotal = paymentTotal(summary, "TRANSFER");
        BigDecimal creditTotal = paymentTotal(summary, "CREDIT");
        BigDecimal cashInHand = amount(request == null ? null : request.getCashInHand());
        DailyClosing closing = new DailyClosing();
        closing.setShop(user.getShop());
        closing.setBusinessDate(date);
        closing.setClosedBy(user);
        closing.setTotalSales(summary.getTotalSales());
        closing.setTotalCost(summary.getTotalCost());
        closing.setTotalProfit(summary.getTotalProfit());
        closing.setProfitMargin(summary.getProfitMargin());
        closing.setCashExpected(cashExpected);
        closing.setCashInHand(cashInHand);
        closing.setCashDifference(cashInHand.subtract(cashExpected));
        closing.setDigitalPayTotal(digitalPayTotal);
        closing.setCreditTotal(creditTotal);
        closing.setRefundAmount(summary.getRefundAmount());
        closing.setTotalOrders(summary.getTotalOrders());
        closing.setItemsSold(summary.getItemsSold());
        closing.setNote(request == null ? null : request.getNote());
        closing.setSnapshotJson(write(summary));
        return DailyClosingResponse.from(dailyClosingRepository.save(closing), summary);
    }

    @Transactional(readOnly = true)
    public DailyClosingResponse get(LocalDate date, String username) {
        return find(date, username)
                .orElseThrow(() -> new RuntimeException("Daily closing not found"));
    }

    @Transactional(readOnly = true)
    public Optional<DailyClosingResponse> find(LocalDate date, String username) {
        User user = requireManager(username);
        return dailyClosingRepository.findByShopIdAndBusinessDate(user.getShop().getId(), date)
                .map(this::response);
    }

    @Transactional(readOnly = true)
    public List<DailyClosingResponse> recent(String username) {
        User user = requireManager(username);
        return dailyClosingRepository.findTop31ByShopIdOrderByBusinessDateDesc(user.getShop().getId())
                .stream().map(this::response).toList();
    }

    private User requireManager(String username) {
        User user = currentUserService.require(username);
        if (user.getRole() != UserRole.ADMIN && user.getRole() != UserRole.MANAGER) {
            throw new RuntimeException("Admin or manager permission required");
        }
        return user;
    }

    private DailyClosingResponse response(DailyClosing closing) {
        try {
            return DailyClosingResponse.from(
                    closing,
                    objectMapper.readValue(closing.getSnapshotJson(), ReportSummaryResponse.class)
            );
        } catch (JacksonException exception) {
            throw new RuntimeException("Daily closing snapshot is invalid");
        }
    }

    private String write(ReportSummaryResponse summary) {
        try {
            return objectMapper.writeValueAsString(summary);
        } catch (JacksonException exception) {
            throw new RuntimeException("Unable to save daily closing");
        }
    }

    private BigDecimal paymentTotal(ReportSummaryResponse summary, String method) {
        return summary.getPayments().stream()
                .filter(payment -> method.equals(payment.getPaymentMethod()))
                .map(ReportSummaryResponse.PaymentBreakdown::getTotalAmount)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal amount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
