package com.exam.tests.util;

import com.exam.model.Question;
import com.exam.util.QuestionImportUtil;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestionImportUtilTest {
    @Test
    void importFromTextShouldRejectExtendedQuestionTypesUpfront() {
        Exception exception = assertThrows(
                Exception.class,
                () -> QuestionImportUtil.importFromText(
                        "SHORT_ANSWER|Java|请简述面向对象三大特征|||||封装、继承、多态|15|MEDIUM|核心面试题",
                        1
                )
        );

        assertTrue(exception.getMessage().contains("仅支持"));
        assertTrue(exception.getMessage().contains("SHORT_ANSWER"));
    }

    @Test
    void importFromTextShouldAcceptObjectiveQuestionTypes() throws Exception {
        List<Question> questions = QuestionImportUtil.importFromText(
                String.join("\n",
                        "SINGLE|Java|Java 中用于定义常量的关键字是？|const|final|static|let|B|5|EASY|final 用于定义常量",
                        "MULTIPLE|Java|Java 中哪些是访问修饰符？|public|private|protected|final|ABC|10|MEDIUM|访问修饰符包括 public、private、protected",
                        "JUDGE|Java|Java 支持类的多继承|正确|错误|||B|5|EASY|Java 不支持类的多继承"
                ),
                1
        );

        assertEquals(3, questions.size());
        assertEquals("Java", questions.get(0).getSubject());
    }

    @Test
    void buildTemplateContentShouldClearlyMarkUnsupportedExtendedTypes() {
        String template = QuestionImportUtil.buildTemplateContent();

        assertTrue(template.contains("SINGLE"));
        assertTrue(template.contains("MULTIPLE"));
        assertTrue(template.contains("JUDGE"));
        assertTrue(template.contains("SHORT_ANSWER"));
        assertTrue(template.contains("暂不支持"));
    }
}
