package dev.jstus.snitch.shadowban;

public enum ShadowAction {
    BAN,
    UNBAN;

    public String toString() {
        return switch (this) {
            case BAN -> "ban";
            case UNBAN -> "unban";
        };
    }

    public static ShadowAction fromString(String string) {
        return switch (string.toLowerCase()) {
            case "ban" -> BAN;
            case "unban" -> UNBAN;
            default -> null;
        };
    }
}
