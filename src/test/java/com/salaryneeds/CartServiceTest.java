package com.salaryneeds;

import com.salaryneeds.dto.CartCountResponseDTO;
import com.salaryneeds.dto.CartItemAddRequestDTO;
import com.salaryneeds.dto.CartItemUpdateRequestDTO;
import com.salaryneeds.dto.CartResponseDTO;
import com.salaryneeds.entity.Cart;
import com.salaryneeds.entity.CartItem;
import com.salaryneeds.entity.Category;
import com.salaryneeds.entity.ServiceItem;
import com.salaryneeds.entity.enums.CartStatus;
import com.salaryneeds.exception.CartItemNotFoundException;
import com.salaryneeds.exception.CartNotFoundException;
import com.salaryneeds.exception.ServiceNotFoundException;
import com.salaryneeds.repository.CartItemRepository;
import com.salaryneeds.repository.CartRepository;
import com.salaryneeds.repository.CustomerRepository;
import com.salaryneeds.repository.ServiceItemRepository;
import com.salaryneeds.service.CartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ServiceItemRepository serviceItemRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private String customerId;
    private ServiceItem sampleService;
    private Category sampleCategory;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID().toString();
        lenient().when(customerRepository.existsById(any(UUID.class))).thenReturn(true);

        sampleCategory = Category.builder()
                .id(UUID.randomUUID())
                .name("Electrician")
                .build();

        sampleService = ServiceItem.builder()
                .id(10L)
                .name("Switch Board Repair")
                .category(sampleCategory)
                .basePrice(BigDecimal.valueOf(299.00))
                .finalAmount(BigDecimal.valueOf(249.00))
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Adding service to cart creates ACTIVE cart, sets authoritative backend price, and does NOT create a booking")
    void testAddToCart_Success() {
        when(serviceItemRepository.findById(10L)).thenReturn(Optional.of(sampleService));
        when(cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
            Cart c = invocation.getArgument(0);
            c.setId(1L);
            return c;
        });

        CartItemAddRequestDTO request = CartItemAddRequestDTO.builder()
                .serviceId(10L)
                .quantity(2)
                .build();

        CartResponseDTO response = cartService.addToCart(customerId, request);

        assertNotNull(response);
        assertEquals(CartStatus.ACTIVE, response.getStatus());
        assertEquals(1, response.getItems().size());
        assertEquals(2, response.getTotalItems());
        // Backend authoritative price is 249.00 * 2 = 498.00
        assertEquals(BigDecimal.valueOf(249.00), response.getItems().get(0).getPrice());
        assertEquals(BigDecimal.valueOf(498.00), response.getItems().get(0).getTotalPrice());
        assertEquals(BigDecimal.valueOf(498.00), response.getTotal());

        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    @DisplayName("Adding same service increments existing item quantity and recalculates totals")
    void testAddToCart_ExistingItem_IncrementsQuantity() {
        when(serviceItemRepository.findById(10L)).thenReturn(Optional.of(sampleService));

        Cart existingCart = Cart.builder()
                .id(5L)
                .customerId(customerId)
                .status(CartStatus.ACTIVE)
                .items(new ArrayList<>())
                .build();

        CartItem existingItem = CartItem.builder()
                .id(20L)
                .cart(existingCart)
                .serviceId(10L)
                .serviceName("Switch Board Repair")
                .quantity(1)
                .price(BigDecimal.valueOf(249.00))
                .totalPrice(BigDecimal.valueOf(249.00))
                .build();
        existingCart.addItem(existingItem);

        when(cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)).thenReturn(Optional.of(existingCart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartItemAddRequestDTO request = CartItemAddRequestDTO.builder()
                .serviceId(10L)
                .quantity(3)
                .build();

        CartResponseDTO response = cartService.addToCart(customerId, request);

        assertEquals(1, response.getItems().size());
        assertEquals(4, response.getTotalItems());
        assertEquals(BigDecimal.valueOf(996.00), response.getTotal());
    }

    @Test
    @DisplayName("Adding inactive service throws IllegalArgumentException")
    void testAddToCart_InactiveService_ThrowsException() {
        sampleService.setIsActive(false);
        when(serviceItemRepository.findById(10L)).thenReturn(Optional.of(sampleService));

        CartItemAddRequestDTO request = CartItemAddRequestDTO.builder()
                .serviceId(10L)
                .quantity(1)
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                cartService.addToCart(customerId, request)
        );
        assertTrue(ex.getMessage().contains("inactive"));
        verify(cartRepository, never()).save(any());
    }

    @Test
    @DisplayName("Adding non-existent service throws ServiceNotFoundException")
    void testAddToCart_NotFoundService_ThrowsException() {
        when(serviceItemRepository.findById(999L)).thenReturn(Optional.empty());

        CartItemAddRequestDTO request = CartItemAddRequestDTO.builder()
                .serviceId(999L)
                .quantity(1)
                .build();

        assertThrows(ServiceNotFoundException.class, () ->
                cartService.addToCart(customerId, request)
        );
    }

    @Test
    @DisplayName("Update item quantity successfully updates quantity and subtotal")
    void testUpdateItemQuantity_Success() {
        Cart cart = Cart.builder()
                .id(1L)
                .customerId(customerId)
                .status(CartStatus.ACTIVE)
                .items(new ArrayList<>())
                .build();

        CartItem item = CartItem.builder()
                .id(101L)
                .cart(cart)
                .serviceId(10L)
                .serviceName("Switch Board Repair")
                .quantity(2)
                .price(BigDecimal.valueOf(249.00))
                .totalPrice(BigDecimal.valueOf(498.00))
                .build();
        cart.addItem(item);

        when(cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartItemUpdateRequestDTO updateRequest = CartItemUpdateRequestDTO.builder()
                .quantity(5)
                .build();

        CartResponseDTO response = cartService.updateItemQuantity(customerId, 101L, updateRequest);

        assertEquals(5, response.getTotalItems());
        assertEquals(BigDecimal.valueOf(1245.00), response.getTotal());
    }

    @Test
    @DisplayName("Update item quantity to 0 removes the item from the cart")
    void testUpdateItemQuantity_ZeroRemovesItem() {
        Cart cart = Cart.builder()
                .id(1L)
                .customerId(customerId)
                .status(CartStatus.ACTIVE)
                .items(new ArrayList<>())
                .build();

        CartItem item = CartItem.builder()
                .id(101L)
                .cart(cart)
                .serviceId(10L)
                .quantity(2)
                .price(BigDecimal.valueOf(200.00))
                .totalPrice(BigDecimal.valueOf(400.00))
                .build();
        cart.addItem(item);

        when(cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartItemUpdateRequestDTO updateRequest = CartItemUpdateRequestDTO.builder()
                .quantity(0)
                .build();

        CartResponseDTO response = cartService.updateItemQuantity(customerId, 101L, updateRequest);

        assertEquals(0, response.getTotalItems());
        assertEquals(BigDecimal.ZERO, response.getTotal());
        assertTrue(response.getItems().isEmpty());
    }

    @Test
    @DisplayName("Security: Updating item belonging to another customer throws CartItemNotFoundException")
    void testUpdateItemQuantity_WrongCustomer_ThrowsException() {
        Cart cart = Cart.builder()
                .id(1L)
                .customerId(customerId)
                .status(CartStatus.ACTIVE)
                .items(new ArrayList<>())
                .build();

        // Customer's cart only has item 101L, someone tries to edit 999L
        CartItem item = CartItem.builder()
                .id(101L)
                .cart(cart)
                .serviceId(10L)
                .quantity(1)
                .price(BigDecimal.valueOf(100.00))
                .totalPrice(BigDecimal.valueOf(100.00))
                .build();
        cart.addItem(item);

        when(cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)).thenReturn(Optional.of(cart));

        CartItemUpdateRequestDTO updateRequest = CartItemUpdateRequestDTO.builder().quantity(2).build();

        assertThrows(CartItemNotFoundException.class, () ->
                cartService.updateItemQuantity(customerId, 999L, updateRequest)
        );
    }

    @Test
    @DisplayName("Removing item explicitly removes item and updates totals")
    void testRemoveItem_Success() {
        Cart cart = Cart.builder()
                .id(1L)
                .customerId(customerId)
                .status(CartStatus.ACTIVE)
                .items(new ArrayList<>())
                .build();

        CartItem item = CartItem.builder()
                .id(202L)
                .cart(cart)
                .serviceId(10L)
                .quantity(1)
                .price(BigDecimal.valueOf(150.00))
                .totalPrice(BigDecimal.valueOf(150.00))
                .build();
        cart.addItem(item);

        when(cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartResponseDTO response = cartService.removeItem(customerId, 202L);

        assertEquals(0, response.getTotalItems());
        assertTrue(response.getItems().isEmpty());
    }

    @Test
    @DisplayName("Clear cart empties all items")
    void testClearCart_Success() {
        Cart cart = Cart.builder()
                .id(1L)
                .customerId(customerId)
                .status(CartStatus.ACTIVE)
                .items(new ArrayList<>())
                .build();

        CartItem item = CartItem.builder()
                .id(301L)
                .cart(cart)
                .serviceId(10L)
                .quantity(2)
                .price(BigDecimal.valueOf(100.00))
                .totalPrice(BigDecimal.valueOf(200.00))
                .build();
        cart.addItem(item);

        when(cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartResponseDTO response = cartService.clearCart(customerId);

        assertEquals(0, response.getTotalItems());
        assertEquals(BigDecimal.ZERO, response.getTotal());
        assertTrue(response.getItems().isEmpty());
    }

    @Test
    @DisplayName("Get cart count returns total quantity across all items")
    void testGetCartCount() {
        Cart cart = Cart.builder()
                .id(1L)
                .customerId(customerId)
                .status(CartStatus.ACTIVE)
                .items(new ArrayList<>())
                .build();

        cart.addItem(CartItem.builder().id(1L).cart(cart).serviceId(10L).quantity(3).price(BigDecimal.TEN).totalPrice(BigDecimal.valueOf(30)).build());
        cart.addItem(CartItem.builder().id(2L).cart(cart).serviceId(20L).quantity(2).price(BigDecimal.TEN).totalPrice(BigDecimal.valueOf(20)).build());

        when(cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)).thenReturn(Optional.of(cart));

        CartCountResponseDTO countDTO = cartService.getCartCount(customerId);
        assertEquals(5, countDTO.getCount());
    }
}
