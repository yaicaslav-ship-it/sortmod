package com.example.sortmod.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class SortModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Запускаем проверку очереди кликов в конце каждого клиентского тика
        ClientTickEvents.END_CLIENT_TICK.register(ChestSorterExecutor::onClientTick);
    }
}
