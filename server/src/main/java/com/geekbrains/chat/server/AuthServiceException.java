package com.geekbrains.chat.server;
//некорректная работа аутентификации
public class AuthServiceException extends RuntimeException {
    public AuthServiceException(String message) {
        super(message);
    }
}
