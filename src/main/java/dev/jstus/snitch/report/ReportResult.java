package dev.jstus.snitch.report;

public record ReportResult(Outcome outcome, double riskScore) {

    public enum Outcome {
        REPORTED,
        SILENT_DENIAL_SHADOW_BANNED,
        SILENT_DENIAL_ALT_PREVENTION,
        ALREADY_REPORTED,
        SELF,
        ERROR
    }

    public static ReportResult reported(double riskScore) {
        return new ReportResult(Outcome.REPORTED, riskScore);
    }

    public static ReportResult shadowBanned() {
        return new ReportResult(Outcome.SILENT_DENIAL_SHADOW_BANNED, 0);
    }

    public static ReportResult altPrevented() {
        return new ReportResult(Outcome.SILENT_DENIAL_ALT_PREVENTION, 0);
    }

    public static ReportResult alreadyReported() {
        return new ReportResult(Outcome.ALREADY_REPORTED, 0);
    }

    public static ReportResult self() {
        return new ReportResult(Outcome.SELF, 0);
    }

    public static ReportResult error() {
        return new ReportResult(Outcome.ERROR, 0);
    }
}
