package com.codex.stagevoice;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

final class StageVoiceConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final long DEFAULT_DELAY_MILLIS = 600L;
    private static final float DEFAULT_VOLUME = 0.8f;
    private static final float MAX_VOLUME = 5.0f;
    private static final long RELOAD_INTERVAL_MILLIS = 1_000L;

    private final Path path;
    private final Logger logger;
    private Settings settings = defaults();
    private long lastModifiedMillis = Long.MIN_VALUE;
    private long nextReloadMillis;

    StageVoiceConfig(Logger logger) {
        this.logger = logger;
        path = FabricLoader.getInstance().getConfigDir().resolve("stagevoice.json");
    }

    void initialize() {
        if (Files.notExists(path)) write();
        reload(true);
    }

    boolean reloadIfChanged(long nowMillis) {
        if (nowMillis < nextReloadMillis) return false;
        nextReloadMillis = nowMillis + RELOAD_INTERVAL_MILLIS;
        return reload(false);
    }

    long delayMillis() {
        return settings.delayMillis;
    }

    float volume() {
        return settings.volume;
    }

    VoicePack highPack() {
        return VoicePack.byId(settings.highPack);
    }

    VoicePack gaspingPack() {
        return VoicePack.byId(settings.gaspingPack);
    }

    VoicePack climaxPack() {
        return VoicePack.byId(settings.climaxPack);
    }

    VoicePack femaleHurtPack() {
        return VoicePack.byId(settings.femaleHurtPack);
    }

    void setDelayMillis(long delayMillis) {
        settings.delayMillis = Math.max(0L, delayMillis);
    }

    void setVolume(float volume) {
        settings.volume = Float.isFinite(volume) ? Math.clamp(volume, 0.0f, MAX_VOLUME) : DEFAULT_VOLUME;
    }

    void setHighPack(VoicePack pack) {
        settings.highPack = pack.id();
    }

    void setGaspingPack(VoicePack pack) {
        settings.gaspingPack = pack.id();
    }

    void setClimaxPack(VoicePack pack) {
        settings.climaxPack = pack.id();
    }

    void setFemaleHurtPack(VoicePack pack) {
        settings.femaleHurtPack = pack.id();
    }

    void save() {
        settings = validate(settings);
        write();
    }

    private boolean reload(boolean force) {
        try {
            long modifiedMillis = Files.getLastModifiedTime(path).toMillis();
            if (!force && modifiedMillis == lastModifiedMillis) return false;
            lastModifiedMillis = modifiedMillis;

            JsonObject root;
            try (Reader reader = Files.newBufferedReader(path)) {
                root = GSON.fromJson(reader, JsonObject.class);
            }
            if (root == null) throw new JsonParseException("Configuration is empty");

            settings = validate(new Settings(
                    longValue(root, "delayMillis", settings.delayMillis),
                    floatValue(root, "volume", settings.volume),
                    stringValue(root, "highPack", settings.highPack),
                    stringValue(root, "gaspingPack", settings.gaspingPack),
                    stringValue(root, "climaxPack", settings.climaxPack),
                    stringValue(root, "femaleHurtPack", settings.femaleHurtPack)));
            logger.info(
                    "Loaded NeedsOfNature Voice Optimization config: delayMillis={}, volume={}, highPack={}, gaspingPack={}, climaxPack={}, femaleHurtPack={}",
                    settings.delayMillis,
                    settings.volume,
                    settings.highPack,
                    settings.gaspingPack,
                    settings.climaxPack,
                    settings.femaleHurtPack);
            return true;
        } catch (IOException | RuntimeException error) {
            logger.error("Could not load {}; keeping the current NeedsOfNature Voice Optimization settings", path, error);
            return false;
        }
    }

    private Settings validate(Settings loaded) {
        long delayMillis = Math.max(0L, loaded.delayMillis);
        float volume = Float.isFinite(loaded.volume)
                ? Math.clamp(loaded.volume, 0.0f, MAX_VOLUME)
                : DEFAULT_VOLUME;
        return new Settings(
                delayMillis,
                volume,
                VoicePack.byId(loaded.highPack).id(),
                VoicePack.byId(loaded.gaspingPack).id(),
                VoicePack.byId(loaded.climaxPack).id(),
                VoicePack.byId(loaded.femaleHurtPack).id());
    }

    private void write() {
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(settings, writer);
            }
            lastModifiedMillis = Files.getLastModifiedTime(path).toMillis();
            logger.info("Saved NeedsOfNature Voice Optimization config to {}", path);
        } catch (IOException error) {
            logger.error("Could not save NeedsOfNature Voice Optimization config to {}", path, error);
        }
    }

    private static Settings defaults() {
        return new Settings(
                DEFAULT_DELAY_MILLIS,
                DEFAULT_VOLUME,
                VoicePack.DEFAULT.id(),
                VoicePack.DEFAULT.id(),
                VoicePack.DEFAULT.id(),
                VoicePack.KATAGIRI_AKI.id());
    }

    private static long longValue(JsonObject root, String name, long fallback) {
        return root.has(name) ? root.get(name).getAsLong() : fallback;
    }

    private static float floatValue(JsonObject root, String name, float fallback) {
        return root.has(name) ? root.get(name).getAsFloat() : fallback;
    }

    private static String stringValue(JsonObject root, String name, String fallback) {
        return root.has(name) ? root.get(name).getAsString() : fallback;
    }

    private static final class Settings {
        private long delayMillis;
        private float volume;
        private String highPack;
        private String gaspingPack;
        private String climaxPack;
        private String femaleHurtPack;

        private Settings(long delayMillis, float volume, String highPack,
                         String gaspingPack, String climaxPack, String femaleHurtPack) {
            this.delayMillis = delayMillis;
            this.volume = volume;
            this.highPack = highPack;
            this.gaspingPack = gaspingPack;
            this.climaxPack = climaxPack;
            this.femaleHurtPack = femaleHurtPack;
        }
    }
}
