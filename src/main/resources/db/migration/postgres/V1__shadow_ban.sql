CREATE TABLE shadow_bans
(
    target     UUID   NOT NULL PRIMARY KEY,
    issuer     UUID,
    created_at BIGINT NOT NULL
);

CREATE TABLE shadow_actions
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    issuer     UUID,
    target     UUID        NOT NULL,
    action     VARCHAR(16) NOT NULL,
    created_at BIGINT      NOT NULL
);

CREATE INDEX idx_shadow_actions_target ON shadow_actions (target);
