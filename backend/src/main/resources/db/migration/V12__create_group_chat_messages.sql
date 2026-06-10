CREATE TABLE group_chat_messages (
    id UUID NOT NULL,
    group_id UUID NOT NULL,
    author_user_id BIGINT NOT NULL,
    content VARCHAR(2000) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT group_chat_messages_pkey PRIMARY KEY (id),
    CONSTRAINT group_chat_messages_group_fk
        FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
    CONSTRAINT group_chat_messages_author_fk
        FOREIGN KEY (author_user_id) REFERENCES users(id),
    CONSTRAINT group_chat_messages_content_check
        CHECK (LENGTH(TRIM(content)) > 0)
);

CREATE INDEX group_chat_messages_group_created_idx
    ON group_chat_messages (group_id, created_at DESC, id DESC);

