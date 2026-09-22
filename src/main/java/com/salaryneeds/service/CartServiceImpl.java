package com.salaryneeds.service;

import com.salaryneeds.dto.*;
import com.salaryneeds.entity.Cart;
import com.salaryneeds.entity.CartItem;
import com.salaryneeds.entity.ServiceItem;
import com.salaryneeds.entity.enums.CartStatus;
import com.salaryneeds.exception.CartItemNotFoundException;
import com.salaryneeds.exception.CustomerNotFoundException;
import com.salaryneeds.exception.ForbiddenException;
import com.salaryneeds.exception.ServiceNotFoundException;
import com.salaryneeds.repository.CartItemRepository;
import com.salaryneeds.repository.CartRepository;
import com.salaryneeds.repository.CustomerRepository;
import com.salaryneeds.repository.ServiceItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CustomerRepository customerRepository;
    private final ServiceItemRepository serviceItemRepository;

    @Override
    public CartItemResponseDTO addToCart(UUID customerId, AddToCartRequestDTO request) {
        customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));

        if (request.getServiceId() == null) {
            throw new IllegalArgumentException("Service ID is required");
        }

        int quantityToAdd = (request.getQuantity() != null && request.getQuantity() > 0) ? request.getQuantity() : 1;

        // Fetch service and ensure active
        ServiceItem service = serviceItemRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ServiceNotFoundException("Service not found with id: " + request.getServiceId()));

        if (service.getIsActive() != null && !service.getIsActive()) {
            throw new IllegalArgumentException("Service is no longer available");
        }

        // Get or create ACTIVE cart for customer
        Cart cart = cartRepository.findFirstByCustomerIdAndStatusOrderByCreatedAtDesc(customerId, CartStatus.ACTIVE)
                .orElseGet(() -> cartRepository.save(
                        Cart.builder()
                                .customerId(customerId)
                                .status(CartStatus.ACTIVE)
                                .build()
                ));

        // Authoritative backend price - NEVER trust frontend price
        BigDecimal unitPrice = (service.getFinalAmount() != null && service.getFinalAmount().compareTo(BigDecimal.ZERO) > 0)
                ? service.getFinalAmount()
                : (service.getBasePrice() != null ? service.getBasePrice() : BigDecimal.ZERO);

        String categoryId = (service.getCategory() != null && service.getCategory().getId() != null)
                ? service.getCategory().getId().toString()
                : null;

        // Check if service already exists in active cart
        Optional<CartItem> existingItemOpt = cartItemRepository.findByCart_IdAndServiceId(cart.getId(), service.getId());
        CartItem itemToSave;

        if (existingItemOpt.isPresent()) {
            itemToSave = existingItemOpt.get();
            itemToSave.setQuantity(itemToSave.getQuantity() + quantityToAdd);
            itemToSave.setPrice(unitPrice);
            itemToSave.recalculateTotalPrice();
        } else {
            itemToSave = CartItem.builder()
                    .cart(cart)
                    .serviceId(service.getId())
                    .serviceName(service.getName())
                    .categoryId(categoryId)
                    .imageUrl(service.getImageUrl())
                    .quantity(quantityToAdd)
                    .price(unitPrice)
                    .totalPrice(unitPrice.multiply(BigDecimal.valueOf(quantityToAdd)))
                    .build();
        }

        CartItem saved = cartItemRepository.save(itemToSave);
        log.info("Service '{}' (id: {}) added to cart #{} for customer {} (Quantity: {}) - ZERO bookings created",
                service.getName(), service.getId(), cart.getId(), customerId, saved.getQuantity());

        return mapToCartItemResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponseDTO getActiveCart(UUID customerId) {
        customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));

        Optional<Cart> cartOpt = cartRepository.findFirstByCustomerIdAndStatusOrderByCreatedAtDesc(customerId, CartStatus.ACTIVE);
        if (cartOpt.isEmpty()) {
            return CartResponseDTO.builder()
                    .customerId(customerId.toString())
                    .items(Collections.emptyList())
                    .subtotal(BigDecimal.ZERO)
                    .total(BigDecimal.ZERO)
                    .status(CartStatus.ACTIVE.name())
                    .build();
        }

        Cart cart = cartOpt.get();
        List<CartItem> items = cartItemRepository.findByCart_Id(cart.getId());

        BigDecimal subtotal = BigDecimal.ZERO;
        List<CartItemResponseDTO> itemDTOs = items.stream().map(item -> {
            // Live refresh of catalog price
            Optional<ServiceItem> serviceOpt = serviceItemRepository.findById(item.getServiceId());
            if (serviceOpt.isPresent()) {
                ServiceItem s = serviceOpt.get();
                BigDecimal livePrice = (s.getFinalAmount() != null && s.getFinalAmount().compareTo(BigDecimal.ZERO) > 0)
                        ? s.getFinalAmount()
                        : (s.getBasePrice() != null ? s.getBasePrice() : item.getPrice());
                item.setPrice(livePrice);
                item.recalculateTotalPrice();
            }
            return mapToCartItemResponseDTO(item);
        }).collect(Collectors.toList());

        for (CartItemResponseDTO dto : itemDTOs) {
            if (dto.getTotalPrice() != null) {
                subtotal = subtotal.add(dto.getTotalPrice());
            }
        }

        return CartResponseDTO.builder()
                .cartId(cart.getId())
                .customerId(customerId.toString())
                .items(itemDTOs)
                .subtotal(subtotal)
                .total(subtotal)
                .status(cart.getStatus().name())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CartCountResponseDTO getCartCount(UUID customerId) {
        customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));

        Optional<Cart> cartOpt = cartRepository.findFirstByCustomerIdAndStatusOrderByCreatedAtDesc(customerId, CartStatus.ACTIVE);
        if (cartOpt.isEmpty()) {
            return new CartCountResponseDTO(0);
        }

        List<CartItem> items = cartItemRepository.findByCart_Id(cartOpt.get().getId());
        int count = items.stream().mapToInt(CartItem::getQuantity).sum();
        return new CartCountResponseDTO(count);
    }

    @Override
    public CartItemResponseDTO updateCartItem(UUID customerId, Long cartItemId, UpdateCartItemRequestDTO request) {
        customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CartItemNotFoundException("Cart item not found with id: " + cartItemId));

        // Validate customer ownership
        if (item.getCart() == null || !customerId.equals(item.getCart().getCustomerId())) {
            throw new ForbiddenException("You are not authorized to modify this cart item");
        }

        if (item.getCart().getStatus() != CartStatus.ACTIVE) {
            throw new IllegalArgumentException("Cart is not in ACTIVE status");
        }

        int newQuantity = (request.getQuantity() != null) ? request.getQuantity() : 0;
        if (newQuantity <= 0) {
            cartItemRepository.delete(item);
            return null;
        }

        // Live refresh price
        Optional<ServiceItem> serviceOpt = serviceItemRepository.findById(item.getServiceId());
        if (serviceOpt.isPresent()) {
            ServiceItem s = serviceOpt.get();
            BigDecimal livePrice = (s.getFinalAmount() != null && s.getFinalAmount().compareTo(BigDecimal.ZERO) > 0)
                    ? s.getFinalAmount()
                    : (s.getBasePrice() != null ? s.getBasePrice() : item.getPrice());
            item.setPrice(livePrice);
        }

        item.setQuantity(newQuantity);
        item.recalculateTotalPrice();
        CartItem saved = cartItemRepository.save(item);

        return mapToCartItemResponseDTO(saved);
    }

    @Override
    public void removeCartItem(UUID customerId, Long cartItemId) {
        customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CartItemNotFoundException("Cart item not found with id: " + cartItemId));

        if (item.getCart() == null || !customerId.equals(item.getCart().getCustomerId())) {
            throw new ForbiddenException("You are not authorized to remove this cart item");
        }

        cartItemRepository.delete(item);
    }

    @Override
    public void clearCart(UUID customerId) {
        customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));

        Optional<Cart> cartOpt = cartRepository.findFirstByCustomerIdAndStatusOrderByCreatedAtDesc(customerId, CartStatus.ACTIVE);
        cartOpt.ifPresent(cart -> cartItemRepository.deleteByCart_Id(cart.getId()));
    }

    private CartItemResponseDTO mapToCartItemResponseDTO(CartItem item) {
        return CartItemResponseDTO.builder()
                .cartItemId(item.getId())
                .serviceId(item.getServiceId())
                .serviceName(item.getServiceName())
                .categoryId(item.getCategoryId())
                .imageUrl(item.getImageUrl())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .totalPrice(item.getTotalPrice())
                .build();
    }
}
