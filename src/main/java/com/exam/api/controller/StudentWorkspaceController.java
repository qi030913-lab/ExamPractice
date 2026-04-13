package com.exam.api.controller;

import com.exam.api.assembler.StudentWorkspaceAssembler;
import com.exam.api.common.ApiResponse;
import com.exam.api.dto.AuthUserResponse;
import com.exam.api.dto.StudentSubmitExamRequest;
import com.exam.api.dto.StudentWorkspaceDtos;
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
    public ApiResponse<StudentWorkspaceDtos.StudentPapersPayload> getStudentPapers(@PathVariable("userId") Integer userId) {
        User student = requireStudent(userId);
        List<Paper> papers = paperService.getAllPublishedPapersOptimized();
        List<ExamRecord> records = examService.getStudentExamRecordsOptimized(userId);
        Map<Integer, ExamRecord> latestRecordByPaperId = assembler.resolveLatestRecordByPaperId(records);

        StudentWorkspaceDtos.StudentPapersPayload payload = new StudentWorkspaceDtos.StudentPapersPayload(
                AuthUserResponse.from(student),
                assembler.buildPaperSummary(papers, records),
                papers.stream()
                        .map(paper -> assembler.toStudentPaperItem(paper, latestRecordByPaperId.get(paper.getPaperId())))
                        .collect(Collectors.toList())
        );
        return ApiResponse.success("学生试卷中心加载成功", payload);
    }

    @GetMapping("/{userId}/papers/{paperId}")
    public ApiResponse<StudentWorkspaceDtos.StudentPaperDetailPayload> getStudentPaperDetail(
            @PathVariable("userId") Integer userId,
            @PathVariable("paperId") Integer paperId
    ) {
        User student = requireStudent(userId);
        Paper paper = requirePublishedPaper(paperId);
        List<ExamRecord> records = examService.getStudentExamRecordsOptimized(userId);
        ExamRecord latestRecord = assembler.resolveLatestRecordByPaperId(records).get(paperId);

        StudentWorkspaceDtos.StudentPaperDetailPayload payload = new StudentWorkspaceDtos.StudentPaperDetailPayload(
                AuthUserResponse.from(student),
                assembler.toStudentPaperItem(paper, latestRecord),
                paper.getQuestions().stream().map(assembler::toQuestionExamItem).collect(Collectors.toList())
        );
        return ApiResponse.success("考试详情加载成功", payload);
    }

    @PostMapping("/{userId}/papers/{paperId}/start")
    public ApiResponse<StudentWorkspaceDtos.StartExamPayload> startExam(
            @PathVariable("userId") Integer userId,
            @PathVariable("paperId") Integer paperId
    ) {
        User student = requireStudent(userId);
        Paper paper = requirePublishedPaper(paperId);
        ExamService.ExamStartResult startResult = examService.startOrResumeExam(userId, paperId);
        ExamRecord record = startResult.getRecord();
        boolean resumed = startResult.isResumed();

        StudentWorkspaceDtos.StartExamPayload payload = new StudentWorkspaceDtos.StartExamPayload(
                AuthUserResponse.from(student),
                assembler.toExamLifecycleRecordItem(record),
                assembler.toStudentPaperItem(paper, record),
                paper.getQuestions().stream().map(assembler::toQuestionExamItem).collect(Collectors.toList()),
                assembler.calculateRemainingSeconds(record, paper),
                assembler.calculateDeadlineTime(record, paper),
                resumed
        );
        return ApiResponse.success(resumed ? "已恢复进行中的考试" : "考试开始成功", payload);
    }

    @GetMapping("/{userId}/records")
    public ApiResponse<StudentWorkspaceDtos.StudentRecordsPayload> getStudentRecords(@PathVariable("userId") Integer userId) {
        User student = requireStudent(userId);
        List<ExamRecord> records = examService.getStudentExamRecordsOptimized(userId);
        List<Integer> recordIds = records.stream()
                .map(ExamRecord::getRecordId)
                .filter(id -> id != null)
                .collect(Collectors.toList());
        Map<Integer, List<AnswerRecord>> answerRecordsMap = examService.getAnswerRecordsBatch(recordIds);

        StudentWorkspaceDtos.StudentRecordsPayload payload = new StudentWorkspaceDtos.StudentRecordsPayload(
                AuthUserResponse.from(student),
                assembler.buildRecordSummary(records),
                records.stream()
                        .map(record -> assembler.toStudentScoreRecordItem(record, answerRecordsMap.get(record.getRecordId())))
                        .collect(Collectors.toList())
        );
        return ApiResponse.success("学生成绩中心加载成功", payload);
    }

    @GetMapping("/{userId}/records/{recordId}")
    public ApiResponse<StudentWorkspaceDtos.StudentRecordDetailPayload> getStudentRecordDetail(
            @PathVariable("userId") Integer userId,
            @PathVariable("recordId") Integer recordId
    ) {
        User student = requireStudent(userId);
        ExamRecord record = requireOwnedRecord(userId, recordId);
        Paper paper = record.getPaper() != null ? record.getPaper() : paperService.getPaperById(record.getPaperId());
        List<AnswerRecord> answerRecords = examService.getAnswerRecords(recordId);

        StudentWorkspaceDtos.StudentRecordDetailPayload payload = new StudentWorkspaceDtos.StudentRecordDetailPayload(
                AuthUserResponse.from(student),
                assembler.toStudentRecordDetailItem(record, paper, answerRecords),
                answerRecords.stream().map(assembler::toAnswerRecordItem).collect(Collectors.toList())
        );
        return ApiResponse.success("考试记录详情加载成功", payload);
    }

    @GetMapping("/{userId}/records/{recordId}/exam")
    public ApiResponse<StudentWorkspaceDtos.StudentExamSessionPayload> getStudentExamSession(
            @PathVariable("userId") Integer userId,
            @PathVariable("recordId") Integer recordId
    ) {
        User student = requireStudent(userId);
        ExamRecord record = requireOwnedRecord(userId, recordId);
        Paper paper = record.getPaper() != null ? record.getPaper() : paperService.getPaperById(record.getPaperId());

        StudentWorkspaceDtos.StudentExamSessionPayload payload = new StudentWorkspaceDtos.StudentExamSessionPayload(
                AuthUserResponse.from(student),
                assembler.toExamLifecycleRecordItem(record),
                assembler.toStudentPaperItem(paper, record),
                paper.getQuestions().stream().map(assembler::toQuestionExamItem).collect(Collectors.toList()),
                assembler.calculateRemainingSeconds(record, paper),
                assembler.calculateDeadlineTime(record, paper)
        );
        return ApiResponse.success("考试作答页加载成功", payload);
    }

    @PostMapping("/{userId}/records/{recordId}/submit")
    public ApiResponse<StudentWorkspaceDtos.StudentSubmitResultPayload> submitExam(
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

        StudentWorkspaceDtos.StudentSubmitResultPayload payload = new StudentWorkspaceDtos.StudentSubmitResultPayload(
                AuthUserResponse.from(student),
                assembler.toSubmitResultItem(submittedRecord, paper, score, answerRecords)
        );
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
