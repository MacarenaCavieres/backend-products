package com.mccr.backend.ecommerce.dto;

public class LoginResponse {

    private String name;
    private String lastname;
    private String email;
    private String token;

    public LoginResponse(String name, String lastname, String email, String token) {
        this.name = name;
        this.lastname = lastname;
        this.email = email;
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
