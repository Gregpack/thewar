package ru.gregpack.thewar.network.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class ErrorMessage {

    @Getter
    private String errorMessage;

}
