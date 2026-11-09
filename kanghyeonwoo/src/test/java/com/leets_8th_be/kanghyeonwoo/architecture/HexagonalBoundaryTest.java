package com.leets_8th_be.kanghyeonwoo.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static org.assertj.core.api.Assertions.assertThat;

import com.leets_8th_be.kanghyeonwoo.blog.application.model.CommentView;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.Comment;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

class HexagonalBoundaryTest {
    private static final String ROOT = "com.leets_8th_be.kanghyeonwoo";

    @Test
    void userAndBlogCoreDoNotDependOnFrameworkOrAdapters() {
        ArchRule rule = classes()
                .that()
                .resideInAnyPackage(
                        ROOT + ".user.domain..", ROOT + ".user.application..",
                        ROOT + ".blog.domain..", ROOT + ".blog.application..",
                        ROOT + ".report.domain..", ROOT + ".report.application..",
                        ROOT + ".search.domain..", ROOT + ".search.application..")
                .should()
                .onlyDependOnClassesThat()
                .resideOutsideOfPackages(
                        "org.springframework..",
                        "jakarta.persistence..",
                        ROOT + ".user.adapter..",
                        ROOT + ".user.config..",
                        ROOT + ".blog.adapter..",
                        ROOT + ".blog.config..",
                        ROOT + ".report.adapter..",
                        ROOT + ".report.config..",
                        ROOT + ".search.adapter..",
                        ROOT + ".search.config..",
                        ROOT + ".controller..",
                        ROOT + ".dto..",
                        ROOT + ".entity..",
                        ROOT + ".repository..");

        rule.check(applicationClasses());
    }

    @Test
    void featuresDoNotDependOnOtherFeatureAdapters() {
        ArchRule blogRule = classes()
                .that()
                .resideInAnyPackage(ROOT + ".blog..")
                .should()
                .onlyDependOnClassesThat()
                .resideOutsideOfPackages(ROOT + ".user.adapter..");
        ArchRule userRule = classes()
                .that()
                .resideInAnyPackage(ROOT + ".user..")
                .should()
                .onlyDependOnClassesThat()
                .resideOutsideOfPackages(ROOT + ".blog.adapter..", ROOT + ".report.adapter..");
        ArchRule blogReportRule = classes()
                .that()
                .resideInAnyPackage(ROOT + ".blog..")
                .should()
                .onlyDependOnClassesThat()
                .resideOutsideOfPackages(ROOT + ".report.adapter..");
        ArchRule reportUserBlogRule = classes()
                .that()
                .resideInAnyPackage(ROOT + ".report..")
                .should()
                .onlyDependOnClassesThat()
                .resideOutsideOfPackages(ROOT + ".user.adapter..", ROOT + ".blog.adapter..");

        blogRule.check(applicationClasses());
        userRule.check(applicationClasses());
        blogReportRule.check(applicationClasses());
        reportUserBlogRule.check(applicationClasses());
    }

    @Test
    void featureCoresDoNotDependOnOtherFeatureCores() {
        ArchRule blogRule = classes()
                .that()
                .resideInAnyPackage(ROOT + ".blog.domain..", ROOT + ".blog.application..")
                .should()
                .onlyDependOnClassesThat()
                .resideOutsideOfPackages(ROOT + ".user..", ROOT + ".report..", ROOT + ".search..");
        ArchRule userRule = classes()
                .that()
                .resideInAnyPackage(ROOT + ".user.domain..", ROOT + ".user.application..")
                .should()
                .onlyDependOnClassesThat()
                .resideOutsideOfPackages(ROOT + ".blog..", ROOT + ".report..", ROOT + ".search..");
        ArchRule reportRule = classes()
                .that()
                .resideInAnyPackage(ROOT + ".report.domain..", ROOT + ".report.application..")
                .should()
                .onlyDependOnClassesThat()
                .resideOutsideOfPackages(ROOT + ".blog..", ROOT + ".user..", ROOT + ".search..");
        ArchRule searchRule = classes()
                .that()
                .resideInAnyPackage(ROOT + ".search.domain..", ROOT + ".search.application..")
                .should()
                .onlyDependOnClassesThat()
                .resideOutsideOfPackages(ROOT + ".blog..", ROOT + ".user..", ROOT + ".report..");

        blogRule.check(applicationClasses());
        userRule.check(applicationClasses());
        reportRule.check(applicationClasses());
        searchRule.check(applicationClasses());
    }

    @Test
    void featureAdaptersUseOnlyOtherModulesPublishedContracts() {
        disallowForeignInternals("blog", "user", "report", "search").check(applicationClasses());
        disallowForeignInternals("user", "blog", "report", "search").check(applicationClasses());
        disallowForeignInternals("report", "blog", "user", "search").check(applicationClasses());
        disallowForeignInternals("search", "blog", "user", "report").check(applicationClasses());
    }

    @Test
    void blogDoesNotDependOnSearchModule() {
        ArchRule rule = classes()
                .that()
                .resideInAnyPackage(ROOT + ".blog..")
                .should()
                .onlyDependOnClassesThat()
                .resideOutsideOfPackages(ROOT + ".search..");
        rule.check(applicationClasses());
    }

    @Test
    void publishedContractsAreImmutableAndDoNotExposeFeatureInternals() {
        var contracts = applicationClasses().stream()
                .filter(type -> type.getPackageName()
                        .matches(ROOT + "\\.(blog|user|report|search)\\.application\\.contract(\\..*)?"))
                .toList();
        assertThat(contracts).isNotEmpty();
        for (var contract : contracts) {
            try {
                Class<?> contractType =
                        Class.forName(contract.getName(), false, getClass().getClassLoader());
                assertThat(contractType.isRecord() || contractType.isEnum())
                        .as(contract.getName())
                        .isTrue();
            } catch (ClassNotFoundException exception) {
                throw new AssertionError("테스트 클래스패스에서 계약을 찾을 수 없습니다: " + contract.getName(), exception);
            }
        }

        String[] applicationInternals = java.util.Arrays.stream(new String[] {"blog", "user", "report", "search"})
                .flatMap(module -> java.util.stream.Stream.of(
                        ROOT + "." + module + ".application.model..",
                        ROOT + "." + module + ".application.usecase..",
                        ROOT + "." + module + ".application.port..",
                        ROOT + "." + module + ".application.exception.."))
                .toArray(String[]::new);
        String[] forbidden = java.util.stream.Stream.concat(
                        java.util.stream.Stream.of(
                                "org.springframework..",
                                "jakarta.persistence..",
                                "jakarta.servlet..",
                                ROOT + ".blog.domain..",
                                ROOT + ".user.domain..",
                                ROOT + ".report.domain..",
                                ROOT + ".search.domain..",
                                ROOT + ".blog.adapter..",
                                ROOT + ".user.adapter..",
                                ROOT + ".report.adapter..",
                                ROOT + ".search.adapter.."),
                        java.util.Arrays.stream(applicationInternals))
                .toArray(String[]::new);
        ArchRule rule = classes()
                .that()
                .resideInAnyPackage(
                        ROOT + ".blog.application.contract..",
                        ROOT + ".search.application.contract..",
                        ROOT + ".user.application.contract..",
                        ROOT + ".report.application.contract..")
                .should()
                .onlyDependOnClassesThat()
                .resideOutsideOfPackages(forbidden);
        rule.check(applicationClasses());
    }

    @Test
    void applicationModelsAreImmutableRecordsWithoutDomainAggregates() {
        var models = applicationClasses().stream()
                .filter(type -> type.getPackageName()
                        .matches(ROOT + "\\.(blog|user|report|search)\\.application\\.model(\\..*)?"))
                .toList();
        assertThat(models).isNotEmpty();
        for (var model : models) {
            try {
                Class<?> modelType =
                        Class.forName(model.getName(), false, getClass().getClassLoader());
                assertThat(modelType.isRecord()).as(model.getName()).isTrue();
                for (var component : modelType.getRecordComponents()) {
                    Class<?> componentType = component.getType();
                    if (componentType
                            .getPackageName()
                            .matches(ROOT + "\\.(blog|user|report|search)\\.domain\\.model")) {
                        assertThat(componentType.isEnum()
                                        || componentType.getSimpleName().endsWith("Id"))
                                .as(model.getName() + "." + component.getName())
                                .isTrue();
                    }
                }
            } catch (ClassNotFoundException exception) {
                throw new AssertionError("테스트 클래스패스에서 모델을 찾을 수 없습니다: " + model.getName(), exception);
            }
        }
    }

    @Test
    void commentUseCaseResultsDoNotExposeDomainAggregates() {
        assertThat(CommentView.class.isRecord()).isTrue();
        assertThat(java.util.Arrays.stream(CommentView.class.getRecordComponents())
                        .noneMatch(component -> component.getType() == Comment.class))
                .isTrue();
    }

    private static ArchRule disallowForeignInternals(String owner, String... otherModules) {
        String[] forbiddenPackages = java.util.Arrays.stream(otherModules)
                .flatMap(module -> java.util.stream.Stream.of(
                        ROOT + "." + module + ".domain..",
                        ROOT + "." + module + ".adapter..",
                        ROOT + "." + module + ".config..",
                        ROOT + "." + module + ".application.usecase..",
                        ROOT + "." + module + ".application.exception..",
                        ROOT + "." + module + ".application.port.out..",
                        ROOT + "." + module + ".application.model.."))
                .toArray(String[]::new);
        return classes()
                .that()
                .resideInAnyPackage(ROOT + "." + owner + ".adapter..", ROOT + "." + owner + ".config..")
                .should()
                .onlyDependOnClassesThat()
                .resideOutsideOfPackages(forbiddenPackages);
    }

    @Test
    void migratedAdaptersDoNotDependOnLegacyPersistencePackages() {
        ArchRule rule = classes()
                .that()
                .resideInAnyPackage(
                        ROOT + ".blog.adapter..",
                        ROOT + ".user.adapter..",
                        ROOT + ".report.adapter..",
                        ROOT + ".search.adapter..",
                        ROOT + ".search.config..")
                .should()
                .onlyDependOnClassesThat()
                .resideOutsideOfPackages(ROOT + ".entity..", ROOT + ".repository..");

        rule.check(applicationClasses());
    }

    @Test
    void domainModelsDoNotDependOnApplicationOrInfrastructure() {
        ArchRule rule = classes()
                .that()
                .resideInAnyPackage(
                        ROOT + ".user.domain..",
                        ROOT + ".blog.domain..",
                        ROOT + ".report.domain..",
                        ROOT + ".search.domain..")
                .should()
                .onlyDependOnClassesThat()
                .resideInAnyPackage(
                        "java..",
                        "javax..",
                        "jakarta.validation..",
                        ROOT + ".user.domain..",
                        ROOT + ".blog.domain..",
                        ROOT + ".report.domain..",
                        ROOT + ".search.domain..",
                        ROOT + ".shared.domain..");
        rule.check(applicationClasses());
    }

    @Test
    void featureAdaptersDoNotDependOnLegacyServicesOrControllers() {
        ArchRule rule = classes()
                .that()
                .resideInAnyPackage(
                        ROOT + ".user.adapter..",
                        ROOT + ".blog.adapter..",
                        ROOT + ".report.adapter..",
                        ROOT + ".search.adapter..")
                .should()
                .onlyDependOnClassesThat()
                .resideOutsideOfPackages(
                        ROOT + ".service..", ROOT + ".controller..", ROOT + ".entity..", ROOT + ".repository..");
        rule.check(applicationClasses());
    }

    @Test
    void legacyPersistencePackagesAreAbsentFromProductionClasses() {
        assertThat(applicationClasses().stream()
                        .noneMatch(type -> type.getPackageName().startsWith(ROOT + ".entity")
                                || type.getPackageName().startsWith(ROOT + ".repository")))
                .isTrue();
    }

    @Test
    void featureWebDtosAreOwnedByTheirFeatureAdapters() {
        assertThat(applicationClasses().stream()
                        .noneMatch(type -> type.getPackageName().startsWith(ROOT + ".dto")))
                .isTrue();

        assertThat(applicationClasses().stream()
                        .filter(type -> type.getSimpleName().endsWith("Request")
                                || type.getSimpleName().endsWith("Response"))
                        .filter(type -> type.getPackageName().startsWith(ROOT + ".blog.")
                                || type.getPackageName().startsWith(ROOT + ".report.")
                                || type.getPackageName().startsWith(ROOT + ".search."))
                        .filter(type -> !type.getPackageName().endsWith(".exception"))
                        .filter(type -> !type.getPackageName().contains(".adapter.in.web"))
                        .map(type -> type.getName())
                        .toList())
                .isEmpty();
    }

    @Test
    void authenticationCoreDoesNotDependOnFrameworksOrConcreteAdapters() {
        classes()
                .that()
                .resideInAnyPackage(ROOT + ".auth.application..")
                .should()
                .onlyDependOnClassesThat()
                .resideOutsideOfPackages(
                        "org.springframework..",
                        "jakarta.persistence..",
                        "jakarta.servlet..",
                        "io.jsonwebtoken..",
                        ROOT + ".user..",
                        ROOT + ".blog..",
                        ROOT + ".report..",
                        ROOT + ".search..")
                .check(applicationClasses());
        classes()
                .that()
                .resideInAnyPackage(ROOT + ".auth.application..")
                .should()
                .onlyDependOnClassesThat()
                .haveNameNotMatching(
                        ROOT.replace(".", "\\.")
                                + "\\.auth\\.(AuthService|KakaoAuthService|JwtTokens|LoginSessions|OAuthTickets|KakaoClient|AuthenticationAdapters|KakaoAdapters)")
                .check(applicationClasses());
    }

    private static com.tngtech.archunit.core.domain.JavaClasses applicationClasses() {
        return new ClassFileImporter().importPath("build/classes/java/main");
    }
}
