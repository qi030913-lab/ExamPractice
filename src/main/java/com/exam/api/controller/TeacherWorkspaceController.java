package com.exam.api.controller;

import com.exam.api.common.ApiResponse;
import com.exam.api.dto.AuthUserResponse;
import com.exam.api.dto.TeacherImportPaperRequest;
import com.exam.api.dto.TeacherUpdatePaperRequest;
import com.exam.exception.BusinessException;
import com.exam.model.ExamRecord;
import com.exam.model.Paper;
import com.exam.model.Question;
import com.exam.model.User;
import com.exam.model.enums.ExamStatus;
import com.exam.model.enums.UserRole;
import com.exam.service.ExamService;
import com.exam.service.PaperService;
import com.exam.service.QuestionService;
import com.exam.service.UserService;
import com.exam.util.QuestionImportUtil;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teacher")
public class TeacherWorkspaceController {
    private final UserService userService;
    private final PaperService paperService;
    private final ExamService examService;
    private final QuestionService questionService;

    public TeacherWorkspaceController(
            UserService userService,
            PaperService paperService,
            ExamService examService,
            QuestionService questionService
    ) {
        this.userService = userService;
        this.paperService = paperService;
        this.examService = examService;
        this.questionService = questionService;
    }

    @GetMapping("/{userId}/papers")
    public ApiResponse<Map<String, Object>> getTeacherPapers(@PathVariable("userId") Integer userId) {
        User teacher = requireTeacher(userId);
        List<Paper> papers = paperService.getAllPapersOptimized();
        long publishedCount = papers.stream()
                .filter(paper -> Boolean.TRUE.equals(paper.getIsPublished()))
                .count();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("paperCount", papers.size());
        summary.put("publishedCount", publishedCount);
        summary.put("unpublishedCount", papers.size() - publishedCount);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("user", AuthUserResponse.from(teacher));
        payload.put("summary", summary);
        payload.put("papers", papers.stream().map(this::toTeacherPaperItem).collect(Collectors.toList()));
        return ApiResponse.success("教师试卷中心加载成功", payload);
    }

    @PostMapping("/{userId}/papers/{paperId}/publish")
    public ApiResponse<Map<String, Object>> publishPaper(
            @PathVariable("userId") Integer userId,
            @PathVariable("paperId") Integer paperId
    ) {
        requireTeacher(userId);
        paperService.publishPaper(paperId);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("paper", toTeacherPaperItem(paperService.getPaperById(paperId)));
        return ApiResponse.success("试卷发布成功", payload);
    }

    @PostMapping("/{userId}/papers/{paperId}/unpublish")
    public ApiResponse<Map<String, Object>> unpublishPaper(
            @PathVariable("userId") Integer userId,
            @PathVariable("paperId") Integer paperId
    ) {
        requireTeacher(userId);
        paperService.unpublishPaper(paperId);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("paper", toTeacherPaperItem(paperService.getPaperById(paperId)));
        return ApiResponse.success("试卷已取消发布", payload);
    }

    @DeleteMapping("/{userId}/papers/{paperId}")
    public ApiResponse<Map<String, Object>> deletePaper(
            @PathVariable("userId") Integer userId,
            @PathVariable("paperId") Integer paperId
    ) {
        requireTeacher(userId);
        paperService.deletePaper(paperId);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("paperId", paperId);
        return ApiResponse.success("试卷删除成功", payload);
    }

    @GetMapping("/{userId}/papers/{paperId}")
    public ApiResponse<Map<String, Object>> getTeacherPaperDetail(
            @PathVariable("userId") Integer userId,
            @PathVariable("paperId") Integer paperId
    ) {
        requireTeacher(userId);
        Paper paper = paperService.getPaperById(paperId);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("paper", toTeacherPaperItem(paper));
        payload.put("questions", paper.getQuestions().stream().map(this::toQuestionDetailItem).collect(Collectors.toList()));
        return ApiResponse.success("试卷详情加载成功", payload);
    }

    @PutMapping("/{userId}/papers/{paperId}")
    public ApiResponse<Map<String, Object>> updateTeacherPaper(
            @PathVariable("userId") Integer userId,
            @PathVariable("paperId") Integer paperId,
            @Valid @RequestBody TeacherUpdatePaperRequest request
    ) {
        requireTeacher(userId);
        Paper paper = paperService.getPaperById(paperId);
        paper.setPaperName(request.getPaperName().trim());
        paper.setSubject(request.getSubject().trim());
        paper.setPassScore(request.getPassScore());
        paper.setDuration(request.getDuration());
        paper.setDescription(normalizeBlank(request.getDescription()));
        paperService.updatePaper(paper);

        Paper updatedPaper = paperService.getPaperById(paperId);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("paper", toTeacherPaperItem(updatedPaper));
        payload.put("questions", updatedPaper.getQuestions().stream().map(this::toQuestionDetailItem).collect(Collectors.toList()));
        return ApiResponse.success("试卷更新成功", payload);
    }

    @GetMapping("/{userId}/students")
    public ApiResponse<Map<String, Object>> getTeacherStudents(@PathVariable("userId") Integer userId) {
        User teacher = requireTeacher(userId);
        List<User> students = userService.getStudents();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("studentCount", students.size());

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("user", AuthUserResponse.from(teacher));
        payload.put("summary", summary);
        payload.put("students", students.stream().map(this::toTeacherStudentItem).collect(Collectors.toList()));
        return ApiResponse.success("教师学生中心加载成功", payload);
    }

    @GetMapping("/{userId}/students/{studentId}/records")
    public ApiResponse<Map<String, Object>> getStudentRecords(
            @PathVariable("userId") Integer userId,
            @PathVariable("studentId") Integer studentId
    ) {
        requireTeacher(userId);
        User student = requireStudent(studentId);
        List<ExamRecord> records = examService.getStudentExamRecordsOptimized(studentId);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("student", AuthUserResponse.from(student));
        payload.put("summary", buildStudentSummary(records));
        payload.put("records", records.stream().map(this::toTeacherStudentRecordItem).collect(Collectors.toList()));
        return ApiResponse.success("学生考试记录加载成功", payload);
    }

    @PostMapping("/{userId}/import-paper")
    public ApiResponse<Map<String, Object>> importPaper(
            @PathVariable("userId") Integer userId,
            @Valid @RequestBody TeacherImportPaperRequest request
    ) {
        requireTeacher(userId);

        List<Question> importedQuestions;
        try {
            importedQuestions = QuestionImportUtil.importFromText(request.getSourceText(), userId);
        } catch (Exception ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }

        Set<Integer> uniqueQuestionIds = new LinkedHashSet<>();
        int createdQuestionCount = 0;
        int reusedQuestionCount = 0;

        for (Question question : importedQuestions) {
            List<Question> existingQuestions = questionService.searchQuestions(
                    question.getContent(),
                    null,
                    null,
                    null,
                    0,
                    Integer.MAX_VALUE
            );

            Question matched = existingQuestions.stream()
                    .filter(existing -> safeEquals(existing.getContent(), question.getContent()))
                    .filter(existing -> safeEquals(existing.getCorrectAnswer(), question.getCorrectAnswer()))
                    .findFirst()
                    .orElse(null);

            Integer questionId;
            if (matched == null) {
                questionId = questionService.addQuestion(question);
                createdQuestionCount++;
            } else {
                questionId = matched.getQuestionId();
                reusedQuestionCount++;
            }

            if (questionId != null && questionId > 0) {
                uniqueQuestionIds.add(questionId);
            }
        }

        List<Integer> questionIds = new ArrayList<>(uniqueQuestionIds);
        if (questionIds.isEmpty()) {
            throw new BusinessException("没有可用于建卷的有效题目");
        }

        Paper paper = new Paper();
        paper.setPaperName(request.getPaperName().trim());
        paper.setSubject(request.getSubject().trim());
        paper.setPassScore(request.getPassScore());
        paper.setDuration(request.getDuration());
        paper.setDescription(normalizeBlank(request.getDescription()));
        paper.setCreatorId(userId);

        int paperId = paperService.createPaper(paper, questionIds);
        Paper createdPaper = paperService.getPaperById(paperId);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("sourceQuestionCount", importedQuestions.size());
        summary.put("linkedQuestionCount", questionIds.size());
        summary.put("createdQuestionCount", createdQuestionCount);
        summary.put("reusedQuestionCount", reusedQuestionCount);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("paper", toTeacherPaperItem(createdPaper));
        payload.put("questions", createdPaper.getQuestions().stream().map(this::toQuestionItem).collect(Collectors.toList()));
        payload.put("summary", summary);
        return ApiResponse.success("导题建卷成功", payload);
    }

    private User requireTeacher(Integer userId) {
        User user = userService.getUserById(userId);
        if (user.getRole() != UserRole.TEACHER) {
            throw new BusinessException("当前用户不是教师角色");
        }
        return user;
    }

    private User requireStudent(Integer userId) {
        User user = userService.getUserById(userId);
        if (user.getRole() != UserRole.STUDENT) {
            throw new BusinessException("当前用户不是学生角色");
        }
        return user;
    }

    private Map<String, Object> toTeacherPaperItem(Paper paper) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("paperId", paper.getPaperId());
        item.put("paperName", paper.getPaperName());
        item.put("subject", paper.getSubject());
        item.put("totalScore", paper.getTotalScore());
        item.put("duration", paper.getDuration());
        item.put("passScore", paper.getPassScore());
        item.put("questionCount", resolveQuestionCount(paper));
        item.put("published", Boolean.TRUE.equals(paper.getIsPublished()));
        item.put("description", paper.getDescription());
        item.put("createTime", paper.getCreateTime());
        item.put("updateTime", paper.getUpdateTime());
        return item;
    }

    private Map<String, Object> toTeacherStudentItem(User student) {
        List<ExamRecord> records = examService.getStudentExamRecordsOptimized(student.getUserId());
        Map<String, Object> summary = buildStudentSummary(records);

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("userId", student.getUserId());
        item.put("realName", student.getRealName());
        item.put("studentNumber", student.getStudentNumber());
        item.put("email", student.getEmail());
        item.put("phone", student.getPhone());
        item.put("gender", student.getGender());
        item.put("status", student.getStatus());
        item.put("recordCount", summary.get("recordCount"));
        item.put("submittedCount", summary.get("submittedCount"));
        item.put("averageScore", summary.get("averageScore"));
        return item;
    }

    private Map<String, Object> toTeacherStudentRecordItem(ExamRecord record) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("recordId", record.getRecordId());
        item.put("paperId", record.getPaperId());
        item.put("paperName", record.getPaper() != null ? record.getPaper().getPaperName() : null);
        item.put("status", record.getStatus() == null ? null : record.getStatus().name());
        item.put("score", record.getScore());
        item.put("startTime", record.getStartTime());
        item.put("submitTime", record.getSubmitTime());
        return item;
    }

    private Map<String, Object> toQuestionItem(Question question) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("questionId", question.getQuestionId());
        item.put("questionType", question.getQuestionType() == null ? null : question.getQuestionType().name());
        item.put("subject", question.getSubject());
        item.put("content", question.getContent());
        item.put("correctAnswer", question.getCorrectAnswer());
        item.put("score", question.getScore());
        item.put("difficulty", question.getDifficulty() == null ? null : question.getDifficulty().name());
        return item;
    }

    private Map<String, Object> toQuestionDetailItem(Question question) {
        Map<String, Object> item = toQuestionItem(question);
        item.put("optionA", question.getOptionA());
        item.put("optionB", question.getOptionB());
        item.put("optionC", question.getOptionC());
        item.put("optionD", question.getOptionD());
        item.put("analysis", question.getAnalysis());
        return item;
    }

    private Map<String, Object> buildStudentSummary(List<ExamRecord> records) {
        long submittedCount = records.stream()
                .filter(record -> record.getStatus() == ExamStatus.SUBMITTED || record.getStatus() == ExamStatus.TIMEOUT)
                .count();
        double averageScore = records.stream()
                .map(ExamRecord::getScore)
                .filter(score -> score != null)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("recordCount", records.size());
        summary.put("submittedCount", submittedCount);
        summary.put("averageScore", averageScore);
        return summary;
    }

    private int resolveQuestionCount(Paper paper) {
        int optimizedCount = paper.getSingleCount() + paper.getMultipleCount() + paper.getJudgeCount() + paper.getBlankCount();
        if (optimizedCount > 0) {
            return optimizedCount;
        }
        return paper.getQuestions() == null ? 0 : paper.getQuestions().size();
    }

    private boolean safeEquals(String left, String right) {
        if (left == null && right == null) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        return left.trim().equals(right.trim());
    }

    private String normalizeBlank(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
