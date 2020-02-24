package com.geekbrains.chat.server;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Server {
    private AuthManager authManager;
    private List<ClientHandler> clients;
    private final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Logger LOGGER = LogManager.getLogger("Server");

    public AuthManager getAuthManager() {
        return authManager;
    }

    public Server(int port) {
        clients = new ArrayList<>();
        authManager = new DbAuthManager();
        authManager.start();                           //подключаемся к базе
        LOGGER.info("Подключились к базе данных");
        try (ServerSocket serverSocket = new ServerSocket(port)) {
           // System.out.println("Сервер запущен. Ожидаем подключения клиентов...");
            LOGGER.info("Сервер запущен. Ожидаем подключения клиентов...");
            while (true) {
                Socket socket = serverSocket.accept();
                //System.out.println("Клиент подключился");
                LOGGER.info("Клиент подключился");
                new ClientHandler(this, socket);// сервер кто подключил, и сокет который образовался при подключении
            }
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.throwing(Level.ALL,e);//логирование ошибок при подключении
        }finally {
            authManager.stop();
            LOGGER.info("Отключились от базы данных");
        }
    }

    public void broadcastMsg(String msg, boolean withDateTime) {
        if (withDateTime) {
            msg = String.format("[%s] %s", LocalDateTime.now().format(DTF), msg);
        }
        for (ClientHandler o : clients) {
            o.sendMsg(msg);
            LOGGER.info(msg);
        }
    }

    public void broadcastClientsList() {
        StringBuilder stringBuilder = new StringBuilder("/clients_list ");
        for (ClientHandler o : clients) {
            stringBuilder.append(o.getNickname()).append(" ");
        }
        stringBuilder.setLength(stringBuilder.length() - 1);
        String out = stringBuilder.toString();
        broadcastMsg(out, false);
    }

    public void sendPrivateMsg(ClientHandler sender, String receiverNickname, String msg) {
        if (sender.getNickname().equals(receiverNickname)) {
            sender.sendMsg("Нельзя посылать личное сообщение самому себе");
            LOGGER.info("Нельзя посылать личное сообщение самому себе");
            return;
        }
        for (ClientHandler o : clients) {
            if (o.getNickname().equals(receiverNickname)) {
                o.sendMsg("from " + sender.getNickname() + ": " + msg);
                sender.sendMsg("to " + receiverNickname + ": " + msg);
                return;
            }
        }
        sender.sendMsg(receiverNickname + " не в сети");
    }

    public boolean isNickBusy(String nickname) {
        for (ClientHandler o : clients) {
            if (o.getNickname().equals(nickname)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        broadcastMsg(clientHandler.getNickname() + " зашел в чат", false);
        LOGGER.info(clientHandler.getNickname() + " зашел в чат");
        clients.add(clientHandler);
        broadcastClientsList();
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastMsg(clientHandler.getNickname() + " вышел из чата", false);
        LOGGER.info(clientHandler.getNickname() + " вышел из чата");
        broadcastClientsList();
    }
}
