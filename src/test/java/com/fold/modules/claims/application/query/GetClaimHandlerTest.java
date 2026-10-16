package com.fold.modules.claims.application.query;

import com.fold.kernel.ids.ClaimId;
import com.fold.modules.claims.api.query.GetClaimQuery;
import com.fold.modules.claims.api.view.ClaimView;
import com.fold.modules.claims.domain.model.Claim;
import com.fold.modules.claims.domain.repository.ClaimRepository;
import com.fold.testsupport.claims.ClaimTestFixtures;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class GetClaimHandlerTest {

    @Test
    void existingClaimShouldReturnPublicView() {
        Claim claim =
                ClaimTestFixtures.claim();

        ClaimRepository repository =
                new ClaimRepository() {
                    @Override
                    public void add(
                            Claim ignored
                    ) {
                    }

                    @Override
                    public Optional<Claim> findById(
                            ClaimId id
                    ) {
                        return id.equals(
                                claim.id()
                        )
                                ? Optional.of(claim)
                                : Optional.empty();
                    }
                };

        GetClaimHandler handler =
                new GetClaimHandler(
                        repository
                );

        Optional<ClaimView> view =
                handler.get(
                        new GetClaimQuery(
                                claim.id()
                        )
                );

        assertThat(view)
                .isPresent();

        assertThat(
                view.orElseThrow().id()
        ).isEqualTo(
                claim.id()
        );
    }

    @Test
    void unknownClaimShouldReturnEmpty() {
        GetClaimHandler handler =
                new GetClaimHandler(
                        new EmptyRepository()
                );

        assertThat(
                handler.get(
                        new GetClaimQuery(
                                ClaimId.random()
                        )
                )
        ).isEmpty();
    }

    private static final class EmptyRepository
            implements ClaimRepository {

        @Override
        public void add(
                Claim claim
        ) {
        }

        @Override
        public Optional<Claim> findById(
                ClaimId id
        ) {
            return Optional.empty();
        }
    }
}