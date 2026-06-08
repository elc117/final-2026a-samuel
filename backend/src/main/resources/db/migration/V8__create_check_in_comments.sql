CREATE TABLE check_in_comments (
    id UUID NOT NULL,
    check_in_id UUID NOT NULL,
    author_user_id BIGINT NOT NULL,
    content VARCHAR(1000) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_in_comments_pkey PRIMARY KEY (id),
    CONSTRAINT check_in_comments_check_in_fk
        FOREIGN KEY (check_in_id) REFERENCES check_ins(id) ON DELETE CASCADE,
    CONSTRAINT check_in_comments_author_fk
        FOREIGN KEY (author_user_id) REFERENCES users(id)
);

CREATE INDEX check_in_comments_check_in_created_idx
    ON check_in_comments (check_in_id, created_at);
