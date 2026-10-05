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

@Mixin(GenericContainerScreen.class)
public abstract class GenericContainerScreenMixin extends HandledScreen<GenericContainerScreenHandler> {

    public GenericContainerScreenMixin(GenericContainerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addSortButton(CallbackInfo ci) {
        int buttonWidth = 38;
        int buttonHeight = 12;
        int x = this.x + this.backgroundWidth - buttonWidth - 6;
        int y = this.y + 4;

        this.addDrawableChild(
                ButtonWidget.builder(Text.literal("Sort"), button -> {
                    ClientPlayNetworking.send(new SortPacketPayload());
                })
                .dimensions(x, y, buttonWidth, buttonHeight)
                .build()
        );
    }
}
