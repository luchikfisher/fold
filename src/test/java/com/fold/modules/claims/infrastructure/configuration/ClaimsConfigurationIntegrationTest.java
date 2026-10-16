package com.fold.modules.claims.infrastructure.configuration;

import com.fold.modules.claims.api.query.ClaimQueries;
import com.fold.modules.claims.application.feature.deriveclaims.ClaimDerivationEngine;
import com.fold.modules.claims.application.feature.deriveclaims.ProcessAcceptedObservationHandler;
import com.fold.modules.claims.application.feature.deriveclaims.ProcessAcceptedObservationUseCase;
import com.fold.modules.claims.application.integration.observations.AcceptedObservationClaimInputProvider;
import com.fold.modules.claims.application.integration.observations.ObservationClaimDerivationInputMapper;
import com.fold.modules.claims.application.query.GetClaimHandler;
import com.fold.modules.claims.domain.repository.ClaimRepository;
import com.fold.modules.claims.infrastructure.persistence.mapper.ClaimObjectJsonCodec;
import com.fold.modules.claims.infrastructure.persistence.mapper.ClaimPersistenceMapper;
import com.fold.modules.claims.infrastructure.transaction.TransactionalProcessAcceptedObservationUseCase;
import com.fold.testsupport.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ClaimsConfigurationIntegrationTest
        extends PostgresIntegrationTest {

    @Autowired
    ClaimDerivationEngine derivationEngine;

    @Autowired
    AcceptedObservationClaimInputProvider inputProvider;

    @Autowired
    ObservationClaimDerivationInputMapper observationMapper;

    @Autowired
    ProcessAcceptedObservationHandler handler;

    @Autowired
    ProcessAcceptedObservationUseCase useCase;

    @Autowired
    ClaimRepository repository;

    @Autowired
    ClaimQueries queries;

    @Autowired
    ClaimObjectJsonCodec objectCodec;

    @Autowired
    ClaimPersistenceMapper persistenceMapper;

    @Test
    void claimsSliceShouldWireCompleteProductionGraph() {
        assertThat(derivationEngine)
                .isNotNull();

        assertThat(inputProvider)
                .isNotNull();

        assertThat(observationMapper)
                .isNotNull();

        assertThat(handler)
                .isNotNull();

        assertThat(repository)
                .isNotNull();

        assertThat(queries)
                .isNotNull();

        assertThat(objectCodec)
                .isNotNull();

        assertThat(persistenceMapper)
                .isNotNull();
    }

    @Test
    void authoritativeUseCaseShouldBeTransactionalDecorator() {
        assertThat(useCase)
                .isInstanceOf(
                        TransactionalProcessAcceptedObservationUseCase.class
                );

        assertThat(useCase)
                .isNotSameAs(handler);
    }

    @Test
    void publicClaimQueriesShouldResolveToGetClaimHandler() {
        assertThat(queries)
                .isInstanceOf(
                        GetClaimHandler.class
                );
    }
}