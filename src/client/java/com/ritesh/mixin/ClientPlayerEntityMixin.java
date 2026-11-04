package com.ritesh.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface ClientPlayerEntityMixin {
    @Invoker("setSharedFlag")
    void invokeSetFlag(int index, boolean value);
}
