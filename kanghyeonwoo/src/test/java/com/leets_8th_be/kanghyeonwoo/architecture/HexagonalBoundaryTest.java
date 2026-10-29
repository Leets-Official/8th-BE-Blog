package com.leets_8th_be.kanghyeonwoo.architecture;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static org.assertj.core.api.Assertions.assertThat;

class HexagonalBoundaryTest {
    private static final String ROOT = "com.leets_8th_be.kanghyeonwoo";

    @Test
    void userAndBlogCoreDoNotDependOnFrameworkOrAdapters() {
        ArchRule rule = classes()
                .that().resideInAnyPackage(
                        ROOT + ".user.domain..", ROOT + ".user.application..",
                        ROOT + ".blog.domain..", ROOT + ".blog.application..",
                        ROOT + ".report.domain..", ROOT + ".report.application..",
                        ROOT + ".search.domain..", ROOT + ".search.application..")
                .should().onlyDependOnClassesThat().resideOutsideOfPackages(
                        "org.springframework..", "jakarta.persistence..",
                        ROOT + ".user.adapter..", ROOT + ".user.config..",
                        ROOT + ".blog.adapter..", ROOT + ".blog.config..",
                        ROOT + ".report.adapter..", ROOT + ".report.config..",
                        ROOT + ".search.adapter..", ROOT + ".search.config..",
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
                .should().onlyDependOnClassesThat().resideOutsideOfPackages(ROOT + ".blog.adapter..",
                        ROOT + ".report.adapter..");
        ArchRule blogReportRule = classes()
                .that().resideInAnyPackage(ROOT + ".blog..")
                .should().onlyDependOnClassesThat().resideOutsideOfPackages(ROOT + ".report.adapter..");
        ArchRule reportUserBlogRule = classes()
                .that().resideInAnyPackage(ROOT + ".report..")
                .should().onlyDependOnClassesThat().resideOutsideOfPackages(ROOT + ".user.adapter..",
                        ROOT + ".blog.adapter..");

        blogRule.check(applicationClasses());
        userRule.check(applicationClasses());
        blogReportRule.check(applicationClasses());
        reportUserBlogRule.check(applicationClasses());
    }

    @Test
    void migratedAdaptersDoNotDependOnLegacyPersistencePackages() {
        ArchRule rule = classes()
                .that().resideInAnyPackage(ROOT + ".blog.adapter..", ROOT + ".user.adapter..",
                        ROOT + ".report.adapter..", ROOT + ".search.adapter..", ROOT + ".search.config..")
                .should().onlyDependOnClassesThat().resideOutsideOfPackages(
                        ROOT + ".entity..", ROOT + ".repository..");

        rule.check(applicationClasses());
    }

    @Test
    void domainModelsDoNotDependOnApplicationOrInfrastructure() {
        ArchRule rule = classes().that().resideInAnyPackage(ROOT + ".user.domain..", ROOT + ".blog.domain..",
                        ROOT + ".report.domain..", ROOT + ".search.domain..")
                .should().onlyDependOnClassesThat().resideInAnyPackage("java..", "javax..", "jakarta.validation..",
                        ROOT + ".user.domain..", ROOT + ".blog.domain..", ROOT + ".report.domain..",
                        ROOT + ".search.domain..");
        rule.check(applicationClasses());
    }

    @Test
    void featureAdaptersDoNotDependOnLegacyServicesOrControllers() {
        ArchRule rule = classes().that().resideInAnyPackage(ROOT + ".user.adapter..", ROOT + ".blog.adapter..",
                        ROOT + ".report.adapter..", ROOT + ".search.adapter..")
                .should().onlyDependOnClassesThat().resideOutsideOfPackages(ROOT + ".service..",
                        ROOT + ".controller..", ROOT + ".entity..", ROOT + ".repository..");
        rule.check(applicationClasses());
    }

    @Test
    void legacyPersistencePackagesAreAbsentFromProductionClasses() {
        assertThat(applicationClasses().stream().noneMatch(type ->
                type.getPackageName().startsWith(ROOT + ".entity")
                        || type.getPackageName().startsWith(ROOT + ".repository"))).isTrue();
    }

    @Test
    void featureWebDtosAreOwnedByTheirFeatureAdapters() {
        assertThat(applicationClasses().stream().noneMatch(type ->
                type.getPackageName().equals(ROOT + ".dto"))).isTrue();

        assertThat(applicationClasses().stream().filter(type -> type.getSimpleName().endsWith("Request")
                        || type.getSimpleName().endsWith("Response"))
                .filter(type -> type.getPackageName().startsWith(ROOT + ".blog.")
                        || type.getPackageName().startsWith(ROOT + ".report.")
                        || type.getPackageName().startsWith(ROOT + ".search."))
                .filter(type -> !type.getPackageName().endsWith(".exception"))
                .filter(type -> !type.getPackageName().contains(".adapter.in.web"))
                .map(type -> type.getName()).toList()).isEmpty();
    }

    private static com.tngtech.archunit.core.domain.JavaClasses applicationClasses() {
        return new ClassFileImporter().importPath("build/classes/java/main");
    }
}
