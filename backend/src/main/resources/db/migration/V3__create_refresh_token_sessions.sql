CREATE TABLE refresh_token_sessions (
    id UUID NOT NULL,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    replaced_by_id UUID,
    CONSTRAINT refresh_token_sessions_pkey PRIMARY KEY (id),
    CONSTRAINT refresh_token_sessions_user_fk
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT refresh_token_sessions_replacement_fk
        FOREIGN KEY (replaced_by_id) REFERENCES refresh_token_sessions(id)
        DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT refresh_token_sessions_token_hash_unique UNIQUE (token_hash),
    CONSTRAINT refresh_token_sessions_expiration_check CHECK (
        expires_at > created_at
    )
);

CREATE INDEX refresh_token_sessions_user_idx
    ON refresh_token_sessions (user_id);

CREATE INDEX refresh_token_sessions_active_idx
    ON refresh_token_sessions (token_hash)
    WHERE revoked_at IS NULL;
