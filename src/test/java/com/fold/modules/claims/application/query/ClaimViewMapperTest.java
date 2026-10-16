package com.fold.modules.claims.application.query;

import com.fold.modules.claims.api.view.ClaimObjectView;
import com.fold.modules.claims.api.view.ClaimView;
import com.fold.modules.claims.domain.model.Claim;
import com.fold.testsupport.claims.ClaimTestFixtures;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClaimViewMapperTest {

    @Test
    void shouldExposeCompleteAuthoritativeClaimState() {
        Claim claim =
                ClaimTestFixtures.claim();

        ClaimView view =
                ClaimViewMapper.map(
                        claim
                );

        assertThat(view.id())
                .isEqualTo(
                        claim.id()
                );

        assertThat(view.subject().namespace())
                .isEqualTo(
                        claim.proposition()
                                .subject()
                                .reference()
                                .namespace()
                );

        assertThat(view.predicate())
                .isEqualTo(
                        claim.proposition()
                                .predicate()
                                .value()
                );

        assertThat(view.object())
                .isInstanceOf(
                        ClaimObjectView.Text.class
                );

        assertThat(view.origin())
                .hasSize(
                        claim.origin()
                                .size()
                );

        assertThat(view.derivation().kind())
                .isEqualTo(
                        claim.derivation()
                                .kind()
                                .name()
                );

        assertThat(view.confidence())
                .isEqualTo(
                        claim.confidence()
                                .orElseThrow()
                                .decimal()
                );

        assertThat(view.validTime())
                .isNotNull();

        assertThat(view.lifecycle())
                .hasSize(1);
    }
}