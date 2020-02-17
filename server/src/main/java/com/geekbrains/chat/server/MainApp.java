package com.geekbrains.chat.server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainApp {
    // Домашнее задание:
    // 1. Разробраться с финальным вариантом кода. Задать вопросы что непонятно.
    // 2. Добавить возможность смены ника через команду '/change_nick newNickname'

    public static void main(String[] args) {
        new Server(8189);
    }
}