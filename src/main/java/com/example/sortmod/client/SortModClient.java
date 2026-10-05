package com.example.sortmod.client;

import com.example.sortmod.network.SortPacketPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class SortModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        PayloadTypeRegistry.playC2S().register(SortPacketPayload.ID, SortPacketPayload.CODEC);
    }
}
