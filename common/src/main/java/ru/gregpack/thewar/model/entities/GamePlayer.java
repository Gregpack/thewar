package ru.gregpack.thewar.model.entities;

import lombok.Data;
import lombok.ToString;
import ru.gregpack.thewar.model.PlayerSubscriber;
import ru.gregpack.thewar.model.entities.basic.Coordinate;
import ru.gregpack.thewar.utils.PropertyUtil;

@Data
public class GamePlayer {

    private int playerId = nextId();
    private String name;
    private Coordinate base;
    private int money = PropertyUtil.getIntProperty("logic.player.startmoney", 200);
    @ToString.Exclude
    private PlayerSubscriber playerSubscriber;

    private static int idCounter = 0;
    private static int nextId() {
        int id = idCounter;
        idCounter++;
        return id;
    }

    public void addMoney(int money) {
        this.money += money;
        if (playerSubscriber != null) {
            playerSubscriber.onMoneyAddition(this);
        }
    }

    public void removeMoney(int money) {
        this.money -= money;
    }

}
