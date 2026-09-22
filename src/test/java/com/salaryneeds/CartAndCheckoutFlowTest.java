package com.salaryneeds;

import com.salaryneeds.dto.*;
import com.salaryneeds.entity.*;
import com.salaryneeds.entity.enums.BookingStatus;
import com.salaryneeds.entity.enums.CartStatus;
import com.salaryneeds.exception.ForbiddenException;
import com.salaryneeds.repository.*;
import com.salaryneeds.service.BookingServiceImpl;
import com.salaryneeds.service.CartServiceImpl;
import com.salaryneeds.service.WorkerMatchingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartAndCheckoutFlowTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ServiceItemRepository serviceItemRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private BookingItemRepository bookingItemRepository;

    @Mock
    private WorkerMatchingService workerMatchingService;

    @InjectMocks
    private CartServiceImpl cartService;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private UUID customerId;
    private Customer customer;
    private ServiceItem electricianService;
    private ServiceItem plumberService;
    private Cart activeCart;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        customer = Customer.builder()
                .id(customerId)
                .name("Aparna Devi")
                .email("aparna@example.com")
                .phone("+919876543210")
                .build();

        Category electricianCat = Category.builder().id(UUID.randomUUID()).name("Electrician").build();
        electricianService = ServiceItem.builder()
                .id(101L)
                .name("Electrician Service")
                .category(electricianCat)
                .basePrice(new BigDecimal("499.00"))
                .finalAmount(new BigDecimal("499.00"))
                .isActive(true)
                .build();

        Category plumberCat = Category.builder().id(UUID.randomUUID()).name("Plumber").build();
        plumberService = ServiceItem.builder()
                .id(102L)
                .name("Plumber Service")
                .category(plumberCat)
                .basePrice(new BigDecimal("399.00"))
                .finalAmount(new BigDecimal("399.00"))
                .isActive(true)
                .build();

        activeCart = Cart.builder()
                .id(501L)
                .customerId(customerId)
                .status(CartStatus.ACTIVE)
                .items(new ArrayList<>())
                .build();
    }

    // =========================================================================
    // 1. ADD TO CART & ZERO BOOKINGS CREATED
    // =========================================================================

    @Test
    @DisplayName("Step 1: Add Electrician to cart gets price from DB and creates ZERO bookings")
    void testAddToCart_Electrician_Success_ZeroBookingsCreated() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(serviceItemRepository.findById(101L)).thenReturn(Optional.of(electricianService));
        when(cartRepository.findFirstByCustomerIdAndStatusOrderByCreatedAtDesc(customerId, CartStatus.ACTIVE))
                .thenReturn(Optional.of(activeCart));
        when(cartItemRepository.findByCart_IdAndServiceId(501L, 101L)).thenReturn(Optional.empty());

        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> {
            CartItem item = invocation.getArgument(0);
            item.setId(1001L);
            return item;
        });

        AddToCartRequestDTO request = AddToCartRequestDTO.builder()
                .serviceId(101L)
                .quantity(1)
                .build();

        CartItemResponseDTO response = cartService.addToCart(customerId, request);

        assertNotNull(response);
        assertEquals(101L, response.getServiceId());
        assertEquals("Electrician Service", response.getServiceName());
        assertEquals(1, response.getQuantity());
        assertEquals(new BigDecimal("499.00"), response.getPrice());
        assertEquals(new BigDecimal("499.00"), response.getTotalPrice());

        // CRITICAL REQUIREMENT: ZERO bookings created on adding to cart
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("Step 2: Adding same service to cart increments quantity and recalculates total")
    void testAddToCart_IncrementExistingQuantity() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(serviceItemRepository.findById(101L)).thenReturn(Optional.of(electricianService));
        when(cartRepository.findFirstByCustomerIdAndStatusOrderByCreatedAtDesc(customerId, CartStatus.ACTIVE))
                .thenReturn(Optional.of(activeCart));

        CartItem existingItem = CartItem.builder()
                .id(1001L)
                .cart(activeCart)
                .serviceId(101L)
                .serviceName("Electrician Service")
                .quantity(1)
                .price(new BigDecimal("499.00"))
                .totalPrice(new BigDecimal("499.00"))
                .build();

        when(cartItemRepository.findByCart_IdAndServiceId(501L, 101L)).thenReturn(Optional.of(existingItem));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AddToCartRequestDTO request = AddToCartRequestDTO.builder()
                .serviceId(101L)
                .quantity(2)
                .build();

        CartItemResponseDTO response = cartService.addToCart(customerId, request);

        assertNotNull(response);
        assertEquals(3, response.getQuantity());
        assertEquals(new BigDecimal("1497.00"), response.getTotalPrice());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    // =========================================================================
    // 2. GET ACTIVE CART & CART COUNT
    // =========================================================================

    @Test
    @DisplayName("Step 3: Review active cart calculates subtotal and total accurately")
    void testGetActiveCart_CalculatesSubtotalAndTotal() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(cartRepository.findFirstByCustomerIdAndStatusOrderByCreatedAtDesc(customerId, CartStatus.ACTIVE))
                .thenReturn(Optional.of(activeCart));

        CartItem item1 = CartItem.builder()
                .id(1001L)
                .cart(activeCart)
                .serviceId(101L)
                .serviceName("Electrician Service")
                .quantity(1)
                .price(new BigDecimal("499.00"))
                .totalPrice(new BigDecimal("499.00"))
                .build();

        CartItem item2 = CartItem.builder()
                .id(1002L)
                .cart(activeCart)
                .serviceId(102L)
                .serviceName("Plumber Service")
                .quantity(1)
                .price(new BigDecimal("399.00"))
                .totalPrice(new BigDecimal("399.00"))
                .build();

        when(cartItemRepository.findByCart_Id(501L)).thenReturn(List.of(item1, item2));
        when(serviceItemRepository.findById(101L)).thenReturn(Optional.of(electricianService));
        when(serviceItemRepository.findById(102L)).thenReturn(Optional.of(plumberService));

        CartResponseDTO response = cartService.getActiveCart(customerId);

        assertNotNull(response);
        assertEquals(501L, response.getCartId());
        assertEquals(2, response.getItems().size());
        assertEquals(new BigDecimal("898.00"), response.getSubtotal());
        assertEquals(new BigDecimal("898.00"), response.getTotal());
        assertEquals("ACTIVE", response.getStatus());

        // ZERO bookings created
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("Step 4: Cart count reflects total items in active cart")
    void testGetCartCount() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(cartRepository.findFirstByCustomerIdAndStatusOrderByCreatedAtDesc(customerId, CartStatus.ACTIVE))
                .thenReturn(Optional.of(activeCart));

        CartItem item1 = CartItem.builder().quantity(1).build();
        CartItem item2 = CartItem.builder().quantity(2).build();
        when(cartItemRepository.findByCart_Id(501L)).thenReturn(List.of(item1, item2));

        CartCountResponseDTO response = cartService.getCartCount(customerId);
        assertEquals(3, response.getCount());
    }

    // =========================================================================
    // 3. UPDATE & REMOVE CART ITEMS
    // =========================================================================

    @Test
    @DisplayName("Step 5: Update cart item quantity recalculates total; zero deletes item")
    void testUpdateCartItem_UpdateAndZeroDeletes() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        CartItem item = CartItem.builder()
                .id(1001L)
                .cart(activeCart)
                .serviceId(101L)
                .serviceName("Electrician Service")
                .quantity(1)
                .price(new BigDecimal("499.00"))
                .totalPrice(new BigDecimal("499.00"))
                .build();

        when(cartItemRepository.findById(1001L)).thenReturn(Optional.of(item));
        when(serviceItemRepository.findById(101L)).thenReturn(Optional.of(electricianService));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Update to quantity 3
        CartItemResponseDTO updated = cartService.updateCartItem(customerId, 1001L, new UpdateCartItemRequestDTO(3));
        assertNotNull(updated);
        assertEquals(3, updated.getQuantity());
        assertEquals(new BigDecimal("1497.00"), updated.getTotalPrice());

        // Update to quantity 0 deletes item
        CartItemResponseDTO deleted = cartService.updateCartItem(customerId, 1001L, new UpdateCartItemRequestDTO(0));
        assertNull(deleted);
        verify(cartItemRepository).delete(item);
    }

    @Test
    @DisplayName("Step 6: Security - Modifying another customer's cart item throws ForbiddenException")
    void testCartSecurity_UnauthorizedCustomerThrowsForbidden() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        UUID otherCustomerId = UUID.randomUUID();
        Cart otherCart = Cart.builder()
                .id(999L)
                .customerId(otherCustomerId)
                .status(CartStatus.ACTIVE)
                .build();

        CartItem otherItem = CartItem.builder()
                .id(2001L)
                .cart(otherCart)
                .serviceId(101L)
                .build();

        when(cartItemRepository.findById(2001L)).thenReturn(Optional.of(otherItem));

        assertThrows(ForbiddenException.class, () ->
                cartService.updateCartItem(customerId, 2001L, new UpdateCartItemRequestDTO(2))
        );

        assertThrows(ForbiddenException.class, () ->
                cartService.removeCartItem(customerId, 2001L)
        );
    }

    // =========================================================================
    // 4. ATOMIC CHECKOUT & WORKER MATCHING
    // =========================================================================

    @Test
    @DisplayName("Step 7: Atomic checkout creates Booking with status PENDING, BookingItems, converts Cart, triggers Worker Matching")
    void testCheckout_CreatesBookingAndBookingItems_ConvertsCart() {
        when(customerRepository.existsById(customerId)).thenReturn(true);

        // Address setup
        UUID addressId = UUID.randomUUID();
        Address address = Address.builder()
                .id(addressId)
                .customer(customer)
                .addressLine("123 Green Valley, Sector 4")
                .city("Hyderabad")
                .pincode("500081")
                .lat(17.4435)
                .lng(78.3842)
                .build();
        when(addressRepository.findByIdAndCustomerId(addressId, customerId)).thenReturn(Optional.of(address));

        // Active Cart setup
        when(cartRepository.findFirstByCustomerIdAndStatusOrderByCreatedAtDesc(customerId, CartStatus.ACTIVE))
                .thenReturn(Optional.of(activeCart));

        CartItem item1 = CartItem.builder()
                .id(1001L)
                .cart(activeCart)
                .serviceId(101L)
                .serviceName("Electrician Service")
                .categoryId(electricianService.getCategory().getId().toString())
                .quantity(1)
                .price(new BigDecimal("499.00"))
                .totalPrice(new BigDecimal("499.00"))
                .build();

        when(cartItemRepository.findByCart_Id(501L)).thenReturn(List.of(item1));
        when(serviceItemRepository.findById(101L)).thenReturn(Optional.of(electricianService));

        // Mock Booking save
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId(8881L);
            return b;
        });

        // Mock BookingItem save
        when(bookingItemRepository.save(any(BookingItem.class))).thenAnswer(invocation -> {
            BookingItem bi = invocation.getArgument(0);
            bi.setId(9001L);
            return bi;
        });

        BookingCheckoutRequestDTO checkoutRequest = BookingCheckoutRequestDTO.builder()
                .addressId(addressId.toString())
                .scheduledDate(LocalDate.now().plusDays(1))
                .scheduledTime("10:00 AM - 01:00 PM")
                .slotId("SLOT-1013")
                .notes("Please ring the bell twice")
                .idempotencyKey("ORDER-IDEMP-001")
                .build();

        BookingCheckoutResponseDTO response = bookingService.checkout(checkoutRequest, customerId.toString(), null);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(501L, response.getCartId());
        assertEquals(new BigDecimal("499.00"), response.getTotalAmount());
        assertEquals(BookingStatus.PENDING, response.getStatus());

        // Verify Cart is marked CONVERTED
        assertEquals(CartStatus.CONVERTED, activeCart.getStatus());
        verify(cartRepository).save(activeCart);
        verify(cartItemRepository).deleteByCart_Id(501L);

        // Verify Booking was created with status PENDING
        verify(bookingRepository, atLeastOnce()).save(any(Booking.class));

        // Verify BookingItem was created
        verify(bookingItemRepository, atLeastOnce()).save(any(BookingItem.class));

        // Verify Worker Matching Engine was triggered
        verify(workerMatchingService, atLeastOnce()).matchAndCreateOffers(any(Booking.class));
    }

    @Test
    @DisplayName("Step 8: Double-click idempotency prevents duplicate booking creation")
    void testCheckout_IdempotencyPreventsDuplicateBooking() {
        when(customerRepository.existsById(customerId)).thenReturn(true);

        Booking existingBooking = Booking.builder()
                .id(7771L)
                .customerId(customerId.toString())
                .checkoutId("IDEMP-KEY-999")
                .status(BookingStatus.PENDING)
                .totalAmount(new BigDecimal("499.00"))
                .payableAmount(new BigDecimal("499.00"))
                .build();

        when(bookingRepository.findByCheckoutId("IDEMP-KEY-999")).thenReturn(List.of(existingBooking));

        BookingCheckoutRequestDTO checkoutRequest = BookingCheckoutRequestDTO.builder()
                .addressId(UUID.randomUUID().toString())
                .scheduledDate(LocalDate.now().plusDays(1))
                .idempotencyKey("IDEMP-KEY-999")
                .build();

        BookingCheckoutResponseDTO response = bookingService.checkout(checkoutRequest, customerId.toString(), "IDEMP-KEY-999");

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Booking already processed (idempotent request)", response.getMessage());

        // Zero new bookings created on duplicate click
        verify(bookingRepository, never()).save(any(Booking.class));
    }
}
