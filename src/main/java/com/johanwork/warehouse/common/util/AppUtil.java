package com.johanwork.warehouse.common.util;

import com.johanwork.warehouse.common.constant.AppConstant;
import com.johanwork.warehouse.common.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Slf4j
public class AppUtil {

    public static String formatCurrency(BigDecimal amount){
        return NumberFormat.getCurrencyInstance(new Locale("id","ID"))
                .format(amount)
                .replace("Rp", "Rp ")
                .trim();
    }

    public static String formatDateTime(LocalDateTime dateTime){
        return dateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm",
                new Locale("id","ID")));
    }

    public static String formatDate(LocalDate date){
        if (date == null) return "-";
        return date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy",
                new Locale("id","ID")));
    }

    public static Instant parseExpiry(String expiryTime) {
        if (expiryTime == null) return null;
        return LocalDateTime.parse(expiryTime.trim(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                .toInstant(ZoneOffset.of("+07:00"));
    }

    public static String formatExpiry(Instant expiryTime) {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneOffset.of("+07:00"))
                .format(expiryTime);
    }

    public static String toJson(List<String> params){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(params);
        }catch (Exception ex){
            log.error("Failed to serialize params: {}", ex.getMessage());
            throw new CustomException(HttpStatus.BAD_REQUEST,
                    AppConstant.Error.TITLE_INTERNAL_SERVER_ERROR,
                    AppConstant.Error.MESSAGE_INTERNAL_SERVER_ERROR);
        }
    }
}
