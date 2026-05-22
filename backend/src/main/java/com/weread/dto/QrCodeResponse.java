package com.weread.dto;

public class QrCodeResponse {
    private String uuid;
    private String qrUrl;

    public QrCodeResponse(String uuid, String qrUrl) {
        this.uuid = uuid;
        this.qrUrl = qrUrl;
    }

    public String getUuid() { return uuid; }
    public String getQrUrl() { return qrUrl; }
}
