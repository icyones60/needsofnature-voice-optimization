package com.codex.stagevoice;

import java.util.Arrays;

enum VoicePack {
    RYUZEN_NIKOMI_SILENT("05ryuzen-nikomi-silent", "05ryuzen-nikomi[无口]", 39, 15, 6),
    KOTONE_AKARI("01kotone-akari", "01kotone-akari", 20, 6, 4),
    MEMENO_HINA("03memeno-hina", "03memeno-hina", 45, 11, 7),
    MIKAMI_SATSUKI("04mikami-satsuki", "04mikami-satsuki", 39, 6, 4),
    RYUZEN_NIKOMI_BREAK("05ryuzen-nikomi-break", "05ryuzen-nikomi[破防]", 43, 15, 7),
    KITAKAMI_TSUBASA("06kitakami-tsubasa", "06kitakami-tsubasa", 39, 9, 8),
    KATAGIRI_AKI("07katagiri-aki", "07katagiri-aki", 32, 10, 7),
    OTOSAKA_KINO("08otosaka-kino", "08otosaka-kino", 39, 20, 6),
    ASAHINA_YUNO("09asahina-yuno", "09asahina-yuno", 39, 15, 6),
    NEKONO_SHIRONE("10nekono-shirone", "10nekono-shirone", 39, 21, 7),
    TADAI_MARISA("11tadai-marisa", "11tadai-marisa", 45, 10, 6);

    static final VoicePack DEFAULT = RYUZEN_NIKOMI_SILENT;
    static final VoicePack[] VALUES = values();

    private final String id;
    private final String displayName;
    private final int highCount;
    private final int gaspingCount;
    private final int climaxCount;

    VoicePack(String id, String displayName, int highCount, int gaspingCount, int climaxCount) {
        this.id = id;
        this.displayName = displayName;
        this.highCount = highCount;
        this.gaspingCount = gaspingCount;
        this.climaxCount = climaxCount;
    }

    String id() {
        return id;
    }

    String displayName() {
        return displayName;
    }

    int count(String band) {
        return switch (band) {
            case "high" -> highCount;
            case "gasping" -> gaspingCount;
            case "climax" -> climaxCount;
            default -> throw new IllegalArgumentException("Unknown voice band: " + band);
        };
    }

    static VoicePack byId(String id) {
        if (id == null) return DEFAULT;
        return Arrays.stream(VALUES)
                .filter(pack -> pack.id.equals(id))
                .findFirst()
                .orElse(DEFAULT);
    }
}
