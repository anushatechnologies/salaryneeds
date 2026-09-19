package com.salaryneeds.service;

import com.salaryneeds.dto.BookingOfferResponseDTO;
import com.salaryneeds.dto.WorkerActionResponseDTO;
import com.salaryneeds.entity.enums.BookingOfferStatus;

import java.util.List;

public interface BookingOfferService {

    List<BookingOfferResponseDTO> getOffersForWorker(String workerId, BookingOfferStatus status);

    WorkerActionResponseDTO acceptOffer(Long offerId, String workerId);

    WorkerActionResponseDTO rejectOffer(Long offerId, String workerId);
}
