package com.leets_8th_be.kanghyeonwoo.report.adapter.transaction;

import com.leets_8th_be.kanghyeonwoo.report.application.model.ReportView;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.ReportPostCommand;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.ReportPostUseCase;
import org.springframework.transaction.support.TransactionTemplate;

public class TransactionalReportUseCases implements ReportPostUseCase {
    private final ReportPostUseCase reportPost;
    private final TransactionTemplate transaction;

    public TransactionalReportUseCases(ReportPostUseCase reportPost, TransactionTemplate transaction) {
        this.reportPost = reportPost;
        this.transaction = transaction;
    }

    @Override
    public ReportView report(ReportPostCommand command) {
        return transaction.execute(status -> reportPost.report(command));
    }
}
