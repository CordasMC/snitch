package dev.jstus.snitch.shadowban;

public record ShadowResponse(ShadowAction action, Status status) {

    public enum Status {
        SUCCESS,
        ALREADY_BANNED,
        NOT_BANNED,
        FAILURE
    }
}
