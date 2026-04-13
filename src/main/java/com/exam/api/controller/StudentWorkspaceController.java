package com.exam.api.controller;

import com.exam.api.assembler.StudentWorkspaceAssembler;
import com.exam.api.common.ApiResponse;
import com.exam.api.dto.AuthUserResponse;
import com.exam.api.dto.StudentSubmitExamRequest;
import com.exam.exception.BusinessException;
import com.exam.model.AnswerRecord;
import com.exam.model.ExamRecord;
import com.exam.model.Paper;
import com.exam.model.User;
import com.exam.model.enums.ExamStatus;
import com.exam.model.enums.UserRole;
import com.exam.service.ExamService;
import com.exam.service.PaperService;
import com.exam.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/student")
public class StudentWorkspaceController {
    private final UserService userService;
    private final PaperService paperService;
    private final ExamService examService;
    private final StudentWorkspaceAssembler assembler;

    public StudentWorkspaceController(
            UserService userService,
            PaperService paperService,
            ExamService examService,
            StudentWorkspaceAssembler assembler
    ) {
        this.userService = userService;
        this.paperService = paperService;
        this.examService = examService;
        this.assembler = assembler;
    }

    @GetMapping("/{userId}/papers")
    public ApiResponse<Map<String, Object>> getStudentPapers(@PathVariable("userId") Integer userId) {
        User student = requireStudent(userId);
        List<Paper> papers = paperService.getAllPublishedPapersOptimized();
        List<ExamRecord> records = examService.getStudentExamRecordsOptimized(userId);
        Map<Integer, ExamRecord> latestRecordByPaperId = assembler.resolveLatestRecordByPaperId(records);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("user", AuthUserResponse.from(student));
        payload.put("summary", assembler.buildPaperSummary(papers, records));
        payload.put("papers", papers.stream()
                .map(paper -> assembler.toStudentPaperItem(paper, latestRecordByPaperId.get(paper.getPaperId())))
                .collect(Collectors.toList()));
        return ApiResponse.success("学生试卷中心加载成功", payload);
    }

    @GetMapping("/{userId}/papers/{paperId}")
    public ApiResponse<Map<String, Object>> getStudentPaperDetail(
            @PathVariable("userId") Integer userId,
            @PathVariable("paperId") Integer paperId
    ) {
        User student = requireStudent(userId);
        Paper paper = requirePublishedPaper(paperId);
        List<ExamRecord> records = examService.getStudentExamRecordsOptimized(userId);
        ExamRecord latestRecord = assembler.resolveLatestRecordByPaperId(records).get(paperId);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("user", AuthUserResponse.from(student));
        payload.put("paper", assembler.toStudentPaperItem(paper, latestRecord));
        payload.put("questions", paper.getQuestions().stream().map(assembler::toQuestionExamItem).collect(Collectors.toList()));
        return ApiResponse.success("考试详情加载成功", payload);
    }

    @PostMapping("/{userId}/papers/{paperId}/start")
    public ApiResponse<Map<String, Object>> startExam(
            @PathVariable("userId") Integer userId,
            @PathVariable("paperId") Integer paperId
    ) {
        User student = requireStudent(userId);
        Paper paper = requirePublishedPaper(paperId);
        ExamService.ExamStartResult startResult = examService.startOrResumeExam(userId, paperId);
        ExamRecord record = startResult.getRecord();
        boolean resumed = startResult.isResumed();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("user", AuthUserResponse.from(student));
        payload.put("record", assembler.toExamLifecycleRecordItem(record));
        payload.put("paper", assembler.toStudentPaperItem(paper, record));
        payload.put("questions", paper.getQuestions().stream().map(assembler::toQuestionExamItem).collect(Collectors.toList()));
        payload.put("remainingSeconds", assembler.calculateRemainingSeconds(record, paper));
        payload.put("deadlineTime", assembler.calculateDeadlineTime(record, paper));
        payload.put("resumed", resumed);
        return ApiResponse.success(resumed ? "已恢复进行中的考试" : "考试开始成功", payload);
    }

    @GetMapping("/{userId}/records")
    public ApiResponse<Map<String, Object>> getStudentRecords(@PathVariable("userId") Integer userId) {
        User student = requireStudent(userId);
        List<ExamRecord> records = examService.getStudentExamRecordsOptimized(userId);
        List<Integer> recordIds = records.stream()
                .map(ExamRecord::getRecordId)
                .filter(id -> id != null)
                .collect(Collectors.toList());
        Map<Integer, List<AnswerRecord>> answerRecordsMap = examService.getAnswerRecordsBatch(recordIds);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("user", AuthUserResponse.from(student));
        payload.put("summary", assembler.buildRecordSummary(records));
        payload.put("records", records.stream()
                .map(record -> assembler.toStudentScoreRecordItem(record, answerRecordsMap.get(record.getRecordId())))
                .collect(Collectors.toList()));
        return ApiResponse.success("学生成绩中心加载成功", payload);
    }

    @GetMapping("/{userId}/records/{recordId}")
    public ApiResponse<Map<String, Object>> getStudentRecordDetail(
            @PathVariable("userId") Integer userId,
            @PathVariable("recordId") Integer recordId
    ) {
        User student = requireStudent(userId);
        ExamRecord record = requireOwnedRecord(userId, recordId);
        Paper paper = record.getPaper() != null ? record.getPaper() : paperService.getPaperById(record.getPaperId());
        List<AnswerRecord> answerRecords = examService.getAnswerRecords(recordId);
        long correctCount = answerRecords.stream()
                .filter(answer -> Boolean.TRUE.equals(answer.getIsCorrect()))
                .count();
        long answeredCount = answerRecords.stream()
                .filter(answer -> answer.getStudentAnswer() != null && !answer.getStudentAnswer().trim().isEmpty())
                .count();
        long wrongCount = answerRecords.stream()
                .filter(answer -> answer.getStudentAnswer() != null && !answer.getStudentAnswer().trim().isEmpty())
                .filter(answer -> !Boolean.TRUE.equals(answer.getIsCorrect()))
                .count();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("user", AuthUserResponse.from(student));
        payload.put("record", assembler.toStudentRecordDetailItem(record, paper, answerRecords, answeredCount, correctCount, wrongCount));
        payload.put("answers", answerRecords.stream().map(assembler::toAnswerRecordItem).collect(Collectors.toList()));
        return ApiResponse.success("考试记录详情加载成功", payload);
    }

    @GetMapping("/{userId}/records/{recordId}/exam")
    public ApiResponse<Map<String, Object>> getStudentExamSession(
            @PathVariable("userId") Integer userId,
            @PathVariable("recordId") Integer recordId
    ) {
        User student = requireStudent(userId);
        ExamRecord record = requireOwnedRecord(userId, recordId);
        Paper paper = record.getPaper() != null ? record.getPaper() : paperService.getPaperById(record.getPaperId());

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("user", AuthUserResponse.from(student));
        payload.put("record", assembler.toExamLifecycleRecordItem(record));
        payload.put("paper", assembler.toStudentPaperItem(paper, record));
        payload.put("questions", paper.getQuestions().stream().map(assembler::toQuestionExamItem).collect(Collectors.toList()));
        payload.put("remainingSeconds", assembler.calculateRemainingSeconds(record, paper));
        payload.put("deadlineTime", assembler.calculateDeadlineTime(record, paper));
        return ApiResponse.success("考试作答页加载成功", payload);
    }

    @PostMapping("/{userId}/records/{recordId}/submit")
    public ApiResponse<Map<String, Object>> submitExam(
            @PathVariable("userId") Integer userId,
            @PathVariable("recordId") Integer recordId,
            @Valid @RequestBody StudentSubmitExamRequest request
    ) {
        User student = requireStudent(userId);
        ExamRecord record = requireOwnedRecord(userId, recordId);
        if (record.getStatus() != ExamStatus.IN_PROGRESS) {
            throw new BusinessException("当前考试不处于可提交状态");
        }

        Map<Integer, String> answers = request.getAnswers() == null
                ? new LinkedHashMap<>()
                : request.getAnswers().stream()
                .filter(item -> item.getQuestionId() != null)
                .collect(Collectors.toMap(
                        StudentSubmitExamRequest.AnswerItem::getQuestionId,
                        item -> normalizeBlank(item.getAnswer()) == null ? "" : item.getAnswer().trim(),
                        (left, right) -> right,
                        LinkedHashMap::new
                ));

        BigDecimal score = examService.submitExam(recordId, answers);
        ExamRecord submittedRecord = examService.getExamRecordById(recordId);
        Paper paper = submittedRecord.getPaper() != null
                ? submittedRecord.getPaper()
                : paperService.getPaperById(submittedRecord.getPaperId());
        List<AnswerRecord> answerRecords = examService.getAnswerRecords(recordId);
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
        payload.put("user", AuthUserResponse.from(student));
        payload.put("result", assembler.toSubmitResultItem(submittedRecord, paper, score, answerRecords.size(), answeredCount, correctCount, wrongCount));
        return ApiResponse.success("考试提交成功", payload);
    }

    private User requireStudent(Integer userId) {
        User user = userService.getUserById(userId);
        if (user.getRole() != UserRole.STUDENT) {
            throw new BusinessException("当前用户不是学生角色");
        }
        return user;
    }

    private Paper requirePublishedPaper(Integer paperId) {
        Paper paper = paperService.getPaperById(paperId);
        if (!Boolean.TRUE.equals(paper.getIsPublished())) {
            throw new BusinessException("该试卷尚未发布");
        }
        return paper;
    }

    private ExamRecord requireOwnedRecord(Integer userId, Integer recordId) {
        ExamRecord record = examService.getExamRecordById(recordId);
        if (record == null) {
            throw new BusinessException("考试记录不存在");
        }
        if (!userId.equals(record.getStudentId())) {
            throw new BusinessException("考试记录不属于当前学生");
        }
        return record;
    }

    private String normalizeBlank(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
