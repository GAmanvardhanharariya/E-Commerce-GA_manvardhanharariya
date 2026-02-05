package com.ecommerce.demo.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CheckoutRequest(
        @NotBlank @Size(max = 200) String shippingName,
        @NotBlank @Size(max = 20) String shippingPhone,
        @NotBlank @Size(max = 500) String addressLine,
        @NotBlank @Size(max = 100) String city,
        @NotBlank @Size(max = 20) String pincode
) {}
