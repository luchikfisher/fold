package com.fold.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ClaimsArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importProductionClasses() {
        classes =
                new ClassFileImporter()
                        .withImportOption(
                                ImportOption.Predefined.DO_NOT_INCLUDE_TESTS
                        )
                        .importPackages(
                                "com.fold"
                        );
    }

    @Test
    void claimsDomainMustNotDependOnObservations() {
        noClasses()
                .that()
                .resideInAPackage(
                        "com.fold.modules.claims.domain.."
                )
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "com.fold.modules.observations.."
                )
                .check(classes);
    }

    @Test
    void claimsDomainMustNotDependOnApplication() {
        noClasses()
                .that()
                .resideInAPackage(
                        "com.fold.modules.claims.domain.."
                )
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "com.fold.modules.claims.application.."
                )
                .check(classes);
    }

    @Test
    void claimsDomainMustNotDependOnInfrastructure() {
        noClasses()
                .that()
                .resideInAPackage(
                        "com.fold.modules.claims.domain.."
                )
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "com.fold.modules.claims.infrastructure.."
                )
                .check(classes);
    }

    @Test
    void claimsDomainMustNotDependOnPublicApi() {
        noClasses()
                .that()
                .resideInAPackage(
                        "com.fold.modules.claims.domain.."
                )
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "com.fold.modules.claims.api.."
                )
                .check(classes);
    }

    @Test
    void claimsDomainMustRemainFrameworkFree() {
        noClasses()
                .that()
                .resideInAPackage(
                        "com.fold.modules.claims.domain.."
                )
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "org.springframework..",
                        "jakarta.persistence..",
                        "org.hibernate..",
                        "tools.jackson..",
                        "com.fasterxml.jackson.."
                )
                .check(classes);
    }

    @Test
    void claimsApplicationMustNotDependOnInfrastructure() {
        noClasses()
                .that()
                .resideInAPackage(
                        "com.fold.modules.claims.application.."
                )
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "com.fold.modules.claims.infrastructure.."
                )
                .check(classes);
    }

    @Test
    void claimsApplicationMustRemainFrameworkFree() {
        noClasses()
                .that()
                .resideInAPackage(
                        "com.fold.modules.claims.application.."
                )
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "org.springframework..",
                        "jakarta.persistence..",
                        "org.hibernate..",
                        "tools.jackson..",
                        "com.fasterxml.jackson.."
                )
                .check(classes);
    }

    @Test
    void claimsApplicationMustNotDependOnObservationInternals() {
        noClasses()
                .that()
                .resideInAPackage(
                        "com.fold.modules.claims.application.."
                )
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "com.fold.modules.observations.domain..",
                        "com.fold.modules.observations.application..",
                        "com.fold.modules.observations.infrastructure.."
                )
                .check(classes);
    }

    @Test
    void claimsPublicApiMustNotDependOnClaimsInternals() {
        noClasses()
                .that()
                .resideInAPackage(
                        "com.fold.modules.claims.api.."
                )
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "com.fold.modules.claims.domain..",
                        "com.fold.modules.claims.application..",
                        "com.fold.modules.claims.infrastructure.."
                )
                .check(classes);
    }
}