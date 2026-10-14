package com.controllerassist.model;

public enum AttendanceStatus {
    ON_TIME("A tiempo"),
    LATE("Tardanza"),
    EARLY_EXIT("Salida anticipada"),
    NORMAL("Normal");

    private final String displayName;

    AttendanceStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
