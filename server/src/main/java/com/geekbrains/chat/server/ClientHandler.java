package com.geekbrains.chat.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nickname;
    private static final Logger LOGGER = LogManager.getLogger(ClientHandler.class);

    public String getNickname() {
        return nickname;
    }

    public ClientHandler(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());//этот сокет образовавшийся при подкл просит ВВ
        this.out = new DataOutputStream(socket.getOutputStream());
        new Thread(() -> {
            try {
                while (true) { // цикл аутентификации
                    String msg = in.readUTF();
                    System.out.print("Сообщение от клиента: " + msg + "\n");
                    if (msg.startsWith("/auth ")) { // /auth login1 pass1
                        String[] tokens = msg.split(" ", 3);
                        LOGGER.info("Клиент прислал команду: "+ "[" + tokens[0] + "]");
                        String nickFromAuthManager = server.getAuthManager().getNicknameByLoginAndPassword(tokens[1], tokens[2]);
                        if (nickFromAuthManager != null) {
                            if (server.isNickBusy(nickFromAuthManager)) {
                                sendMsg("Данный пользователь уже в чате");
                                LOGGER.info("Попытка войти под этим логином -" + "["+ server.getAuthManager().getNicknameByLoginAndPassword(tokens[1], tokens[2]) +
                                        "]" + " этот пользователь в сети");
                                continue;
                            }
                            nickname = nickFromAuthManager;
                            sendMsg("/authok " + nickname);
                            LOGGER.info(nickname + " Авторизовался");
                            server.subscribe(this);
                            break;
                        } else {
                            sendMsg("Указан неверный логин/пароль");
                            LOGGER.info("Указан неверный логин/пароль");
                        }
                    }
                }
                while (true) { // цикл общения с сервером (обмен текстовыми сообщениями и командами)
                    String msg = in.readUTF();
                    System.out.print("Сообщение от клиента: " + msg + "\n");
                    if (msg.startsWith("/")) {
                        if (msg.startsWith("/w ")) {
                            String[] tokens = msg.split(" ", 3); // /w user2 hello, user2
                            server.sendPrivateMsg(this, tokens[1], tokens[2]);
                            continue;
                        }
                        if (msg.startsWith("/changenick ")) {
                            String[] tokens = msg.split(" ", 2); // /changenick newNickname
                            LOGGER.info("Клиент прислал команду: "+ "[" + tokens[0] + "]");
                            String newNickname = tokens[1];
                            if (server.getAuthManager().changeNickname(nickname,newNickname)){
                                nickname = newNickname;
                                sendMsg("/set_nick_to " + newNickname);
                                LOGGER.info( "[/set_nick_to]" + " никнейм изменен на " + newNickname);

                            }else{
                                sendMsg("Сервер: пользователь с таким ником уже существует");
                                LOGGER.info("Пользователь с ником " + nickname + " существует");
                            }
                            continue;
                        }
                        if (msg.equals("/end")) {
                            sendMsg("/end_confirm");
                            LOGGER.info("Клиент прислал команду: "+ "[/end]");
                            break;
                        }
                    } else {
                        server.broadcastMsg(nickname + ": " + msg, true);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                close();
            }
        }).start();
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        server.unsubscribe(this);
        nickname = null;
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
