package com.example.sortmod.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

import java.util.*;

public class ChestSorterExecutor {
    // Задержка между кликами в тиках (1 тик = 50 мс).
    // Значение 2-3 тика оптимально и не вызывает подозрений у античитов.
    private static final int DELAY_TICKS = 2;

    private static final Queue<SlotClickAction> ACTION_QUEUE = new ArrayDeque<>();
    private static int tickCooldown = 0;

    public record SlotClickAction(int syncId, int slotId, int button, SlotActionType actionType) {}

    public static void startSorting(GenericContainerScreenHandler handler) {
        ACTION_QUEUE.clear();
        tickCooldown = 0;

        int chestSlotCount = handler.getRows() * 9;
        int syncId = handler.syncId;

        // Копируем текущее состояние только слотов сундука
        List<ItemStack> currentSlots = new ArrayList<>();
        for (int i = 0; i < chestSlotCount; i++) {
            currentSlots.add(handler.getSlot(i).getStack().copy());
        }

        // Вычисляем целевой отсортированный расклад
        List<ItemStack> targetSlots = calculateTargetLayout(currentSlots, chestSlotCount);

        // Формируем пошаговую очередь кликов замены (Selection Sort / Swap)
        for (int targetIdx = 0; targetIdx < chestSlotCount; targetIdx++) {
            ItemStack desired = targetSlots.get(targetIdx);
            ItemStack current = currentSlots.get(targetIdx);

            if (areEqual(desired, current) && desired.getCount() == current.getCount()) {
                continue;
            }

            // Ищем слот, где сейчас лежит подходящий предмет
            int sourceIdx = -1;
            for (int j = targetIdx + 1; j < chestSlotCount; j++) {
                if (areEqual(currentSlots.get(j), desired) && currentSlots.get(j).getCount() == desired.getCount()) {
                    sourceIdx = j;
                    break;
                }
            }

            if (sourceIdx != -1) {
                // Эмуляция действий мыши:
                // 1. Взять предмет из слота sourceIdx
                // 2. Положить/обменять в слот targetIdx
                // 3. Вернуть оставшийся предмет обратно в sourceIdx
                ACTION_QUEUE.add(new SlotClickAction(syncId, sourceIdx, 0, SlotActionType.PICKUP));
                ACTION_QUEUE.add(new SlotClickAction(syncId, targetIdx, 0, SlotActionType.PICKUP));
                ACTION_QUEUE.add(new SlotClickAction(syncId, sourceIdx, 0, SlotActionType.PICKUP));

                // Обновляем виртуальную карту слотов
                ItemStack temp = currentSlots.get(targetIdx);
                currentSlots.set(targetIdx, currentSlots.get(sourceIdx));
                currentSlots.set(sourceIdx, temp);
            }
        }
    }

    private static List<ItemStack> calculateTargetLayout(List<ItemStack> original, int totalSlots) {
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack stack : original) {
            if (!stack.isEmpty()) {
                items.add(stack.copy());
            }
        }

        // 1. Объединение неполных стаков одинаковых предметов
        List<ItemStack> consolidated = new ArrayList<>();
        for (ItemStack item : items) {
            for (ItemStack existing : consolidated) {
                if (ItemStack.areItemsAndComponentsEqual(item, existing) && existing.getCount() < existing.getMaxCount()) {
                    int space = existing.getMaxCount() - existing.getCount();
                    int transfer = Math.min(space, item.getCount());
                    existing.increment(transfer);
                    item.decrement(transfer);
                    if (item.isEmpty()) break;
                }
            }
            if (!item.isEmpty()) {
                consolidated.add(item);
            }
        }

        // 2. Сортировка: по ID, названию и количеству по убыванию
        consolidated.sort(
                Comparator.comparing((ItemStack s) -> s.getItem().toString())
                        .thenComparing(s -> s.getName().getString())
                        .thenComparingInt(ItemStack::getCount).reversed()
        );

        // 3. Дополняем пустыми слотами до размера контейнера
        List<ItemStack> result = new ArrayList<>(consolidated);
        while (result.size() < totalSlots) {
            result.add(ItemStack.EMPTY);
        }
        return result;
    }

    private static boolean areEqual(ItemStack a, ItemStack b) {
        if (a.isEmpty() && b.isEmpty()) return true;
        return ItemStack.areItemsAndComponentsEqual(a, b);
    }

    // Вызывается каждый клиентский тик
    public static void onClientTick(MinecraftClient client) {
        if (ACTION_QUEUE.isEmpty()) return;

        // Если сундук был закрыт до завершения сортировки — сбрасываем очередь
        if (client.player == null || !(client.player.currentScreenHandler instanceof GenericContainerScreenHandler)) {
            ACTION_QUEUE.clear();
            return;
        }

        if (tickCooldown > 0) {
            tickCooldown--;
            return;
        }

        // Выполняем один запланированный клик
        SlotClickAction action = ACTION_QUEUE.poll();
        if (action != null && client.interactionManager != null) {
            client.interactionManager.clickSlot(
                    action.syncId(),
                    action.slotId(),
                    action.button(),
                    action.actionType(),
                    client.player
            );
            tickCooldown = DELAY_TICKS;
        }
    }
}
