package com.exam.bridge;

import com.exam.exception.AuthenticationException;
import com.exam.exception.BusinessException;
import com.exam.model.ExamRecord;
import com.exam.model.Paper;
import com.exam.model.User;
import com.exam.model.enums.ExamStatus;
import com.exam.model.enums.UserRole;
import com.exam.service.ExamService;
import com.exam.service.PaperService;
import com.exam.service.UserService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
        User user = userService.getUserById(userId);
        if (user.getRole() != UserRole.STUDENT) {
            throw new BusinessException("Requested user is not a student.");
        }

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
        User user = userService.getUserById(userId);
        if (user.getRole() != UserRole.TEACHER) {
            throw new BusinessException("Requested user is not a teacher.");
        }

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
        int questionCount = paper.getSingleCount() + paper.getMultipleCount() + paper.getJudgeCount() + paper.getBlankCount();
        return object(
                numberField("paperId", paper.getPaperId()),
                stringField("paperName", paper.getPaperName()),
                stringField("subject", paper.getSubject()),
                numberField("duration", paper.getDuration()),
                numberField("totalScore", paper.getTotalScore()),
                numberField("questionCount", questionCount),
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
                numberField("questionCount", paper.getSingleCount()),
                booleanField("isPublished", Boolean.TRUE.equals(paper.getIsPublished()))
        );
    }

    private String serializeRecord(ExamRecord record) {
        return object(
                numberField("recordId", record.getRecordId()),
                stringField("paperName", record.getPaper() != null ? record.getPaper().getPaperName() : null),
                stringField("status", record.getStatus() != null ? record.getStatus().name() : null),
                decimalField("score", record.getScore()),
                stringField("submitTime", formatDateTime(record.getSubmitTime())),
                stringField("startTime", formatDateTime(record.getStartTime()))
        );
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
        return java.util.Arrays.stream(fields)
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
