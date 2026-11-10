package com.leets_8th_be.kanghyeonwoo.auth.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

class AuthBoundaryTest {
    private static final String ROOT = "com.leets_8th_be.kanghyeonwoo";

    @Test
    void newCoreContractsDoNotDependOnWebFrameworksOrAdapters() {
        var imported = new ClassFileImporter().importPath("build/classes/java/main");
        classes()
                .that()
                .resideInAnyPackage(ROOT + ".auth.domain..", ROOT + ".auth.application..")
                .should()
                .onlyDependOnClassesThat()
                .resideInAnyPackage("java..", ROOT + ".auth.domain..", ROOT + ".auth.application..")
                .check(imported);
    }

    @Test
    void rootPackageHasNoLegacyImplementations() {
        var imported = new ClassFileImporter().importPath("build/classes/java/main");
        assertThat(imported.stream()
                        .filter(type -> type.getPackageName().equals(ROOT + ".auth"))
                        .map(type -> type.getName())
                        .toList())
                .isEmpty();
    }

    @Test
    void inboundAdaptersUseInputPortsInsteadOfConcreteServicesOrOutboundAdapters() {
        var imported = new ClassFileImporter().importPath("build/classes/java/main");
        classes()
                .that()
                .resideInAnyPackage(ROOT + ".auth.adapter.in..")
                .should()
                .onlyDependOnClassesThat()
                .resideOutsideOfPackages(
                        ROOT + ".auth.application.usecase..",
                        ROOT + ".auth.application.port.out..",
                        ROOT + ".auth.adapter.out..",
                        ROOT + ".auth.adapter.transaction..",
                        ROOT + ".auth.config..")
                .check(imported);
    }
}
