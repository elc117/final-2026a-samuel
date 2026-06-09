CREATE TABLE challenges (
    id UUID NOT NULL,
    group_id UUID NOT NULL,
    creator_user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(1000),
    period VARCHAR(20) NOT NULL,
    allow_multiple_check_ins_per_day BOOLEAN NOT NULL,
    starts_at DATE NOT NULL,
    ends_at DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMPTZ,
    CONSTRAINT challenges_pkey PRIMARY KEY (id),
    CONSTRAINT challenges_group_fk
        FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
    CONSTRAINT challenges_creator_fk
        FOREIGN KEY (creator_user_id) REFERENCES users(id),
    CONSTRAINT challenges_period_check CHECK (
        period IN ('WEEKLY', 'QUARTERLY', 'SEMIANNUAL', 'ANNUAL')
    ),
    CONSTRAINT challenges_status_check CHECK (
        status IN ('ACTIVE', 'ENDED')
    ),
    CONSTRAINT challenges_dates_check CHECK (ends_at >= starts_at)
);

CREATE UNIQUE INDEX challenges_one_active_per_group_idx
    ON challenges (group_id)
    WHERE status = 'ACTIVE';

CREATE INDEX challenges_group_created_idx
    ON challenges (group_id, created_at DESC);
