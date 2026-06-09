ALTER TABLE challenges
    DROP CONSTRAINT challenges_period_check;

ALTER TABLE challenges
    ADD CONSTRAINT challenges_period_check CHECK (
        period IN (
            'WEEKLY',
            'QUARTERLY',
            'SEMIANNUAL',
            'ANNUAL',
            'CUSTOM'
        )
    );
