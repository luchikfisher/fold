package com.fold.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class ObservationsArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(
                        ImportOption.Predefined.DO_NOT_INCLUDE_TESTS
                )
                .importPackages(
                        "com.fold.modules.observations"
                );
    }

    @Test
    void observationsDomainMustNotDependOnSpring() {
        noClasses()
                .that()
                .resideInAPackage(
                        "com.fold.modules.observations.domain.."
                )
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "org.springframework.."
                )
                .check(classes);
    }

    @Test
    void observationsDomainMustNotDependOnInfrastructure() {
        noClasses()
                .that()
                .resideInAPackage(
                        "com.fold.modules.observations.domain.."
                )
                .should()
                .dependOnClassesThat()
                .resideInAPackage(
                        "com.fold.modules.observations.infrastructure.."
                )
                .check(classes);
    }

    @Test
    void observationsDomainMustNotDependOnApplicationLayer() {
        noClasses()
                .that()
                .resideInAPackage(
                        "com.fold.modules.observations.domain.."
                )
                .should()
                .dependOnClassesThat()
                .resideInAPackage(
                        "com.fold.modules.observations.application.."
                )
                .check(classes);
    }

    @Test
    void observationsDomainMustNotDependOnModuleApi() {
        noClasses()
                .that()
                .resideInAPackage(
                        "com.fold.modules.observations.domain.."
                )
                .should()
                .dependOnClassesThat()
                .resideInAPackage(
                        "com.fold.modules.observations.api.."
                )
                .check(classes);
    }

    @Test
    void observationsDomainRepositoryMustRemainInfrastructureIndependent() {
        noClasses()
                .that()
                .resideInAPackage(
                        "com.fold.modules.observations.domain.repository.."
                )
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "org.springframework..",
                        "jakarta.persistence..",
                        "org.hibernate..",
                        "org.springframework.data.."
                )
                .check(classes);
    }

    @Test
    void observationsDomainMustNotDependOnJackson() {
        noClasses()
                .that()
                .resideInAPackage(
                        "com.fold.modules.observations.domain.."
                )
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "com.fasterxml.jackson..",
                        "tools.jackson.."
                )
                .check(classes);
    }

    @Test
    void observationsApplicationMustNotDependOnInfrastructure() {
        noClasses()
                .that()
                .resideInAPackage(
                        "com.fold.modules.observations.application.."
                )
                .should()
                .dependOnClassesThat()
                .resideInAPackage(
                        "com.fold.modules.observations.infrastructure.."
                )
                .check(classes);
    }

    @Test
    void observationsApplicationMustNotDependOnSpring() {
        noClasses()
                .that()
                .resideInAPackage(
                        "com.fold.modules.observations.application.."
                )
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "org.springframework.."
                )
                .check(classes);
    }

    @Test
    void observationsApiEventsMustNotDependOnDomainInternals() {
        noClasses()
                .that()
                .resideInAPackage(
                        "com.fold.modules.observations.api.event.."
                )
                .should()
                .dependOnClassesThat()
                .resideInAPackage(
                        "com.fold.modules.observations.domain.."
                )
                .check(classes);
    }

    @Test
    void domainMustNotDependOnJdbc() {
        noClasses()
                .that()
                .resideInAPackage(
                        "com.fold.modules.observations.domain.."
                )
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "org.springframework.jdbc..",
                        "java.sql.."
                )
                .check(classes);
    }

    @Test
    void applicationMustNotDependOnJdbc() {
        noClasses()
                .that()
                .resideInAPackage(
                        "com.fold.modules.observations.application.."
                )
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "org.springframework.jdbc..",
                        "java.sql.."
                )
                .check(classes);
    }

    @Test
    void applicationMustNotDependOnJackson() {
        noClasses()
                .that()
                .resideInAPackage(
                        "com.fold.modules.observations.application.."
                )
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "com.fasterxml.jackson..",
                        "tools.jackson.."
                )
                .check(classes);
    }
}
