CREATE TABLE check_ins (
    id UUID NOT NULL,
    group_id UUID NOT NULL,
    author_user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(1000),
    image_url TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_ins_pkey PRIMARY KEY (id),
    CONSTRAINT check_ins_group_fk
        FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
    CONSTRAINT check_ins_author_fk
        FOREIGN KEY (author_user_id) REFERENCES users(id)
);

CREATE INDEX check_ins_group_created_idx
    ON check_ins (group_id, created_at DESC);

CREATE INDEX check_ins_author_idx
    ON check_ins (author_user_id);
