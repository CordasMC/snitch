CREATE TABLE trust_scores (
    player UUID NOT NULL PRIMARY KEY,
    score DOUBLE PRECISION NOT NULL,
    updated_at BIGINT NOT NULL
);

CREATE TABLE report_cases (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    reported UUID NOT NULL,
    risk_score DOUBLE PRECISION NOT NULL,
    open BOOLEAN,
    resolution VARCHAR(16),
    created_at BIGINT NOT NULL,
    last_report_at BIGINT NOT NULL,
    resolved_by UUID,
    resolved_at BIGINT,
    UNIQUE (reported, open),
    CHECK (open),
    CHECK ((open IS NULL) <> (resolution IS NULL))
);

CREATE TABLE reports (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    case_id BIGINT NOT NULL REFERENCES report_cases (id) ON DELETE CASCADE,
    reporter UUID NOT NULL,
    trust_weight DOUBLE PRECISION NOT NULL,
    multiplier DOUBLE PRECISION NOT NULL,
    reason VARCHAR(255),
    created_at BIGINT NOT NULL,
    UNIQUE (case_id, reporter)
);

CREATE INDEX idx_reports_reporter ON reports (reporter);
