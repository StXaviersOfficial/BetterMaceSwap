package com.ritesh.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Environment(EnvType.CLIENT)
@Mixin(Entity.class)
public interface ClientPlayerEntityMixin {
    @Invoker("setFlag")
    void invokeSetFlag(int index, boolean value);
}
