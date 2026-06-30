package com.mccr.backend.ecommerce.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.mccr.backend.ecommerce.dto.PaymentRequest;
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
    public Payment createPayment(String token, PaymentRequest newPayment) {
        Long userId = Long.parseLong(jwtService.getUserIdFromToken(token));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        List<Long> productsIds = newPayment.getItems().stream().map(PaymentRequest.ItemCartDTO::getProductId)
                .collect(Collectors.toList());

        List<Product> products = productRepository.findAllById(productsIds);

        // convierte la lista desordenada que devolvio la base de datos en un mapa clave
        // valor con el id del producto como clave
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        Payment payment = new Payment();
        payment.setAmount(newPayment.getAmount());
        payment.setAddress(newPayment.getAddress());
        payment.setUser(user);

        for (PaymentRequest.ItemCartDTO item : newPayment.getItems()) {
            Product product = productMap.get(item.getProductId());

            if (product == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "El producto con ID " + item.getProductId() + " no existe en el catálogo");

            }

            if (product.getQuantity() < item.getQuantity()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Stock insuficiente para el producto: " + product.getName());
            }

            product.setQuantity(product.getQuantity() - item.getQuantity());

            OrderItem paymentItem = new OrderItem();
            paymentItem.setProduct(product);
            paymentItem.setQuantity(item.getQuantity());
            paymentItem.setPriceAtPurchase(item.getPriceAtPurchase());
            paymentItem.setPayment(payment);

            payment.getItems().add(paymentItem);
        }

        return paymentRepository.save(payment);

    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

}
