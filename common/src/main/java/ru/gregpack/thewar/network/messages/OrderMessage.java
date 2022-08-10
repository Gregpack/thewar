package ru.gregpack.thewar.network.messages;

import lombok.*;
import ru.gregpack.thewar.network.messages.orders.Order;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OrderMessage {
    @Getter @Setter
    private List<Order> orders = new ArrayList<>();
}
