package com.leets_8th_be.kanghyeonwoo.report.config;

import com.leets_8th_be.kanghyeonwoo.report.adapter.transaction.TransactionalReportUseCases;
import com.leets_8th_be.kanghyeonwoo.report.application.port.out.ReportStore;
import com.leets_8th_be.kanghyeonwoo.report.application.port.out.ReportTargetPort;
import com.leets_8th_be.kanghyeonwoo.report.application.port.out.ReporterDirectory;
import com.leets_8th_be.kanghyeonwoo.report.application.usecase.GetReportsService;
import com.leets_8th_be.kanghyeonwoo.report.application.usecase.ReportCommentService;
import com.leets_8th_be.kanghyeonwoo.report.application.usecase.ReportPostService;
import com.leets_8th_be.kanghyeonwoo.report.application.usecase.ResolveReportService;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class ReportModuleConfiguration {
    @Bean
    TransactionalReportUseCases reportUseCases(
            ReporterDirectory reporters,
            ReportTargetPort targets,
            ReportStore reports,
            Clock clock,
            PlatformTransactionManager transactionManager) {
        return new TransactionalReportUseCases(
                new ReportPostService(reporters, targets, reports),
                new ReportCommentService(reporters, targets, reports),
                new GetReportsService(reports),
                new TransactionTemplate(transactionManager),
                readTransaction(transactionManager),
                new ResolveReportService(reporters, reports, targets, clock));
    }

    private static TransactionTemplate readTransaction(PlatformTransactionManager transactionManager) {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        transaction.setReadOnly(true);
        return transaction;
    }
}
