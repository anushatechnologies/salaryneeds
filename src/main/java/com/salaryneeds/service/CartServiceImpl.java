package com.salaryneeds.service;

import com.salaryneeds.dto.*;
import com.salaryneeds.entity.Cart;
import com.salaryneeds.entity.CartItem;
import com.salaryneeds.entity.ServiceItem;
import com.salaryneeds.entity.enums.CartStatus;
import com.salaryneeds.exception.CartItemNotFoundException;
import com.salaryneeds.exception.CartNotFoundException;
import com.salaryneeds.exception.CustomerNotFoundException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ServiceItemRepository serviceItemRepository;
    private final CustomerRepository customerRepository;

    private void validateCustomer(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer ID is required");
        }
        try {
            UUID uuid = UUID.fromString(customerId.trim());
            if (!customerRepository.existsById(uuid)) {
                throw new CustomerNotFoundException("Customer not found with id: " + customerId);
            }
        } catch (IllegalArgumentException e) {
            // Non-UUID customer identifier (e.g. gateway/mock), allow as valid customer string
        }
    }

    private BigDecimal resolveServicePrice(ServiceItem service) {
        if (service.getFinalAmount() != null && service.getFinalAmount().compareTo(BigDecimal.ZERO) > 0) {
            return service.getFinalAmount();
        }
        if (service.getDiscountPrice() != null && service.getDiscountPrice().compareTo(BigDecimal.ZERO) > 0) {
            return service.getDiscountPrice();
        }
        if (service.getBasePrice() != null && service.getBasePrice().compareTo(BigDecimal.ZERO) > 0) {
            return service.getBasePrice();
        }
        return BigDecimal.ZERO;
    }

    @Override
    @Transactional
    public CartResponseDTO addToCart(String customerId, CartItemAddRequestDTO request) {
        validateCustomer(customerId);

        if (request.getServiceId() == null) {
            throw new IllegalArgumentException("serviceId is required");
        }
        int quantityToAdd = (request.getQuantity() != null && request.getQuantity() > 0) ? request.getQuantity() : 1;

        ServiceItem service = serviceItemRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ServiceNotFoundException("Service not found with id: " + request.getServiceId()));

        if (Boolean.FALSE.equals(service.getIsActive())) {
            throw new IllegalArgumentException("Selected service '" + service.getName() + "' is currently inactive");
        }

        BigDecimal unitPrice = resolveServicePrice(service);
        String categoryId = service.getCategory() != null ? service.getCategory().getId().toString() : null;
        String categoryName = service.getCategory() != null ? service.getCategory().getName() : null;

        // Retrieve existing ACTIVE cart or initialize a new one
        Cart cart = cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)
                .orElseGet(() -> Cart.builder()
                        .customerId(customerId)
                        .status(CartStatus.ACTIVE)
                        .items(new ArrayList<>())
                        .build());

        // Check if service is already present in this cart
        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getServiceId().equals(service.getId()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + quantityToAdd);
            existingItem.setPrice(unitPrice);
            existingItem.recalculateTotalPrice();
        } else {
            CartItem newItem = CartItem.builder()
                    .serviceId(service.getId())
                    .serviceName(service.getName())
                    .categoryId(categoryId)
                    .categoryName(categoryName)
                    .quantity(quantityToAdd)
                    .price(unitPrice)
                    .totalPrice(unitPrice.multiply(BigDecimal.valueOf(quantityToAdd)))
                    .build();
            cart.addItem(newItem);
        }

        cart.recalculateTotals();
        Cart savedCart = cartRepository.save(cart);
        log.info("Added serviceId={} qty={} to cartId={} for customerId={}", service.getId(), quantityToAdd, savedCart.getId(), customerId);

        return mapToDTO(savedCart);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponseDTO getActiveCart(String customerId) {
        validateCustomer(customerId);

        return cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)
                .map(this::mapToDTO)
                .orElseGet(() -> CartResponseDTO.builder()
                        .customerId(customerId)
                        .status(CartStatus.ACTIVE)
                        .items(new ArrayList<>())
                        .totalItems(0)
                        .subtotal(BigDecimal.ZERO)
                        .total(BigDecimal.ZERO)
                        .build());
    }

    @Override
    @Transactional(readOnly = true)
    public CartCountResponseDTO getCartCount(String customerId) {
        validateCustomer(customerId);

        int count = cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)
                .map(cart -> cart.getItems().stream()
                        .mapToInt(item -> item.getQuantity() != null ? item.getQuantity() : 0)
                        .sum())
                .orElse(0);

        return CartCountResponseDTO.builder().count(count).build();
    }

    @Override
    @Transactional
    public CartResponseDTO updateItemQuantity(String customerId, Long cartItemId, CartItemUpdateRequestDTO request) {
        validateCustomer(customerId);

        Cart cart = cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)
                .orElseThrow(() -> new CartNotFoundException("No active cart found for customer: " + customerId));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new CartItemNotFoundException("Cart item with ID " + cartItemId + " not found in customer's cart"));

        int newQuantity = (request.getQuantity() != null) ? request.getQuantity() : 0;
        if (newQuantity <= 0) {
            cart.removeItem(item);
            log.info("Removed cartItemId={} from cartId={} due to quantity <= 0", cartItemId, cart.getId());
        } else {
            item.setQuantity(newQuantity);
            item.recalculateTotalPrice();
            log.info("Updated cartItemId={} quantity to {} in cartId={}", cartItemId, newQuantity, cart.getId());
        }

        cart.recalculateTotals();
        Cart savedCart = cartRepository.save(cart);
        return mapToDTO(savedCart);
    }

    @Override
    @Transactional
    public CartResponseDTO removeItem(String customerId, Long cartItemId) {
        validateCustomer(customerId);

        Cart cart = cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)
                .orElseThrow(() -> new CartNotFoundException("No active cart found for customer: " + customerId));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new CartItemNotFoundException("Cart item with ID " + cartItemId + " not found in customer's cart"));

        cart.removeItem(item);
        cart.recalculateTotals();
        Cart savedCart = cartRepository.save(cart);
        log.info("Removed cartItemId={} from cartId={}", cartItemId, cart.getId());

        return mapToDTO(savedCart);
    }

    @Override
    @Transactional
    public CartResponseDTO clearCart(String customerId) {
        validateCustomer(customerId);

        Cart cart = cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)
                .orElse(null);

        if (cart != null) {
            cart.clearItems();
            cart.recalculateTotals();
            Cart savedCart = cartRepository.save(cart);
            log.info("Cleared all items from cartId={} for customerId={}", savedCart.getId(), customerId);
            return mapToDTO(savedCart);
        }

        return CartResponseDTO.builder()
                .customerId(customerId)
                .status(CartStatus.ACTIVE)
                .items(new ArrayList<>())
                .totalItems(0)
                .subtotal(BigDecimal.ZERO)
                .total(BigDecimal.ZERO)
                .build();
    }

    private CartResponseDTO mapToDTO(Cart cart) {
        List<CartItemResponseDTO> itemDTOs = cart.getItems().stream()
                .map(this::mapItemToDTO)
                .collect(Collectors.toList());

        int totalItems = cart.getItems().stream()
                .mapToInt(i -> i.getQuantity() != null ? i.getQuantity() : 0)
                .sum();

        BigDecimal subtotal = (cart.getTotalAmount() != null) ? cart.getTotalAmount() : BigDecimal.ZERO;

        return CartResponseDTO.builder()
                .id(cart.getId())
                .customerId(cart.getCustomerId())
                .status(cart.getStatus())
                .items(itemDTOs)
                .totalItems(totalItems)
                .subtotal(subtotal)
                .total(subtotal)
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    private CartItemResponseDTO mapItemToDTO(CartItem item) {
        return CartItemResponseDTO.builder()
                .id(item.getId())
                .serviceId(item.getServiceId())
                .serviceName(item.getServiceName())
                .categoryId(item.getCategoryId())
                .categoryName(item.getCategoryName())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .totalPrice(item.getTotalPrice())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
