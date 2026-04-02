package com.exam.api.controller;

import com.exam.api.common.ApiResponse;
import com.exam.api.dto.AuthUserResponse;
import com.exam.exception.BusinessException;
import com.exam.model.ExamRecord;
import com.exam.model.Paper;
import com.exam.model.User;
import com.exam.model.enums.ExamStatus;
import com.exam.model.enums.UserRole;
import com.exam.service.ExamService;
import com.exam.service.PaperService;
import com.exam.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/workbench")
public class WorkbenchController {
    private static final int DEFAULT_LIST_LIMIT = 6;

    private final UserService userService;
    private final PaperService paperService;
    private final ExamService examService;

    public WorkbenchController(UserService userService, PaperService paperService, ExamService examService) {
        this.userService = userService;
        this.paperService = paperService;
        this.examService = examService;
    }

    @GetMapping("/teacher/{userId}")
    public ApiResponse<Map<String, Object>> teacherWorkbench(@PathVariable("userId") Integer userId) {
        User teacher = requireRole(userId, UserRole.TEACHER);
        List<Paper> papers = paperService.getAllPapersOptimized();
        List<User> students = userService.getStudents();
        long publishedCount = papers.stream()
                .filter(paper -> Boolean.TRUE.equals(paper.getIsPublished()))
                .count();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("paperCount", papers.size());
        stats.put("publishedCount", publishedCount);
        stats.put("studentCount", students.size());

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("user", AuthUserResponse.from(teacher));
        payload.put("stats", stats);
        payload.put("recentPapers", papers.stream()
                .limit(DEFAULT_LIST_LIMIT)
                .map(this::toTeacherPaperCard)
                .collect(Collectors.toList()));
        payload.put("recentStudents", students.stream()
                .limit(DEFAULT_LIST_LIMIT)
                .map(this::toTeacherStudentCard)
                .collect(Collectors.toList()));

        return ApiResponse.success("教师工作台加载成功", payload);
    }

    @GetMapping("/student/{userId}")
    public ApiResponse<Map<String, Object>> studentWorkbench(@PathVariable("userId") Integer userId) {
        User student = requireRole(userId, UserRole.STUDENT);
        List<Paper> publishedPapers = paperService.getAllPublishedPapersOptimized();
        List<ExamRecord> records = examService.getStudentExamRecordsOptimized(userId);
        long submittedCount = records.stream()
                .filter(record -> record.getStatus() == ExamStatus.SUBMITTED || record.getStatus() == ExamStatus.TIMEOUT)
                .count();
        double averageScore = records.stream()
                .map(ExamRecord::getScore)
                .filter(score -> score != null)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("publishedPaperCount", publishedPapers.size());
        stats.put("recordCount", records.size());
        stats.put("submittedCount", submittedCount);
        stats.put("averageScore", averageScore);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("user", AuthUserResponse.from(student));
        payload.put("stats", stats);
        payload.put("availablePapers", publishedPapers.stream()
                .limit(DEFAULT_LIST_LIMIT)
                .map(this::toStudentPaperCard)
                .collect(Collectors.toList()));
        payload.put("recentRecords", records.stream()
                .sorted((left, right) -> compareRecordOrder(right, left))
                .limit(DEFAULT_LIST_LIMIT)
                .map(this::toStudentRecordCard)
                .collect(Collectors.toList()));

        return ApiResponse.success("学生工作台加载成功", payload);
    }

    private User requireRole(Integer userId, UserRole role) {
        User user = userService.getUserById(userId);
        if (user.getRole() != role) {
            throw new BusinessException("当前用户角色不匹配");
        }
        return user;
    }

    private Map<String, Object> toTeacherPaperCard(Paper paper) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("paperId", paper.getPaperId());
        item.put("paperName", paper.getPaperName());
        item.put("subject", paper.getSubject());
        item.put("questionCount", resolveQuestionCount(paper));
        item.put("duration", paper.getDuration());
        item.put("published", Boolean.TRUE.equals(paper.getIsPublished()));
        return item;
    }

    private Map<String, Object> toTeacherStudentCard(User student) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("userId", student.getUserId());
        item.put("realName", student.getRealName());
        item.put("loginId", student.getLoginId());
        item.put("email", student.getEmail());
        item.put("phone", student.getPhone());
        return item;
    }

    private Map<String, Object> toStudentPaperCard(Paper paper) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("paperId", paper.getPaperId());
        item.put("paperName", paper.getPaperName());
        item.put("subject", paper.getSubject());
        item.put("duration", paper.getDuration());
        item.put("totalScore", paper.getTotalScore());
        item.put("passScore", paper.getPassScore());
        item.put("questionCount", resolveQuestionCount(paper));
        return item;
    }

    private Map<String, Object> toStudentRecordCard(ExamRecord record) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("recordId", record.getRecordId());
        item.put("paperId", record.getPaperId());
        item.put("paperName", record.getPaper() != null ? record.getPaper().getPaperName() : null);
        item.put("status", record.getStatus() != null ? record.getStatus().name() : null);
        item.put("score", record.getScore());
        item.put("startTime", record.getStartTime());
        item.put("submitTime", record.getSubmitTime());
        item.put("durationSeconds", calculateDurationSeconds(record));
        item.put("resumeAvailable", record.getStatus() == ExamStatus.IN_PROGRESS);
        return item;
    }

    private int compareRecordOrder(ExamRecord left, ExamRecord right) {
        Integer leftId = left.getRecordId();
        Integer rightId = right.getRecordId();
        if (leftId == null && rightId == null) {
            return 0;
        }
        if (leftId == null) {
            return -1;
        }
        if (rightId == null) {
            return 1;
        }
        return Integer.compare(leftId, rightId);
    }

    private int resolveQuestionCount(Paper paper) {
        int optimizedCount = paper.getSingleCount() + paper.getMultipleCount() + paper.getJudgeCount() + paper.getBlankCount();
        if (optimizedCount > 0) {
            return optimizedCount;
        }
        return paper.getQuestions() == null ? 0 : paper.getQuestions().size();
    }

    private long calculateDurationSeconds(ExamRecord record) {
        if (record.getStartTime() == null) {
            return 0;
        }

        LocalDateTime endTime = record.getSubmitTime() != null ? record.getSubmitTime() : record.getEndTime();
        if (endTime == null) {
            return 0;
        }

        return Math.max(0, Duration.between(record.getStartTime(), endTime).getSeconds());
    }
}
