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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        
        // 创建进度对话框
        JDialog progressDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(mainFrame), "正在导入题目", true);
        progressDialog.setSize(450, 200);
        progressDialog.setLocationRelativeTo(mainFrame);
        progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        // 标题
        JLabel titleLabel = new JLabel("正在导入题目，请稍候...");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // 进度信息面板
        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        infoPanel.setBackground(Color.WHITE);
        
        JLabel statusLabel = new JLabel("准备导入...");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setFont(new Font("微软雅黑", Font.BOLD, 12));
        progressBar.setPreferredSize(new Dimension(350, 25));
        
        JLabel countLabel = new JLabel("0 / " + questions.size());
        countLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        countLabel.setHorizontalAlignment(SwingConstants.CENTER);
        countLabel.setForeground(new Color(100, 100, 100));
        
        infoPanel.add(statusLabel);
        infoPanel.add(progressBar);
        infoPanel.add(countLabel);
        
        panel.add(infoPanel, BorderLayout.CENTER);
        progressDialog.add(panel);
        
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
                        int progress = (i + 1) * 100 / total;
                        publish(progress);
                        
                        // 模拟处理时间，让进度条更明显（实际导入很快）
                        Thread.sleep(50);
                    } catch (Exception e) {
                        e.printStackTrace();
                        // 继续处理其他题目
                    }
                }
                
                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
                // 更新进度条
                if (!chunks.isEmpty()) {
                    int progress = chunks.get(chunks.size() - 1);
                    progressBar.setValue(progress);
                    int current = (int) Math.ceil(progress * questions.size() / 100.0);
                    countLabel.setText(current + " / " + questions.size());
                    statusLabel.setText("正在导入第 " + current + " 道题目...");
                }
            }

            @Override
            protected void done() {
                progressDialog.dispose();
                try {
                    get(); // 等待后台任务完成
                    int skipped = questions.size() - importedCount[0];
                    String message = "导入完成！\n成功导入题目：" + importedCount[0] + " 道";
                    if (skipped > 0) {
                        message += "\n跳过重复题目：" + skipped + " 道";
                    }
                    UIUtil.showInfo(mainFrame, message);
                    if (callback != null) {
                        callback.onImportSuccess();
                    }
                } catch (Exception e) {
                    UIUtil.showError(mainFrame, "导入失败：" + e.getMessage());
                }
            }
        };
        
        worker.execute();
        progressDialog.setVisible(true);
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

            // 关闭表单对话框
            dialog.dispose();
            
            // 创建进度对话框
            JDialog progressDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(mainFrame), "正在生成试卷", true);
            progressDialog.setSize(450, 200);
            progressDialog.setLocationRelativeTo(mainFrame);
            progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
            
            JLabel titleLabel = new JLabel("正在导入题目并生成试卷，请稍候...");
            titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
            titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(titleLabel, BorderLayout.NORTH);
            
            JPanel infoPanel = new JPanel(new GridLayout(3, 1, 5, 5));
            infoPanel.setBackground(Color.WHITE);
            
            JLabel statusLabel = new JLabel("准备导入...");
            statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
            statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            JProgressBar progressBar = new JProgressBar(0, 100);
            progressBar.setStringPainted(true);
            progressBar.setFont(new Font("微软雅黑", Font.BOLD, 12));
            progressBar.setPreferredSize(new Dimension(350, 25));
            
            JLabel countLabel = new JLabel("0 / " + questions.size());
            countLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            countLabel.setHorizontalAlignment(SwingConstants.CENTER);
            countLabel.setForeground(new Color(100, 100, 100));
            
            infoPanel.add(statusLabel);
            infoPanel.add(progressBar);
            infoPanel.add(countLabel);
            
            panel.add(infoPanel, BorderLayout.CENTER);
            progressDialog.add(panel);

            // 使用SwingWorker在后台执行
            SwingWorker<Integer, Integer> worker = new SwingWorker<Integer, Integer>() {
                @Override
                protected Integer doInBackground() throws Exception {
                    // 先导入题目并获取它们的ID
                    List<Integer> questionIds = new ArrayList<>();
                    Set<Integer> addedQuestionIds = new HashSet<>();
                    int total = questions.size();
                    
                    if (questions != null && !questions.isEmpty()) {
                        for (int i = 0; i < questions.size(); i++) {
                            Question question = questions.get(i);
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
                                
                                // 只添加不重复的题目ID
                                if (questionId > 0 && !addedQuestionIds.contains(questionId)) {
                                    questionIds.add(questionId);
                                    addedQuestionIds.add(questionId);
                                }
                                
                                // 更新进度
                                int progress = (i + 1) * 80 / total; // 0-80%用于导入题目
                                publish(progress);
                                Thread.sleep(50);
                            }
                        }
                    }

                    if (questionIds.isEmpty()) {
                        throw new Exception("没有有效的题目可以生成试卷");
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
                    publish(90); // 90%
                    Thread.sleep(100);
                    int paperId = mainFrame.getPaperService().createPaper(paper, questionIds);
                    publish(100); // 100%
                    
                    return questionIds.size();
                }
                
                @Override
                protected void process(List<Integer> chunks) {
                    if (!chunks.isEmpty()) {
                        int progress = chunks.get(chunks.size() - 1);
                        progressBar.setValue(progress);
                        
                        if (progress <= 80) {
                            int current = (int) Math.ceil(progress * questions.size() / 80.0);
                            countLabel.setText(current + " / " + questions.size());
                            statusLabel.setText("正在导入第 " + current + " 道题目...");
                        } else if (progress == 90) {
                            statusLabel.setText("正在生成试卷...");
                            countLabel.setText("即将完成");
                        } else if (progress == 100) {
                            statusLabel.setText("完成！");
                        }
                    }
                }
                
                @Override
                protected void done() {
                    progressDialog.dispose();
                    try {
                        int count = get();
                        UIUtil.showInfo(mainFrame, "成功生成试卷！\n导入题目：" + count + " 道");
                        mainFrame.refreshQuestionData();
                        mainFrame.refreshPaperData();
                    } catch (Exception ex) {
                        UIUtil.showError(mainFrame, "生成试卷失败：" + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            };
            
            worker.execute();
            progressDialog.setVisible(true);
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