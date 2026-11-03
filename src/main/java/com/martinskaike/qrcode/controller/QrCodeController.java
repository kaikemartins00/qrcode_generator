package com.martinskaike.qrcode.controller;

import com.martinskaike.qrcode.dto.QrCodeGenerateRequest;
import com.martinskaike.qrcode.dto.QrCodeGenerateResponse;
import com.martinskaike.qrcode.service.QrCodeGeneratorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/qrcode")
public class QrCodeController {

    private final QrCodeGeneratorService service;

    public QrCodeController(QrCodeGeneratorService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<QrCodeGenerateResponse> generate(@RequestBody QrCodeGenerateRequest request) {
        try {
            QrCodeGenerateResponse response = this.service.generateAndUploadQrCode(request.text());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
