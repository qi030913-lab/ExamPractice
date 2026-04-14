package com.exam.util;

import com.exam.model.Question;
import com.exam.model.enums.Difficulty;
import com.exam.model.enums.QuestionType;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class QuestionImportUtil {
    private static final String SUPPORTED_TYPES = QuestionType.getAutoExamSupportedTypeNames();

    public static List<Question> importFromTextFile(File file, Integer creatorId) throws Exception {
        List<Question> questions = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) {
                    continue;
                }

                try {
                    questions.add(parseLine(line, creatorId));
                } catch (Exception e) {
                    throw new Exception("第 " + lineNumber + " 行解析错误：" + e.getMessage());
                }
            }
        }

        if (questions.isEmpty()) {
            throw new Exception("文件中没有有效的题目数据");
        }

        return questions;
    }

    private static Question parseLine(String line, Integer creatorId) throws Exception {
        String[] parts = line.split("\\|");
        if (parts.length < 8) {
            throw new Exception("数据格式不正确，至少需要 8 个字段（用 | 分隔）");
        }

        Question question = new Question();
        try {
            question.setQuestionType(QuestionType.valueOf(parts[0].trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new Exception("题目类型无效：" + parts[0] + "，当前导题建卷仅支持 " + SUPPORTED_TYPES + " 题型");
        }

        if (!question.getQuestionType().isSupportedForAutoExam()) {
            throw new Exception("导题建卷当前仅支持 " + SUPPORTED_TYPES + " 题型，暂不支持：" + question.getQuestionType().name());
        }

        question.setSubject(parts[1].trim());
        if (question.getSubject().isEmpty()) {
            throw new Exception("科目不能为空");
        }

        question.setContent(parts[2].trim());
        if (question.getContent().isEmpty()) {
            throw new Exception("题目内容不能为空");
        }

        question.setOptionA(parts.length > 3 ? parts[3].trim() : null);
        question.setOptionB(parts.length > 4 ? parts[4].trim() : null);
        question.setOptionC(parts.length > 5 ? parts[5].trim() : null);
        question.setOptionD(parts.length > 6 ? parts[6].trim() : null);

        question.setCorrectAnswer(parts[7].trim().toUpperCase());
        if (question.getCorrectAnswer().isEmpty()) {
            throw new Exception("正确答案不能为空");
        }

        try {
            question.setScore(parts.length > 8 && !parts[8].trim().isEmpty()
                    ? Integer.parseInt(parts[8].trim())
                    : 5);
        } catch (NumberFormatException e) {
            throw new Exception("分值必须是数字");
        }

        if (parts.length > 9 && !parts[9].trim().isEmpty()) {
            try {
                question.setDifficulty(Difficulty.valueOf(parts[9].trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                question.setDifficulty(Difficulty.MEDIUM);
            }
        } else {
            question.setDifficulty(Difficulty.MEDIUM);
        }

        question.setAnalysis(parts.length > 10 ? parts[10].trim() : "");
        question.setCreatorId(creatorId);
        return question;
    }

    public static String buildTemplateContent() {
        return String.join("\n",
                "# 题目导入模板文件",
                "# 格式说明：题目类型|科目|题目内容|选项A|选项B|选项C|选项D|正确答案|分值|难度|解析",
                "# 当前导题建卷仅支持：SINGLE(单选题)、MULTIPLE(多选题)、JUDGE(判断题)",
                "# 难度：EASY(简单)、MEDIUM(中等)、HARD(困难)",
                "# 以 # 开头的行为注释，会被忽略",
                "",
                "# 示例题目",
                "SINGLE|Java|Java 中用于定义常量的关键字是？|const|final|static|let|B|5|EASY|final 用于定义常量",
                "MULTIPLE|Java|Java 中哪些是访问修饰符？|public|private|protected|final|ABC|10|EASY|public、private、protected 是访问修饰符",
                "JUDGE|Java|Java 支持类的多继承|正确|错误|||B|5|EASY|Java 不支持类的多继承",
                "",
                "# 提示：SHORT_ANSWER、BLANK、WRITING 等扩展题型暂不支持导题建卷，请勿使用在当前模板中"
        ) + "\n";
    }

    public static void generateTemplate(File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.write(buildTemplateContent());
        }
    }

    public static List<Question> importQuestionsFromFile(String filePath) throws Exception {
        return importFromTextFile(new File(filePath), null);
    }

    public static List<Question> importFromText(String sourceText, Integer creatorId) throws Exception {
        if (sourceText == null || sourceText.trim().isEmpty()) {
            throw new Exception("导入内容不能为空");
        }

        List<Question> questions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new StringReader(sourceText))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) {
                    continue;
                }

                try {
                    questions.add(parseLine(line, creatorId));
                } catch (Exception e) {
                    throw new Exception("第 " + lineNumber + " 行解析错误：" + e.getMessage());
                }
            }
        }

        if (questions.isEmpty()) {
            throw new Exception("导入内容中没有有效题目");
        }

        return questions;
    }
}
