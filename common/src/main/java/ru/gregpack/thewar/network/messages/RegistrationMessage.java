package ru.gregpack.thewar.network.messages;

import lombok.*;

import javax.annotation.Nullable;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RegistrationMessage {
    @Getter
    @Setter
    private Role role;

    @Getter
    @Setter
    @Nullable
    private String name;
}
