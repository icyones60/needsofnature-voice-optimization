package com.codex.stagevoice.mixin;

import com.codex.stagevoice.StageVoiceClient;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.UUID;

@Mixin(targets = "com.nonid.internal.animation.client.runtime.ClientAnimationRuntime")
public abstract class ClientAnimationRuntimeMixin {
    @Inject(method = "queueStart", at = @At("HEAD"))
    private static void stagevoice$onQueueStart(
            Identifier animationId,
            List<List<Identifier>> modelRootsByActor,
            UUID instanceId,
            List<UUID> actorUuids,
            List<String> actorKeys,
            List<?> stages,
            List<?> dynamicProps,
            long startTick,
            int stageIndex,
            long stageStartTick,
            long stageEndTick,
            int normalStageDurationSeconds,
            double speed,
            boolean lockOrientation,
            float lockedYaw,
            float lockedHeadYaw,
            float lockedPitch,
            boolean continuousTorsoTracking,
            List<?> cameraIgnoredBlockPositions,
            boolean existingInstanceSync,
            CallbackInfo callbackInfo) {
        StageVoiceClient.onAnimationQueued(
                animationId,
                instanceId,
                actorUuids,
                actorKeys,
                stages == null ? 0 : stages.size(),
                stageIndex,
                stageStartTick);
    }
}
