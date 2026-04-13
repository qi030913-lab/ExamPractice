package com.exam.dao;

import com.exam.exception.DatabaseException;
import com.exam.model.Question;
import com.exam.model.enums.Difficulty;
import com.exam.model.enums.QuestionType;
import com.exam.util.DBUtil;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class QuestionDao {

    public Question findById(Integer questionId) {
        String sql = "SELECT * FROM question WHERE question_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, questionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractQuestion(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("查询题目失败", e);
        }
        return null;
    }

    public Map<Integer, Question> findByIds(Collection<Integer> questionIds) {
        Map<Integer, Question> resultMap = new HashMap<>();
        if (questionIds == null || questionIds.isEmpty()) {
            return resultMap;
        }

        String placeholders = String.join(",", Collections.nCopies(questionIds.size(), "?"));
        String sql = "SELECT * FROM question WHERE question_id IN (" + placeholders + ")";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int index = 1;
            for (Integer questionId : questionIds) {
                if (questionId == null) {
                    throw new DatabaseException("题目ID不能为空");
                }
                pstmt.setInt(index++, questionId);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Question question = extractQuestion(rs);
                    resultMap.put(question.getQuestionId(), question);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("批量查询题目失败", e);
        }

        return resultMap;
    }

    public List<Question> findAll() {
        String sql = "SELECT * FROM question ORDER BY question_id";
        List<Question> questions = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                questions.add(extractQuestion(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("查询题目列表失败", e);
        }
        return questions;
    }

    public List<Question> findBySubject(String subject) {
        String sql = "SELECT * FROM question WHERE subject = ? ORDER BY question_id";
        List<Question> questions = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, subject);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    questions.add(extractQuestion(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("按科目查询题目失败", e);
        }
        return questions;
    }

    public List<Question> findByPaperId(Integer paperId) {
        String sql = "SELECT q.* FROM question q " +
                "INNER JOIN paper_question pq ON q.question_id = pq.question_id " +
                "WHERE pq.paper_id = ? ORDER BY pq.question_order";
        List<Question> questions = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, paperId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    questions.add(extractQuestion(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("查询试卷题目失败", e);
        }
        return questions;
    }

    public Question findByExactSignature(String subject, QuestionType type, String content, String correctAnswer) {
        try (Connection conn = DBUtil.getConnection()) {
            return findByExactSignature(conn, subject, type, content, correctAnswer);
        } catch (SQLException e) {
            throw new DatabaseException("按题目签名查询失败", e);
        }
    }

    public Question findByExactSignature(Connection conn, String subject, QuestionType type, String content, String correctAnswer) {
        String sql = "SELECT * FROM question " +
                "WHERE subject = ? AND question_type = ? AND TRIM(content) = ? AND TRIM(correct_answer) = ? " +
                "ORDER BY question_id LIMIT 1";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, subject);
            pstmt.setString(2, type.name());
            pstmt.setString(3, content.trim());
            pstmt.setString(4, correctAnswer.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractQuestion(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("按题目签名查询失败", e);
        }
        return null;
    }

    public Map<Integer, List<Question>> findByPaperIds(Collection<Integer> paperIds) {
        Map<Integer, List<Question>> resultMap = new HashMap<>();
        if (paperIds == null || paperIds.isEmpty()) {
            return resultMap;
        }

        for (Integer paperId : paperIds) {
            resultMap.put(paperId, new ArrayList<>());
        }

        String placeholders = String.join(",", Collections.nCopies(paperIds.size(), "?"));
        String sql = "SELECT q.*, pq.paper_id FROM question q " +
                "INNER JOIN paper_question pq ON q.question_id = pq.question_id " +
                "WHERE pq.paper_id IN (" + placeholders + ") " +
                "ORDER BY pq.paper_id, pq.question_order";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int index = 1;
            for (Integer paperId : paperIds) {
                pstmt.setInt(index++, paperId);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Question question = extractQuestion(rs);
                    Integer paperId = rs.getInt("paper_id");
                    resultMap.get(paperId).add(question);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("批量查询试卷题目失败", e);
        }
        return resultMap;
    }

    public int insert(Question question) {
        String sql = "INSERT INTO question (question_type, subject, content, option_a, option_b, " +
                "option_c, option_d, correct_answer, score, difficulty, analysis, creator_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            bindInsertParameters(pstmt, question);
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("新增题目失败", e);
        }
        return 0;
    }

    public int insert(Connection conn, Question question) {
        String sql = "INSERT INTO question (question_type, subject, content, option_a, option_b, " +
                "option_c, option_d, correct_answer, score, difficulty, analysis, creator_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindInsertParameters(pstmt, question);
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("新增题目失败", e);
        }
        return 0;
    }

    public int update(Question question) {
        String sql = "UPDATE question SET question_type = ?, subject = ?, content = ?, " +
                "option_a = ?, option_b = ?, option_c = ?, option_d = ?, " +
                "correct_answer = ?, score = ?, difficulty = ?, analysis = ? " +
                "WHERE question_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, question.getQuestionType().name());
            pstmt.setString(2, question.getSubject());
            pstmt.setString(3, question.getContent());
            pstmt.setString(4, question.getOptionA());
            pstmt.setString(5, question.getOptionB());
            pstmt.setString(6, question.getOptionC());
            pstmt.setString(7, question.getOptionD());
            pstmt.setString(8, question.getCorrectAnswer());
            pstmt.setInt(9, question.getScore());
            pstmt.setString(10, question.getDifficulty().name());
            pstmt.setString(11, question.getAnalysis());
            pstmt.setInt(12, question.getQuestionId());

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("更新题目失败", e);
        }
    }

    public int delete(Integer questionId) {
        String sql = "DELETE FROM question WHERE question_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, questionId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("删除题目失败", e);
        }
    }

    private Question extractQuestion(ResultSet rs) throws SQLException {
        Question question = new Question();
        question.setQuestionId(rs.getInt("question_id"));
        question.setQuestionType(QuestionType.valueOf(rs.getString("question_type")));
        question.setSubject(rs.getString("subject"));
        question.setContent(rs.getString("content"));
        question.setOptionA(rs.getString("option_a"));
        question.setOptionB(rs.getString("option_b"));
        question.setOptionC(rs.getString("option_c"));
        question.setOptionD(rs.getString("option_d"));
        question.setCorrectAnswer(rs.getString("correct_answer"));
        question.setScore(rs.getInt("score"));

        String difficulty = rs.getString("difficulty");
        if (difficulty != null) {
            question.setDifficulty(Difficulty.valueOf(difficulty));
        }

        question.setAnalysis(rs.getString("analysis"));
        question.setCreatorId(rs.getInt("creator_id"));

        Timestamp createTime = rs.getTimestamp("create_time");
        if (createTime != null) {
            question.setCreateTime(createTime.toLocalDateTime());
        }

        Timestamp updateTime = rs.getTimestamp("update_time");
        if (updateTime != null) {
            question.setUpdateTime(updateTime.toLocalDateTime());
        }

        return question;
    }

    private void bindInsertParameters(PreparedStatement pstmt, Question question) throws SQLException {
        pstmt.setString(1, question.getQuestionType().name());
        pstmt.setString(2, question.getSubject());
        pstmt.setString(3, question.getContent());
        pstmt.setString(4, question.getOptionA());
        pstmt.setString(5, question.getOptionB());
        pstmt.setString(6, question.getOptionC());
        pstmt.setString(7, question.getOptionD());
        pstmt.setString(8, question.getCorrectAnswer());
        pstmt.setInt(9, question.getScore());
        pstmt.setString(10, question.getDifficulty() != null ? question.getDifficulty().name() : "MEDIUM");
        pstmt.setString(11, question.getAnalysis());

        if (question.getCreatorId() != null) {
            pstmt.setInt(12, question.getCreatorId());
        } else {
            pstmt.setNull(12, Types.INTEGER);
        }
    }

    public List<Question> search(
            String content,
            String subject,
            QuestionType type,
            Difficulty difficulty,
            int offset,
            int limit
    ) {
        StringBuilder sql = new StringBuilder("SELECT * FROM question WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (content != null && !content.trim().isEmpty()) {
            sql.append(" AND content LIKE ?");
            params.add("%" + content + "%");
        }

        if (subject != null && !subject.trim().isEmpty()) {
            sql.append(" AND subject = ?");
            params.add(subject);
        }

        if (type != null) {
            sql.append(" AND question_type = ?");
            params.add(type.name());
        }

        if (difficulty != null) {
            sql.append(" AND difficulty = ?");
            params.add(difficulty.name());
        }

        sql.append(" ORDER BY question_id LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        List<Question> questions = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    questions.add(extractQuestion(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("搜索题目失败", e);
        }

        return questions;
    }

    public int countQuestions(String content, String subject, QuestionType type, Difficulty difficulty) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM question WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (content != null && !content.trim().isEmpty()) {
            sql.append(" AND content LIKE ?");
            params.add("%" + content + "%");
        }

        if (subject != null && !subject.trim().isEmpty()) {
            sql.append(" AND subject = ?");
            params.add(subject);
        }

        if (type != null) {
            sql.append(" AND question_type = ?");
            params.add(type.name());
        }

        if (difficulty != null) {
            sql.append(" AND difficulty = ?");
            params.add(difficulty.name());
        }

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("统计题目总数失败", e);
        }

        return 0;
    }
}
