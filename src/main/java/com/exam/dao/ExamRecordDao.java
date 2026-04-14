package com.exam.dao;

import com.exam.model.AnswerRecord;
import com.exam.model.ExamRecord;
import com.exam.model.enums.ExamStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface ExamRecordDao {
    ExamRecord findById(@Param("recordId") Integer recordId);

    ExamRecord findByIdForUpdate(@Param("recordId") Integer recordId);

    ExamRecord findByIdWithPaper(@Param("recordId") Integer recordId);

    List<ExamRecord> findByStudentId(@Param("studentId") Integer studentId);

    ExamRecord findInProgressByStudentIdAndPaperId(
            @Param("studentId") Integer studentId,
            @Param("paperId") Integer paperId,
            @Param("status") ExamStatus status
    );

    int countByStudentId(@Param("studentId") Integer studentId);

    List<ExamRecord> findByStudentIdWithPaper(@Param("studentId") Integer studentId);

    List<StudentExamRecordRelation> findByStudentIdsWithPaperFlat(@Param("studentIds") Collection<Integer> studentIds);

    List<ExamRecord> findByStudentIdWithPaperPaginated(
            @Param("studentId") Integer studentId,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );

    List<ExamRecord> findByPaperId(@Param("paperId") Integer paperId);

    int insert(ExamRecord record);

    int update(ExamRecord record);

    void insertAnswerRecord(AnswerRecord answerRecord);

    void insertAnswerRecordsBatch(@Param("answerRecords") List<AnswerRecord> answerRecords);

    List<AnswerRecord> findAnswerRecords(@Param("recordId") Integer recordId);

    List<RecordAnswerRelation> findAnswerRecordsByRecordIdsFlat(@Param("recordIds") List<Integer> recordIds);

    default Map<Integer, List<ExamRecord>> findByStudentIdsWithPaper(Collection<Integer> studentIds) {
        Map<Integer, List<ExamRecord>> result = new HashMap<>();
        if (studentIds == null || studentIds.isEmpty()) {
            return result;
        }
        for (Integer studentId : studentIds) {
            result.put(studentId, new ArrayList<>());
        }
        for (StudentExamRecordRelation relation : findByStudentIdsWithPaperFlat(studentIds)) {
            result.computeIfAbsent(relation.getStudentId(), key -> new ArrayList<>()).add(relation.getRecord());
        }
        return result;
    }

    default Map<Integer, List<AnswerRecord>> findAnswerRecordsByRecordIds(List<Integer> recordIds) {
        Map<Integer, List<AnswerRecord>> result = new HashMap<>();
        if (recordIds == null || recordIds.isEmpty()) {
            return result;
        }
        for (Integer recordId : recordIds) {
            result.put(recordId, new ArrayList<>());
        }
        for (RecordAnswerRelation relation : findAnswerRecordsByRecordIdsFlat(recordIds)) {
            result.computeIfAbsent(relation.getRecordId(), key -> new ArrayList<>()).add(relation.getAnswerRecord());
        }
        return result;
    }

    class StudentExamRecordRelation {
        private Integer studentId;
        private ExamRecord record;

        public Integer getStudentId() {
            return studentId;
        }

        public void setStudentId(Integer studentId) {
            this.studentId = studentId;
        }

        public ExamRecord getRecord() {
            return record;
        }

        public void setRecord(ExamRecord record) {
            this.record = record;
        }
    }

    class RecordAnswerRelation {
        private Integer recordId;
        private AnswerRecord answerRecord;

        public Integer getRecordId() {
            return recordId;
        }

        public void setRecordId(Integer recordId) {
            this.recordId = recordId;
        }

        public AnswerRecord getAnswerRecord() {
            return answerRecord;
        }

        public void setAnswerRecord(AnswerRecord answerRecord) {
            this.answerRecord = answerRecord;
        }
    }
}
