package com.mccr.backend.ecommerce.dto;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public class PaymentRequest {
    @NotBlank(message = "El monto no puede estar vacío")
    private String amount;

    @NotBlank(message = "La dirección no puede estar vacía")
    private String address;

    @NotEmpty(message = "La lista de productos no puede estar vacía")
    private List<ItemCartDTO> items;

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<ItemCartDTO> getItems() {
        return items;
    }

    public void setItems(List<ItemCartDTO> items) {
        this.items = items;
    }

    public static class ItemCartDTO {
        private Long productId;
        private Integer quantity;
        private Long priceAtPurchase;

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public Long getPriceAtPurchase() {
            return priceAtPurchase;
        }

        public void setPriceAtPurchase(Long priceAtPurchase) {
            this.priceAtPurchase = priceAtPurchase;
        }
    }
}