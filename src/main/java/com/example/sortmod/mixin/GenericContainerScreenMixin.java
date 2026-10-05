package com.example.sortmod.mixin;

import com.example.sortmod.network.SortPacketPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class GenericContainerScreenMixin {

    @Inject(method = "init", at = @At("TAIL"))
    private void addSortButton(CallbackInfo ci) {
        // Проверяем, что открыт именно сундук/контейнер, а не любой другой инвентарь
        if (!((Object) this instanceof GenericContainerScreen containerScreen)) {
            return;
        }

        int buttonWidth = 36;
        int buttonHeight = 16;

        // Координаты: слева от рамки сундука
        int btnX = containerScreen.x - buttonWidth - 3;
        int btnY = containerScreen.y + 6;

        containerScreen.addDrawableChild(
                ButtonWidget.builder(Text.literal("Sort"), button -> {
                    ClientPlayNetworking.send(new SortPacketPayload());
                })
                .dimensions(btnX, btnY, buttonWidth, buttonHeight)
                .build()
        );
    }
}
