package com.example.game.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;

@Sharable
public class GameServerHandler extends SimpleChannelInboundHandler<String> {
    private final PlayerManager playerManager;
    private final TicTacToeGame game;

    public GameServerHandler() {
        this.playerManager = new PlayerManager();
        this.game = new TicTacToeGame();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("Попытка подключения нового игрока...");
        if (playerManager.getPlayerIndex(ctx.channel()) != -1) {
            System.out.println("Этот клиент уже подключен!");
            ctx.writeAndFlush("Connected");
            ctx.close();
            return;
        }
        if (!playerManager.addPlayer(ctx.channel())) {
            System.out.println("Игра уже заполнена");
            ctx.writeAndFlush("Full");
            ctx.close();
            return;
        }
        int playerIndex = playerManager.getPlayerIndex(ctx.channel());
        char symbol = playerIndex == 0 ? 'X' : 'O';
        System.out.println("Игрок " + (playerIndex + 1) + " " + symbol);

        ctx.writeAndFlush("Welcome " + symbol);

        if (playerManager.isGameReady()) {
            System.out.println("Игра начинается! Количество игроков: " + playerManager.getPlayerCount());
            game.setGameState(GameState.IN_PROGRESS);
            playerManager.broadcast("Start");
            notifyCurrentPlayer();
        } else {
            System.out.println("Ожидание второго игрока. Количество игроков: " + playerManager.getPlayerCount());
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        if (game.getGameState() != GameState.IN_PROGRESS) {
            ctx.writeAndFlush("Error");
            return;
        }

        int playerIndex = playerManager.getPlayerIndex(ctx.channel());
        char playerSymbol = playerIndex == 0 ? 'X' : 'O';

        System.out.println("Ход игрока " + playerSymbol);

        if (game.getCurrentPlayer() != playerSymbol) {
            System.out.println("Сейчас не ход игрока " + playerSymbol);
            ctx.writeAndFlush("Wait");
            return;
        }

        try {
            String[] parts = msg.split(",");
            int row = Integer.parseInt(parts[0]);
            int col = Integer.parseInt(parts[1]);

            System.out.println("Попытка сделать ход: (" + row + "," + col + ")");

            if (!game.makeMove(row, col)) {
                System.out.println("Недопустимый ход!");
                ctx.writeAndFlush("Error");
                return;
            }

            System.out.println("Ход выполнен");
            GameState state = game.getGameState();

            playerManager.broadcast("Board:\n" + game.toString());

            if (state == GameState.WINNER_X || state == GameState.WINNER_O) {
                if (state == GameState.WINNER_X) {
                    playerManager.broadcast("Win X");
                } else {
                    playerManager.broadcast("Win O");
                }
                endGame();
            } else if (state == GameState.DRAW) {
                playerManager.broadcast("Draw");
                endGame();
            } else {
                int nextPlayerIndex = game.getCurrentPlayer() == 'X' ? 0 : 1;
                playerManager.notifyPlayer(nextPlayerIndex, "Turn");
            }
        } catch (Exception e) {
            ctx.writeAndFlush("Error");
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        int playerIndex = playerManager.getPlayerIndex(ctx.channel());
        System.out.println("Игрок " + (playerIndex + 1) + " отключился");

        playerManager.removePlayer(ctx.channel());
        if (game.getGameState() == GameState.IN_PROGRESS) {
            playerManager.broadcast("Player disconnected.");
            endGame();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("Ошибка: " + cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }

    private void notifyCurrentPlayer() {
        char currentPlayer = game.getCurrentPlayer();
        int playerIndex = currentPlayer == 'X' ? 0 : 1;
        playerManager.notifyPlayer(playerIndex, "Turn");
    }

    private void endGame() {
        game.initializeGame();
        game.setGameState(GameState.WAITING_FOR_PLAYERS);
        playerManager.disconnectAll();
    }
}