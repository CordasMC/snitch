CREATE TABLE shadow_bans
(
    target     CHAR(36) NOT NULL PRIMARY KEY,
    issuer     CHAR(36),
    created_at BIGINT   NOT NULL
);

CREATE TABLE shadow_actions
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    issuer     CHAR(36),
    target     CHAR(36)    NOT NULL,
    action     VARCHAR(16) NOT NULL,
    created_at BIGINT      NOT NULL
);

CREATE INDEX idx_shadow_actions_target ON shadow_actions (target);
