package com.exam.api.controller;

import com.exam.api.assembler.StudentWorkspaceAssembler;
import com.exam.api.common.ApiResponse;
import com.exam.api.dto.AuthUserResponse;
import com.exam.api.dto.StudentWorkspaceDtos;
import com.exam.api.dto.WorkbenchDtos;
import com.exam.api.support.UserRoleGuard;
import com.exam.model.ExamRecord;
import com.exam.model.Paper;
import com.exam.model.User;
import com.exam.model.enums.ExamStatus;
import com.exam.service.ExamService;
import com.exam.service.PaperService;
import com.exam.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/workbench")
public class WorkbenchController {
    private final UserService userService;
    private final PaperService paperService;
    private final ExamService examService;
    private final StudentWorkspaceAssembler studentWorkspaceAssembler;
    private final UserRoleGuard userRoleGuard;

    public WorkbenchController(
            UserService userService,
            PaperService paperService,
            ExamService examService,
            StudentWorkspaceAssembler studentWorkspaceAssembler,
            UserRoleGuard userRoleGuard
    ) {
        this.userService = userService;
        this.paperService = paperService;
        this.examService = examService;
        this.studentWorkspaceAssembler = studentWorkspaceAssembler;
        this.userRoleGuard = userRoleGuard;
    }

    @GetMapping("/teacher/{userId}")
    public ApiResponse<WorkbenchDtos.TeacherWorkbenchPayload> teacherWorkbench(@PathVariable("userId") Integer userId) {
        User teacher = userRoleGuard.requireTeacher(userId);
        List<Paper> papers = paperService.getAllPapersOptimized();
        List<User> students = userService.getStudents();
        long publishedCount = papers.stream()
                .filter(paper -> Boolean.TRUE.equals(paper.getIsPublished()))
                .count();

        WorkbenchDtos.TeacherWorkbenchPayload payload = new WorkbenchDtos.TeacherWorkbenchPayload(
                AuthUserResponse.from(teacher),
                new WorkbenchDtos.TeacherStats(papers.size(), publishedCount, students.size())
        );
        return ApiResponse.success("教师工作台加载成功", payload);
    }

    @GetMapping("/student/{userId}")
    public ApiResponse<WorkbenchDtos.StudentWorkbenchPayload> studentWorkbench(@PathVariable("userId") Integer userId) {
        User student = userRoleGuard.requireStudent(userId);
        List<Paper> publishedPapers = paperService.getAllPublishedPapersOptimized();
        List<ExamRecord> records = examService.getStudentExamRecordsOptimized(userId);
        StudentWorkspaceDtos.RecordSummary recordSummary = studentWorkspaceAssembler.buildRecordSummary(records);
        StudentWorkspaceDtos.StudentRecordCard ongoingRecord = records.stream()
                .filter(record -> record.getStatus() == ExamStatus.IN_PROGRESS)
                .max(Comparator.comparing(ExamRecord::getRecordId, Comparator.nullsFirst(Integer::compareTo)))
                .map(studentWorkspaceAssembler::toStudentRecordCard)
                .orElse(null);

        WorkbenchDtos.StudentWorkbenchPayload payload = new WorkbenchDtos.StudentWorkbenchPayload(
                AuthUserResponse.from(student),
                new WorkbenchDtos.StudentStats(
                        publishedPapers.size(),
                        recordSummary.recordCount(),
                        recordSummary.submittedCount(),
                        recordSummary.averageScore()
                ),
                ongoingRecord
        );
        return ApiResponse.success("学生工作台加载成功", payload);
    }
}
