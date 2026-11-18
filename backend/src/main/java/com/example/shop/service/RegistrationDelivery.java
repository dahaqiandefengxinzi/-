package com.example.shop.service;

public interface RegistrationDelivery {
    void send(String channel, String identifier, String code);
    default String codeFor(String channel, String identifier, String randomCode) { return randomCode; }
    default void created(String channel, String identifier, Long userId) {}
}
