package com.geekbrains.chat.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    TextArea textArea;

    @FXML
    TextField msgField, loginField;

    @FXML
    PasswordField passField;

    @FXML
    HBox loginBox;

    @FXML
    ListView<String> clientsList;

    private Network network;
    private boolean authenticated;
    private String nickname;
    private List<File> history = new ArrayList<>();

    private void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        loginBox.setVisible(!authenticated);
        loginBox.setManaged(!authenticated);
        msgField.setVisible(authenticated);
        msgField.setManaged(authenticated);
        clientsList.setVisible(authenticated);
        clientsList.setManaged(authenticated);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setAuthenticated(false);
        clientsList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                msgField.setText("/w " + clientsList.getSelectionModel().getSelectedItem() + " ");
                msgField.requestFocus();
                msgField.selectEnd();
            }
        });
    }

    private void tryToConnect() {
        try {
            if (network != null && network.isConnected()) {
                return;
            }
            setAuthenticated(false);
            network = new Network(8189);
            Thread t = new Thread(() -> {
                try {
                    while (true) {
                        String msg = network.readMsg();//ответ о смене ника пользователю
                        if (msg.startsWith("/authok ")) { // /authok nick1
                            nickname = msg.split(" ")[1];
                            textArea.appendText("Вы зашли в чат под ником: " + nickname + "\n");
                            textArea.appendText(getHistory());//добавим 100 последних строка истории в лог
                            setAuthenticated(true);
                            addListHistory(nickname);
                            break;
                        }
                        textArea.appendText(msg + "\n");
                    }
                    while (true) {
                        String msg = network.readMsg();
                        if (msg.startsWith("/")) {
                            if (msg.equals("/end_confirm")) {
                                textArea.appendText("Завершено общение с сервером\n");
                                break;
                            }
                            if (msg.startsWith("/set_nick_to  ")) {
                                nickname = msg.split(" ")[1];
                                textArea.appendText("Ваш новый ник: " + nickname + "\n");
                                continue;
                            }
                            if (msg.startsWith("/clients_list ")) { // '/clients_list user1 user2 user3'
                                Platform.runLater(() -> {
                                    clientsList.getItems().clear();
                                    String[] tokens = msg.split(" ");
                                    for (int i = 1; i < tokens.length; i++) {
                                        if (!nickname.equals(tokens[i])) {
                                            clientsList.getItems().add(tokens[i]);
                                        }
                                    }
                                });
                            }
                        } else {
                            textArea.appendText(msg + "\n");      //сообщение что пишет юзер в текстполе
                            wrtMsgToLogFile(msg);
                        }
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.WARNING, "Соединение с серверов разорвано", ButtonType.OK);
                        alert.showAndWait();
                    });
                } finally {
                    network.close();
                    setAuthenticated(false);
                    nickname = null;
                }
            });
            t.setDaemon(true);
            t.start();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Невозможно подключиться к серверу", ButtonType.OK);
            alert.showAndWait();
        }
    }
    //достанем последние 100 строка из файла
    private String getHistory() {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader out = new BufferedReader(new FileReader("HistoryLog/history_" + nickname + ".txt"))) {
            List<String> list = Files.readAllLines(Paths.get("HistoryLog/history_" + nickname + ".txt"));
            int start = 0;
            if (list.size() > 100) start = list.size() - 100;
            for (int i = start; i < list.size(); i++) {
                 sb.append(list.get(i)).append("\n");
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return sb.toString();

    }

    //в текстовое поле текст и очищается поле ввода(по Enter)
    public void sendMsg(ActionEvent actionEvent) {
        try {
            network.sendMsg(msgField.getText());//запиши в исход все что в текстполе
            msgField.clear();
            msgField.requestFocus();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Не удалось отправить сообщение, проверьте сетевое подключение", ButtonType.OK);
            alert.showAndWait();
        }
    }

    //как пользователь авторизовался => создаем именной файл =>добавляем в список с файлами
    private void addListHistory(String nickname) {
        File file = new File("history_" + nickname + ".txt");
        history.add(file);
    }

    //записываем в файл => в такой то директории
    private void wrtMsgToLogFile(String msg) {
        try (BufferedWriter out = new BufferedWriter(new FileWriter("HistoryLog/history_" + nickname + ".txt", true))) {
            out.write(msg + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToAuth(ActionEvent actionEvent) {
        try {
            tryToConnect();
            network.sendMsg("/auth " + loginField.getText() + " " + passField.getText());
            loginField.clear();
            passField.clear();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Не удалось отправить сообщение, проверьте сетевое подключение", ButtonType.OK);
            alert.showAndWait();
        }
    }
}
