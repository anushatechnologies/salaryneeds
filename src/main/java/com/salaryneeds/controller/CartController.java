package com.salaryneeds.controller;

import com.salaryneeds.dto.*;
import com.salaryneeds.service.BookingService;
import com.salaryneeds.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/cart", "/api/v1/cart", "/cart", "/v1/cart"})
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final BookingService bookingService;

    private String resolveCustomerId(String customerIdHeader, String customerIdParam) {
        if (customerIdHeader != null && !customerIdHeader.isBlank()) {
            return customerIdHeader.trim();
        }
        if (customerIdParam != null && !customerIdParam.isBlank()) {
            return customerIdParam.trim();
        }
        throw new IllegalArgumentException("Customer ID is required either in 'X-Customer-Id' header or 'customerId' parameter");
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponseDTO> addItemToCart(
            @RequestHeader(value = "X-Customer-Id", required = false) String customerIdHeader,
            @RequestParam(value = "customerId", required = false) String customerIdParam,
            @Valid @RequestBody CartItemAddRequestDTO request
    ) {
        String customerId = resolveCustomerId(customerIdHeader, customerIdParam);
        CartResponseDTO response = cartService.addToCart(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<CartResponseDTO> getActiveCart(
            @RequestHeader(value = "X-Customer-Id", required = false) String customerIdHeader,
            @RequestParam(value = "customerId", required = false) String customerIdParam
    ) {
        String customerId = resolveCustomerId(customerIdHeader, customerIdParam);
        CartResponseDTO response = cartService.getActiveCart(customerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/count")
    public ResponseEntity<CartCountResponseDTO> getCartCount(
            @RequestHeader(value = "X-Customer-Id", required = false) String customerIdHeader,
            @RequestParam(value = "customerId", required = false) String customerIdParam
    ) {
        String customerId = resolveCustomerId(customerIdHeader, customerIdParam);
        CartCountResponseDTO response = cartService.getCartCount(customerId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponseDTO> updateItemQuantity(
            @PathVariable Long cartItemId,
            @RequestHeader(value = "X-Customer-Id", required = false) String customerIdHeader,
            @RequestParam(value = "customerId", required = false) String customerIdParam,
            @Valid @RequestBody CartItemUpdateRequestDTO request
    ) {
        String customerId = resolveCustomerId(customerIdHeader, customerIdParam);
        CartResponseDTO response = cartService.updateItemQuantity(customerId, cartItemId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponseDTO> patchItemQuantity(
            @PathVariable Long cartItemId,
            @RequestHeader(value = "X-Customer-Id", required = false) String customerIdHeader,
            @RequestParam(value = "customerId", required = false) String customerIdParam,
            @Valid @RequestBody CartItemUpdateRequestDTO request
    ) {
        String customerId = resolveCustomerId(customerIdHeader, customerIdParam);
        CartResponseDTO response = cartService.updateItemQuantity(customerId, cartItemId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponseDTO> removeItem(
            @PathVariable Long cartItemId,
            @RequestHeader(value = "X-Customer-Id", required = false) String customerIdHeader,
            @RequestParam(value = "customerId", required = false) String customerIdParam
    ) {
        String customerId = resolveCustomerId(customerIdHeader, customerIdParam);
        CartResponseDTO response = cartService.removeItem(customerId, cartItemId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<CartResponseDTO> clearCart(
            @RequestHeader(value = "X-Customer-Id", required = false) String customerIdHeader,
            @RequestParam(value = "customerId", required = false) String customerIdParam
    ) {
        String customerId = resolveCustomerId(customerIdHeader, customerIdParam);
        CartResponseDTO response = cartService.clearCart(customerId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/checkout")
    public ResponseEntity<CartCheckoutResponseDTO> checkoutCart(
            @RequestHeader(value = "X-Customer-Id", required = false) String customerIdHeader,
            @RequestParam(value = "customerId", required = false) String customerIdParam,
            @Valid @RequestBody CartCheckoutRequestDTO request
    ) {
        String customerId = resolveCustomerId(customerIdHeader, customerIdParam);
        CartCheckoutResponseDTO response = bookingService.checkoutCart(request, customerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
