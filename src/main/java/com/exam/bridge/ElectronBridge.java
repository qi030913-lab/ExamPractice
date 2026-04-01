package com.exam.bridge;

import com.exam.exception.AuthenticationException;
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

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Minimal CLI bridge for Electron.
 * This lets the Electron app call the current Java business layer
 * without opening Swing windows.
 */
public class ElectronBridge {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final int DEFAULT_LIST_LIMIT = 6;

    private final UserService userService;
    private final PaperService paperService;
    private final ExamService examService;

    public ElectronBridge() {
        this.userService = new UserService();
        this.paperService = new PaperService();
        this.examService = new ExamService();
    }

    public static void main(String[] args) {
        ElectronBridge bridge = new ElectronBridge();
        bridge.run(args);
    }

    private void run(String[] args) {
        if (args == null || args.length == 0) {
            printAndExit(false, "Missing bridge command.", null, 1);
            return;
        }

        String command = args[0];
        try {
            switch (command) {
                case "health":
                    printAndExit(true, "Bridge is ready.", object(
                            stringField("bridge", "exam-electron-bridge"),
                            stringField("version", "1"),
                            stringField("javaVersion", System.getProperty("java.version"))
                    ), 0);
                    break;
                case "login":
                    handleLogin(args);
                    break;
                case "student-overview":
                    handleStudentOverview(args);
                    break;
                case "teacher-overview":
                    handleTeacherOverview(args);
                    break;
                case "student-papers":
                    handleStudentPapers(args);
                    break;
                case "student-records":
                    handleStudentRecords(args);
                    break;
                case "paper-detail":
                    handlePaperDetail(args);
                    break;
                case "record-detail":
                    handleRecordDetail(args);
                    break;
                case "start-exam":
                    handleStartExam(args);
                    break;
                case "submit-exam":
                    handleSubmitExam(args);
                    break;
                default:
                    printAndExit(false, "Unknown bridge command: " + command, null, 1);
                    break;
            }
        } catch (AuthenticationException | BusinessException ex) {
            printAndExit(false, ex.getMessage(), null, 1);
        } catch (Throwable ex) {
            printAndExit(false, resolveErrorMessage(ex), null, 1);
        }
    }

    private void handleLogin(String[] args) {
        requireLength(args, 5, "Usage: login <student|teacher> <realName> <account> <password>");

        UserRole role = parseRole(args[1]);
        String realName = args[2];
        String account = args[3];
        String password = args[4];

        User user = userService.login(realName, account, password, role);
        printAndExit(true, "Login succeeded.", object(
                stringField("role", role.name()),
                field("user", serializeUser(user))
        ), 0);
    }

    private void handleStudentOverview(String[] args) {
        requireLength(args, 2, "Usage: student-overview <userId>");

        Integer userId = parseInteger(args[1], "Student userId must be an integer.");
        User user = requireStudentUser(userId);

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

        printAndExit(true, "Student overview loaded.", object(
                field("summary", object(
                        stringField("role", "STUDENT"),
                        stringField("displayName", user.getRealName()),
                        stringField("studentNumber", user.getStudentNumber()),
                        numberField("publishedPaperCount", publishedPapers.size()),
                        numberField("recordCount", records.size()),
                        numberField("submittedCount", submittedCount),
                        decimalField("averageScore", averageScore)
                )),
                field("papers", array(publishedPapers.stream()
                        .limit(DEFAULT_LIST_LIMIT)
                        .map(this::serializePublishedPaper)
                        .collect(Collectors.toList()))),
                field("records", array(records.stream()
                        .limit(DEFAULT_LIST_LIMIT)
                        .map(this::serializeRecord)
                        .collect(Collectors.toList())))
        ), 0);
    }

    private void handleTeacherOverview(String[] args) {
        requireLength(args, 2, "Usage: teacher-overview <userId>");

        Integer userId = parseInteger(args[1], "Teacher userId must be an integer.");
        User user = requireTeacherUser(userId);

        List<Paper> papers = paperService.getAllPapersOptimized();
        List<User> students = userService.getStudents();
        long publishedCount = papers.stream()
                .filter(paper -> Boolean.TRUE.equals(paper.getIsPublished()))
                .count();

        printAndExit(true, "Teacher overview loaded.", object(
                field("summary", object(
                        stringField("role", "TEACHER"),
                        stringField("displayName", user.getRealName()),
                        stringField("teacherNumber", user.getStudentNumber()),
                        numberField("paperCount", papers.size()),
                        numberField("publishedCount", publishedCount),
                        numberField("studentCount", students.size())
                )),
                field("papers", array(papers.stream()
                        .limit(DEFAULT_LIST_LIMIT)
                        .map(this::serializeManagedPaper)
                        .collect(Collectors.toList()))),
                field("students", array(students.stream()
                        .limit(DEFAULT_LIST_LIMIT)
                        .map(this::serializeStudent)
                        .collect(Collectors.toList())))
        ), 0);
    }

    private void handleStudentPapers(String[] args) {
        requireLength(args, 2, "Usage: student-papers <userId>");

        Integer userId = parseInteger(args[1], "Student userId must be an integer.");
        User user = requireStudentUser(userId);

        List<Paper> publishedPapers = paperService.getAllPublishedPapersOptimized();
        List<ExamRecord> records = examService.getStudentExamRecordsOptimized(userId);
        Map<Integer, ExamRecord> latestRecordByPaperId = resolveLatestRecordByPaperId(records);
        long completedCount = records.stream()
                .filter(record -> record.getStatus() == ExamStatus.SUBMITTED || record.getStatus() == ExamStatus.TIMEOUT)
                .count();
        long inProgressCount = records.stream()
                .filter(record -> record.getStatus() == ExamStatus.IN_PROGRESS)
                .count();

        printAndExit(true, "Student paper center loaded.", object(
                field("summary", object(
                        stringField("role", "STUDENT"),
                        stringField("displayName", user.getRealName()),
                        stringField("studentNumber", user.getStudentNumber()),
                        numberField("paperCount", publishedPapers.size()),
                        numberField("completedCount", completedCount),
                        numberField("inProgressCount", inProgressCount)
                )),
                field("papers", array(publishedPapers.stream()
                        .map(paper -> serializeStudentPaper(paper, latestRecordByPaperId.get(paper.getPaperId())))
                        .collect(Collectors.toList())))
        ), 0);
    }

    private void handleStudentRecords(String[] args) {
        requireLength(args, 2, "Usage: student-records <userId>");

        Integer userId = parseInteger(args[1], "Student userId must be an integer.");
        User user = requireStudentUser(userId);
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

        printAndExit(true, "Student record center loaded.", object(
                field("summary", object(
                        stringField("role", "STUDENT"),
                        stringField("displayName", user.getRealName()),
                        stringField("studentNumber", user.getStudentNumber()),
                        numberField("recordCount", records.size()),
                        numberField("submittedCount", submittedCount),
                        decimalField("averageScore", averageScore)
                )),
                field("records", array(records.stream()
                        .map(record -> serializeStudentScoreRecord(record, answerRecordsMap.get(record.getRecordId())))
                        .collect(Collectors.toList())))
        ), 0);
    }

    private void handlePaperDetail(String[] args) {
        requireLength(args, 2, "Usage: paper-detail <paperId>");

        Integer paperId = parseInteger(args[1], "Paper id must be an integer.");
        Paper paper = paperService.getPaperById(paperId);

        printAndExit(true, "Paper detail loaded.", object(
                field("paper", serializePaperDetail(paper)),
                field("questions", array(serializeQuestions(paper.getQuestions())))
        ), 0);
    }

    private void handleRecordDetail(String[] args) {
        requireLength(args, 3, "Usage: record-detail <studentId> <recordId>");

        Integer userId = parseInteger(args[1], "Student userId must be an integer.");
        Integer recordId = parseInteger(args[2], "Record id must be an integer.");
        requireStudentUser(userId);

        ExamRecord record = examService.getExamRecordById(recordId);
        if (record == null) {
            throw new BusinessException("Requested exam record does not exist.");
        }
        if (!userId.equals(record.getStudentId())) {
            throw new BusinessException("Requested exam record does not belong to current student.");
        }

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

        printAndExit(true, "Exam record detail loaded.", object(
                field("record", serializeStudentRecordDetail(record, paper, answerRecords, answeredCount, correctCount, wrongCount)),
                field("answers", array(serializeAnswerRecords(answerRecords)))
        ), 0);
    }

    private void handleStartExam(String[] args) {
        requireLength(args, 3, "Usage: start-exam <studentId> <paperId>");

        Integer studentId = parseInteger(args[1], "Student userId must be an integer.");
        Integer paperId = parseInteger(args[2], "Paper id must be an integer.");
        requireStudentUser(studentId);
        Paper paper = requirePublishedPaper(paperId);
        ExamRecord record = examService.startExam(studentId, paperId);

        printAndExit(true, "Exam started successfully.", object(
                field("record", serializeExamLifecycleRecord(record)),
                field("paper", serializePaperSummary(paper))
        ), 0);
    }

    private void handleSubmitExam(String[] args) {
        requireLength(args, 2, "Usage: submit-exam <recordId> [<questionId> <answer> ...]");

        if ((args.length - 2) % 2 != 0) {
            throw new BusinessException("Answer arguments must be provided as <questionId> <answer> pairs.");
        }

        Integer recordId = parseInteger(args[1], "Record id must be an integer.");
        Map<Integer, String> answers = parseAnswers(args);
        BigDecimal score = examService.submitExam(recordId, answers);
        ExamRecord record = examService.getExamRecordById(recordId);
        Paper paper = record.getPaper() != null ? record.getPaper() : paperService.getPaperById(record.getPaperId());

        printAndExit(true, "Exam submitted successfully.", object(
                field("result", serializeSubmitResult(record, paper, score))
        ), 0);
    }

    private User requireStudentUser(Integer userId) {
        User user = userService.getUserById(userId);
        if (user.getRole() != UserRole.STUDENT) {
            throw new BusinessException("Requested user is not a student.");
        }
        return user;
    }

    private User requireTeacherUser(Integer userId) {
        User user = userService.getUserById(userId);
        if (user.getRole() != UserRole.TEACHER) {
            throw new BusinessException("Requested user is not a teacher.");
        }
        return user;
    }

    private Paper requirePublishedPaper(Integer paperId) {
        Paper paper = paperService.getPaperById(paperId);
        if (!Boolean.TRUE.equals(paper.getIsPublished())) {
            throw new BusinessException("Selected paper is not published.");
        }
        return paper;
    }

    private Map<Integer, String> parseAnswers(String[] args) {
        Map<Integer, String> answers = new LinkedHashMap<>();
        for (int index = 2; index < args.length; index += 2) {
            Integer questionId = parseInteger(args[index], "Question id must be an integer.");
            String answer = args[index + 1];
            answers.put(questionId, answer == null ? "" : answer);
        }
        return answers;
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

    private UserRole parseRole(String rawRole) {
        if (rawRole == null || rawRole.trim().isEmpty()) {
            throw new BusinessException("Role is required.");
        }

        String normalized = rawRole.trim().toUpperCase(Locale.ROOT);
        if ("STUDENT".equals(normalized)) {
            return UserRole.STUDENT;
        }
        if ("TEACHER".equals(normalized)) {
            return UserRole.TEACHER;
        }
        throw new BusinessException("Unsupported role: " + rawRole);
    }

    private Integer parseInteger(String rawValue, String errorMessage) {
        try {
            return Integer.parseInt(rawValue);
        } catch (NumberFormatException ex) {
            throw new BusinessException(errorMessage);
        }
    }

    private void requireLength(String[] args, int expectedLength, String usage) {
        if (args.length < expectedLength) {
            throw new BusinessException(usage);
        }
    }

    private String serializeUser(User user) {
        return object(
                numberField("userId", user.getUserId()),
                stringField("realName", user.getRealName()),
                stringField("studentNumber", user.getStudentNumber()),
                stringField("role", user.getRole() != null ? user.getRole().name() : null)
        );
    }

    private String serializeStudent(User user) {
        return object(
                numberField("userId", user.getUserId()),
                stringField("realName", user.getRealName()),
                stringField("studentNumber", user.getStudentNumber()),
                stringField("createTime", formatDateTime(user.getCreateTime()))
        );
    }

    private String serializePublishedPaper(Paper paper) {
        return object(
                numberField("paperId", paper.getPaperId()),
                stringField("paperName", paper.getPaperName()),
                stringField("subject", paper.getSubject()),
                numberField("duration", paper.getDuration()),
                numberField("totalScore", paper.getTotalScore()),
                numberField("questionCount", resolveQuestionCount(paper)),
                booleanField("isPublished", Boolean.TRUE.equals(paper.getIsPublished()))
        );
    }

    private String serializeManagedPaper(Paper paper) {
        return object(
                numberField("paperId", paper.getPaperId()),
                stringField("paperName", paper.getPaperName()),
                stringField("subject", paper.getSubject()),
                numberField("totalScore", paper.getTotalScore()),
                numberField("duration", paper.getDuration()),
                numberField("questionCount", resolveQuestionCount(paper)),
                booleanField("isPublished", Boolean.TRUE.equals(paper.getIsPublished()))
        );
    }

    private String serializeStudentPaper(Paper paper, ExamRecord latestRecord) {
        List<String> fields = new ArrayList<>();
        fields.add(numberField("paperId", paper.getPaperId()));
        fields.add(stringField("paperName", paper.getPaperName()));
        fields.add(stringField("subject", paper.getSubject()));
        fields.add(numberField("duration", paper.getDuration()));
        fields.add(numberField("totalScore", paper.getTotalScore()));
        fields.add(numberField("passScore", paper.getPassScore()));
        fields.add(stringField("description", paper.getDescription()));
        fields.add(numberField("questionCount", resolveQuestionCount(paper)));
        fields.add(booleanField("isPublished", Boolean.TRUE.equals(paper.getIsPublished())));
        if (latestRecord != null) {
            fields.add(field("latestRecord", serializeRecord(latestRecord)));
        }
        return object(fields.toArray(new String[0]));
    }

    private String serializeStudentScoreRecord(ExamRecord record, List<AnswerRecord> answerRecords) {
        List<AnswerRecord> safeAnswers = answerRecords == null ? new ArrayList<>() : answerRecords;
        long correctCount = safeAnswers.stream()
                .filter(answer -> Boolean.TRUE.equals(answer.getIsCorrect()))
                .count();
        long wrongCount = safeAnswers.stream()
                .filter(answer -> answer.getStudentAnswer() != null && !answer.getStudentAnswer().trim().isEmpty())
                .filter(answer -> !Boolean.TRUE.equals(answer.getIsCorrect()))
                .count();

        return object(
                numberField("recordId", record.getRecordId()),
                numberField("paperId", record.getPaperId()),
                stringField("paperName", record.getPaper() != null ? record.getPaper().getPaperName() : null),
                numberField("totalScore", record.getPaper() != null ? record.getPaper().getTotalScore() : null),
                decimalField("score", record.getScore()),
                stringField("status", record.getStatus() != null ? record.getStatus().name() : null),
                stringField("submitTime", formatDateTime(record.getSubmitTime())),
                stringField("startTime", formatDateTime(record.getStartTime())),
                numberField("durationSeconds", calculateDurationSeconds(record)),
                numberField("correctCount", correctCount),
                numberField("wrongCount", wrongCount)
        );
    }

    private String serializePaperSummary(Paper paper) {
        return object(
                numberField("paperId", paper.getPaperId()),
                stringField("paperName", paper.getPaperName()),
                stringField("subject", paper.getSubject()),
                numberField("duration", paper.getDuration()),
                numberField("totalScore", paper.getTotalScore()),
                numberField("passScore", paper.getPassScore()),
                stringField("description", paper.getDescription()),
                numberField("questionCount", resolveQuestionCount(paper)),
                booleanField("isPublished", Boolean.TRUE.equals(paper.getIsPublished()))
        );
    }

    private String serializePaperDetail(Paper paper) {
        return object(
                numberField("paperId", paper.getPaperId()),
                stringField("paperName", paper.getPaperName()),
                stringField("subject", paper.getSubject()),
                numberField("duration", paper.getDuration()),
                numberField("totalScore", paper.getTotalScore()),
                numberField("passScore", paper.getPassScore()),
                stringField("description", paper.getDescription()),
                numberField("questionCount", resolveQuestionCount(paper)),
                numberField("singleCount", paper.getSingleCount()),
                numberField("multipleCount", paper.getMultipleCount()),
                numberField("judgeCount", paper.getJudgeCount()),
                numberField("blankCount", paper.getBlankCount()),
                booleanField("isPublished", Boolean.TRUE.equals(paper.getIsPublished()))
        );
    }

    private List<String> serializeQuestions(List<Question> questions) {
        List<String> payload = new ArrayList<>();
        for (int index = 0; index < questions.size(); index++) {
            payload.add(serializeQuestion(questions.get(index), index + 1));
        }
        return payload;
    }

    private String serializeQuestion(Question question, int position) {
        return object(
                numberField("questionId", question.getQuestionId()),
                numberField("position", position),
                stringField("questionType", question.getQuestionType() != null ? question.getQuestionType().name() : null),
                stringField("subject", question.getSubject()),
                stringField("content", question.getContent()),
                stringField("optionA", question.getOptionA()),
                stringField("optionB", question.getOptionB()),
                stringField("optionC", question.getOptionC()),
                stringField("optionD", question.getOptionD()),
                numberField("score", question.getScore())
        );
    }

    private String serializeRecord(ExamRecord record) {
        return object(
                numberField("recordId", record.getRecordId()),
                numberField("paperId", record.getPaperId()),
                stringField("paperName", record.getPaper() != null ? record.getPaper().getPaperName() : null),
                stringField("status", record.getStatus() != null ? record.getStatus().name() : null),
                decimalField("score", record.getScore()),
                stringField("submitTime", formatDateTime(record.getSubmitTime())),
                stringField("startTime", formatDateTime(record.getStartTime()))
        );
    }

    private String serializeExamLifecycleRecord(ExamRecord record) {
        return object(
                numberField("recordId", record.getRecordId()),
                numberField("studentId", record.getStudentId()),
                numberField("paperId", record.getPaperId()),
                stringField("status", record.getStatus() != null ? record.getStatus().name() : null),
                stringField("startTime", formatDateTime(record.getStartTime()))
        );
    }

    private String serializeStudentRecordDetail(
            ExamRecord record,
            Paper paper,
            List<AnswerRecord> answerRecords,
            long answeredCount,
            long correctCount,
            long wrongCount
    ) {
        return object(
                numberField("recordId", record.getRecordId()),
                numberField("paperId", record.getPaperId()),
                stringField("paperName", paper != null ? paper.getPaperName() : null),
                stringField("subject", paper != null ? paper.getSubject() : null),
                numberField("totalScore", paper != null ? paper.getTotalScore() : null),
                numberField("passScore", paper != null ? paper.getPassScore() : null),
                decimalField("score", record.getScore()),
                stringField("status", record.getStatus() != null ? record.getStatus().name() : null),
                stringField("startTime", formatDateTime(record.getStartTime())),
                stringField("submitTime", formatDateTime(record.getSubmitTime())),
                numberField("durationSeconds", calculateDurationSeconds(record)),
                numberField("questionCount", answerRecords.size()),
                numberField("answeredCount", answeredCount),
                numberField("correctCount", correctCount),
                numberField("wrongCount", wrongCount),
                booleanField("passed", record.getScore() != null
                        && paper != null
                        && paper.getPassScore() != null
                        && record.getScore().compareTo(BigDecimal.valueOf(paper.getPassScore())) >= 0)
        );
    }

    private List<String> serializeAnswerRecords(List<AnswerRecord> answerRecords) {
        List<String> payload = new ArrayList<>();
        for (int index = 0; index < answerRecords.size(); index++) {
            payload.add(serializeAnswerRecord(answerRecords.get(index), index + 1));
        }
        return payload;
    }

    private String serializeAnswerRecord(AnswerRecord answerRecord, int position) {
        Question question = answerRecord.getQuestion();
        return object(
                numberField("position", position),
                numberField("questionId", answerRecord.getQuestionId()),
                stringField("questionType", question != null && question.getQuestionType() != null ? question.getQuestionType().name() : null),
                stringField("content", question != null ? question.getContent() : null),
                stringField("studentAnswer", answerRecord.getStudentAnswer()),
                stringField("correctAnswer", question != null ? question.getCorrectAnswer() : null),
                stringField("analysis", question != null ? question.getAnalysis() : null),
                decimalField("score", answerRecord.getScore()),
                booleanField("isCorrect", Boolean.TRUE.equals(answerRecord.getIsCorrect()))
        );
    }

    private String serializeSubmitResult(ExamRecord record, Paper paper, BigDecimal score) {
        boolean passed = false;
        if (score != null && paper.getPassScore() != null) {
            passed = score.compareTo(BigDecimal.valueOf(paper.getPassScore())) >= 0;
        }

        return object(
                numberField("recordId", record.getRecordId()),
                numberField("paperId", record.getPaperId()),
                stringField("paperName", paper.getPaperName()),
                decimalField("score", score),
                numberField("totalScore", paper.getTotalScore()),
                numberField("passScore", paper.getPassScore()),
                booleanField("passed", passed),
                stringField("status", record.getStatus() != null ? record.getStatus().name() : null),
                stringField("submitTime", formatDateTime(record.getSubmitTime()))
        );
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

        LocalDateTime endTime = record.getSubmitTime() != null
                ? record.getSubmitTime()
                : record.getEndTime();
        if (endTime == null) {
            return 0;
        }

        return Math.max(0, Duration.between(record.getStartTime(), endTime).getSeconds());
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : DATE_TIME_FORMATTER.format(dateTime);
    }

    private String resolveErrorMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }

        String message = current.getMessage();
        if (message == null || message.trim().isEmpty()) {
            message = throwable.getMessage();
        }

        if (message == null || message.trim().isEmpty()) {
            return "Bridge execution failed.";
        }

        return message;
    }

    private void printAndExit(boolean ok, String message, String dataJson, int code) {
        List<String> fields = new ArrayList<>();
        fields.add(booleanField("ok", ok));
        fields.add(stringField("message", message));
        if (dataJson != null) {
            fields.add(field("data", dataJson));
        }

        System.out.println(object(fields.toArray(new String[0])));
        System.exit(code);
    }

    private String object(String... fields) {
        return "{" + joinFields(fields) + "}";
    }

    private String array(List<String> items) {
        return "[" + String.join(",", items) + "]";
    }

    private String joinFields(String... fields) {
        return Arrays.stream(fields)
                .filter(field -> field != null && !field.isEmpty())
                .collect(Collectors.joining(","));
    }

    private String field(String key, String rawJsonValue) {
        return quote(key) + ":" + rawJsonValue;
    }

    private String stringField(String key, String value) {
        return field(key, value == null ? "null" : quote(value));
    }

    private String numberField(String key, Number value) {
        return field(key, value == null ? "null" : String.valueOf(value));
    }

    private String decimalField(String key, double value) {
        return field(key, String.format(Locale.US, "%.2f", value));
    }

    private String decimalField(String key, BigDecimal value) {
        return field(key, value == null ? "null" : value.toPlainString());
    }

    private String booleanField(String key, boolean value) {
        return field(key, String.valueOf(value));
    }

    private String quote(String value) {
        return "\"" + escape(value) + "\"";
    }

    private String escape(String value) {
        StringBuilder escaped = new StringBuilder();
        for (int index = 0; index < value.length(); index++) {
            char ch = value.charAt(index);
            switch (ch) {
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    if (ch < 32) {
                        escaped.append(String.format("\\u%04x", (int) ch));
                    } else {
                        escaped.append(ch);
                    }
                    break;
            }
        }
        return escaped.toString();
    }
}
