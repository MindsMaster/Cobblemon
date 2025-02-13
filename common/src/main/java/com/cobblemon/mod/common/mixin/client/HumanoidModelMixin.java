package com.cobblemon.mod.common.mixin.client;
import com.cobblemon.mod.common.api.riding.Rideable;
import com.cobblemon.mod.common.api.riding.RidingManager;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public class HumanoidModelMixin {
    @Shadow @Final public ModelPart head;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;cos(F)F"))
    private void cobblemon$setHeadRotation(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci){
        if (!(entity instanceof Player)) return;
        Entity vehicle = entity.getVehicle();

        if (!(vehicle instanceof PokemonEntity)) return;
        RidingManager ridingManager = ((Rideable) vehicle).getRiding();
        if (ridingManager.shouldRotatePlayerHead((PokemonEntity) vehicle)) return;

        this.head.yRot = 0f;
        this.head.xRot = 0f;
    }
}
