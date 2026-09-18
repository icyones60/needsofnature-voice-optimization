package com.codex.stagevoice;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.EntityTrackingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class StageVoiceClient implements ClientModInitializer {
    private static final String MOD_ID = "stagevoice";
    private static final String NON_RUNTIME = "com.nonid.internal.animation.client.runtime.ClientAnimationRuntime";
    private static final Identifier FEMALE_HURT_SOUND = Identifier.of("wildfire_gender", "female_hurt");
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final StageVoiceConfig CONFIG = new StageVoiceConfig(LOGGER);
    private static final List<String> GASPING_MARKERS = List.of(
            "defeat",
            "defeated",
            "on_back",
            "on_belly",
            "on_block",
            "on_wall"
    );

    private static final Map<String, List<SoundEvent>> PACK_SOUNDS = registerPackSounds();

    private static final Map<UUID, TrackedInstance> TRACKED = new HashMap<>();
    private static final Map<UUID, QueuedStart> QUEUED_STARTS = new HashMap<>();
    private static final Map<UUID, PlayingSound> PLAYING_SOUNDS = new HashMap<>();
    private static RuntimeAccess runtime;
    private static boolean runtimeWarningLogged;

    private static SoundEvent register(String name) {
        Identifier id = Identifier.of(MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    private static Map<String, List<SoundEvent>> registerPackSounds() {
        Map<String, List<SoundEvent>> sounds = new HashMap<>();
        for (VoicePack pack : VoicePack.VALUES) {
            for (StageBand band : StageBand.values()) {
                String groupKey = soundGroupKey(pack, band);
                List<SoundEvent> events = new ArrayList<>(pack.count(band.id));
                for (int index = 1; index <= pack.count(band.id); index++) {
                    events.add(register(groupKey + "/" + band.id + "_" + index));
                }
                sounds.put(groupKey, List.copyOf(events));
            }
        }
        return Map.copyOf(sounds);
    }

    @Override
    public void onInitializeClient() {
        CONFIG.initialize();
        ClientTickEvents.END_CLIENT_TICK.register(StageVoiceClient::tick);
        LOGGER.info("NeedsOfNature Voice Optimization initialized; animation and female hurt audio is enabled");
    }

    static StageVoiceConfig config() {
        return CONFIG;
    }

    static void onConfigSaved() {
        updateWaitingDelays(monotonicMillis());
    }

    /** Replaces Female Gender Mod's female hurt sound without changing its trigger conditions. */
    public static boolean replaceFemaleHurtSound(PlayerEntity player, SoundEvent soundEvent,
                                                  float originalVolume, float originalPitch) {
        if (soundEvent == null || !FEMALE_HURT_SOUND.equals(soundEvent.id())) {
            return false;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || player.getEntityWorld() != client.world) {
            return false;
        }

        float configuredVolume = CONFIG.volume();
        float totalVolume = Math.max(0.0f, originalVolume) * configuredVolume;
        if (totalVolume <= 0.0f) {
            return true;
        }

        SoundEvent replacement = randomSound(CONFIG.femaleHurtPack(), StageBand.HIGH);
        for (SoundInstance layer : createGainLayers(
                replacement,
                player,
                totalVolume,
                originalPitch)) {
            client.getSoundManager().play(layer);
        }
        return true;
    }

    /** Called by the queueStart mixin before NeedsOfNature creates its runtime state. */
    public static void onAnimationQueued(Identifier animationId, UUID instanceId,
                                         List<UUID> actorUuids, List<String> actorKeys,
                                         int stageCount, int stageIndex,
                                         long stageStartTick) {
        if (instanceId == null || actorUuids == null || actorUuids.isEmpty()
                || stageCount <= 0 || stageIndex < 0 || stageIndex >= stageCount) {
            return;
        }

        List<UUID> actors = actorUuids.stream()
                .filter(actorUuid -> actorUuid != null)
                .toList();
        if (actors.isEmpty()) return;

        List<String> keys = actorKeys == null ? List.of() : actorKeys.stream()
                .filter(actorKey -> actorKey != null && !actorKey.isBlank())
                .toList();
        QUEUED_STARTS.put(instanceId, new QueuedStart(
                animationId == null ? "" : animationId.toString(),
                actors,
                keys,
                stageCount,
                stageIndex,
                stageStartTick));
    }

    private static void tick(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            clearClientState();
            return;
        }

        RuntimeAccess access = getRuntime();
        if (access == null) return;

        long nowTick = client.world.getTime();
        long nowMillis = monotonicMillis();
        if (CONFIG.reloadIfChanged(nowMillis)) {
            updateWaitingDelays(nowMillis);
        }
        Set<UUID> seen = new HashSet<>();
        Map<UUID, PlayerEntity> anchors = new HashMap<>();

        registerQueuedStarts(client, nowTick, nowMillis, seen, anchors);

        for (PlayerEntity player : client.world.getPlayers()) {
            UUID instanceId = access.latestInstanceContaining(player.getUuid());
            if (instanceId == null) continue;
            seen.add(instanceId);
            PlayerEntity previous = anchors.get(instanceId);
            if (previous == null || player == client.player) anchors.put(instanceId, player);
        }

        TRACKED.keySet().removeIf(instanceId -> !seen.contains(instanceId));
        observeStages(access, nowTick, nowMillis, anchors);
        playDueSounds(client, nowMillis);
    }

    private static void registerQueuedStarts(MinecraftClient client, long nowTick, long nowMillis,
                                             Set<UUID> seen,
                                             Map<UUID, PlayerEntity> anchors) {
        Iterator<Map.Entry<UUID, QueuedStart>> iterator = QUEUED_STARTS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, QueuedStart> entry = iterator.next();
            QueuedStart start = entry.getValue();
            PlayerEntity anchor = findAnchor(client, start.actorUuids);
            if (anchor == null) continue;

            UUID instanceId = entry.getKey();
            seen.add(instanceId);
            anchors.put(instanceId, preferLocalPlayer(anchors.get(instanceId), anchor, client.player));
            observeStage(
                    instanceId,
                    anchor,
                    start.stageIndex,
                    start.stageCount,
                    start.stageStartTick,
                    start.animationId,
                    start.actorKeys,
                    "",
                    "",
                    "",
                    nowTick,
                    nowMillis);
            iterator.remove();
        }
    }

    private static void observeStages(RuntimeAccess access, long nowTick, long nowMillis,
                                      Map<UUID, PlayerEntity> anchors) {
        for (Map.Entry<UUID, PlayerEntity> entry : anchors.entrySet()) {
            UUID instanceId = entry.getKey();
            PlayerEntity anchor = entry.getValue();
            Object stage = access.currentStage(instanceId);
            if (stage == null) continue;

            int stageIndex = access.findStageIndex(instanceId, stage);
            int stageCount = access.findStageCount(instanceId);
            if (stageIndex < 0 || stageCount <= 0) continue;

            TrackedInstance tracked = TRACKED.get(instanceId);
            String queuedAnimationId = tracked == null ? "" : tracked.animationId;
            List<String> actorKeys = tracked == null ? List.of() : tracked.actorKeys;
            observeStage(
                    instanceId,
                    anchor,
                    stageIndex,
                    stageCount,
                    access.stageStartTick(instanceId),
                    queuedAnimationId,
                    actorKeys,
                    access.latestAnimationIdContaining(anchor.getUuid()),
                    access.stageAnimationId(stage),
                    access.stageEffectiveAnimationId(stage),
                    nowTick,
                    nowMillis);
        }
    }

    private static void observeStage(UUID instanceId, PlayerEntity anchor,
                                     int stageIndex, int stageCount,
                                     long stageStartTick,
                                     String animationId, List<String> actorKeys,
                                     String runtimeAnimationId, String stageAnimationId,
                                     String effectiveAnimationId,
                                     long nowTick, long nowMillis) {
        TrackedInstance tracked = TRACKED.get(instanceId);
        boolean stageStartKnown = stageStartTick >= 0L;
        boolean newStage = tracked == null
                || tracked.stageIndex != stageIndex
                || (stageStartKnown && tracked.stageStartKnown
                && tracked.stageStartTick != stageStartTick);
        long effectiveStageStart = stageStartKnown
                ? stageStartTick
                : newStage || tracked == null ? nowTick : tracked.stageStartTick;

        boolean hasStageAnimation = !stageAnimationId.isBlank() || !effectiveAnimationId.isBlank();
        boolean gaspingDetected = hasStageAnimation
                ? isGaspingAnimation(List.of(), stageAnimationId, effectiveAnimationId)
                : isGaspingAnimation(actorKeys, animationId, runtimeAnimationId);

        if (tracked == null) {
            tracked = new TrackedInstance(
                    stageIndex,
                    effectiveStageStart,
                    stageStartKnown,
                    animationId,
                    actorKeys);
            TRACKED.put(instanceId, tracked);
        } else {
            if (tracked.animationId.isBlank() && animationId != null && !animationId.isBlank()) {
                tracked.animationId = animationId;
            }
            if (tracked.actorKeys.isEmpty() && actorKeys != null && !actorKeys.isEmpty()) {
                tracked.actorKeys = List.copyOf(actorKeys);
            }
            if (newStage) {
                tracked.stageIndex = stageIndex;
                tracked.stageStartTick = effectiveStageStart;
                tracked.stageStartKnown = stageStartKnown;
            } else if (stageStartKnown && !tracked.stageStartKnown) {
                tracked.stageStartTick = stageStartTick;
                tracked.stageStartKnown = true;
            }
        }

        tracked.anchor = anchor;
        StageBand expectedBand = expectedStageBand(tracked, gaspingDetected, stageIndex, stageCount);
        if (newStage || tracked.activeBand != expectedBand) {
            selectStageAudio(tracked, gaspingDetected, stageIndex, stageCount, nowMillis);
        }
    }

    private static boolean isGaspingAnimation(List<String> actorKeys, String... animationIds) {
        if (actorKeys != null) {
            for (String actorKey : actorKeys) {
                if (containsGaspingMarker(actorKey)) return true;
            }
        }
        for (String animationId : animationIds) {
            if (containsGaspingMarker(animationId)) return true;
        }
        return false;
    }

    private static boolean containsGaspingMarker(String value) {
        if (value == null || value.isBlank()) return false;
        String normalized = value.toLowerCase(Locale.ROOT).replace('\\', '/');
        for (String marker : GASPING_MARKERS) {
            if (normalized.contains(marker)) return true;
        }
        return false;
    }

    private static void selectStageAudio(TrackedInstance tracked, boolean gasping,
                                         int stageIndex, int stageCount, long nowMillis) {
        if (gasping) {
            tracked.activeBand = StageBand.GASPING;
            tracked.nextSoundMillis = delayDeadline(nowMillis);
            return;
        }
        if (stageIndex == stageCount - 1) {
            tracked.activeBand = StageBand.CLIMAX;
            tracked.nextSoundMillis = nowMillis;
            return;
        }
        tracked.activeBand = StageBand.HIGH;
        tracked.nextSoundMillis = delayDeadline(nowMillis);
    }

    private static StageBand expectedStageBand(TrackedInstance tracked, boolean gasping,
                                               int stageIndex, int stageCount) {
        if (gasping) return StageBand.GASPING;
        if (stageIndex == stageCount - 1) return StageBand.CLIMAX;
        return StageBand.HIGH;
    }

    private static PlayerEntity findAnchor(MinecraftClient client, List<UUID> actorUuids) {
        PlayerEntity fallback = null;
        for (UUID actorUuid : actorUuids) {
            PlayerEntity player = client.world.getPlayerByUuid(actorUuid);
            if (player == null) continue;
            if (player == client.player) return player;
            if (fallback == null) fallback = player;
        }
        return fallback;
    }

    private static PlayerEntity preferLocalPlayer(PlayerEntity current, PlayerEntity candidate,
                                                  ClientPlayerEntity localPlayer) {
        if (candidate == localPlayer) return candidate;
        return current == null ? candidate : current;
    }

    private static void playDueSounds(MinecraftClient client, long nowMillis) {
        Iterator<Map.Entry<UUID, PlayingSound>> playingIterator = PLAYING_SOUNDS.entrySet().iterator();
        while (playingIterator.hasNext()) {
            Map.Entry<UUID, PlayingSound> entry = playingIterator.next();
            boolean anyLayerPlaying = entry.getValue().layers.stream()
                    .anyMatch(client.getSoundManager()::isPlaying);
            if (anyLayerPlaying) continue;

            TrackedInstance tracked = TRACKED.get(entry.getKey());
            playingIterator.remove();
            if (tracked != null && tracked.activeBand != null && tracked.activeBand.isLooping()) {
                tracked.nextSoundMillis = Math.max(
                        tracked.nextSoundMillis,
                        delayDeadline(nowMillis));
            }
        }

        for (Map.Entry<UUID, TrackedInstance> entry : TRACKED.entrySet()) {
            UUID instanceId = entry.getKey();
            TrackedInstance tracked = entry.getValue();
            if (PLAYING_SOUNDS.containsKey(instanceId)
                    || tracked.activeBand == null
                    || tracked.nextSoundMillis > nowMillis) {
                continue;
            }
            if (tracked.anchor == null || tracked.anchor.getEntityWorld() != client.world) continue;

            float requestedVolume = CONFIG.volume();
            if (requestedVolume <= 0.0f) {
                tracked.nextSoundMillis = delayDeadline(nowMillis);
                continue;
            }

            SoundEvent soundEvent = randomSound(tracked.activeBand);
            List<SoundInstance> layers = createGainLayers(
                    soundEvent,
                    tracked.anchor,
                    requestedVolume);
            for (SoundInstance layer : layers) {
                client.getSoundManager().play(layer);
            }
            PLAYING_SOUNDS.put(instanceId, new PlayingSound(layers));
        }
    }

    private static SoundEvent randomSound(StageBand band) {
        VoicePack pack = switch (band) {
            case HIGH -> CONFIG.highPack();
            case CLIMAX -> CONFIG.climaxPack();
            case GASPING -> CONFIG.gaspingPack();
        };
        return randomSound(pack, band);
    }

    private static SoundEvent randomSound(VoicePack pack, StageBand band) {
        List<SoundEvent> events = PACK_SOUNDS.get(soundGroupKey(pack, band));
        return events.get(ThreadLocalRandom.current().nextInt(events.size()));
    }

    private static List<SoundInstance> createGainLayers(SoundEvent soundEvent,
                                                         PlayerEntity anchor,
                                                         float volume) {
        return createGainLayers(soundEvent, anchor, volume, 1.0f);
    }

    private static List<SoundInstance> createGainLayers(SoundEvent soundEvent,
                                                         PlayerEntity anchor,
                                                         float volume,
                                                         float pitch) {
        int fullLayers = (int) Math.floor(volume);
        float partialLayer = volume - fullLayers;
        int layerCount = fullLayers + (partialLayer > 0.001f ? 1 : 0);
        List<SoundInstance> layers = new ArrayList<>(Math.max(1, layerCount));
        for (int index = 0; index < fullLayers; index++) {
            layers.add(createSound(soundEvent, anchor, 1.0f, pitch));
        }
        if (partialLayer > 0.001f) {
            layers.add(createSound(soundEvent, anchor, partialLayer, pitch));
        }
        return List.copyOf(layers);
    }

    private static SoundInstance createSound(SoundEvent soundEvent, PlayerEntity anchor, float volume) {
        return createSound(soundEvent, anchor, volume, 1.0f);
    }

    private static SoundInstance createSound(SoundEvent soundEvent, PlayerEntity anchor,
                                             float volume, float pitch) {
        return new EntityTrackingSoundInstance(
                soundEvent,
                SoundCategory.PLAYERS,
                volume,
                pitch,
                anchor,
                ThreadLocalRandom.current().nextLong());
    }

    private static String soundGroupKey(VoicePack pack, StageBand band) {
        return "voice/packs/" + pack.id() + "/" + band.id;
    }

    private static long monotonicMillis() {
        return System.nanoTime() / 1_000_000L;
    }

    private static long delayDeadline(long nowMillis) {
        long delayMillis = CONFIG.delayMillis();
        return delayMillis > Long.MAX_VALUE - nowMillis
                ? Long.MAX_VALUE
                : nowMillis + delayMillis;
    }

    private static void updateWaitingDelays(long nowMillis) {
        long deadline = delayDeadline(nowMillis);
        for (Map.Entry<UUID, TrackedInstance> entry : TRACKED.entrySet()) {
            TrackedInstance tracked = entry.getValue();
            if (tracked.activeBand != null
                    && tracked.activeBand.isLooping()
                    && !PLAYING_SOUNDS.containsKey(entry.getKey())) {
                tracked.nextSoundMillis = deadline;
            }
        }
    }

    private static void clearClientState() {
        TRACKED.clear();
        QUEUED_STARTS.clear();
        PLAYING_SOUNDS.clear();
    }

    private static RuntimeAccess getRuntime() {
        if (runtime != null) return runtime;
        try {
            runtime = new RuntimeAccess(Class.forName(NON_RUNTIME));
            return runtime;
        } catch (ReflectiveOperationException | LinkageError error) {
            if (!runtimeWarningLogged) {
                runtimeWarningLogged = true;
                LOGGER.error("Could not connect to NeedsOfNature animation runtime", error);
            }
            return null;
        }
    }

    private enum StageBand {
        HIGH,
        CLIMAX,
        GASPING;

        private boolean isLooping() {
            return true;
        }

        private final String id = name().toLowerCase(Locale.ROOT);
    }

    private static final class TrackedInstance {
        private int stageIndex;
        private long stageStartTick;
        private boolean stageStartKnown;
        private String animationId;
        private List<String> actorKeys;
        private PlayerEntity anchor;
        private StageBand activeBand;
        private long nextSoundMillis;

        private TrackedInstance(int stageIndex, long stageStartTick, boolean stageStartKnown,
                                String animationId, List<String> actorKeys) {
            this.stageIndex = stageIndex;
            this.stageStartTick = stageStartTick;
            this.stageStartKnown = stageStartKnown;
            this.animationId = animationId == null ? "" : animationId;
            this.actorKeys = actorKeys == null ? List.of() : List.copyOf(actorKeys);
        }
    }

    private static final class QueuedStart {
        private final String animationId;
        private final List<UUID> actorUuids;
        private final List<String> actorKeys;
        private final int stageCount;
        private final int stageIndex;
        private final long stageStartTick;

        private QueuedStart(String animationId, List<UUID> actorUuids, List<String> actorKeys,
                            int stageCount, int stageIndex, long stageStartTick) {
            this.animationId = animationId;
            this.actorUuids = actorUuids;
            this.actorKeys = actorKeys;
            this.stageCount = stageCount;
            this.stageIndex = stageIndex;
            this.stageStartTick = stageStartTick;
        }
    }

    private static final class PlayingSound {
        private final List<SoundInstance> layers;

        private PlayingSound(List<SoundInstance> layers) {
            this.layers = layers;
        }
    }

    private static final class RuntimeAccess {
        private final Method latestInstanceContaining;
        private final Method latestAnimationIdContaining;
        private final Method currentStage;
        private final Method findStage;
        private final Method stageStartTick;
        private Class<?> stageClass;
        private Method stageAnimationId;
        private Method stageEffectiveAnimationId;
        private boolean stageMetadataWarningLogged;

        private RuntimeAccess(Class<?> runtimeClass) throws ReflectiveOperationException {
            latestInstanceContaining = runtimeClass.getMethod(
                    "findLatestActiveInstanceContaining", UUID.class);
            latestAnimationIdContaining = runtimeClass.getMethod(
                    "findLatestActiveAnimationIdContaining", UUID.class);
            currentStage = runtimeClass.getMethod("findCurrentStage", UUID.class);
            findStage = runtimeClass.getMethod("findStage", UUID.class, int.class);
            stageStartTick = runtimeClass.getMethod("findStageStartTick", UUID.class);
        }

        private UUID latestInstanceContaining(UUID actorUuid) {
            try {
                return (UUID) latestInstanceContaining.invoke(null, actorUuid);
            } catch (ReflectiveOperationException error) {
                return null;
            }
        }

        private String latestAnimationIdContaining(UUID actorUuid) {
            try {
                return identifierString(latestAnimationIdContaining.invoke(null, actorUuid));
            } catch (ReflectiveOperationException error) {
                return "";
            }
        }

        private Object currentStage(UUID instanceId) {
            try {
                return currentStage.invoke(null, instanceId);
            } catch (ReflectiveOperationException error) {
                return null;
            }
        }

        private long stageStartTick(UUID instanceId) {
            try {
                return ((Number) stageStartTick.invoke(null, instanceId)).longValue();
            } catch (ReflectiveOperationException error) {
                return -1L;
            }
        }

        private Object findStage(UUID instanceId, int index) {
            try {
                return findStage.invoke(null, instanceId, index);
            } catch (ReflectiveOperationException error) {
                return null;
            }
        }

        private int findStageIndex(UUID instanceId, Object current) {
            for (int index = 0; index < 128; index++) {
                Object candidate = findStage(instanceId, index);
                if (candidate == null) return -1;
                if (candidate.equals(current)) return index;
            }
            return -1;
        }

        private int findStageCount(UUID instanceId) {
            for (int index = 0; index < 128; index++) {
                if (findStage(instanceId, index) == null) return index;
            }
            return 0;
        }

        private String stageAnimationId(Object stage) {
            return invokeStageIdentifier(stage, false);
        }

        private String stageEffectiveAnimationId(Object stage) {
            return invokeStageIdentifier(stage, true);
        }

        private String invokeStageIdentifier(Object stage, boolean effective) {
            try {
                ensureStageMethods(stage.getClass());
                Method method = effective ? stageEffectiveAnimationId : stageAnimationId;
                return identifierString(method.invoke(stage));
            } catch (ReflectiveOperationException error) {
                if (!stageMetadataWarningLogged) {
                    stageMetadataWarningLogged = true;
                    LOGGER.warn("Could not read NeedsOfNature stage animation identifiers", error);
                }
                return "";
            }
        }

        private void ensureStageMethods(Class<?> currentStageClass) throws NoSuchMethodException {
            if (stageClass == currentStageClass) return;
            stageClass = currentStageClass;
            stageAnimationId = currentStageClass.getMethod("animationId");
            stageEffectiveAnimationId = currentStageClass.getMethod("effectiveAnimationId");
        }

        private static String identifierString(Object value) {
            return value == null ? "" : value.toString();
        }
    }
}
