package com.leets_8th_be.kanghyeonwoo.auth.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

class AuthBoundaryTest {
    private static final String ROOT = "com.leets_8th_be.kanghyeonwoo";

    @Test
    void newCoreContractsDoNotDependOnWebFrameworksOrAdapters() {
        var imported = new ClassFileImporter().importPath("build/classes/java/main");
        classes()
                .that()
                .resideInAnyPackage(
                        ROOT + ".auth.domain..", ROOT + ".auth.application.port..", ROOT + ".auth.application.model..")
                .should()
                .onlyDependOnClassesThat()
                .resideInAnyPackage(
                        "java..",
                        ROOT + ".auth.domain..",
                        ROOT + ".auth.application.port..",
                        ROOT + ".auth.application.model..")
                .check(imported);
    }
}
