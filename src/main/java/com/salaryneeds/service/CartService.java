package com.salaryneeds.service;

import com.salaryneeds.dto.*;

import java.util.UUID;

public interface CartService {

    CartItemResponseDTO addToCart(UUID customerId, AddToCartRequestDTO request);

    CartResponseDTO getActiveCart(UUID customerId);

    CartCountResponseDTO getCartCount(UUID customerId);

    CartItemResponseDTO updateCartItem(UUID customerId, Long cartItemId, UpdateCartItemRequestDTO request);

    void removeCartItem(UUID customerId, Long cartItemId);

    void clearCart(UUID customerId);
}
