package com.example.demo.report.converter;

import com.example.demo.post.dto.PostResDTO;
import com.example.demo.post.entity.Post;
import com.example.demo.report.dto.ReportResDTO;
import com.example.demo.report.entity.ReportComment;
import com.example.demo.report.entity.ReportPost;
import com.example.demo.report.entity.ReportPostId;
import com.example.demo.report.enums.ReportStatus;
import org.springframework.stereotype.Component;

@Component
public class ReportConverter {

    public ReportResDTO.PostReportResponse toReportPostCreateResponse(ReportPost reportPost){
        return new ReportResDTO.PostReportResponse(reportPost.getId());
    }
    public ReportResDTO.CommentReportResponse toReportCommentCreateResponse(ReportComment reportComment){
        return new ReportResDTO.CommentReportResponse(reportComment.getId());
    }

    public ReportResDTO.PostReportStatusResponse toReportPostStatusUpdateResponse(ReportPost reportPost, ReportStatus status){
        return new ReportResDTO.PostReportStatusResponse(reportPost.getId(),status);
    }
}
