package in.lekhai.gsp.ewb.domain.model;

import java.time.Instant;

public record ExtendValidity(
        String ewbNo,
        Instant updateAt,
        Instant newValidUpTo
) { }
