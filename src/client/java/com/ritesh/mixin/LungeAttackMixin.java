package com.ritesh.mixin;

import com.ritesh.feature.Coordinator;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class LungeAttackMixin {
    @Inject(method = "startAttack", at = @At("HEAD"))
    private void onStartAttack(CallbackInfoReturnable<Boolean> cir) {
        Minecraft client = (Minecraft)(Object) this;
        if (client.player != null) {
            Coordinator.onAttack(client.player);
        }
    }
}
