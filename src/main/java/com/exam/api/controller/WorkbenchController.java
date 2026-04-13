package com.exam.api.controller;

import com.exam.api.assembler.StudentWorkspaceAssembler;
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

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workbench")
public class WorkbenchController {
    private final UserService userService;
    private final PaperService paperService;
    private final ExamService examService;
    private final StudentWorkspaceAssembler studentWorkspaceAssembler;

    public WorkbenchController(
            UserService userService,
            PaperService paperService,
            ExamService examService,
            StudentWorkspaceAssembler studentWorkspaceAssembler
    ) {
        this.userService = userService;
        this.paperService = paperService;
        this.examService = examService;
        this.studentWorkspaceAssembler = studentWorkspaceAssembler;
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

        return ApiResponse.success("教师工作台加载成功", payload);
    }

    @GetMapping("/student/{userId}")
    public ApiResponse<Map<String, Object>> studentWorkbench(@PathVariable("userId") Integer userId) {
        User student = requireRole(userId, UserRole.STUDENT);
        List<Paper> publishedPapers = paperService.getAllPublishedPapersOptimized();
        List<ExamRecord> records = examService.getStudentExamRecordsOptimized(userId);
        Map<String, Object> recordSummary = studentWorkspaceAssembler.buildRecordSummary(records);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("publishedPaperCount", publishedPapers.size());
        stats.put("recordCount", recordSummary.get("recordCount"));
        stats.put("submittedCount", recordSummary.get("submittedCount"));
        stats.put("averageScore", recordSummary.get("averageScore"));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("user", AuthUserResponse.from(student));
        payload.put("stats", stats);
        payload.put("ongoingRecord", records.stream()
                .filter(record -> record.getStatus() == ExamStatus.IN_PROGRESS)
                .max(Comparator.comparing(ExamRecord::getRecordId, Comparator.nullsFirst(Integer::compareTo)))
                .map(studentWorkspaceAssembler::toStudentRecordCard)
                .orElse(null));

        return ApiResponse.success("学生工作台加载成功", payload);
    }

    private User requireRole(Integer userId, UserRole role) {
        User user = userService.getUserById(userId);
        if (user.getRole() != role) {
            throw new BusinessException("当前用户角色不匹配");
        }
        return user;
    }
}
