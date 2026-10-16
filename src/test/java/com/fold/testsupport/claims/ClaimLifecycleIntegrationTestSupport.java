package com.fold.testsupport.claims;

import com.fold.kernel.events.EventEnvelope;
import com.fold.kernel.events.EventPublisher;
import com.fold.kernel.events.IntegrationEvent;
import com.fold.kernel.time.FoldClock;
import com.fold.kernel.time.Timestamp;
import com.fold.modules.claims.api.query.ClaimQueries;
import com.fold.modules.claims.application.feature.lifecycle.ClaimLifecycleUseCase;
import com.fold.modules.claims.domain.model.Claim;
import com.fold.modules.claims.domain.repository.ClaimRepository;
import com.fold.modules.claims.domain.value.ClaimLifecycleReason;
import com.fold.modules.claims.domain.value.ClaimRevision;
import com.fold.platform.events.JdbcOutboxEventPublisher;
import com.fold.testsupport.PostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.atomic.AtomicBoolean;

/** Real production wiring with a deterministic clock and failure after a real outbox write. */
@SpringBootTest(classes = com.fold.FoldApplication.class)
@Import(ClaimLifecycleIntegrationTestSupport.Configuration.class)
public abstract class ClaimLifecycleIntegrationTestSupport extends PostgresIntegrationTest {

    protected static final Timestamp NOW = Timestamp.parse("2026-11-01T10:00:00.123456789Z");
    protected static final ClaimLifecycleReason REASON = new ClaimLifecycleReason("CORRECTION", "Explicit correction");
    protected static final ClaimRevision INITIAL = ClaimRevision.initial();

    @Autowired protected ClaimLifecycleUseCase useCase;
    @Autowired protected ClaimRepository repository;
    @Autowired protected ClaimQueries queries;
    @Autowired protected JdbcTemplate jdbc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected PlatformTransactionManager transactionManager;
    @Autowired protected FailingPublisher publisher;

    @BeforeEach
    void clearLifecycleData() {
        publisher.failAfterWrite.set(false);
        jdbc.execute("""
                DELETE FROM integration_outbox;
                DELETE FROM claim_processing;
                DELETE FROM claim_lifecycle_revisions;
                DELETE FROM claim_origin_references;
                DELETE FROM claims;
                """);
    }

    protected Claim persistClaim() {
        Claim claim = ClaimTestFixtures.claim();
        tx().executeWithoutResult(status -> repository.add(claim));
        return claim;
    }

    protected TransactionTemplate tx() {
        return new TransactionTemplate(transactionManager);
    }

    protected long eventCount() {
        return jdbc.queryForObject("SELECT count(*) FROM integration_outbox", Long.class);
    }

    @TestConfiguration
    public static class Configuration {
        @Bean
        @Primary
        FoldClock lifecycleClock() {
            return () -> NOW;
        }

        @Bean
        @Primary
        FailingPublisher lifecyclePublisher(JdbcOutboxEventPublisher delegate) {
            return new FailingPublisher(delegate);
        }
    }

    public static final class FailingPublisher implements EventPublisher {
        public final AtomicBoolean failAfterWrite = new AtomicBoolean();
        private final JdbcOutboxEventPublisher delegate;

        FailingPublisher(JdbcOutboxEventPublisher delegate) {
            this.delegate = delegate;
        }

        @Override
        public void publish(EventEnvelope<? extends IntegrationEvent> envelope) {
            delegate.publish(envelope);
            if (failAfterWrite.getAndSet(false)) {
                throw new IllegalStateException("Failure after durable outbox insert");
            }
        }
    }
}
