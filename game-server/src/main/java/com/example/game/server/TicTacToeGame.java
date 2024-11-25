package com.example.game.server;

public class TicTacToeGame {
    private char[][] board;
    private char currentPlayer;
    private GameState gameState;

    public TicTacToeGame() {
        initializeGame();
    }

    public void initializeGame() {
        board = new char[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = ' ';
            }
        }
        currentPlayer = 'X';
        gameState = GameState.WAITING_FOR_PLAYERS;
    }

    public boolean makeMove(int row, int col) {
        if (!isValidMove(row, col)) {
            return false;
        }
        board[row][col] = currentPlayer;

        if (checkWin()) {
            gameState = currentPlayer == 'X' ? GameState.WINNER_X : GameState.WINNER_O;
        } else if (isBoardFull()) {
            gameState = GameState.DRAW;
        } else {
            switchPlayer();
        }

        return true;
    }

    private boolean isValidMove(int row, int col) {
        return row >= 0 && row < 3 && col >= 0 && col < 3 && board[row][col] == ' ';
    }

    private void updateGameState() {
        if (checkWin()) {
            gameState = currentPlayer == 'X' ? GameState.WINNER_X : GameState.WINNER_O;
        } else if (isBoardFull()) {
            gameState = GameState.DRAW;
        }
    }

    public boolean checkWin() {
        for (int i = 0; i < 3; i++) {
            if (board[i][0] != ' ' && board[i][0] == board[i][1] && board[i][1] == board[i][2]) return true;
            if (board[0][i] != ' ' && board[0][i] == board[1][i] && board[1][i] == board[2][i]) return true;
        }
        if (board[0][0] != ' ' && board[0][0] == board[1][1] && board[1][1] == board[2][2]) return true;
        if (board[0][2] != ' ' && board[0][2] == board[1][1] && board[1][1] == board[2][0]) return true;
        return false;
    }

    public boolean isBoardFull() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == ' ') return false;
            }
        }
        return true;
    }

    private void switchPlayer() {
        currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
    }

    public char getCurrentPlayer() {
        return currentPlayer;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState state) {
        this.gameState = state;
    }

    public char[][] getBoard() {
        return board;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                sb.append(board[i][j] == ' ' ? '-' : board[i][j]).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
