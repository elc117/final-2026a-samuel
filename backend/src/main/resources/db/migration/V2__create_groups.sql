CREATE TABLE groups (
    id UUID NOT NULL,
    admin_user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    image_url TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT groups_pkey PRIMARY KEY (id),
    CONSTRAINT groups_admin_user_fk
        FOREIGN KEY (admin_user_id) REFERENCES users(id)
);

CREATE TABLE group_members (
    group_id UUID NOT NULL,
    user_id BIGINT NOT NULL,
    joined_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT group_members_pkey PRIMARY KEY (group_id, user_id),
    CONSTRAINT group_members_group_fk
        FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
    CONSTRAINT group_members_user_fk
        FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT group_members_user_unique UNIQUE (user_id)
);

CREATE TABLE group_invitations (
    id UUID NOT NULL,
    group_id UUID NOT NULL,
    inviter_user_id BIGINT NOT NULL,
    invitee_user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT group_invitations_pkey PRIMARY KEY (id),
    CONSTRAINT group_invitations_group_fk
        FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
    CONSTRAINT group_invitations_inviter_fk
        FOREIGN KEY (inviter_user_id) REFERENCES users(id),
    CONSTRAINT group_invitations_invitee_fk
        FOREIGN KEY (invitee_user_id) REFERENCES users(id),
    CONSTRAINT group_invitations_status_check CHECK (
        status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'EXPIRED', 'CANCELLED')
    ),
    CONSTRAINT group_invitations_different_users_check CHECK (
        inviter_user_id <> invitee_user_id
    )
);

CREATE INDEX groups_admin_user_idx ON groups (admin_user_id);
CREATE INDEX group_members_group_idx ON group_members (group_id);
CREATE INDEX group_invitations_group_idx ON group_invitations (group_id);
CREATE INDEX group_invitations_invitee_idx
    ON group_invitations (invitee_user_id, status);
