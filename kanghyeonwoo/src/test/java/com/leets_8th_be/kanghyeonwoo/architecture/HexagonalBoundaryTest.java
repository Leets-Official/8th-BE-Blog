package com.leets_8th_be.kanghyeonwoo.architecture;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

class HexagonalBoundaryTest {
    private static final String ROOT = "com.leets_8th_be.kanghyeonwoo";

    @Test
    void userAndBlogCoreDoNotDependOnFrameworkOrAdapters() {
        ArchRule rule = classes()
                .that().resideInAnyPackage(
                        ROOT + ".user.domain..", ROOT + ".user.application..",
                        ROOT + ".blog.domain..", ROOT + ".blog.application..")
                .should().onlyDependOnClassesThat().resideOutsideOfPackages(
                        "org.springframework..", "jakarta.persistence..",
                        ROOT + ".user.adapter..", ROOT + ".user.config..",
                        ROOT + ".blog.adapter..", ROOT + ".blog.config..",
                        ROOT + ".controller..", ROOT + ".dto..", ROOT + ".entity..",
                        ROOT + ".repository..");

        rule.check(applicationClasses());
    }

    @Test
    void featuresDoNotDependOnOtherFeatureAdapters() {
        ArchRule blogRule = classes()
                .that().resideInAnyPackage(ROOT + ".blog..")
                .should().onlyDependOnClassesThat().resideOutsideOfPackages(ROOT + ".user.adapter..");
        ArchRule userRule = classes()
                .that().resideInAnyPackage(ROOT + ".user..")
                .should().onlyDependOnClassesThat().resideOutsideOfPackages(ROOT + ".blog.adapter..");

        blogRule.check(applicationClasses());
        userRule.check(applicationClasses());
    }

    @Test
    void migratedAdaptersDoNotDependOnLegacyPersistencePackages() {
        ArchRule rule = classes()
                .that().resideInAnyPackage(ROOT + ".blog.adapter..", ROOT + ".user.adapter..")
                .should().onlyDependOnClassesThat().resideOutsideOfPackages(
                        ROOT + ".entity..", ROOT + ".repository..");

        rule.check(applicationClasses());
    }

    private static com.tngtech.archunit.core.domain.JavaClasses applicationClasses() {
        return new ClassFileImporter().importPath("build/classes/java/main");
    }
}
