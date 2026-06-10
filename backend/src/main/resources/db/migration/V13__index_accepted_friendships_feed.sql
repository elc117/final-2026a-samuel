CREATE INDEX friendships_requester_accepted_feed_idx
    ON friendships (requester_user_id, updated_at DESC, id DESC)
    WHERE status = 'ACCEPTED';

CREATE INDEX friendships_receiver_accepted_feed_idx
    ON friendships (receiver_user_id, updated_at DESC, id DESC)
    WHERE status = 'ACCEPTED';
