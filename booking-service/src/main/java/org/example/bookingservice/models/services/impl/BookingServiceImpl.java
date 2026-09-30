package org.example.bookingservice.models.services.impl;

import lombok.RequiredArgsConstructor;
import org.example.bookingservice.models.constants.BookingStatus;
import org.example.bookingservice.models.dto.requests.CreateBookingDetailRequest;
import org.example.bookingservice.models.dto.requests.CreateBookingRequest;
import org.example.bookingservice.models.dto.responses.BookingDetailResponse;
import org.example.bookingservice.models.dto.responses.BookingResponse;
import org.example.bookingservice.models.dto.responses.MovieResponse;
import org.example.bookingservice.models.entities.Booking;
import org.example.bookingservice.models.entities.BookingDetail;
import org.example.bookingservice.models.repositories.BookingDetailRepository;
import org.example.bookingservice.models.repositories.BookingRepository;
import org.example.bookingservice.models.services.BookingService;
import org.jspecify.annotations.NonNull;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final MovieGatewayService movieGatewayService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String TOPIC = "booking-created";

    @Override
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        double total = 0.0;
        List<BookingDetailResponse> bookingDetailResponseList = new ArrayList<>();

        for (CreateBookingDetailRequest item : request.items()) {
            MovieResponse movie = movieGatewayService.getMovieById(item.movieId());

            total += movie.ticketPrice() * item.quantity();
        }

        Booking booking = Booking.builder()
                .customerName(request.customerName())
                .customerEmail(request.customerEmail())
                .total(total)
                .status(BookingStatus.PENDING)
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        for (CreateBookingDetailRequest item : request.items()) {
            MovieResponse movie = movieGatewayService.getMovieById(item.movieId());

            double unitPrice = movie.ticketPrice();
            double subtotal = unitPrice * item.quantity();

            BookingDetail bookingDetail = BookingDetail.builder()
                    .booking(savedBooking)
                    .movieId(movie.id())
                    .quantity(item.quantity())
                    .unitPrice(unitPrice)
                    .build();

            BookingDetail savedBookingDetail = bookingDetailRepository.save(bookingDetail);

            bookingDetailResponseList.add(BookingDetailResponse.builder()
                    .id(savedBookingDetail.getId())
                    .movieId(savedBookingDetail.getMovieId())
                    .movieTitle(movie.title())
                    .quantity(savedBookingDetail.getQuantity())
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .build());

        }

        kafkaTemplate.send(TOPIC, savedBooking.getCustomerEmail());

        return BookingResponse.builder()
                .id(savedBooking.getId())
                .customerName(savedBooking.getCustomerName())
                .customerEmail(savedBooking.getCustomerEmail())
                .total(total)
                .status(savedBooking.getStatus())
                .items(bookingDetailResponseList)
                .build();
    }
}
