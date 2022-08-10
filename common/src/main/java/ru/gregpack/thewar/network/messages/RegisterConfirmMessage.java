package ru.gregpack.thewar.network.messages;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RegisterConfirmMessage {

    @Getter
    @Setter
    private int playerId;

}
