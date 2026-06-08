CREATE TABLE group_invite_links (
    token UUID NOT NULL,
    group_id UUID NOT NULL,
    created_by_user_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT group_invite_links_pkey PRIMARY KEY (token),
    CONSTRAINT group_invite_links_group_unique UNIQUE (group_id),
    CONSTRAINT group_invite_links_group_fk
        FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
    CONSTRAINT group_invite_links_creator_fk
        FOREIGN KEY (created_by_user_id) REFERENCES users(id)
);

CREATE INDEX group_invite_links_creator_idx
    ON group_invite_links (created_by_user_id);
