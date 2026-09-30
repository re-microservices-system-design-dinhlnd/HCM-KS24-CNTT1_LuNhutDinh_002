package org.example.bookingservice.models.dto.responses;

import lombok.Builder;

@Builder
public record BookingDetailResponse(
        Long id,
        Long movieId,
        String movieTitle,
        Integer quantity,
        Double unitPrice,
        Double subtotal
) {
}
