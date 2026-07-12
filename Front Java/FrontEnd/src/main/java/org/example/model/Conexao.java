package org.example.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {

    private static final String URL =
            "jdbc:mysql://192.168.15.18:3307/netarq?serverTimezone=America/Sao_Paulo";

    private static final String USER = "root";
    private static final String PASSWORD = "root";

    public static Connection conectar() throws SQLException {
        System.out.println("URL = " + URL);
        System.out.println("USER = " + USER);

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}