package com.example.sortmod.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SortPacketPayload() implements CustomPayload {
    public static final CustomPayload.Id<SortPacketPayload> ID = 
            new CustomPayload.Id<>(Identifier.of("sortmod", "sort_chest"));
    
    public static final PacketCodec<RegistryByteBuf, SortPacketPayload> CODEC = 
            PacketCodec.unit(new SortPacketPayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
