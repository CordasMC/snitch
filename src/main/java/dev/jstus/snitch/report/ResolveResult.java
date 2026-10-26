package dev.jstus.snitch.report;

public record ResolveResult(Outcome outcome, int reporters) {

    public enum Outcome {
        RESOLVED,
        NO_OPEN_CASE,
        ERROR
    }

    public static ResolveResult resolved(int reporters) {
        return new ResolveResult(Outcome.RESOLVED, reporters);
    }

    public static ResolveResult noOpenCase() {
        return new ResolveResult(Outcome.NO_OPEN_CASE, 0);
    }

    public static ResolveResult error() {
        return new ResolveResult(Outcome.ERROR, 0);
    }
}
