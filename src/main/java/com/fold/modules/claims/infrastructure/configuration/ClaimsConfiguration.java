package com.fold.modules.claims.infrastructure.configuration;

import com.fold.kernel.events.EventPublisher;
import com.fold.kernel.time.FoldClock;
import com.fold.modules.claims.application.feature.deriveclaims.ClaimDerivationEngine;
import com.fold.modules.claims.application.feature.deriveclaims.ProcessAcceptedObservationHandler;
import com.fold.modules.claims.application.feature.lifecycle.ClaimLifecycleHandler;
import com.fold.modules.claims.application.idempotency.ClaimProcessingRepository;
import com.fold.modules.claims.application.integration.observations.AcceptedObservationClaimInputProvider;
import com.fold.modules.claims.application.integration.observations.ObservationClaimDerivationInputMapper;
import com.fold.modules.claims.application.query.ClaimDiscoveryReader;
import com.fold.modules.claims.application.query.DefaultClaimQueries;
import com.fold.modules.claims.application.query.GetClaimHandler;
import com.fold.modules.claims.domain.derivation.ClaimDerivationRule;
import com.fold.modules.claims.domain.repository.ClaimRepository;
import com.fold.modules.claims.infrastructure.persistence.mapper.ClaimObjectJsonCodec;
import com.fold.modules.claims.infrastructure.persistence.mapper.ClaimPersistenceMapper;
import com.fold.modules.observations.api.query.ObservationQueries;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class ClaimsConfiguration {

    @Bean
    ClaimLifecycleHandler claimLifecycleHandler(
            ClaimRepository repository,
            FoldClock clock,
            EventPublisher publisher
    ) {
        return new ClaimLifecycleHandler(repository, clock, publisher);
    }

    @Bean
    ClaimDerivationEngine claimDerivationEngine(
            ObjectProvider<ClaimDerivationRule> rules
    ) {
        return new ClaimDerivationEngine(
                rules.orderedStream()
                        .toList()
        );
    }

    @Bean
    ObservationClaimDerivationInputMapper
    observationClaimDerivationInputMapper() {
        return new ObservationClaimDerivationInputMapper();
    }

    @Bean
    AcceptedObservationClaimInputProvider
    acceptedObservationClaimInputProvider(
            ObservationQueries observationQueries,
            ObservationClaimDerivationInputMapper mapper
    ) {
        return new AcceptedObservationClaimInputProvider(
                observationQueries,
                mapper
        );
    }

    @Bean
    ProcessAcceptedObservationHandler
    processAcceptedObservationHandler(
            AcceptedObservationClaimInputProvider inputProvider,
            ClaimDerivationEngine derivationEngine,
            ClaimProcessingRepository processingRepository,
            ClaimRepository claimRepository,
            FoldClock clock,
            EventPublisher eventPublisher
    ) {
        return new ProcessAcceptedObservationHandler(
                inputProvider,
                derivationEngine,
                processingRepository,
                claimRepository,
                clock,
                eventPublisher
        );
    }

    @Bean
    ClaimObjectJsonCodec claimObjectJsonCodec(
            ObjectMapper objectMapper
    ) {
        return new ClaimObjectJsonCodec(
                objectMapper
        );
    }

    @Bean
    ClaimPersistenceMapper claimPersistenceMapper(
            ClaimObjectJsonCodec codec
    ) {
        return new ClaimPersistenceMapper(
                codec
        );
    }

    @Bean
    DefaultClaimQueries claimQueries(GetClaimHandler details, ClaimDiscoveryReader discovery) {
        return new DefaultClaimQueries(details, discovery);
    }

    @Bean
    GetClaimHandler getClaimHandler(
            ClaimRepository repository
    ) {
        return new GetClaimHandler(
                repository
        );
    }
}
