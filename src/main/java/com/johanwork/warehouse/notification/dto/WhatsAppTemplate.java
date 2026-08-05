package com.johanwork.warehouse.notification.dto;

import java.util.List;

public enum WhatsAppTemplate {

    PAYMENT_CREATED_V1(4, """
            Halo *{{1}}* 👋,
            pesanan {{2}} sudah kami terima.

            💰 Total pembayaran: *{{3}}*
            ⏰ Batas waktu: *{{4}}*

            Silakan scan QRIS di atas dan transfer sesuai nominal \
            agar pembayaran dapat kami verifikasi otomatis.

            Bakedbyerina"""),

    PAYMENT_CONFIRMED_V1(3, """
            Halo *{{1}}* 👋,
            pembayaran untuk pesanan {{2}} telah kami terima dan konfirmasi.

            💰 Total: *{{3}}*

            Terima kasih sudah berbelanja, pesanan Anda akan segera kami proses.

            Bakedbyerina""");

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
