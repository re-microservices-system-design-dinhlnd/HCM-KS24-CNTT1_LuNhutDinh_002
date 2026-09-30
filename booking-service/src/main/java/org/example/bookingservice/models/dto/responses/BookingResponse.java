package org.example.bookingservice.models.dto.responses;

import lombok.Builder;
import org.example.bookingservice.models.constants.BookingStatus;

import java.util.List;

@Builder
public record BookingResponse(
        Long id,
        String customerName,
        String customerEmail,
        Double total,
        BookingStatus status,
        List<BookingDetailResponse> items
) {
}
