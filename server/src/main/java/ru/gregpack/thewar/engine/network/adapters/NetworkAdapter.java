package ru.gregpack.thewar.engine.network.adapters;

import ru.gregpack.thewar.model.GameTickSubscriber;

import java.io.IOException;

public interface NetworkAdapter extends GameTickSubscriber {
    void initAdapter() throws IOException;
}
