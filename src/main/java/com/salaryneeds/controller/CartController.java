package com.salaryneeds.controller;

import com.salaryneeds.dto.*;
import com.salaryneeds.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    private UUID resolveCustomerId(String headerId, String paramId) {
        String idStr = (headerId != null && !headerId.isBlank()) ? headerId.trim() : paramId;
        if (idStr != null && !idStr.isBlank()) {
            try {
                return UUID.fromString(idStr.trim());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid Customer ID format: " + idStr);
            }
        }
        throw new IllegalArgumentException("Customer ID is required in X-Customer-Id header or customerId query parameter");
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartItemResponseDTO>> addToCart(
            @RequestHeader(value = "X-Customer-Id", required = false) String customerIdHeader,
            @RequestParam(value = "customerId", required = false) String customerIdParam,
            @Valid @RequestBody AddToCartRequestDTO request
    ) {
        UUID customerId = resolveCustomerId(customerIdHeader, customerIdParam);
        CartItemResponseDTO cartItem = cartService.addToCart(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Service added to cart", cartItem));
    }

    @GetMapping
    public ResponseEntity<CartResponseDTO> getCart(
            @RequestHeader(value = "X-Customer-Id", required = false) String customerIdHeader,
            @RequestParam(value = "customerId", required = false) String customerIdParam
    ) {
        UUID customerId = resolveCustomerId(customerIdHeader, customerIdParam);
        return ResponseEntity.ok(cartService.getActiveCart(customerId));
    }

    @GetMapping("/count")
    public ResponseEntity<CartCountResponseDTO> getCartCount(
            @RequestHeader(value = "X-Customer-Id", required = false) String customerIdHeader,
            @RequestParam(value = "customerId", required = false) String customerIdParam
    ) {
        UUID customerId = resolveCustomerId(customerIdHeader, customerIdParam);
        return ResponseEntity.ok(cartService.getCartCount(customerId));
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartItemResponseDTO>> updateCartItem(
            @PathVariable Long cartItemId,
            @RequestHeader(value = "X-Customer-Id", required = false) String customerIdHeader,
            @RequestParam(value = "customerId", required = false) String customerIdParam,
            @Valid @RequestBody UpdateCartItemRequestDTO request
    ) {
        UUID customerId = resolveCustomerId(customerIdHeader, customerIdParam);
        CartItemResponseDTO updated = cartService.updateCartItem(customerId, cartItemId, request);
        return ResponseEntity.ok(ApiResponse.ok("Cart item updated successfully", updated));
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<Void>> removeCartItem(
            @PathVariable Long cartItemId,
            @RequestHeader(value = "X-Customer-Id", required = false) String customerIdHeader,
            @RequestParam(value = "customerId", required = false) String customerIdParam
    ) {
        UUID customerId = resolveCustomerId(customerIdHeader, customerIdParam);
        cartService.removeCartItem(customerId, cartItemId);
        return ResponseEntity.ok(ApiResponse.ok("Cart item removed successfully", null));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @RequestHeader(value = "X-Customer-Id", required = false) String customerIdHeader,
            @RequestParam(value = "customerId", required = false) String customerIdParam
    ) {
        UUID customerId = resolveCustomerId(customerIdHeader, customerIdParam);
        cartService.clearCart(customerId);
        return ResponseEntity.ok(ApiResponse.ok("Cart cleared successfully", null));
    }
}
