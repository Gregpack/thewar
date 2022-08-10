package ru.gregpack.thewar;

import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.Getter;
import ru.gregpack.thewar.engine.bots.BotType;
import ru.gregpack.thewar.engine.logic.LogicModule;
import ru.gregpack.thewar.engine.network.GameMode;
import ru.gregpack.thewar.engine.network.GameServer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class GameServerMain {
    public static void main(String[] args) {
        try {
            Injector injector = Guice.createInjector(new LogicModule());
            GameServer gameServer = injector.getInstance(GameServer.class);
            Scanner scanner = new Scanner(System.in);
            int[] gm = new int[]{-1};
            while (gm[0] < 1 || gm[0] > 3) {
                System.out.println("Select gamemode. 1 - singleplayer, 2 - multiplayer, 3 - AIvsAI");
                gm[0] = scanner.nextInt();
            }
            GameMode gameMode = Arrays.stream(GameMode.values()).filter(v -> gm[0] == v.getValue()).findFirst().get();
            int[] bots = new int[]{-1, -1};
            switch (gameMode) {
                case Multiplayer:
                    break;
                case AIvsAI:
                    while (bots[1] < 1 || bots[1] > 3) {
                        System.out.println("Select first bot level. 1 - easy, 2 - medium, 3 - hard");
                        bots[1] = scanner.nextInt();
                    }
                case Singleplayer:
                    while (bots[0] < 1 || bots[0] > 3) {
                        System.out.println("Select opponent bot level. 1 - easy, 2 - medium, 3 - hard");
                        bots[0] = scanner.nextInt();
                    }
                    break;
            }
            List<BotType> botTypes = new ArrayList<>();
            for (int bot : bots) {
                if (bot != -1) {
                    BotType botType = Arrays.stream(BotType.values()).filter(b -> b.getValue() == bot).findFirst().get();
                    botTypes.add(botType);
                }
            }
            gameServer.launch(gameMode, botTypes);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
