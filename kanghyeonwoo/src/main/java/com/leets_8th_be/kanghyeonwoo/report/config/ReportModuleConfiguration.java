package com.leets_8th_be.kanghyeonwoo.report.config;

import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.GetPostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.GetCommentTargetUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.InspectCommentTargetUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.InspectPostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.HidePostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.HideCommentUseCase;
import com.leets_8th_be.kanghyeonwoo.report.adapter.transaction.TransactionalReportUseCases;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.ReportPostUseCase;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.ReportCommentUseCase;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.GetReportsUseCase;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.ResolveReportUseCase;
import com.leets_8th_be.kanghyeonwoo.report.application.port.out.ReportStore;
import com.leets_8th_be.kanghyeonwoo.report.application.usecase.ReportPostService;
import com.leets_8th_be.kanghyeonwoo.report.application.usecase.ReportCommentService;
import com.leets_8th_be.kanghyeonwoo.report.application.usecase.GetReportsService;
import com.leets_8th_be.kanghyeonwoo.report.application.usecase.ResolveReportService;
import java.time.Clock;
import com.leets_8th_be.kanghyeonwoo.user.application.port.in.UserProfileLookup;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class ReportModuleConfiguration {
    @Bean
    TransactionalReportUseCases reportUseCases(UserProfileLookup users, GetPostUseCase posts,
                                                InspectPostUseCase inspectPosts, HidePostUseCase hidePosts,
                                                GetCommentTargetUseCase comments,
                                                InspectCommentTargetUseCase inspectComments,
                                                HideCommentUseCase hideComments,
                                                ReportStore reports, Clock clock,
                                                PlatformTransactionManager transactionManager) {
        return new TransactionalReportUseCases(new ReportPostService(users, posts, reports),
                new ReportCommentService(users, comments, reports), new GetReportsService(reports),
                new TransactionTemplate(transactionManager), readTransaction(transactionManager),
                new ResolveReportService(users, reports, inspectPosts, hidePosts, inspectComments, hideComments, clock));
    }

    private static TransactionTemplate readTransaction(PlatformTransactionManager transactionManager) {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        transaction.setReadOnly(true);
        return transaction;
    }
}
