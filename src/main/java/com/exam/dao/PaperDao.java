package com.exam.dao;

import com.exam.model.Paper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PaperDao {
    Paper findById(@Param("paperId") Integer paperId);

    Paper findByName(@Param("paperName") String paperName);

    List<Paper> findAll();

    List<Paper> findAllPublished();

    List<Paper> findAllPublishedWithQuestionStats();

    int insert(Paper paper);

    void addPaperQuestion(
            @Param("paperId") Integer paperId,
            @Param("questionId") Integer questionId,
            @Param("order") Integer order
    );

    void addPaperQuestionsBatch(
            @Param("paperId") Integer paperId,
            @Param("questionIds") List<Integer> questionIds
    );

    void deletePaperQuestions(@Param("paperId") Integer paperId);

    List<Paper> findPapersUsingQuestion(@Param("questionId") Integer questionId);

    int update(Paper paper);

    int updatePublishStatus(@Param("paperId") Integer paperId, @Param("isPublished") Boolean isPublished);

    int delete(@Param("paperId") Integer paperId);

    List<Paper> findAllWithQuestionCount();
}
