package org.example;

import org.example.controllers.FunkosController;
import org.example.database.DataBaseManager;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {
        var funkosController = FunkosController.getInstance();
    }
}