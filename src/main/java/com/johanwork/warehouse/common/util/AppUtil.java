package com.johanwork.warehouse.common.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

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
}
