package com.geekbrains.chat.server;

import java.sql.*;

public class DbAuthManager implements AuthManager {

    private Connection connection;
    private Statement stmt;
    private PreparedStatement psGetNicknameByLoginAndPassword;
    private PreparedStatement psChangeNickname;
    private PreparedStatement psGetUserByNickname;

    @Override
    public void start() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:users.db");//открываем соединение с базой
            stmt = connection.createStatement();
            checkUsersTable();//проверяем есть ли таблица если нет создаем
            psGetNicknameByLoginAndPassword = connection.prepareStatement//подготовили запрос на поиск никнейма с парой (логин/пароль)
                                     ("SELECT nickname FROM users WHERE login = ? AND password = ?;");
            psChangeNickname = connection.prepareStatement("UPDATE users SET nickname = ? WHERE nickname = ?;");
            psGetUserByNickname = connection.prepareStatement("SELECT * FROM users WHERE nickname = ?;");

        } catch (ClassNotFoundException |SQLException e) {
            throw new AuthServiceException("Unable to connect to users database");
        }

    }

    public void checkUsersTable() throws SQLException {
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                                            "login TEXT, password TEXT, nickname TEXT);");
    }

    @Override
    public void stop() {
        if (psChangeNickname != null){
            try{
                psChangeNickname.close();
            }catch (SQLException e){
                e.printStackTrace();
            }
        }
        if (psGetNicknameByLoginAndPassword != null){
            try{
                psGetNicknameByLoginAndPassword.close();
            }catch (SQLException e){
                e.printStackTrace();
            }
        }
        if (psGetUserByNickname != null){
            try{
                psGetUserByNickname.close();
            }catch (SQLException e){
                e.printStackTrace();
            }
        }
        if (stmt != null){
            try{
                stmt.close();
            }catch (SQLException e){
                e.printStackTrace();
            }
        }
        if (connection != null){
            try{
                connection.close();
            }catch (SQLException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        try {
            psGetNicknameByLoginAndPassword.setString(1, login);
            psGetNicknameByLoginAndPassword.setString(2, password);
            try (ResultSet rs = psGetNicknameByLoginAndPassword.executeQuery()) {
                if (!rs.next()){   //такого пользователя нет
                    return null;
                }
                return rs.getString(1);//если есть возвращаем
            }
        }catch (SQLException e){
            throw new AuthServiceException("Unable to get nickname by login/password");
        }
    }

    @Override
    public boolean changeNickname(String oldNickname, String newNickname) {
        try {
            if (isNicknameExists(newNickname)){
                return false;                   //не удалось сменить
            }
            psChangeNickname.setString(1,newNickname);// на первое место ставим новый ник
            psChangeNickname.setString(2,oldNickname);//тот который хотим заменить
            psChangeNickname.executeUpdate();
            return true;                       //удалось сменить ник

        }catch (SQLException e){
            throw new AuthServiceException("Unable to change nickname");
        }
    }
    public boolean isNicknameExists(String nickname) {
        try {
            psGetUserByNickname.setString(1,nickname);//отдаем в запрос имя пользователя
            try(ResultSet rs = psGetUserByNickname.executeQuery()){//пробуем послать запрос в базу
                if (rs.next()){
                    return true;
                }
                return false;
            }
        }catch (SQLException e){
            throw new AuthServiceException("Unable to change nickname");
        }
    }





}
