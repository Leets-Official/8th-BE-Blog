package com.leets_8th_be.kanghyeonwoo.report.config;

import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.GetPostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.GetCommentTargetUseCase;
import com.leets_8th_be.kanghyeonwoo.report.adapter.transaction.TransactionalReportUseCases;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.ReportPostUseCase;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.ReportCommentUseCase;
import com.leets_8th_be.kanghyeonwoo.report.application.port.out.ReportStore;
import com.leets_8th_be.kanghyeonwoo.report.application.usecase.ReportPostService;
import com.leets_8th_be.kanghyeonwoo.report.application.usecase.ReportCommentService;
import com.leets_8th_be.kanghyeonwoo.user.application.port.in.UserProfileLookup;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class ReportModuleConfiguration {
    @Bean
    TransactionalReportUseCases reportUseCases(UserProfileLookup users, GetPostUseCase posts,
                                                GetCommentTargetUseCase comments, ReportStore reports,
                                                PlatformTransactionManager transactionManager) {
        return new TransactionalReportUseCases(new ReportPostService(users, posts, reports),
                new ReportCommentService(users, comments, reports), new TransactionTemplate(transactionManager));
    }
}
