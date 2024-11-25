package com.example.game.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    private Network network;
    @FXML
    private Button btn1, btn2, btn3;
    @FXML
    private Button btn4, btn5, btn6;
    @FXML
    private Button btn7, btn8, btn9;
    @FXML
    private Label statusLabel;

    private Button[][] buttons;
    private boolean myTurn = false;
    private char mySymbol;

    @FXML
    private void handleButtonClick(ActionEvent event) {
        if (!myTurn) {
            showError("Подождите свой ход!");
            return;
        }

        Button sourceButton = (Button) event.getSource();
        if (!sourceButton.getText().equals(" ")) {
            showError("Эта клетка уже занята!");
            return;
        }
        String position = (String) sourceButton.getUserData();
        sourceButton.setText(String.valueOf(mySymbol));
        myTurn = false;
        updateStatus();
        
        network.sendMessage(position);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        buttons = new Button[][]{{btn1, btn2, btn3}, {btn4, btn5, btn6}, {btn7, btn8, btn9}};
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j].setUserData(i + "," + j);
                buttons[i][j].setText(" ");
            }
        }
        statusLabel.setText("Подключение к серверу...");
        network = new Network(msg -> Platform.runLater(() -> handleServerMessage(msg)));
    }

    private void handleServerMessage(String message) {
        String[] messages = message.split("(?=Welcome|Start|Board:|Turn|Win|Draw)");
        for (String msg : messages) {
            msg = msg.trim();
            if (msg.isEmpty()) continue;

            if (msg.startsWith("Welcome")) {
                mySymbol = msg.charAt(msg.length() - 1);
                myTurn = mySymbol == 'X';
                updateStatus();
            }
            else if (msg.equals("Start")) {
                System.out.println("Игра начинается");
            }
            else if (msg.equals("Turn")) {
                myTurn = true;
                updateStatus();
            }
            else if (msg.startsWith("Board:")) {
                String finalMsg = msg;
                Platform.runLater(() -> {
                    updateBoard(finalMsg.substring(finalMsg.indexOf('\n') + 1));
                });
            }
            else if (msg.startsWith("Win")) {
                char winner = msg.charAt(4);
                String winMessage = winner == mySymbol ? "Вы победили!" : "Вы проиграли!";
                showGameOver(winMessage);
            }
            else if (msg.equals("Draw")) {
                showGameOver("Ничья!");
            }
            else if (msg.equals("Error")) {
                showError("Ошибка!");
                myTurn = true;
                updateStatus();
            }
            else if (msg.equals("Wait")) {
                myTurn = false;
                updateStatus();
            }
            else if (msg.equals("Full")) {
                showError("Игра занята");
                Platform.exit();
            }
        }
    }

    private void updateStatus() {
        Platform.runLater(() -> {
            String status = "Вы играете: " + mySymbol;
            if (myTurn) {
                status += " (Ваш ход)";
            } else {
                status += " (Ход противника)";
            }
            statusLabel.setText(status);
            System.out.println("Статус обновлен: " + status);
        });
    }

    private void updateBoard(String boardState) {
        String[] rows = boardState.split("\n");
        for (int i = 0; i < 3; i++) {
            String[] cells = rows[i].split(" ");
            for (int j = 0; j < 3; j++) {
                String value = cells[j].equals("-") ? " " : cells[j];
                buttons[i][j].setText(value);
            }
        }
    }

    private void showGameOver(String message) {
        myTurn = false;
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Игра окончена");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        resetGame();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void resetGame() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j].setText(" ");
            }
        }
        myTurn = false;
        statusLabel.setText("Ожидание новой игры...");
    }
}