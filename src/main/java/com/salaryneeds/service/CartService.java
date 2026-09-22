package com.salaryneeds.service;

import com.salaryneeds.dto.CartCountResponseDTO;
import com.salaryneeds.dto.CartItemAddRequestDTO;
import com.salaryneeds.dto.CartItemUpdateRequestDTO;
import com.salaryneeds.dto.CartResponseDTO;

public interface CartService {

    CartResponseDTO addToCart(String customerId, CartItemAddRequestDTO request);

    CartResponseDTO getActiveCart(String customerId);

    CartCountResponseDTO getCartCount(String customerId);

    CartResponseDTO updateItemQuantity(String customerId, Long cartItemId, CartItemUpdateRequestDTO request);

    CartResponseDTO removeItem(String customerId, Long cartItemId);

    CartResponseDTO clearCart(String customerId);
}
