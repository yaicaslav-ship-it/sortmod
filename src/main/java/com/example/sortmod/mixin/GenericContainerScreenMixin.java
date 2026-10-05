package com.example.sortmod.mixin;

import com.example.sortmod.network.SortPacketPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class GenericContainerScreenMixin<T extends ScreenHandler> extends Screen {

    @Shadow
    protected int x;

    @Shadow
    protected int y;

    protected GenericContainerScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addSortButton(CallbackInfo ci) {
        // Проверяем, что открыт именно сундук/контейнер
        if (!((Object) this instanceof GenericContainerScreen)) {
            return;
        }

        int buttonWidth = 36;
        int buttonHeight = 16;

        // Координаты слева от рамки контейнера
        int btnX = this.x - buttonWidth - 3;
        int btnY = this.y + 6;

        this.addDrawableChild(
                ButtonWidget.builder(Text.literal("Sort"), button -> {
                    ClientPlayNetworking.send(new SortPacketPayload());
                })
                .dimensions(btnX, btnY, buttonWidth, buttonHeight)
                .build()
        );
    }
}
