package com.fold.modules.observations.infrastructure.configuration;

import com.fold.kernel.events.EventPublisher;
import com.fold.kernel.time.FoldClock;
import com.fold.modules.observations.application.feature.submitobservation.SubmitObservationHandler;
import com.fold.modules.observations.application.query.GetObservationHandler;
import com.fold.modules.observations.domain.policy.*;
import com.fold.modules.observations.domain.repository.ObservationRepository;
import com.fold.modules.observations.infrastructure.persistence.mapper.ObservationPayloadJsonCodec;
import com.fold.modules.observations.infrastructure.persistence.mapper.ObservationPersistenceMapper;
import com.fold.modules.observations.infrastructure.web.mapper.ObservationHttpMapper;
import com.fold.platform.events.OutboxEventSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class ObservationsConfiguration {

    @Bean
    ObservationFingerprintPolicy observationFingerprintPolicy() {
        return new Sha256ObservationFingerprintPolicy();
    }

    @Bean
    ObservationAcceptancePolicy observationAcceptancePolicy() {
        return new DefaultObservationAcceptancePolicy();
    }

    @Bean
    ObservationDeduplicationPolicy observationDeduplicationPolicy(
            ObservationRepository repository
    ) {
        return new ObservationDeduplicationPolicy(
                repository
        );
    }

    @Bean
    SubmitObservationHandler submitObservationHandler(
            ObservationFingerprintPolicy fingerprintPolicy,
            ObservationDeduplicationPolicy deduplicationPolicy,
            ObservationAcceptancePolicy acceptancePolicy,
            ObservationRepository repository,
            FoldClock clock,
            EventPublisher eventPublisher
    ) {
        return new SubmitObservationHandler(
                fingerprintPolicy,
                deduplicationPolicy,
                acceptancePolicy,
                repository,
                clock,
                eventPublisher
        );
    }

    @Bean
    GetObservationHandler getObservationHandler(
            ObservationRepository repository
    ) {
        return new GetObservationHandler(
                repository
        );
    }

    @Bean
    ObservationPayloadJsonCodec observationPayloadJsonCodec(
            ObjectMapper objectMapper
    ) {
        return new ObservationPayloadJsonCodec(
                objectMapper
        );
    }

    @Bean
    ObservationPersistenceMapper observationPersistenceMapper(
            ObservationPayloadJsonCodec codec
    ) {
        return new ObservationPersistenceMapper(
                codec
        );
    }

    @Bean
    OutboxEventSerializer outboxEventSerializer(
            ObjectMapper objectMapper
    ) {
        return new OutboxEventSerializer(
                objectMapper
        );
    }

    @Bean
    ObservationHttpMapper observationHttpMapper() {
        return new ObservationHttpMapper();
    }
}