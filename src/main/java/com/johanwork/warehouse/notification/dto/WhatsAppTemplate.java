package com.johanwork.warehouse.notification.dto;

import java.util.List;

public enum WhatsAppTemplate {

    PAYMENT_CREATED_V1(5, """
            Halo kak *{{1}}* 👋,
            pesanan kamu sudah kami terima.

            🛍 Detail Pesanan
            {{2}}

            💰 Total pembayaran: *{{3}}*
            ⏰ Batas waktu: *{{4}}*
            📅 Tgl Pengiriman/Pick-up: *{{5}}*

            Silakan scan QRIS di atas dan transfer sesuai nominal \
            agar pembayaran dapat kami verifikasi otomatis.

            Thank you for ordering Bakedbyerina! ✨"""),

    PAYMENT_CONFIRMED_V1(3, """
            Halo kak *{{1}}* 👋,
            pembayaran untuk pesanan kamu telah kami terima dan konfirmasi.

            💰 Total: *{{2}}*
            📅 Tgl Pengiriman/Pick-up: *{{3}}*
            
            Terima kasih 🙏🏻 Pesanan Anda akan segera kami proses
            
            Thank you for ordering Bakedbyerina! ✨""");

    private final int paramCount;
    private final String body;

    WhatsAppTemplate(int paramCount, String body) {
        this.paramCount = paramCount;
        this.body = body;
    }

    public String render(List<String> params) {
        validate(params);
        String result = body;
        for (int i = 0; i < params.size(); i++) {
            String value = params.get(i) == null ? "" : params.get(i);
            result = result.replace("{{" + (i + 1) + "}}", value);
        }
        return result;
    }

    public void validate(List<String> params) {
        if (params == null || params.size() != paramCount) {
            throw new IllegalArgumentException(
                    "Template %s butuh %d parameter, diberi %d"
                            .formatted(name(), paramCount, params == null ? 0 : params.size()));
        }
    }

    public int paramCount() {
        return paramCount;
    }

}
