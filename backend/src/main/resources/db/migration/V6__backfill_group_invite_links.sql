INSERT INTO group_invite_links (
    token,
    group_id,
    created_by_user_id,
    created_at
)
SELECT
    gen_random_uuid(),
    g.id,
    g.admin_user_id,
    g.created_at
FROM groups g
WHERE NOT EXISTS (
    SELECT 1
    FROM group_invite_links l
    WHERE l.group_id = g.id
);
