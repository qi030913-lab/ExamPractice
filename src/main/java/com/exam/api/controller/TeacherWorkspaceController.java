package com.exam.api.controller;

import com.exam.api.assembler.TeacherWorkspaceAssembler;
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
    private final TeacherWorkspaceAssembler assembler;

    public TeacherWorkspaceController(
            UserService userService,
            PaperService paperService,
            ExamService examService,
            QuestionService questionService,
            TeacherWorkspaceAssembler assembler
    ) {
        this.userService = userService;
        this.paperService = paperService;
        this.examService = examService;
        this.questionService = questionService;
        this.assembler = assembler;
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
        payload.put("papers", papers.stream().map(assembler::toTeacherPaperItem).collect(Collectors.toList()));
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
        payload.put("paper", assembler.toTeacherPaperItem(paperService.getPaperById(paperId)));
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
        payload.put("paper", assembler.toTeacherPaperItem(paperService.getPaperById(paperId)));
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
        payload.put("paper", assembler.toTeacherPaperItem(paper));
        payload.put("questions", paper.getQuestions().stream().map(assembler::toQuestionDetailItem).collect(Collectors.toList()));
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
        payload.put("paper", assembler.toTeacherPaperItem(updatedPaper));
        payload.put("questions", updatedPaper.getQuestions().stream().map(assembler::toQuestionDetailItem).collect(Collectors.toList()));
        return ApiResponse.success("试卷更新成功", payload);
    }

    @GetMapping("/{userId}/students")
    public ApiResponse<Map<String, Object>> getTeacherStudents(@PathVariable("userId") Integer userId) {
        User teacher = requireTeacher(userId);
        List<User> students = userService.getStudents();
        List<Integer> studentIds = students.stream()
                .map(User::getUserId)
                .filter(id -> id != null)
                .collect(Collectors.toList());
        Map<Integer, List<ExamRecord>> recordsByStudentId = examService.getStudentExamRecordsByStudentIds(studentIds);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("studentCount", students.size());

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("user", AuthUserResponse.from(teacher));
        payload.put("summary", summary);
        payload.put("students", students.stream()
                .map(student -> assembler.toTeacherStudentItem(
                        student,
                        recordsByStudentId.getOrDefault(student.getUserId(), List.of())
                ))
                .collect(Collectors.toList()));
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
        payload.put("summary", assembler.buildStudentSummary(records));
        payload.put("records", records.stream().map(assembler::toTeacherStudentRecordItem).collect(Collectors.toList()));
        return ApiResponse.success("学生考试记录加载成功", payload);
    }

    @GetMapping("/{userId}/students/{studentId}")
    public ApiResponse<Map<String, Object>> getTeacherStudentDetail(
            @PathVariable("userId") Integer userId,
            @PathVariable("studentId") Integer studentId
    ) {
        requireTeacher(userId);
        User student = requireStudent(studentId);
        List<ExamRecord> records = examService.getStudentExamRecordsOptimized(studentId);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("student", assembler.toTeacherStudentItem(student, records));
        payload.put("summary", assembler.buildStudentSummary(records));
        payload.put("records", records.stream().map(assembler::toTeacherStudentRecordItem).collect(Collectors.toList()));
        return ApiResponse.success("学生详情加载成功", payload);
    }

    @GetMapping("/{userId}/students/{studentId}/records/{recordId}")
    public ApiResponse<Map<String, Object>> getTeacherStudentRecordDetail(
            @PathVariable("userId") Integer userId,
            @PathVariable("studentId") Integer studentId,
            @PathVariable("recordId") Integer recordId
    ) {
        requireTeacher(userId);
        User student = requireStudent(studentId);
        ExamRecord record = examService.getExamRecordById(recordId);
        if (record == null) {
            throw new BusinessException("考试记录不存在");
        }
        if (!studentId.equals(record.getStudentId())) {
            throw new BusinessException("考试记录不属于当前学生");
        }

        Paper paper = record.getPaper() != null ? record.getPaper() : paperService.getPaperById(record.getPaperId());
        List<com.exam.model.AnswerRecord> answerRecords = examService.getAnswerRecords(recordId);
        long answeredCount = answerRecords.stream()
                .filter(answer -> answer.getStudentAnswer() != null && !answer.getStudentAnswer().trim().isEmpty())
                .count();
        long correctCount = answerRecords.stream()
                .filter(answer -> Boolean.TRUE.equals(answer.getIsCorrect()))
                .count();
        long wrongCount = answerRecords.stream()
                .filter(answer -> answer.getStudentAnswer() != null && !answer.getStudentAnswer().trim().isEmpty())
                .filter(answer -> !Boolean.TRUE.equals(answer.getIsCorrect()))
                .count();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("student", assembler.toTeacherStudentItem(student, examService.getStudentExamRecordsOptimized(student.getUserId())));
        payload.put("record", assembler.toTeacherStudentRecordDetailItem(record, paper, answerRecords, answeredCount, correctCount, wrongCount));
        payload.put("answers", answerRecords.stream().map(assembler::toAnswerRecordItem).collect(Collectors.toList()));
        return ApiResponse.success("考试记录详情加载成功", payload);
    }

    @GetMapping("/{userId}/import-template")
    public ApiResponse<Map<String, Object>> getImportTemplate(@PathVariable("userId") Integer userId) {
        requireTeacher(userId);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("fileName", "题目导入模板.txt");
        payload.put("content", QuestionImportUtil.buildTemplateContent());
        return ApiResponse.success("导题模板加载成功", payload);
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
            questionService.validateSupportedForAutoExam(question);

            Question matched = questionService.findExactQuestion(
                    question.getSubject(),
                    question.getQuestionType(),
                    question.getContent(),
                    question.getCorrectAnswer()
            );

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
        payload.put("paper", assembler.toTeacherPaperItem(createdPaper));
        payload.put("questions", createdPaper.getQuestions().stream().map(assembler::toQuestionItem).collect(Collectors.toList()));
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

    private String normalizeBlank(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
