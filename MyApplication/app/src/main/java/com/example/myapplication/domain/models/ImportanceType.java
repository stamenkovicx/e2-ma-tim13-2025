package com.example.myapplication.domain.models;

public enum ImportanceType {
    NORMAL("Normalan", 1),
    IMPORTANT("Vazan", 3),
    EXTREMELY_IMPORTANT("Ekstremno vazan", 10),
    SPECIAL("Specijalan", 100);

    private final String serbianName;
    private final int xpValue;

    ImportanceType(String serbianName, int xpValue) {
        this.serbianName = serbianName;
        this.xpValue = xpValue;
    }

    public String getSerbianName() {
        return serbianName;
    }

    public int getXpValue() {
        return xpValue;
    }

    public static ImportanceType fromSerbianName(String serbianName) {
        for (ImportanceType type : ImportanceType.values()) {
            if (type.serbianName.equals(serbianName)) {
                return type;
            }
        }
        return null;
    }
}
