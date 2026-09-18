package com.codex.stagevoice.mixin;

import com.codex.stagevoice.StageVoiceClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// The local player overrides playSound without calling PlayerEntity.playSound.
@Mixin({PlayerEntity.class, ClientPlayerEntity.class})
public abstract class PlayerEntityMixin {
    @Inject(
            method = "playSound(Lnet/minecraft/sound/SoundEvent;FF)V",
            at = @At("HEAD"),
            cancellable = true)
    private void stagevoice$replaceFemaleHurtSound(SoundEvent soundEvent,
                                                    float volume,
                                                    float pitch,
                                                    CallbackInfo callbackInfo) {
        if (StageVoiceClient.replaceFemaleHurtSound(
                (PlayerEntity) (Object) this,
                soundEvent,
                volume,
                pitch)) {
            callbackInfo.cancel();
        }
    }
}
