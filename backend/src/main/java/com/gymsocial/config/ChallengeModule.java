package com.gymsocial.config;

import com.gymsocial.challenge.ChallengeController;
import com.gymsocial.challenge.ChallengeRepository;
import com.gymsocial.challenge.ChallengeService;
import com.gymsocial.shared.storage.ImageStorage;
import com.gymsocial.shared.validation.RequestValidator;

import javax.sql.DataSource;

public final class ChallengeModule {

    private ChallengeModule() {
    }

    public static ChallengeController create(
        DataSource dataSource,
        ImageStorage imageStorage
    ) {
        return new ChallengeController(new ChallengeService(
            new ChallengeRepository(dataSource),
            new RequestValidator(),
            imageStorage
        ));
    }
}
