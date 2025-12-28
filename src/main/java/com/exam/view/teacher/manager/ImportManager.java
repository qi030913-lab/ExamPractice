package com.exam.view.teacher.manager;

import com.exam.model.Question;
import com.exam.model.Paper;
import com.exam.service.QuestionService;
import com.exam.util.QuestionImportUtil;
import com.exam.util.UIUtil;
import com.exam.view.teacher.TeacherMainFrame;
import com.exam.view.teacher.ui.components.TeacherImportPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 教师端导入管理器 - 处理所有与题目导入相关的操作
 */
public class ImportManager {
    private final QuestionService questionService;
    private final TeacherMainFrame mainFrame;

    public ImportManager(QuestionService questionService, TeacherMainFrame mainFrame) {
        this.questionService = questionService;
        this.mainFrame = mainFrame;
    }

    /**
     * 导入题目到数据库
     * @param questions 题目列表
     * @param callback 导入完成后的回调
     */
    public void importQuestions(List<Question> questions, TeacherImportPanel.TeacherImportCallback callback) {
        int[] importedCount = {0}; // 使用数组来存储计数，以便在内部类中访问
        
        SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() throws Exception {
                int total = questions.size();

                for (int i = 0; i < total; i++) {
                    Question q = questions.get(i);
                    try {
                        // 检查是否已存在相同内容的题目
                        List<Question> existingQuestions = questionService.searchQuestions(q.getContent(), null, null, null, 0, Integer.MAX_VALUE);
                        boolean exists = existingQuestions.stream()
                                .anyMatch(existing -> existing.getContent().equals(q.getContent()) && 
                                                    existing.getCorrectAnswer().equals(q.getCorrectAnswer()));

                        if (!exists) {
                            questionService.addQuestion(q);
                            importedCount[0]++;
                        }

                        // 更新进度
                        publish((i + 1) * 100 / total);
                    } catch (Exception e) {
                        e.printStackTrace();
                        // 继续处理其他题目
                    }
                }
                
                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
                // 更新进度（这里可以添加进度条更新逻辑）
            }

            @Override
            protected void done() {
                try {
                    get(); // 等待后台任务完成
                    UIUtil.showInfo(null, "导入完成！\n成功导入题目：" + importedCount[0] + " 道");
                    if (callback != null) {
                        callback.onImportSuccess();
                    }
                } catch (Exception e) {
                    UIUtil.showError(null, "导入失败：" + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    /**
     * 导入题目并自动生成试卷
     * @param questions 题目列表
     */
    public void importAndGeneratePaper(List<Question> questions) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(mainFrame), "自动生成试卷", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 表单面板
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 15));
        formPanel.setBorder(BorderFactory.createTitledBorder("试卷信息"));

        JLabel nameLabel = new JLabel("试卷名称：");
        JTextField nameField = new JTextField("导入题目_" + System.currentTimeMillis());
        formPanel.add(nameLabel);
        formPanel.add(nameField);

        JLabel subjectLabel = new JLabel("科目：");
        String[] subjects = {"Java", "Vue", "数据结构", "马克思主义", "计算机网络", "操作系统", "数据库", "其他"};
        JComboBox<String> subjectBox = new JComboBox<>(subjects);
        formPanel.add(subjectLabel);
        formPanel.add(subjectBox);

        JLabel passScoreLabel = new JLabel("及格分数：");
        JSpinner passScoreSpinner = new JSpinner(new javax.swing.SpinnerNumberModel((int)60, (int)0, (int)100, (int)1));
        formPanel.add(passScoreLabel);
        formPanel.add(passScoreSpinner);

        JLabel durationLabel = new JLabel("考试时长(分钟)：");
        JSpinner durationSpinner = new JSpinner(new javax.swing.SpinnerNumberModel((int)90, (int)1, (int)300, (int)5));
        formPanel.add(durationLabel);
        formPanel.add(durationSpinner);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));

        JButton confirmButton = new JButton("确定");
        confirmButton.addActionListener(e -> {
            String paperName = nameField.getText().trim();
            String subject = (String) subjectBox.getSelectedItem();
            int passScore = (Integer) passScoreSpinner.getValue();
            int duration = (Integer) durationSpinner.getValue();

            if (paperName.isEmpty()) {
                UIUtil.showWarning(dialog, "请输入试卷名称");
                return;
            }

            try {
                // 先导入题目并获取它们的ID
                List<Integer> questionIds = new ArrayList<>();
                if (questions != null && !questions.isEmpty()) {
                    for (Question question : questions) {
                        if (question != null) {
                            // 检查题目是否已存在
                            List<Question> existingQuestions = questionService.searchQuestions(question.getContent(), null, null, null, 0, Integer.MAX_VALUE);
                            boolean exists = existingQuestions.stream()
                                    .anyMatch(existing -> existing.getContent().equals(question.getContent()) && 
                                                        existing.getCorrectAnswer().equals(question.getCorrectAnswer()));
                            
                            int questionId;
                            if (!exists) {
                                // 如果题目不存在，添加到数据库
                                questionId = questionService.addQuestion(question);
                            } else {
                                // 如果题目已存在，使用现有ID
                                questionId = existingQuestions.get(0).getQuestionId();
                            }
                            
                            if (questionId > 0) {
                                questionIds.add(questionId);
                            }
                        }
                    }
                }

                if (questionIds.isEmpty()) {
                    UIUtil.showError(dialog, "没有有效的题目可以生成试卷");
                    return;
                }

                // 计算总分
                int totalScore = questions.stream()
                        .filter(q -> q != null)
                        .mapToInt(Question::getScore)
                        .sum();

                // 创建试卷对象
                Paper paper = new Paper();
                paper.setPaperName(paperName);
                paper.setSubject(subject);
                paper.setPassScore(passScore);
                paper.setDuration(duration);
                paper.setTotalScore(totalScore);
                paper.setCreatorId(mainFrame.getTeacher().getUserId());

                // 创建试卷并关联题目
                int paperId = mainFrame.getPaperService().createPaper(paper, questionIds);

                UIUtil.showInfo(dialog, "成功生成试卷！\n导入题目：" + questionIds.size() + " 道");
                dialog.dispose();
                mainFrame.refreshQuestionData();
                mainFrame.refreshPaperData();

            } catch (Exception ex) {
                UIUtil.showError(dialog, "生成试卷失败：" + ex.getMessage());
                ex.printStackTrace();
            }
        });

        JButton cancelButton = new JButton("取消");
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
}