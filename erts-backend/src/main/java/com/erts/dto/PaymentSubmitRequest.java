package com.erts.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentSubmitRequest {

    @NotBlank
    private String registrationId;

    @NotBlank
    private String transactionId;

    // Optional — URL of uploaded screenshot (stored after multipart upload)
    private String screenshotUrl;
}
