package ru.gregpack.thewar;

import ru.gregpack.thewar.network.ViewerClient;
import ru.gregpack.thewar.network.messages.dto.GameStateDto;
import ru.gregpack.thewar.network.messages.dto.PlayerDto;
import ru.gregpack.thewar.network.messages.ViewerConfirmMessage;
import ru.gregpack.thewar.view.Visualiser;

import java.io.IOException;
import java.util.function.Consumer;

public class VisualiserMain {
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
        ViewerClient viewerClient;
        ViewerConfirmMessage options;
        try {
            viewerClient = new ViewerClient(ip, port);
            options = viewerClient.connect();
        } catch (IOException e) {
            System.err.println("Can't connect to server! Error: " + e.getMessage());
            return;
        }
        Visualiser.setClientDelegate(new ClientDelegate() {
            @Override
            public void startListening(Consumer<GameStateDto> onNewGameState, Runnable onServerFail, Consumer<PlayerDto> onGameEnd) {
                viewerClient.listen(onNewGameState, onServerFail, onGameEnd);
            }

            @Override
            public void shutdown() {
                viewerClient.shutdown();
            }
        });
        Visualiser.start(new String[] {
                options.getLength().toString(),
                options.getHeight().toString(),
                options.getGoldToWin(),
                options.getBaseLength().toString(),
                options.getBaseHeight().toString(),
                options.getTickRate().toString(),
                options.getBases().get(0).getX() + ";" + options.getBases().get(0).getY(),
                options.getBases().get(1).getX() + ";" + options.getBases().get(1).getY(),
        });
    }
}
