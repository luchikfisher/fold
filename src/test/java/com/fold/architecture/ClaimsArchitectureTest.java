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
        classes = new ClassFileImporter()
                .withImportOption(
                        ImportOption.Predefined.DO_NOT_INCLUDE_TESTS
                )
                .importPackages("com.fold");
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
    void claimsDomainMustNotDependOnSpring() {
        noClasses()
                .that()
                .resideInAPackage(
                        "com.fold.modules.claims.domain.."
                )
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "org.springframework.."
                )
                .check(classes);
    }

    @Test
    void claimsDomainMustNotDependOnJackson() {
        noClasses()
                .that()
                .resideInAPackage(
                        "com.fold.modules.claims.domain.."
                )
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "tools.jackson..",
                        "com.fasterxml.jackson.."
                )
                .check(classes);
    }

    @Test
    void claimsDomainMustNotDependOnPersistenceLibraries() {
        noClasses()
                .that()
                .resideInAPackage(
                        "com.fold.modules.claims.domain.."
                )
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "jakarta.persistence..",
                        "org.hibernate..",
                        "org.springframework.data.."
                )
                .check(classes);
    }

    @Test
    void claimsDomainMustNotDependOnItsFutureInfrastructure() {
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
    void claimsDomainMustNotDependOnItsFutureApplicationLayer() {
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
    void claimsDomainMustNotDependOnItsFuturePublicApi() {
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
}