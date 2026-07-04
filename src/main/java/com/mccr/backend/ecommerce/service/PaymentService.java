package com.mccr.backend.ecommerce.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.mccr.backend.ecommerce.dto.ItemCart;
import com.mccr.backend.ecommerce.dto.OrderItemResponse;
import com.mccr.backend.ecommerce.dto.PaymentRequest;
import com.mccr.backend.ecommerce.dto.PaymentResponse;
import com.mccr.backend.ecommerce.dto.PaymentSummary;
import com.mccr.backend.ecommerce.dto.ProductSummaryResponse;
import com.mccr.backend.ecommerce.model.Payment;
import com.mccr.backend.ecommerce.model.OrderItem;
import com.mccr.backend.ecommerce.model.Product;
import com.mccr.backend.ecommerce.model.User;
import com.mccr.backend.ecommerce.repository.PaymentRepository;
import com.mccr.backend.ecommerce.repository.ProductRepository;
import com.mccr.backend.ecommerce.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final JwtService jwtService;

    @SuppressWarnings("null")
    public PaymentResponse createPayment(String token, PaymentRequest newPayment) {
        Long userId = Long.parseLong(jwtService.getUserIdFromToken(token));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"));

        List<Long> productsIds = newPayment.items().stream().map(ItemCart::productId).toList();

        List<Product> products = productRepository.findAllById(productsIds);

        // convierte la lista desordenada que devolvio la base de datos en un mapa clave
        // valor con el id del producto como clave
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        Payment payment = new Payment();
        payment.setAmount(newPayment.amount());
        payment.setAddress(newPayment.address());
        payment.setUser(user);

        for (ItemCart item : newPayment.items()) {
            Product product = productMap.get(item.productId());

            if (product == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "El producto con ID " + item.productId() + " no existe en el catálogo");

            }

            if (product.getQuantity() < item.quantity()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Stock insuficiente para el producto: " + product.getName());
            }

            product.setQuantity(product.getQuantity() - item.quantity());

            OrderItem paymentItem = new OrderItem();
            paymentItem.setProduct(product);
            paymentItem.setQuantity(item.quantity());
            paymentItem.setPriceAtPurchase(item.priceAtPurchase());
            paymentItem.setPayment(payment);

            payment.getItems().add(paymentItem);
        }

        Payment paymentSaved = paymentRepository.save(payment);

        List<OrderItemResponse> itemsResponse = paymentSaved.getItems().stream().map(item -> {
            Product prod = item.getProduct();

            ProductSummaryResponse productSummary = new ProductSummaryResponse(
                    prod.getId(),
                    prod.getName(),
                    prod.getUrlImage(),
                    prod.getBrand(),
                    prod.getModel());

            return new OrderItemResponse(
                    item.getId(),
                    item.getQuantity(),
                    item.getPriceAtPurchase(),
                    productSummary);
        }).toList();

        return new PaymentResponse(
                paymentSaved.getId(),
                paymentSaved.getAmount(),
                paymentSaved.getAddress(),
                paymentSaved.getCreatedAt(),
                itemsResponse);

    }

    public List<PaymentSummary> getAllPayments(String token) {
        Long userId = Long.parseLong(jwtService.getUserIdFromToken(token));

        List<Payment> payments = paymentRepository.findAllByUserId(userId);

        return payments.stream().map(item -> {
            return new PaymentSummary(
                    item.getId(),
                    item.getAmount(),
                    item.getAddress(),
                    item.getCreatedAt());
        }).toList();

    }

    public PaymentResponse getPaymentDetailById(String token, Long paymentId) {
        Long userId = Long.parseLong(jwtService.getUserIdFromToken(token));

        Optional<Payment> optionalPayment = paymentRepository.findByIdAndUserId(paymentId, userId);

        if (optionalPayment.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El pago no se encontro");
        }

        List<OrderItemResponse> itemsResponse = optionalPayment.get().getItems().stream().map(item -> {
            Product prod = item.getProduct();

            ProductSummaryResponse productSummary = new ProductSummaryResponse(
                    prod.getId(),
                    prod.getName(),
                    prod.getUrlImage(),
                    prod.getBrand(),
                    prod.getModel());

            return new OrderItemResponse(
                    item.getId(),
                    item.getQuantity(),
                    item.getPriceAtPurchase(),
                    productSummary);
        }).toList();

        return new PaymentResponse(
                optionalPayment.get().getId(),
                optionalPayment.get().getAmount(),
                optionalPayment.get().getAddress(),
                optionalPayment.get().getCreatedAt(),
                itemsResponse);

    }

}
