package com.johanwork.warehouse.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Converts a static EMV QRIS payload (e.g. BCA static merchant QR) into a
 * dynamic payload for a specific transaction amount, following the EMVCo
 * Merchant Presented Mode TLV structure used by QRIS in Indonesia.
 */
public class QrisUtil {

    private static final String TAG_POINT_OF_INITIATION = "01";
    private static final String POI_DYNAMIC = "12";
    private static final String TAG_CURRENCY = "53";
    private static final String TAG_AMOUNT = "54";
    private static final String TAG_CRC = "63";

    private QrisUtil() {
    }

    public static String injectDynamicAmount(String staticPayload, BigDecimal amount) {
        LinkedHashMap<String, String> tags = parseTags(staticPayload.trim());
        tags.remove(TAG_CRC);

        String amountValue = amount.setScale(0, RoundingMode.HALF_UP).toBigInteger().toString();

        LinkedHashMap<String, String> ordered = new LinkedHashMap<>();
        boolean amountInserted = false;
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            String tag = entry.getKey();
            if (tag.equals(TAG_AMOUNT)) {
                continue; // drop any pre-existing amount tag, we control it below
            }
            if (tag.equals(TAG_POINT_OF_INITIATION)) {
                ordered.put(TAG_POINT_OF_INITIATION, POI_DYNAMIC);
                continue;
            }
            ordered.put(tag, entry.getValue());
            if (tag.equals(TAG_CURRENCY)) {
                ordered.put(TAG_AMOUNT, amountValue);
                amountInserted = true;
            }
        }
        if (!amountInserted) {
            ordered.put(TAG_AMOUNT, amountValue);
        }

        String bodyWithCrcTag = serialize(ordered) + TAG_CRC + "04";
        return bodyWithCrcTag + crc16Ccitt(bodyWithCrcTag);
    }

    private static LinkedHashMap<String, String> parseTags(String payload) {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>();
        int i = 0;
        while (i + 4 <= payload.length()) {
            String tag = payload.substring(i, i + 2);
            int len = Integer.parseInt(payload.substring(i + 2, i + 4));
            int valueStart = i + 4;
            int valueEnd = valueStart + len;
            tags.put(tag, payload.substring(valueStart, valueEnd));
            i = valueEnd;
        }
        return tags;
    }

    private static String serialize(LinkedHashMap<String, String> tags) {
        StringBuilder sb = new StringBuilder();
        tags.forEach((tag, value) -> sb.append(tag)
                .append(String.format("%02d", value.length()))
                .append(value));
        return sb.toString();
    }

    // CRC-16/CCITT-FALSE (poly 0x1021, init 0xFFFF) — the checksum algorithm mandated by the QRIS spec for tag 63
    private static String crc16Ccitt(String data) {
        int crc = 0xFFFF;
        for (byte b : data.getBytes(StandardCharsets.US_ASCII)) {
            crc ^= (b & 0xFF) << 8;
            for (int bit = 0; bit < 8; bit++) {
                crc = ((crc & 0x8000) != 0) ? (crc << 1) ^ 0x1021 : crc << 1;
                crc &= 0xFFFF;
            }
        }
        return String.format("%04X", crc);
    }
}
