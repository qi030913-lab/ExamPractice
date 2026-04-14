package com.exam.dao;

import com.exam.model.Question;
import com.exam.model.enums.Difficulty;
import com.exam.model.enums.QuestionType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface QuestionDao {
    Question findById(@Param("questionId") Integer questionId);

    List<Question> findByIdsList(@Param("questionIds") Collection<Integer> questionIds);

    List<Question> findAll();

    List<Question> findBySubject(@Param("subject") String subject);

    List<Question> findByPaperId(@Param("paperId") Integer paperId);

    Question findByExactSignature(
            @Param("subject") String subject,
            @Param("type") QuestionType type,
            @Param("content") String content,
            @Param("correctAnswer") String correctAnswer
    );

    List<QuestionPaperRelation> findByPaperIdsFlat(@Param("paperIds") Collection<Integer> paperIds);

    int insert(Question question);

    int update(Question question);

    int delete(@Param("questionId") Integer questionId);

    List<Question> search(
            @Param("content") String content,
            @Param("subject") String subject,
            @Param("type") QuestionType type,
            @Param("difficulty") Difficulty difficulty,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    int countQuestions(
            @Param("content") String content,
            @Param("subject") String subject,
            @Param("type") QuestionType type,
            @Param("difficulty") Difficulty difficulty
    );

    default Map<Integer, Question> findByIds(Collection<Integer> questionIds) {
        Map<Integer, Question> result = new HashMap<>();
        if (questionIds == null || questionIds.isEmpty()) {
            return result;
        }
        for (Question question : findByIdsList(questionIds)) {
            result.put(question.getQuestionId(), question);
        }
        return result;
    }

    default Map<Integer, List<Question>> findByPaperIds(Collection<Integer> paperIds) {
        Map<Integer, List<Question>> result = new HashMap<>();
        if (paperIds == null || paperIds.isEmpty()) {
            return result;
        }
        for (Integer paperId : paperIds) {
            result.put(paperId, new ArrayList<>());
        }
        for (QuestionPaperRelation relation : findByPaperIdsFlat(paperIds)) {
            result.computeIfAbsent(relation.getPaperId(), key -> new ArrayList<>()).add(relation.getQuestion());
        }
        return result;
    }

    class QuestionPaperRelation {
        private Integer paperId;
        private Question question;

        public Integer getPaperId() {
            return paperId;
        }

        public void setPaperId(Integer paperId) {
            this.paperId = paperId;
        }

        public Question getQuestion() {
            return question;
        }

        public void setQuestion(Question question) {
            this.question = question;
        }
    }
}
