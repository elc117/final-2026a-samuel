package com.gymsocial.challenge;

import com.gymsocial.challenge.dto.ChallengeRankingResponse;
import com.gymsocial.challenge.dto.ChallengeResponse;
import com.gymsocial.challenge.dto.CreateChallengeRequest;
import com.gymsocial.shared.exception.ConflictException;
import com.gymsocial.shared.exception.ForbiddenException;
import com.gymsocial.shared.exception.NotFoundException;
import com.gymsocial.shared.exception.ValidationException;
import com.gymsocial.shared.storage.ImageStorage;
import com.gymsocial.shared.validation.RequestValidator;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class ChallengeService {

    private static final Set<String> ALLOWED_PERIODS = Set.of(
        "WEEKLY",
        "QUARTERLY",
        "SEMIANNUAL",
        "ANNUAL",
        "CUSTOM"
    );

    private final ChallengeRepository repository;
    private final RequestValidator requestValidator;
    private final ImageStorage imageStorage;

    public ChallengeService(
        ChallengeRepository repository,
        RequestValidator requestValidator,
        ImageStorage imageStorage
    ) {
        this.repository = repository;
        this.requestValidator = requestValidator;
        this.imageStorage = imageStorage;
    }

    public Optional<ChallengeResponse> findCurrent(long userId) {
        var group = findGroup(userId);
        return repository.findActive(group.groupId()).map(this::toResponse);
    }

    public ChallengeResponse create(
        long userId,
        CreateChallengeRequest request
    ) {
        requestValidator.validate(request);
        var group = findGroup(userId);
        requireAdministrator(group, userId);

        String period = request.period()
            .trim()
            .toUpperCase(Locale.ROOT);
        if (!ALLOWED_PERIODS.contains(period)) {
            throw new ValidationException(Map.of(
                "period",
                "Selecione um período válido."
            ));
        }

        LocalDate startsAt = LocalDate.now();
        LocalDate endsAt = calculateEndsAt(
            period,
            startsAt,
            request.endsAt()
        );
        if (endsAt.isBefore(startsAt)) {
            throw new ValidationException(Map.of(
                "endsAt",
                "A data final não pode estar no passado."
            ));
        }

        if (repository.findActive(group.groupId()).isPresent()) {
            throw new ConflictException(
                "Encerre o desafio atual antes de criar outro."
            );
        }

        var challenge = new Challenge(
            UUID.randomUUID(),
            group.groupId(),
            userId,
            request.title().trim(),
            normalizeDescription(request.description()),
            period,
            request.allowMultipleCheckInsPerDay(),
            startsAt,
            endsAt,
            "ACTIVE",
            Instant.now()
        );

        return toResponse(repository.create(challenge));
    }

    public void endCurrent(long userId) {
        var group = findGroup(userId);
        requireAdministrator(group, userId);

        if (!repository.endActive(group.groupId())) {
            throw new NotFoundException("Não há desafio ativo para encerrar.");
        }
    }

    private ChallengeRepository.GroupAccess findGroup(long userId) {
        return repository.findGroupByMember(userId)
            .orElseThrow(() -> new ForbiddenException(
                "Você precisa participar de um grupo."
            ));
    }

    private void requireAdministrator(
        ChallengeRepository.GroupAccess group,
        long userId
    ) {
        if (group.adminUserId() != userId) {
            throw new ForbiddenException(
                "Somente o administrador pode gerenciar desafios."
            );
        }
    }

    private ChallengeResponse toResponse(Challenge challenge) {
        List<ChallengeRankingResponse> ranking = repository
            .findRanking(challenge)
            .stream()
            .map(entry -> new ChallengeRankingResponse(
                entry.userId(),
                entry.name(),
                entry.profileImageUrl() == null
                    ? null
                    : imageStorage.createReadUrl(entry.profileImageUrl()),
                entry.score()
            ))
            .toList();

        return ChallengeResponse.from(challenge, ranking);
    }

    private String normalizeDescription(String description) {
        return description == null || description.isBlank()
            ? null
            : description.trim();
    }

    private LocalDate calculateEndsAt(
        String period,
        LocalDate startsAt,
        LocalDate customEndsAt
    ) {
        return switch (period) {
            case "WEEKLY" -> startsAt.with(
                TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)
            );
            case "QUARTERLY" -> startsAt.plusMonths(3).minusDays(1);
            case "SEMIANNUAL" -> startsAt.plusMonths(6).minusDays(1);
            case "ANNUAL" -> startsAt.plusYears(1).minusDays(1);
            case "CUSTOM" -> {
                if (customEndsAt == null) {
                    throw new ValidationException(Map.of(
                        "endsAt",
                        "Informe a data final personalizada."
                    ));
                }
                yield customEndsAt;
            }
            default -> throw new ValidationException(Map.of(
                "period",
                "Selecione um período válido."
            ));
        };
    }
}
