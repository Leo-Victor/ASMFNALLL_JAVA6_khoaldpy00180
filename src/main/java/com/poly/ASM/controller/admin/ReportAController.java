package com.poly.ASM.controller.admin;

import com.poly.ASM.dto.common.ApiResponse;
import com.poly.ASM.service.order.ReportService;
import com.poly.ASM.service.order.dto.RevenueOrderRow;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class ReportAController {

    private final ReportService reportService;

    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<?>> revenue(@RequestParam(value = "fromDate", required = false) LocalDate fromDate,
                                                   @RequestParam(value = "toDate", required = false) LocalDate toDate,
                                                   @RequestParam(value = "sortField", required = false) String sortField,
                                                   @RequestParam(value = "sortDir", required = false) String sortDir) {
        String fieldValue = sortField != null ? sortField : "orderId";
        String dirValue = sortDir != null ? sortDir : "asc";
        java.util.List<RevenueOrderRow> rows = reportService.revenueByDeliveredOrders(fromDate, toDate, fieldValue, dirValue);
        BigDecimal total = BigDecimal.ZERO;
        for (RevenueOrderRow row : rows) {
            if (row.getLineTotal() != null) {
                total = total.add(row.getLineTotal());
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("rows", rows);
        data.put("grandTotal", total);
        data.put("fromDate", fromDate);
        data.put("toDate", toDate);
        data.put("sortField", fieldValue);
        data.put("sortDir", dirValue);
        return ResponseEntity.ok(ApiResponse.success("Lấy báo cáo doanh thu thành công", data));
    }

    @GetMapping("/vip")
    public ResponseEntity<ApiResponse<?>> vip() {
        return ResponseEntity.ok(ApiResponse.success("Lấy top khách hàng VIP thành công", reportService.top10VipCustomers()));
    }
}
