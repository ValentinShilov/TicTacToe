package com.example.game.server;

import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.List;

public class PlayerManager {
    private volatile Channel player1;
    private volatile Channel player2;

    public PlayerManager() {
        player1 = null;
        player2 = null;
    }

    public synchronized boolean addPlayer(Channel playerChannel) {
        System.out.println("Попытка добавить игрока. Player1: " + (player1 != null) + ", Player2: " + (player2 != null));

        if (player1 == null) {
            player1 = playerChannel;
            System.out.println("Игрок добавлен как Player1");
            return true;
        }
        if (player2 == null) {
            player2 = playerChannel;
            System.out.println("Игрок добавлен как Player2");
            return true;
        }
        return false;
    }

    public synchronized void removePlayer(Channel playerChannel) {
        if (playerChannel.equals(player1)) {
            player1 = null;
        } else if (playerChannel.equals(player2)) {
            player2 = null;
        }
    }

    public void broadcast(String message) {
        sendIfActive(player1, message);
        sendIfActive(player2, message);
    }

    private void sendIfActive(Channel channel, String message) {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(message);
        }
    }

    public void notifyPlayer(int playerIndex, String message) {
        Channel channel = getPlayer(playerIndex);
        sendIfActive(channel, message);
    }

    public Channel getPlayer(int index) {
        return index == 0 ? player1 : player2;
    }

    public synchronized int getPlayerCount() {
        int count = 0;
        if (player1 != null && player1.isActive()) count++;
        if (player2 != null && player2.isActive()) count++;
        return count;
    }

    public synchronized boolean isGameReady() {
        return player1 != null && player1.isActive() &&
                player2 != null && player2.isActive();
    }

    public synchronized void disconnectAll() {
        if (player1 != null && player1.isActive()) {
            player1.close();
        }
        if (player2 != null && player2.isActive()) {
            player2.close();
        }
        player1 = null;
        player2 = null;
    }

    public synchronized int getPlayerIndex(Channel channel) {
        if (channel.equals(player1)) return 0;
        if (channel.equals(player2)) return 1;
        return -1;
    }
}
