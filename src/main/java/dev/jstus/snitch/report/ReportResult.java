package dev.jstus.snitch.report;

public record ReportResult(Outcome outcome, double riskScore) {

    public enum Outcome {
        REPORTED,
        ALREADY_REPORTED,
        SELF,
        ERROR
    }

    public static ReportResult reported(double riskScore) {
        return new ReportResult(Outcome.REPORTED, riskScore);
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
