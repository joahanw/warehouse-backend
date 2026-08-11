package com.johanwork.warehouse.notification.dto;

import java.util.List;

public enum WhatsAppTemplate {

    PAYMENT_CREATED_V1(5, """
            Halo kak *{{1}}* 👋
            Pesanan kamu sudah kami terima.
            
            {{2}}
            
            🚚 Ongkir: *{{3}}*
            💰 Total pembayaran: *{{4}}*
            ⏰ Batas waktu: *{{5}}*
            📅 Tgl Pengiriman/Pick-up: *{{6}}*

            Silakan scan QRIS di atas dan transfer sesuai nominal \
            agar pembayaran dapat kami verifikasi otomatis.

            Thank you for ordering Bakedbyerina! ✨"""),

    PAYMENT_CONFIRMED_V1(3, """
            Terima kasih 🙏🏻Pembayaran telah kami terima dan konfirmasi.

            💰 Total: *{{1}}*
            📅 Tgl Pengiriman/Pick-up: *{{2}}*
            
            Pesanan akan segera kami proses. Mohon ditunggu untuk pengirimannya.""");

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
