package com.exam.api.support;

import com.exam.exception.BusinessException;
import com.exam.model.ExamRecord;
import com.exam.model.Paper;
import com.exam.service.ExamService;
import com.exam.service.PaperService;
import org.springframework.stereotype.Component;

@Component
public class ExamAccessGuard {
    private final PaperService paperService;
    private final ExamService examService;

    public ExamAccessGuard(PaperService paperService, ExamService examService) {
        this.paperService = paperService;
        this.examService = examService;
    }

    public Paper requirePublishedPaper(Integer paperId) {
        Paper paper = paperService.getPaperById(paperId);
        if (!Boolean.TRUE.equals(paper.getIsPublished())) {
            throw new BusinessException("该试卷尚未发布");
        }
        return paper;
    }

    public ExamRecord requireOwnedRecord(Integer userId, Integer recordId) {
        ExamRecord record = examService.getExamRecordById(recordId);
        if (record == null) {
            throw new BusinessException("考试记录不存在");
        }
        if (!userId.equals(record.getStudentId())) {
            throw new BusinessException("考试记录不属于当前学生");
        }
        return record;
    }

    public Paper resolvePaper(ExamRecord record) {
        return record.getPaper() != null ? record.getPaper() : paperService.getPaperById(record.getPaperId());
    }
}
