package com.geekbrains.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private AuthManager authManager;
    private List<ClientHandler> clients;
    private final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AuthManager getAuthManager() {
        return authManager;
    }

    public Server(int port) {
        clients = new ArrayList<>();
        authManager = new DbAuthManager();
        authManager.start();                           //подключаемся к базе
        ExecutorService service = Executors.newFixedThreadPool(4);
        service.execute(()->{
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("Сервер запущен. Ожидаем подключения клиентов...");
                while (true) {
                    System.out.println("Имя потока принимающего клиента " + Thread.currentThread().getName());
                    Socket socket = serverSocket.accept();
                    System.out.println("Клиент подключился "+ socket.getPort()+" "+ socket.getInetAddress());
                    new ClientHandler(this, socket);// сервер кто подключил, и сокет который образовался при подключении
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                authManager.stop();
            }
        });
        service.shutdown();
        //много предположений, но нет уверенности в их верности насчет нужен ли пул потоков
    }

    public void broadcastMsg(String msg, boolean withDateTime) {
        if (withDateTime) {
            msg = String.format("[%s] %s", LocalDateTime.now().format(DTF), msg);
        }
        for (ClientHandler o : clients) {
            o.sendMsg(msg);
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
        clients.add(clientHandler);
        broadcastClientsList();
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastMsg(clientHandler.getNickname() + " вышел из чата", false);
        broadcastClientsList();
    }
}
