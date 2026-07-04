package com.fantasta.auth_service.dto;

public class LoginResponse {

    private String accessToken;
    private Long userId;
    private String email;
    private String name;
    private String surname;

    public LoginResponse() {
    }

    public LoginResponse(String accessToken, Long userId, String email, String name, String surname) {
        this.accessToken = accessToken;
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.surname = surname;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }
}