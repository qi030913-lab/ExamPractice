package com.exam.api.controller;

import com.exam.api.assembler.TeacherWorkspaceAssembler;
import com.exam.api.common.ApiResponse;
import com.exam.api.dto.AuthUserResponse;
import com.exam.api.dto.TeacherImportPaperRequest;
import com.exam.api.dto.TeacherUpdatePaperRequest;
import com.exam.api.dto.TeacherWorkspaceDtos;
import com.exam.api.support.ExamAccessGuard;
import com.exam.api.support.UserRoleGuard;
import com.exam.exception.BusinessException;
import com.exam.model.AnswerRecord;
import com.exam.model.ExamRecord;
import com.exam.model.Paper;
import com.exam.model.Question;
import com.exam.model.User;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teacher")
public class TeacherWorkspaceController {
    private final UserService userService;
    private final PaperService paperService;
    private final ExamService examService;
    private final QuestionService questionService;
    private final TeacherWorkspaceAssembler assembler;
    private final UserRoleGuard userRoleGuard;
    private final ExamAccessGuard examAccessGuard;

    public TeacherWorkspaceController(
            UserService userService,
            PaperService paperService,
            ExamService examService,
            QuestionService questionService,
            TeacherWorkspaceAssembler assembler,
            UserRoleGuard userRoleGuard,
            ExamAccessGuard examAccessGuard
    ) {
        this.userService = userService;
        this.paperService = paperService;
        this.examService = examService;
        this.questionService = questionService;
        this.assembler = assembler;
        this.userRoleGuard = userRoleGuard;
        this.examAccessGuard = examAccessGuard;
    }

    @GetMapping("/{userId}/papers")
    public ApiResponse<TeacherWorkspaceDtos.TeacherPapersPayload> getTeacherPapers(@PathVariable("userId") Integer userId) {
        User teacher = userRoleGuard.requireTeacher(userId);
        List<Paper> papers = paperService.getAllPapersOptimized();
        long publishedCount = papers.stream()
                .filter(paper -> Boolean.TRUE.equals(paper.getIsPublished()))
                .count();

        TeacherWorkspaceDtos.TeacherPapersPayload payload = new TeacherWorkspaceDtos.TeacherPapersPayload(
                AuthUserResponse.from(teacher),
                new TeacherWorkspaceDtos.PaperSummary(papers.size(), publishedCount, papers.size() - publishedCount),
                papers.stream().map(assembler::toTeacherPaperItem).collect(Collectors.toList())
        );
        return ApiResponse.success("教师试卷中心加载成功", payload);
    }

    @PostMapping("/{userId}/papers/{paperId}/publish")
    public ApiResponse<TeacherWorkspaceDtos.PaperMutationPayload> publishPaper(
            @PathVariable("userId") Integer userId,
            @PathVariable("paperId") Integer paperId
    ) {
        userRoleGuard.requireTeacher(userId);
        paperService.publishPaper(paperId);
        return ApiResponse.success(
                "试卷发布成功",
                new TeacherWorkspaceDtos.PaperMutationPayload(assembler.toTeacherPaperItem(paperService.getPaperById(paperId)))
        );
    }

    @PostMapping("/{userId}/papers/{paperId}/unpublish")
    public ApiResponse<TeacherWorkspaceDtos.PaperMutationPayload> unpublishPaper(
            @PathVariable("userId") Integer userId,
            @PathVariable("paperId") Integer paperId
    ) {
        userRoleGuard.requireTeacher(userId);
        paperService.unpublishPaper(paperId);
        return ApiResponse.success(
                "试卷已取消发布",
                new TeacherWorkspaceDtos.PaperMutationPayload(assembler.toTeacherPaperItem(paperService.getPaperById(paperId)))
        );
    }

    @DeleteMapping("/{userId}/papers/{paperId}")
    public ApiResponse<TeacherWorkspaceDtos.DeletePaperPayload> deletePaper(
            @PathVariable("userId") Integer userId,
            @PathVariable("paperId") Integer paperId
    ) {
        userRoleGuard.requireTeacher(userId);
        paperService.deletePaper(paperId);
        return ApiResponse.success("试卷删除成功", new TeacherWorkspaceDtos.DeletePaperPayload(paperId));
    }

    @GetMapping("/{userId}/papers/{paperId}")
    public ApiResponse<TeacherWorkspaceDtos.TeacherPaperDetailPayload> getTeacherPaperDetail(
            @PathVariable("userId") Integer userId,
            @PathVariable("paperId") Integer paperId
    ) {
        userRoleGuard.requireTeacher(userId);
        Paper paper = paperService.getPaperById(paperId);

        TeacherWorkspaceDtos.TeacherPaperDetailPayload payload = new TeacherWorkspaceDtos.TeacherPaperDetailPayload(
                assembler.toTeacherPaperItem(paper),
                paper.getQuestions().stream().map(assembler::toQuestionDetailItem).collect(Collectors.toList())
        );
        return ApiResponse.success("试卷详情加载成功", payload);
    }

    @PutMapping("/{userId}/papers/{paperId}")
    public ApiResponse<TeacherWorkspaceDtos.TeacherPaperDetailPayload> updateTeacherPaper(
            @PathVariable("userId") Integer userId,
            @PathVariable("paperId") Integer paperId,
            @Valid @RequestBody TeacherUpdatePaperRequest request
    ) {
        userRoleGuard.requireTeacher(userId);
        Paper paper = paperService.getPaperById(paperId);
        paper.setPaperName(request.getPaperName().trim());
        paper.setSubject(request.getSubject().trim());
        paper.setPassScore(request.getPassScore());
        paper.setDuration(request.getDuration());
        paper.setDescription(normalizeBlank(request.getDescription()));
        paperService.updatePaper(paper);

        Paper updatedPaper = paperService.getPaperById(paperId);
        TeacherWorkspaceDtos.TeacherPaperDetailPayload payload = new TeacherWorkspaceDtos.TeacherPaperDetailPayload(
                assembler.toTeacherPaperItem(updatedPaper),
                updatedPaper.getQuestions().stream().map(assembler::toQuestionDetailItem).collect(Collectors.toList())
        );
        return ApiResponse.success("试卷更新成功", payload);
    }

    @GetMapping("/{userId}/students")
    public ApiResponse<TeacherWorkspaceDtos.TeacherStudentsPayload> getTeacherStudents(@PathVariable("userId") Integer userId) {
        User teacher = userRoleGuard.requireTeacher(userId);
        List<User> students = userService.getStudents();
        List<Integer> studentIds = students.stream()
                .map(User::getUserId)
                .filter(id -> id != null)
                .collect(Collectors.toList());
        var recordsByStudentId = examService.getStudentExamRecordsByStudentIds(studentIds);

        TeacherWorkspaceDtos.TeacherStudentsPayload payload = new TeacherWorkspaceDtos.TeacherStudentsPayload(
                AuthUserResponse.from(teacher),
                new TeacherWorkspaceDtos.StudentListSummary(students.size()),
                students.stream()
                        .map(student -> assembler.toTeacherStudentItem(
                                student,
                                recordsByStudentId.getOrDefault(student.getUserId(), List.of())
                        ))
                        .collect(Collectors.toList())
        );
        return ApiResponse.success("教师学生中心加载成功", payload);
    }

    @GetMapping("/{userId}/students/{studentId}/records")
    public ApiResponse<TeacherWorkspaceDtos.TeacherStudentRecordsPayload> getStudentRecords(
            @PathVariable("userId") Integer userId,
            @PathVariable("studentId") Integer studentId
    ) {
        userRoleGuard.requireTeacher(userId);
        User student = userRoleGuard.requireStudent(studentId);
        List<ExamRecord> records = examService.getStudentExamRecordsOptimized(studentId);

        TeacherWorkspaceDtos.TeacherStudentRecordsPayload payload = new TeacherWorkspaceDtos.TeacherStudentRecordsPayload(
                AuthUserResponse.from(student),
                assembler.buildStudentSummary(records),
                records.stream().map(assembler::toTeacherStudentRecordItem).collect(Collectors.toList())
        );
        return ApiResponse.success("学生考试记录加载成功", payload);
    }

    @GetMapping("/{userId}/students/{studentId}")
    public ApiResponse<TeacherWorkspaceDtos.TeacherStudentDetailPayload> getTeacherStudentDetail(
            @PathVariable("userId") Integer userId,
            @PathVariable("studentId") Integer studentId
    ) {
        userRoleGuard.requireTeacher(userId);
        User student = userRoleGuard.requireStudent(studentId);
        List<ExamRecord> records = examService.getStudentExamRecordsOptimized(studentId);

        TeacherWorkspaceDtos.TeacherStudentDetailPayload payload = new TeacherWorkspaceDtos.TeacherStudentDetailPayload(
                assembler.toTeacherStudentItem(student, records),
                assembler.buildStudentSummary(records),
                records.stream().map(assembler::toTeacherStudentRecordItem).collect(Collectors.toList())
        );
        return ApiResponse.success("学生详情加载成功", payload);
    }

    @GetMapping("/{userId}/students/{studentId}/records/{recordId}")
    public ApiResponse<TeacherWorkspaceDtos.TeacherStudentRecordDetailPayload> getTeacherStudentRecordDetail(
            @PathVariable("userId") Integer userId,
            @PathVariable("studentId") Integer studentId,
            @PathVariable("recordId") Integer recordId
    ) {
        userRoleGuard.requireTeacher(userId);
        User student = userRoleGuard.requireStudent(studentId);
        List<ExamRecord> studentRecords = examService.getStudentExamRecordsOptimized(studentId);
        ExamRecord record = examAccessGuard.requireOwnedRecord(studentId, recordId);
        Paper paper = examAccessGuard.resolvePaper(record);
        List<AnswerRecord> answerRecords = examService.getAnswerRecords(recordId);

        TeacherWorkspaceDtos.TeacherStudentRecordDetailPayload payload = new TeacherWorkspaceDtos.TeacherStudentRecordDetailPayload(
                assembler.toTeacherStudentItem(student, studentRecords),
                assembler.toTeacherStudentRecordDetailItem(record, paper, answerRecords),
                answerRecords.stream().map(assembler::toAnswerRecordItem).collect(Collectors.toList())
        );
        return ApiResponse.success("考试记录详情加载成功", payload);
    }

    @GetMapping("/{userId}/import-template")
    public ApiResponse<TeacherWorkspaceDtos.ImportTemplatePayload> getImportTemplate(@PathVariable("userId") Integer userId) {
        userRoleGuard.requireTeacher(userId);
        return ApiResponse.success(
                "导题模板加载成功",
                new TeacherWorkspaceDtos.ImportTemplatePayload("题目导入模板.txt", QuestionImportUtil.buildTemplateContent())
        );
    }

    @PostMapping("/{userId}/import-paper")
    public ApiResponse<TeacherWorkspaceDtos.ImportPaperPayload> importPaper(
            @PathVariable("userId") Integer userId,
            @Valid @RequestBody TeacherImportPaperRequest request
    ) {
        userRoleGuard.requireTeacher(userId);

        List<Question> importedQuestions;
        try {
            importedQuestions = QuestionImportUtil.importFromText(request.getSourceText(), userId);
        } catch (Exception ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }

        Paper paper = new Paper();
        paper.setPaperName(request.getPaperName().trim());
        paper.setSubject(request.getSubject().trim());
        paper.setPassScore(request.getPassScore());
        paper.setDuration(request.getDuration());
        paper.setDescription(normalizeBlank(request.getDescription()));
        paper.setCreatorId(userId);

        PaperService.ImportPaperResult importResult = paperService.importPaper(paper, importedQuestions);
        Paper createdPaper = paperService.getPaperById(importResult.getPaperId());

        TeacherWorkspaceDtos.ImportPaperPayload payload = new TeacherWorkspaceDtos.ImportPaperPayload(
                assembler.toTeacherPaperItem(createdPaper),
                createdPaper.getQuestions().stream().map(assembler::toQuestionItem).collect(Collectors.toList()),
                new TeacherWorkspaceDtos.ImportPaperSummary(
                        importResult.getSourceQuestionCount(),
                        importResult.getLinkedQuestionCount(),
                        importResult.getCreatedQuestionCount(),
                        importResult.getReusedQuestionCount()
                )
        );
        return ApiResponse.success("导题建卷成功", payload);
    }

    private String normalizeBlank(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
