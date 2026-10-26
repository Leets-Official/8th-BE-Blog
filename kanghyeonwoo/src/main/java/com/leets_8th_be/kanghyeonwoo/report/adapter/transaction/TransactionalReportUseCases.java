package com.leets_8th_be.kanghyeonwoo.report.adapter.transaction;

import com.leets_8th_be.kanghyeonwoo.report.application.model.ReportView;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.ReportPostCommand;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.ReportPostUseCase;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.ReportCommentUseCase;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.ReportCommentCommand;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.GetReportsUseCase;
import com.leets_8th_be.kanghyeonwoo.report.application.port.in.ReportSearchQuery;
import com.leets_8th_be.kanghyeonwoo.report.application.model.ReportPage;
import org.springframework.transaction.support.TransactionTemplate;

public class TransactionalReportUseCases implements ReportPostUseCase, ReportCommentUseCase, GetReportsUseCase {
    private final ReportPostUseCase reportPost;
    private final ReportCommentUseCase reportComment;
    private final GetReportsUseCase getReports;
    private final TransactionTemplate transaction;
    private final TransactionTemplate readTransaction;

    public TransactionalReportUseCases(ReportPostUseCase reportPost, ReportCommentUseCase reportComment,
                                       GetReportsUseCase getReports, TransactionTemplate transaction,
                                       TransactionTemplate readTransaction) {
        this.reportPost = reportPost;
        this.reportComment = reportComment;
        this.getReports = getReports;
        this.transaction = transaction;
        this.readTransaction = readTransaction;
    }

    @Override
    public ReportView report(ReportPostCommand command) {
        return transaction.execute(status -> reportPost.report(command));
    }

    @Override
    public ReportView report(ReportCommentCommand command) {
        return transaction.execute(status -> reportComment.report(command));
    }

    @Override
    public ReportPage getReports(ReportSearchQuery query) {
        return readTransaction.execute(status -> getReports.getReports(query));
    }
}
