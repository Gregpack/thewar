package ru.gregpack.thewar;

import ru.gregpack.thewar.network.GameClient;

import java.io.IOException;

public class ClientMain {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Not enough arguments! Usage: java -jar view.jar <server_ip> <port>");
            return;
        }
        String ip = args[0];
        int port;
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Wrong port format! Port is a number > 1024 and < 65535.");
            return;
        }
        try {
            GameClient gameClient = new GameClient(ip, port);
            gameClient.connect();
            gameClient.run();
        } catch (IOException e) {
            System.err.println("Connection refused from server. Maybe you forgot to launch the server?");
        }
    }
}
