package com.salaryneeds.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCustomerNotFound(CustomerNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Customer Not Found", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCategoryNotFound(CategoryNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Category Not Found", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(CatalogServiceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCatalogServiceNotFound(CatalogServiceNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Service Not Found", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(SubCategoryNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleSubCategoryNotFound(SubCategoryNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Sub-Category Not Found", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(AddressNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleAddressNotFound(AddressNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Address Not Found", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleBookingNotFound(BookingNotFoundException ex, HttpServletRequest request) {
        String uri = request != null ? request.getRequestURI() : "";
        if (uri.contains("worker")) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "BOOKING_NOT_FOUND");
            response.put("message", ex.getMessage());
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Booking Not Found", ex.getMessage(), uri);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApiException(ApiException ex, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", ex.getStatus() != null ? ex.getStatus().value() : HttpStatus.BAD_REQUEST.value());
        response.put("error", ex.getErrorCode() != null ? ex.getErrorCode() : "API_ERROR");
        response.put("message", ex.getMessage());
        response.put("path", request != null ? request.getRequestURI() : "");
        return ResponseEntity.status(ex.getStatus() != null ? ex.getStatus() : HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(WorkerNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleWorkerNotFound(WorkerNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Worker Not Found", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(ServiceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleServiceNotFound(ServiceNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Service Not Found", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(CouponNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCouponNotFound(CouponNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Coupon Not Found", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(DuplicateCatalogServiceException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateCatalogService(DuplicateCatalogServiceException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, "Duplicate Service", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(DuplicateCategoryException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateCategory(DuplicateCategoryException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, "Duplicate Category", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(DuplicateSubCategoryException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateSubCategory(DuplicateSubCategoryException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, "Duplicate Sub-Category", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateEmail(DuplicateEmailException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, "Duplicate Email", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(PhoneAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handlePhoneAlreadyExists(PhoneAlreadyExistsException ex, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "PHONE_ALREADY_EXISTS");
        response.put("message", ex.getMessage());
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidOtp(InvalidOtpException ex, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "INVALID_OTP");
        response.put("message", ex.getMessage());
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(AccountNotActiveException.class)
    public ResponseEntity<Map<String, Object>> handleAccountNotActive(AccountNotActiveException ex, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "ACCOUNT_NOT_ACTIVE");
        response.put("message", ex.getMessage());
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(LeadAlreadyClaimedException.class)
    public ResponseEntity<Map<String, Object>> handleLeadAlreadyClaimed(LeadAlreadyClaimedException ex, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "LEAD_ALREADY_CLAIMED");
        response.put("message", ex.getMessage());
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(BookingAlreadyAssignedException.class)
    public ResponseEntity<Map<String, Object>> handleBookingAlreadyAssigned(BookingAlreadyAssignedException ex, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "LEAD_ALREADY_CLAIMED");
        response.put("message", ex.getMessage());
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(BookingOfferNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleBookingOfferNotFound(BookingOfferNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "BOOKING_NOT_FOUND", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(UnprocessableStatusException.class)
    public ResponseEntity<Map<String, Object>> handleUnprocessableStatus(UnprocessableStatusException ex, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "UNPROCESSABLE_STATUS");
        response.put("message", ex.getMessage());
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    @ExceptionHandler(InvalidCatalogDataException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCatalogData(InvalidCatalogDataException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Catalog Data", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, Object>> handleForbidden(ForbiddenException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(InvalidBookingStateException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidBookingState(InvalidBookingStateException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Booking State", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(InvalidPinException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidPin(InvalidPinException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid PIN", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, "Data Conflict", "Database constraint violated: duplicate or invalid data", request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );
        String uri = request != null ? request.getRequestURI() : "";
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        if (uri.contains("worker")) {
            response.put("error", "INVALID_PAYLOAD");
        } else {
            response.put("error", "Validation Failed");
        }
        response.put("message", "One or more request parameters failed validation");
        response.put("path", uri);
        response.put("fieldErrors", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String name = ex.getName();
        Object value = ex.getValue();
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Type Mismatch", "Invalid format for parameter '" + name + "': " + value, request.getRequestURI());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage(), request.getRequestURI());
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String error, String message, String path) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());
        response.put("error", error);
        response.put("message", message);
        response.put("path", path);
        return ResponseEntity.status(status).body(response);
    }
}
