package com.exam.api.controller;

import com.exam.api.common.ApiResponse;
import com.exam.api.dto.AuthUserResponse;
import com.exam.api.dto.StudentSubmitExamRequest;
import com.exam.exception.BusinessException;
import com.exam.model.AnswerRecord;
import com.exam.model.ExamRecord;
import com.exam.model.Paper;
import com.exam.model.Question;
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
import java.time.Duration;
import java.time.LocalDateTime;
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

    public StudentWorkspaceController(UserService userService, PaperService paperService, ExamService examService) {
        this.userService = userService;
        this.paperService = paperService;
        this.examService = examService;
    }

    @GetMapping("/{userId}/papers")
    public ApiResponse<Map<String, Object>> getStudentPapers(@PathVariable("userId") Integer userId) {
        User student = requireStudent(userId);
        List<Paper> papers = paperService.getAllPublishedPapersOptimized();
        List<ExamRecord> records = examService.getStudentExamRecordsOptimized(userId);
        Map<Integer, ExamRecord> latestRecordByPaperId = resolveLatestRecordByPaperId(records);

        long completedCount = records.stream()
                .filter(record -> record.getStatus() == ExamStatus.SUBMITTED || record.getStatus() == ExamStatus.TIMEOUT)
                .count();
        long inProgressCount = records.stream()
                .filter(record -> record.getStatus() == ExamStatus.IN_PROGRESS)
                .count();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("paperCount", papers.size());
        summary.put("completedCount", completedCount);
        summary.put("inProgressCount", inProgressCount);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("user", AuthUserResponse.from(student));
        payload.put("summary", summary);
        payload.put("papers", papers.stream()
                .map(paper -> toStudentPaperItem(paper, latestRecordByPaperId.get(paper.getPaperId())))
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
        ExamRecord latestRecord = resolveLatestRecordByPaperId(records).get(paperId);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("user", AuthUserResponse.from(student));
        payload.put("paper", toStudentPaperItem(paper, latestRecord));
        payload.put("questions", paper.getQuestions().stream().map(this::toQuestionExamItem).collect(Collectors.toList()));
        return ApiResponse.success("考试详情加载成功", payload);
    }

    @PostMapping("/{userId}/papers/{paperId}/start")
    public ApiResponse<Map<String, Object>> startExam(
            @PathVariable("userId") Integer userId,
            @PathVariable("paperId") Integer paperId
    ) {
        User student = requireStudent(userId);
        Paper paper = requirePublishedPaper(paperId);
        ExamRecord record = findInProgressRecord(userId, paperId);
        boolean resumed = record != null;
        if (record == null) {
            record = examService.startExam(userId, paperId);
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("user", AuthUserResponse.from(student));
        payload.put("record", toExamLifecycleRecordItem(record));
        payload.put("paper", toStudentPaperItem(paper, record));
        payload.put("questions", paper.getQuestions().stream().map(this::toQuestionExamItem).collect(Collectors.toList()));
        payload.put("remainingSeconds", calculateRemainingSeconds(record, paper));
        payload.put("deadlineTime", calculateDeadlineTime(record, paper));
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

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("user", AuthUserResponse.from(student));
        payload.put("summary", summary);
        payload.put("records", records.stream()
                .map(record -> toStudentScoreRecordItem(record, answerRecordsMap.get(record.getRecordId())))
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
        payload.put("record", toStudentRecordDetailItem(record, paper, answerRecords, answeredCount, correctCount, wrongCount));
        payload.put("answers", answerRecords.stream().map(this::toAnswerRecordItem).collect(Collectors.toList()));
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
        payload.put("record", toExamLifecycleRecordItem(record));
        payload.put("paper", toStudentPaperItem(paper, record));
        payload.put("questions", paper.getQuestions().stream().map(this::toQuestionExamItem).collect(Collectors.toList()));
        payload.put("remainingSeconds", calculateRemainingSeconds(record, paper));
        payload.put("deadlineTime", calculateDeadlineTime(record, paper));
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
        payload.put("result", toSubmitResultItem(submittedRecord, paper, score, answerRecords.size(), answeredCount, correctCount, wrongCount));
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

    private ExamRecord findInProgressRecord(Integer userId, Integer paperId) {
        List<ExamRecord> records = examService.getStudentExamRecordsOptimized(userId);
        return records.stream()
                .filter(record -> paperId.equals(record.getPaperId()))
                .filter(record -> record.getStatus() == ExamStatus.IN_PROGRESS)
                .findFirst()
                .orElse(null);
    }

    private Map<String, Object> toStudentPaperItem(Paper paper, ExamRecord latestRecord) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("paperId", paper.getPaperId());
        item.put("paperName", paper.getPaperName());
        item.put("subject", paper.getSubject());
        item.put("duration", paper.getDuration());
        item.put("totalScore", paper.getTotalScore());
        item.put("passScore", paper.getPassScore());
        item.put("description", paper.getDescription());
        item.put("questionCount", resolveQuestionCount(paper));
        item.put("published", Boolean.TRUE.equals(paper.getIsPublished()));
        item.put("hasInProgressRecord", latestRecord != null && latestRecord.getStatus() == ExamStatus.IN_PROGRESS);
        if (latestRecord != null) {
            item.put("latestRecord", toStudentRecordCard(latestRecord));
        }
        return item;
    }

    private Map<String, Object> toQuestionExamItem(Question question) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("questionId", question.getQuestionId());
        item.put("questionType", question.getQuestionType() == null ? null : question.getQuestionType().name());
        item.put("subject", question.getSubject());
        item.put("content", question.getContent());
        item.put("optionA", question.getOptionA());
        item.put("optionB", question.getOptionB());
        item.put("optionC", question.getOptionC());
        item.put("optionD", question.getOptionD());
        item.put("score", question.getScore());
        return item;
    }

    private Map<String, Object> toExamLifecycleRecordItem(ExamRecord record) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("recordId", record.getRecordId());
        item.put("studentId", record.getStudentId());
        item.put("paperId", record.getPaperId());
        item.put("status", record.getStatus() == null ? null : record.getStatus().name());
        item.put("startTime", record.getStartTime());
        item.put("submitTime", record.getSubmitTime());
        item.put("durationSeconds", calculateDurationSeconds(record));
        item.put("resumeAvailable", record.getStatus() == ExamStatus.IN_PROGRESS);
        return item;
    }

    private Map<String, Object> toStudentRecordCard(ExamRecord record) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("recordId", record.getRecordId());
        item.put("paperId", record.getPaperId());
        item.put("paperName", record.getPaper() != null ? record.getPaper().getPaperName() : null);
        item.put("status", record.getStatus() != null ? record.getStatus().name() : null);
        item.put("score", record.getScore());
        item.put("submitTime", record.getSubmitTime());
        item.put("startTime", record.getStartTime());
        item.put("durationSeconds", calculateDurationSeconds(record));
        item.put("resumeAvailable", record.getStatus() == ExamStatus.IN_PROGRESS);
        return item;
    }

    private Map<String, Object> toStudentScoreRecordItem(ExamRecord record, List<AnswerRecord> answerRecords) {
        List<AnswerRecord> safeAnswers = answerRecords == null ? List.of() : answerRecords;
        long correctCount = safeAnswers.stream()
                .filter(answer -> Boolean.TRUE.equals(answer.getIsCorrect()))
                .count();
        long wrongCount = safeAnswers.stream()
                .filter(answer -> answer.getStudentAnswer() != null && !answer.getStudentAnswer().trim().isEmpty())
                .filter(answer -> !Boolean.TRUE.equals(answer.getIsCorrect()))
                .count();

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("recordId", record.getRecordId());
        item.put("paperId", record.getPaperId());
        item.put("paperName", record.getPaper() != null ? record.getPaper().getPaperName() : null);
        item.put("totalScore", record.getPaper() != null ? record.getPaper().getTotalScore() : null);
        item.put("score", record.getScore());
        item.put("status", record.getStatus() != null ? record.getStatus().name() : null);
        item.put("submitTime", record.getSubmitTime());
        item.put("startTime", record.getStartTime());
        item.put("durationSeconds", calculateDurationSeconds(record));
        item.put("correctCount", correctCount);
        item.put("wrongCount", wrongCount);
        item.put("resumeAvailable", record.getStatus() == ExamStatus.IN_PROGRESS);
        return item;
    }

    private Map<String, Object> toStudentRecordDetailItem(
            ExamRecord record,
            Paper paper,
            List<AnswerRecord> answerRecords,
            long answeredCount,
            long correctCount,
            long wrongCount
    ) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("recordId", record.getRecordId());
        item.put("paperId", record.getPaperId());
        item.put("paperName", paper != null ? paper.getPaperName() : null);
        item.put("subject", paper != null ? paper.getSubject() : null);
        item.put("totalScore", paper != null ? paper.getTotalScore() : null);
        item.put("passScore", paper != null ? paper.getPassScore() : null);
        item.put("score", record.getScore());
        item.put("status", record.getStatus() == null ? null : record.getStatus().name());
        item.put("startTime", record.getStartTime());
        item.put("submitTime", record.getSubmitTime());
        item.put("durationSeconds", calculateDurationSeconds(record));
        item.put("questionCount", answerRecords.size());
        item.put("answeredCount", answeredCount);
        item.put("correctCount", correctCount);
        item.put("wrongCount", wrongCount);
        item.put("resumeAvailable", record.getStatus() == ExamStatus.IN_PROGRESS);
        item.put("passed",
                record.getScore() != null
                        && paper != null
                        && paper.getPassScore() != null
                        && record.getScore().compareTo(BigDecimal.valueOf(paper.getPassScore())) >= 0);
        return item;
    }

    private Map<String, Object> toAnswerRecordItem(AnswerRecord answerRecord) {
        Question question = answerRecord.getQuestion();
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("answerId", answerRecord.getAnswerId());
        item.put("recordId", answerRecord.getRecordId());
        item.put("questionId", answerRecord.getQuestionId());
        item.put("questionType", question != null && question.getQuestionType() != null ? question.getQuestionType().name() : null);
        item.put("content", question != null ? question.getContent() : null);
        item.put("optionA", question != null ? question.getOptionA() : null);
        item.put("optionB", question != null ? question.getOptionB() : null);
        item.put("optionC", question != null ? question.getOptionC() : null);
        item.put("optionD", question != null ? question.getOptionD() : null);
        item.put("studentAnswer", answerRecord.getStudentAnswer());
        item.put("correctAnswer", question != null ? question.getCorrectAnswer() : null);
        item.put("analysis", question != null ? question.getAnalysis() : null);
        item.put("score", answerRecord.getScore());
        item.put("isCorrect", Boolean.TRUE.equals(answerRecord.getIsCorrect()));
        return item;
    }

    private Map<String, Object> toSubmitResultItem(
            ExamRecord record,
            Paper paper,
            BigDecimal score,
            int questionCount,
            long answeredCount,
            long correctCount,
            long wrongCount
    ) {
        boolean passed = false;
        if (score != null && paper.getPassScore() != null) {
            passed = score.compareTo(BigDecimal.valueOf(paper.getPassScore())) >= 0;
        }

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("recordId", record.getRecordId());
        item.put("paperId", record.getPaperId());
        item.put("paperName", paper.getPaperName());
        item.put("subject", paper.getSubject());
        item.put("score", score);
        item.put("totalScore", paper.getTotalScore());
        item.put("passScore", paper.getPassScore());
        item.put("passed", passed);
        item.put("status", record.getStatus() != null ? record.getStatus().name() : null);
        item.put("submitTime", record.getSubmitTime());
        item.put("durationSeconds", calculateDurationSeconds(record));
        item.put("questionCount", questionCount);
        item.put("answeredCount", answeredCount);
        item.put("correctCount", correctCount);
        item.put("wrongCount", wrongCount);
        return item;
    }

    private Map<Integer, ExamRecord> resolveLatestRecordByPaperId(List<ExamRecord> records) {
        Map<Integer, ExamRecord> latest = new LinkedHashMap<>();
        for (ExamRecord record : records) {
            Integer paperId = record.getPaperId();
            if (paperId == null) {
                continue;
            }

            ExamRecord existing = latest.get(paperId);
            if (existing == null || compareRecordOrder(record, existing) > 0) {
                latest.put(paperId, record);
            }
        }
        return latest;
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
        int questionCount = paper.getSingleCount()
                + paper.getMultipleCount()
                + paper.getJudgeCount()
                + paper.getBlankCount();
        if (questionCount > 0) {
            return questionCount;
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

    private long calculateRemainingSeconds(ExamRecord record, Paper paper) {
        if (record.getStatus() != ExamStatus.IN_PROGRESS) {
            return 0;
        }
        if (record.getStartTime() == null || paper == null || paper.getDuration() == null) {
            return 0;
        }

        LocalDateTime deadline = record.getStartTime().plusMinutes(paper.getDuration());
        return Math.max(0, Duration.between(LocalDateTime.now(), deadline).getSeconds());
    }

    private LocalDateTime calculateDeadlineTime(ExamRecord record, Paper paper) {
        if (record.getStartTime() == null || paper == null || paper.getDuration() == null) {
            return null;
        }
        return record.getStartTime().plusMinutes(paper.getDuration());
    }

    private String normalizeBlank(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
