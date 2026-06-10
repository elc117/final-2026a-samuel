CREATE INDEX check_ins_group_feed_idx
    ON check_ins (group_id, created_at DESC, id DESC);
