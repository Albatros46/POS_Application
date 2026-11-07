package com.sakcadag.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record CreateOrderRequest(
        @NotNull Long customerId,
        @NotEmpty List<@NotNull @Valid Line> items) {

    public record Line(@NotNull Long productId, @NotNull @Positive Integer quantity) {}
}
