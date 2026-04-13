package com.exam.api.assembler;

import com.exam.api.dto.AuthUserResponse;
import com.exam.api.dto.StudentAchievementDtos;
import com.exam.api.service.StudentAchievementService;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class StudentAchievementAssembler {

    public StudentAchievementDtos.StudentAchievementPayload toPayload(
            AuthUserResponse user,
            StudentAchievementService.StudentAchievementSnapshot snapshot
    ) {
        return new StudentAchievementDtos.StudentAchievementPayload(
                user,
                toSummary(snapshot.getSummary()),
                snapshot.getScoreTrend().stream().map(this::toScoreTrendItem).collect(Collectors.toList()),
                snapshot.getQuestionTypeAccuracy().stream().map(this::toTypeAccuracyItem).collect(Collectors.toList()),
                snapshot.getSubjectPerformance().stream().map(this::toSubjectPerformanceItem).collect(Collectors.toList()),
                snapshot.getLatestUpdatedAt()
        );
    }

    private StudentAchievementDtos.AchievementSummary toSummary(StudentAchievementService.AchievementSummary summary) {
        return new StudentAchievementDtos.AchievementSummary(
                summary.getTotalExamCount(),
                summary.getCompletedExamCount(),
                summary.getInProgressCount(),
                summary.getAverageScore(),
                summary.getBestScore(),
                summary.getPassCount(),
                summary.getFailCount(),
                summary.getTotalQuestionCount(),
                summary.getTotalAnsweredCount(),
                summary.getTotalCorrectCount(),
                summary.getAccuracyRate()
        );
    }

    private StudentAchievementDtos.ScoreTrendItem toScoreTrendItem(StudentAchievementService.ScoreTrendItem scoreTrendItem) {
        return new StudentAchievementDtos.ScoreTrendItem(
                scoreTrendItem.getRecordId(),
                scoreTrendItem.getPaperId(),
                scoreTrendItem.getPaperName(),
                scoreTrendItem.getSubject(),
                scoreTrendItem.getScore(),
                scoreTrendItem.getTotalScore(),
                scoreTrendItem.getPassScore(),
                scoreTrendItem.getStatus(),
                scoreTrendItem.getSubmitTime(),
                scoreTrendItem.getStartTime(),
                scoreTrendItem.isPassed()
        );
    }

    private StudentAchievementDtos.TypeAccuracyItem toTypeAccuracyItem(StudentAchievementService.TypeAccuracyItem typeAccuracyItem) {
        return new StudentAchievementDtos.TypeAccuracyItem(
                typeAccuracyItem.getQuestionType(),
                typeAccuracyItem.getLabel(),
                typeAccuracyItem.getTotalCount(),
                typeAccuracyItem.getAnsweredCount(),
                typeAccuracyItem.getCorrectCount(),
                typeAccuracyItem.getAccuracyRate()
        );
    }

    private StudentAchievementDtos.SubjectPerformanceItem toSubjectPerformanceItem(StudentAchievementService.SubjectPerformanceItem subjectPerformanceItem) {
        return new StudentAchievementDtos.SubjectPerformanceItem(
                subjectPerformanceItem.getSubject(),
                subjectPerformanceItem.getRecordCount(),
                subjectPerformanceItem.getAverageScore(),
                subjectPerformanceItem.getPassCount(),
                subjectPerformanceItem.getFailCount(),
                subjectPerformanceItem.getTotalQuestionCount(),
                subjectPerformanceItem.getAnsweredCount(),
                subjectPerformanceItem.getCorrectCount(),
                subjectPerformanceItem.getAccuracyRate(),
                subjectPerformanceItem.getLatestSubmitTime()
        );
    }
}
