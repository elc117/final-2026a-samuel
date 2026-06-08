CREATE TABLE friendships (
    id UUID NOT NULL,
    requester_user_id BIGINT NOT NULL,
    receiver_user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT friendships_pkey PRIMARY KEY (id),
    CONSTRAINT friendships_requester_fk
        FOREIGN KEY (requester_user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT friendships_receiver_fk
        FOREIGN KEY (receiver_user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT friendships_different_users_check CHECK (
        requester_user_id <> receiver_user_id
    ),
    CONSTRAINT friendships_status_check CHECK (
        status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'CANCELLED')
    )
);

CREATE UNIQUE INDEX friendships_user_pair_unique_idx
    ON friendships (
        LEAST(requester_user_id, receiver_user_id),
        GREATEST(requester_user_id, receiver_user_id)
    );

CREATE INDEX friendships_requester_status_idx
    ON friendships (requester_user_id, status);

CREATE INDEX friendships_receiver_status_idx
    ON friendships (receiver_user_id, status);
