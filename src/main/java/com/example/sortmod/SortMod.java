package com.example.sortmod;

import com.example.sortmod.network.SortPacketPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SortMod implements ModInitializer {
    public static final String MOD_ID = "sortmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Chest Sorter Mod for Minecraft 1.21.4");

        PayloadTypeRegistry.playC2S().register(SortPacketPayload.ID, SortPacketPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SortPacketPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                var player = context.player();
                if (player.currentScreenHandler instanceof GenericContainerScreenHandler containerHandler) {
                    sortContainer(containerHandler);
                }
            });
        });
    }

    private static void sortContainer(GenericContainerScreenHandler handler) {
        int containerRows = handler.getRows();
        int slotCount = containerRows * 9;

        List<ItemStack> items = new ArrayList<>();

        for (int i = 0; i < slotCount; i++) {
            Slot slot = handler.getSlot(i);
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty()) {
                items.add(stack.copy());
                slot.setStack(ItemStack.EMPTY);
            }
        }

        // 1. Слияние стаков
        List<ItemStack> consolidated = new ArrayList<>();
        for (ItemStack item : items) {
            for (ItemStack existing : consolidated) {
                if (ItemStack.areItemsAndComponentsEqual(item, existing) && existing.getCount() < existing.getMaxCount()) {
                    int space = existing.getMaxCount() - existing.getCount();
                    int transfer = Math.min(space, item.getCount());
                    existing.increment(transfer);
                    item.decrement(transfer);
                    if (item.isEmpty()) {
                        break;
                    }
                }
            }
            if (!item.isEmpty()) {
                consolidated.add(item);
            }
        }

        // 2. Сортировка: по ID, имени и количеству (по убыванию)
        consolidated.sort(
                Comparator.comparing((ItemStack s) -> s.getItem().toString())
                        .thenComparing(s -> s.getName().getString())
                        .thenComparingInt(ItemStack::getCount).reversed()
        );

        // 3. Распределение обратно по слотам
        for (int i = 0; i < consolidated.size(); i++) {
            handler.getSlot(i).setStack(consolidated.get(i));
        }

        handler.sendContentUpdates();
    }
}
