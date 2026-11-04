package com.ritesh.mixin;

import com.ritesh.feature.Coordinator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(EnderPearlEntity.class)
public class EnderPearlEntityMixin {
    @Inject(method = "onEntityHit", at = @At("HEAD"))
    private void onEntityHit(EntityHitResult hitResult, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            EnderPearlEntity pearl = (EnderPearlEntity)(Object) this;
            if (pearl.getOwner() == mc.player) {
                Entity target = hitResult.getEntity();
                if (target instanceof PlayerEntity) {
                    Coordinator.onPearlHitEntity(target);
                }
            }
        }
    }
}