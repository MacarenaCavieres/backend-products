package com.mccr.backend.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.mccr.backend.ecommerce.dto.ItemCart;
import com.mccr.backend.ecommerce.dto.PaymentRequest;
import com.mccr.backend.ecommerce.dto.PaymentResponse;
import com.mccr.backend.ecommerce.model.OrderItem;
import com.mccr.backend.ecommerce.model.Payment;
import com.mccr.backend.ecommerce.model.Product;
import com.mccr.backend.ecommerce.model.User;
import com.mccr.backend.ecommerce.repository.PaymentRepository;
import com.mccr.backend.ecommerce.repository.ProductRepository;
import com.mccr.backend.ecommerce.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {
    @Mock
    JwtService jwtService;

    @Mock
    UserRepository userRepository;

    @Mock
    ProductRepository productRepository;

    @Mock
    PaymentRepository paymentRepository;

    @InjectMocks
    PaymentService paymentService;

    @Test
    @DisplayName("It should throw a ResponseStatusException when the user obtained from the token does not exist")
    void shouldThrowExceptionWhenUserFromTokenDoesNotExist() {
        when(jwtService.getUserIdFromToken("dfiugdf8gydf8g9hndf")).thenReturn("1");
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> paymentService.createPayment("dfiugdf8gydf8g9hndf", null));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Usuario no encontrado", ex.getReason());

        verify(jwtService).getUserIdFromToken("dfiugdf8gydf8g9hndf");
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("It should throw a ResponseStatusException when one of the products does not exist")
    void shouldThrowExceptionWhenOneProductDoesNotExist() {
        User u = buildUser();
        u.setId(1L);

        when(jwtService.getUserIdFromToken("dfiugdf8gydf8g9hndf")).thenReturn("1");
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(productRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> paymentService.createPayment("dfiugdf8gydf8g9hndf", buildPayment()));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("El producto con ID " + 1L + " no existe en el catálogo", ex.getReason());

        verify(jwtService).getUserIdFromToken("dfiugdf8gydf8g9hndf");
        verify(userRepository).findById(1L);
        verify(productRepository).findAllById(List.of(1L, 2L));
    }

    @Test
    @DisplayName("It should throw a ResponseStatusException when a product's stock is insufficient")
    void shouldThrowExceptionWhenProductStockIsInsufficient() {
        User u = buildUser();
        u.setId(1L);

        List<Product> products = buidlProducts();
        products.get(0).setQuantity(0L);

        when(jwtService.getUserIdFromToken("dfiugdf8gydf8g9hndf")).thenReturn("1");
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(productRepository.findAllById(List.of(1L, 2L))).thenReturn(products);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> paymentService.createPayment("dfiugdf8gydf8g9hndf", buildPayment()));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Stock insuficiente para el producto: " + products.get(0).getName(), ex.getReason());

        verify(jwtService).getUserIdFromToken("dfiugdf8gydf8g9hndf");
        verify(userRepository).findById(1L);
        verify(productRepository).findAllById(List.of(1L, 2L));
    }

    @Test
    @DisplayName("It should correctly deduct the stock of the purchased products")
    void shouldDeductStockOfTheProductPurchased() {
        User u = buildUser();
        u.setId(1L);

        List<Product> products = buidlProducts();

        when(jwtService.getUserIdFromToken("dfiugdf8gydf8g9hndf")).thenReturn("1");
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(productRepository.findAllById(List.of(1L, 2L))).thenReturn(products);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        paymentService.createPayment("dfiugdf8gydf8g9hndf", buildPayment());

        assertEquals(9L, products.get(0).getQuantity());
        assertEquals(9L, products.get(1).getQuantity());

        verify(jwtService).getUserIdFromToken("dfiugdf8gydf8g9hndf");
        verify(userRepository).findById(1L);
        verify(productRepository).findAllById(List.of(1L, 2L));
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("It should create an OrderItem for each product in the cart")
    void shouldCreateAnOrderItemForEachProductInCart() {
        User u = buildUser();
        u.setId(1L);

        List<Product> products = buidlProducts();

        when(jwtService.getUserIdFromToken("dfiugdf8gydf8g9hndf")).thenReturn("1");
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(productRepository.findAllById(List.of(1L, 2L))).thenReturn(products);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);

        paymentService.createPayment("dfiugdf8gydf8g9hndf", buildPayment());

        verify(paymentRepository).save(paymentCaptor.capture());
        Payment savedPayment = paymentCaptor.getValue();

        assertEquals(2, savedPayment.getItems().size());

        verify(jwtService).getUserIdFromToken("dfiugdf8gydf8g9hndf");
        verify(userRepository).findById(1L);
        verify(productRepository).findAllById(List.of(1L, 2L));
    }

    @Test
    @DisplayName("It should associate each OrderItem with the Payment")
    void shouldAssociateEachOrderItemWithThePayment() {
        User u = buildUser();
        u.setId(1L);

        List<Product> products = buidlProducts();

        when(jwtService.getUserIdFromToken("dfiugdf8gydf8g9hndf")).thenReturn("1");
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(productRepository.findAllById(List.of(1L, 2L))).thenReturn(products);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);

        paymentService.createPayment("dfiugdf8gydf8g9hndf", buildPayment());

        verify(paymentRepository).save(paymentCaptor.capture());
        Payment savedPayment = paymentCaptor.getValue();

        assertFalse(savedPayment.getItems().isEmpty());
        for (OrderItem item : savedPayment.getItems()) {
            assertEquals(savedPayment, item.getPayment());
        }

        verify(jwtService).getUserIdFromToken("dfiugdf8gydf8g9hndf");
        verify(userRepository).findById(1L);
        verify(productRepository).findAllById(List.of(1L, 2L));
    }

    @Test
    @DisplayName("It should save the payment in the repository")
    void shouldSaveThePaymentInTheRepository() {
        User u = buildUser();
        u.setId(1L);

        List<Product> products = buidlProducts();

        when(jwtService.getUserIdFromToken("dfiugdf8gydf8g9hndf")).thenReturn("1");
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(productRepository.findAllById(List.of(1L, 2L))).thenReturn(products);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        paymentService.createPayment("dfiugdf8gydf8g9hndf", buildPayment());

        verify(paymentRepository).save(any(Payment.class));
        verify(jwtService).getUserIdFromToken("dfiugdf8gydf8g9hndf");
        verify(userRepository).findById(1L);
        verify(productRepository).findAllById(List.of(1L, 2L));
    }

    @Test
    @DisplayName("It should return a PaymentResponse with the payment data")
    void shouldReturnPaymentResponseWithPaymentData() {
        User u = buildUser();
        u.setId(1L);

        List<Product> products = buidlProducts();
        Instant now = Instant.now();

        when(jwtService.getUserIdFromToken("dfiugdf8gydf8g9hndf")).thenReturn("1");
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(productRepository.findAllById(List.of(1L, 2L))).thenReturn(products);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            p.setId(100L);
            p.setCreatedAt(now);
            return p;
        });

        PaymentResponse response = paymentService.createPayment("dfiugdf8gydf8g9hndf", buildPayment());

        assertNotNull(response);
        assertEquals(100L, response.id());
        assertEquals(100000, response.amount());
        assertEquals("Alameda 123", response.address());
        assertEquals(now, response.createdAt());
        verify(jwtService).getUserIdFromToken("dfiugdf8gydf8g9hndf");
        verify(userRepository).findById(1L);
        verify(productRepository).findAllById(List.of(1L, 2L));
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("It should return a PaymentResponse with the list of purchased products")
    void shouldReturnPaymentResponseWithListOfPurchasedProducts() {
        User u = buildUser();
        u.setId(1L);
        List<Product> products = buidlProducts();

        when(jwtService.getUserIdFromToken("dfiugdf8gydf8g9hndf")).thenReturn("1");
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(productRepository.findAllById(List.of(1L, 2L))).thenReturn(products);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = paymentService.createPayment("dfiugdf8gydf8g9hndf", buildPayment());

        assertNotNull(response);
        assertEquals(2, response.items().size());
        assertEquals(1L, response.items().get(0).product().id());
        assertEquals(2L, response.items().get(1).product().id());
        verify(jwtService).getUserIdFromToken("dfiugdf8gydf8g9hndf");
        verify(userRepository).findById(1L);
        verify(productRepository).findAllById(List.of(1L, 2L));
        verify(paymentRepository).save(any(Payment.class));
    }

    private List<Product> buidlProducts() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Cámara Mirrorless Canon EOS R50 + Lente 18-45mm IS STM");
        product.setDescription(
                "El sensor APS-C de gran tamaño proporciona una calidad de imagen increíble y control sobre la profundidad de campo. Perfecciona un aspecto profesional. Selecciona el modo Escena nocturna sin trípode para tomar varias imágenes y combinarlas automáticamente en una imagen más clara.");
        product.setUrlImage(
                "https://images.unsplash.com/photo-1502920917128-1aa500764cbd?fm=jpg&q=60&w=3000&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D");
        product.setPrice(749990L);
        product.setQuantity(10L);
        product.setBrand("Canon");
        product.setModel("EOS R50 18-45 IS STM");
        product.setOrigin("Vietnam");
        product.setUsefulLife("5 años");

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Cámara Mirrorless Canon EOS R50 + Lente 18-45mm IS STM");
        product2.setDescription(
                "El sensor APS-C de gran tamaño proporciona una calidad de imagen increíble y control sobre la profundidad de campo. Perfecciona un aspecto profesional. Selecciona el modo Escena nocturna sin trípode para tomar varias imágenes y combinarlas automáticamente en una imagen más clara.");
        product2.setUrlImage(
                "https://images.unsplash.com/photo-1502920917128-1aa500764cbd?fm=jpg&q=60&w=3000&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D");
        product2.setPrice(749990L);
        product2.setQuantity(10L);
        product2.setBrand("Canon");
        product2.setModel("EOS R50 18-45 IS STM");
        product2.setOrigin("Vietnam");
        product2.setUsefulLife("5 años");

        List<Product> products = new ArrayList<>();

        products.add(product);
        products.add(product2);

        return products;

    }

    private PaymentRequest buildPayment() {
        ItemCart item = new ItemCart(1L, 1, 100_000L);
        ItemCart item2 = new ItemCart(2L, 1, 200_000L);

        List<ItemCart> products = new ArrayList<>();
        products.add(item);
        products.add(item2);

        return new PaymentRequest(100000, "Alameda 123", products);
    }

    private User buildUser() {
        User user = new User();
        user.setName("Alexandra");
        user.setLastname("Trikru");
        user.setEmail("lexa@mail.com");
        user.setPassword("12345678");

        return user;
    }

}
