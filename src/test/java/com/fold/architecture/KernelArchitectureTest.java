package com.fold.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class KernelArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .importPackages("com.fold");
    }

    @Test
    void kernelMustNotDependOnSpring() {
        ArchRule rule =
                noClasses()
                        .that()
                        .resideInAPackage(
                                "com.fold.kernel.."
                        )
                        .should()
                        .dependOnClassesThat()
                        .resideInAnyPackage(
                                "org.springframework.."
                        );

        rule.check(classes);
    }

    @Test
    void kernelMustNotDependOnPlatform() {
        ArchRule rule =
                noClasses()
                        .that()
                        .resideInAPackage(
                                "com.fold.kernel.."
                        )
                        .should()
                        .dependOnClassesThat()
                        .resideInAnyPackage(
                                "com.fold.platform.."
                        );

        rule.check(classes);
    }

    @Test
    void kernelMustNotDependOnModules() {
        ArchRule rule =
                noClasses()
                        .that()
                        .resideInAPackage(
                                "com.fold.kernel.."
                        )
                        .should()
                        .dependOnClassesThat()
                        .resideInAnyPackage(
                                "com.fold.modules.."
                        );

        rule.check(classes);
    }

    @Test
    void kernelMustNotDependOnComposition() {
        ArchRule rule =
                noClasses()
                        .that()
                        .resideInAPackage(
                                "com.fold.kernel.."
                        )
                        .should()
                        .dependOnClassesThat()
                        .resideInAnyPackage(
                                "com.fold.composition.."
                        );

        rule.check(classes);
    }

    @Test
    void kernelMustNotDependOnPersistenceLibraries() {
        ArchRule rule =
                noClasses()
                        .that()
                        .resideInAPackage(
                                "com.fold.kernel.."
                        )
                        .should()
                        .dependOnClassesThat()
                        .resideInAnyPackage(
                                "jakarta.persistence..",
                                "org.hibernate..",
                                "org.springframework.data.."
                        );

        rule.check(classes);
    }

    @Test
    void kernelMustNotDependOnMessagingLibraries() {
        ArchRule rule =
                noClasses()
                        .that()
                        .resideInAPackage(
                                "com.fold.kernel.."
                        )
                        .should()
                        .dependOnClassesThat()
                        .resideInAnyPackage(
                                "org.apache.kafka..",
                                "org.springframework.kafka.."
                        );

        rule.check(classes);
    }
}