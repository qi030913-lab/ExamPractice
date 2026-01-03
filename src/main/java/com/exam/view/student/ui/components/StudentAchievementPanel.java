package com.exam.view.student.ui.components;

import com.exam.model.User;
import com.exam.model.ExamRecord;
import com.exam.model.AnswerRecord;
import com.exam.service.ExamService;
import com.exam.util.UIUtil;
import com.exam.util.IconUtil;
import com.exam.view.student.manager.StudentAchievementManager;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * 学生端 - 我的成就面板
 * 
 * 功能说明：
 * 1. 统计卡片展示（考试次数、平均分、正确题数、正确率）
 * 2. 成绩趋势图（折线图）
 * 3. 题型准确率图（柱状图）
 * 
 * 性能优化版本：使用缓存和异步加载
 * 
 * @author 系统管理员
 * @version 1.0
 */
public class StudentAchievementPanel extends JPanel {
    private final User student;
    private final ExamService examService;
    private final StudentAchievementManager achievementManager;
    
    // 缓存绘图所需数据，避免paintComponent重复查询
    private volatile double[] cachedStats;
    private volatile List<ExamRecord> cachedRecords;
    private volatile int[] cachedAccuracyData;
    private volatile boolean dataLoaded = false;
    
    // UI组件引用
    private JPanel statsPanel;
    private JPanel chartsPanel;

    public StudentAchievementPanel(User student) {
        this.student = student;
        this.examService = new ExamService();
        this.achievementManager = new StudentAchievementManager(student);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 20));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // 标题区域
        JPanel titlePanel = createTitlePanel();
        add(titlePanel, BorderLayout.NORTH);

        // 主内容区域（初始显示加载中）
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        
        JLabel loadingLabel = new JLabel("加载中...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        loadingLabel.setForeground(new Color(100, 100, 100));
        contentPanel.add(loadingLabel, BorderLayout.CENTER);
        
        add(contentPanel, BorderLayout.CENTER);
        
        // 异步加载数据
        loadDataAsync(contentPanel);
    }
    
    /**
     * 异步加载数据
     */
    private void loadDataAsync(JPanel contentPanel) {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                // 在后台线程加载数据
                cachedStats = achievementManager.getStatistics();
                cachedRecords = achievementManager.getExamRecords();
                cachedAccuracyData = achievementManager.getQuestionTypeAccuracy();
                dataLoaded = true;
                return null;
            }
            
            @Override
            protected void done() {
                // 在EDT线程更新UI
                contentPanel.removeAll();
                contentPanel.setLayout(new BorderLayout(0, 20));
                
                // 统计卡片区域
                statsPanel = createStatsPanel();
                contentPanel.add(statsPanel, BorderLayout.NORTH);

                // 图表区域
                chartsPanel = createChartsPanel();
                contentPanel.add(chartsPanel, BorderLayout.CENTER);
                
                contentPanel.revalidate();
                contentPanel.repaint();
            }
        };
        worker.execute();
    }

    /**
     * 创建标题面板
     */
    private JPanel createTitlePanel() {
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel("我的成就");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(UIUtil.PRIMARY_COLOR);
        titlePanel.add(titleLabel, BorderLayout.WEST);

        return titlePanel;
    }

    /**
     * 创建内容面板
     */
    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new BorderLayout(0, 20));
        contentPanel.setBackground(Color.WHITE);

        // 统计卡片区域
        JPanel statsPanel = createStatsPanel();
        contentPanel.add(statsPanel, BorderLayout.NORTH);

        // 图表区域
        JPanel chartsPanel = createChartsPanel();
        contentPanel.add(chartsPanel, BorderLayout.CENTER);

        return contentPanel;
    }

    /**
     * 创建统计卡片面板（性能优化版本 - 使用缓存数据）
     */
    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));

        // 使用缓存的统计数据
        double[] stats = cachedStats != null ? cachedStats : new double[]{0, 0, 0, 0};
        int totalExams = (int) stats[0];
        double avgScore = stats[1];
        long totalCorrect = (long) stats[2];
        double accuracy = stats[3];

        // 创建统计卡片
        statsPanel.add(createStatCard(IconUtil.createDocumentIcon(new Color(52, 152, 219), 40), "考试次数", String.valueOf(totalExams), new Color(52, 152, 219)));
        statsPanel.add(createStatCard(IconUtil.createTargetIcon(new Color(46, 204, 113), 40), "平均分", String.format("%.1f", avgScore), new Color(46, 204, 113)));
        statsPanel.add(createStatCard(IconUtil.createCheckIcon(new Color(155, 89, 182), 40), "正确题数", String.valueOf(totalCorrect), new Color(155, 89, 182)));
        statsPanel.add(createStatCard(IconUtil.createTrendUpIcon(new Color(231, 76, 60), 40), "正确率", String.format("%.1f%%", accuracy), new Color(231, 76, 60)));

        return statsPanel;
    }

    /**
     * 创建统计卡片
     */
    private JPanel createStatCard(Icon icon, String label, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        
        JLabel iconLabel = new JLabel(icon);
        topPanel.add(iconLabel, BorderLayout.WEST);

        card.add(topPanel, BorderLayout.NORTH);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBackground(Color.WHITE);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("微软雅黑", Font.BOLD, 28));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel labelLabel = new JLabel(label);
        labelLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        labelLabel.setForeground(new Color(120, 120, 120));
        labelLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        bottomPanel.add(valueLabel);
        bottomPanel.add(Box.createVerticalStrut(5));
        bottomPanel.add(labelLabel);

        card.add(bottomPanel, BorderLayout.CENTER);

        return card;
    }

    /**
     * 创建图表面板
     */
    private JPanel createChartsPanel() {
        JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        chartsPanel.setBackground(Color.WHITE);

        // 成绩趋势图
        chartsPanel.add(createScoreTrendChart());
        
        // 题型准确率图
        chartsPanel.add(createAccuracyChart());

        return chartsPanel;
    }

    /**
     * 创建成绩趋势图（性能优化版本 - 使用缓存数据）
     */
    private JPanel createScoreTrendChart() {
        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel("成绩趋势图");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        chartPanel.add(titleLabel, BorderLayout.NORTH);

        // 绘制区域 - 使用缓存数据
        final List<ExamRecord> records = cachedRecords;
        
        JPanel drawPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();
                int padding = 40;

                // 直接使用缓存的数据，不再调用achievementManager
                if (records == null || records.isEmpty()) {
                    g2d.setFont(new Font("微软雅黑", Font.PLAIN, 14));
                    g2d.setColor(new Color(150, 150, 150));
                    String msg = "暂无考试记录";
                    FontMetrics fm = g2d.getFontMetrics();
                    int msgWidth = fm.stringWidth(msg);
                    g2d.drawString(msg, (width - msgWidth) / 2, height / 2);
                    return;
                }

                // 绘制坐标轴
                g2d.setColor(new Color(200, 200, 200));
                g2d.drawLine(padding, height - padding, width - padding, height - padding);
                g2d.drawLine(padding, padding, padding, height - padding);

                // 绘制网格线
                g2d.setColor(new Color(240, 240, 240));
                for (int i = 1; i <= 4; i++) {
                    int y = padding + (height - 2 * padding) * i / 5;
                    g2d.drawLine(padding, y, width - padding, y);
                }

                int maxScore = 100;
                int chartWidth = width - 2 * padding;
                int chartHeight = height - 2 * padding;
                int pointGap = chartWidth / Math.max(records.size() - 1, 1);

                // 绘制数据点和线条
                g2d.setStroke(new BasicStroke(2));
                g2d.setColor(new Color(52, 152, 219));

                int prevX = padding;
                int firstScore = records.get(0).getScore() != null ? records.get(0).getScore().intValue() : 0;
                int prevY = height - padding - (firstScore * chartHeight / maxScore);

                for (int i = 0; i < records.size(); i++) {
                    ExamRecord record = records.get(i);
                    int x = padding + (records.size() > 1 ? i * pointGap : chartWidth / 2);
                    int score = record.getScore() != null ? record.getScore().intValue() : 0;
                    int y = height - padding - (score * chartHeight / maxScore);

                    if (i > 0) {
                        g2d.drawLine(prevX, prevY, x, y);
                    }

                    // 绘制数据点
                    g2d.fillOval(x - 4, y - 4, 8, 8);

                    // 显示分数
                    g2d.setFont(new Font("微软雅黑", Font.PLAIN, 11));
                    String scoreStr = String.valueOf(score);
                    FontMetrics fm = g2d.getFontMetrics();
                    g2d.drawString(scoreStr, x - fm.stringWidth(scoreStr) / 2, y - 10);

                    // 显示考试序号
                    g2d.setColor(new Color(120, 120, 120));
                    String label = "#" + (i + 1);
                    g2d.drawString(label, x - fm.stringWidth(label) / 2, height - padding + 20);
                    g2d.setColor(new Color(52, 152, 219));

                    prevX = x;
                    prevY = y;
                }

                // Y轴刻度
                g2d.setColor(new Color(120, 120, 120));
                g2d.setFont(new Font("微软雅黑", Font.PLAIN, 11));
                for (int i = 0; i <= 5; i++) {
                    int score = i * 20;
                    int y = height - padding - (height - 2 * padding) * i / 5;
                    g2d.drawString(String.valueOf(score), padding - 30, y + 5);
                }
            }
        };
        drawPanel.setBackground(Color.WHITE);
        chartPanel.add(drawPanel, BorderLayout.CENTER);

        return chartPanel;
    }

    /**
     * 创建题型准确率图（性能优化版本 - 使用缓存数据）
     */
    private JPanel createAccuracyChart() {
        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel("题型准确率分析");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        chartPanel.add(titleLabel, BorderLayout.NORTH);

        // 使用缓存的数据
        final int[] data = cachedAccuracyData != null ? cachedAccuracyData : new int[8];
        
        JPanel drawPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();
                int padding = 40;

                // 直接使用缓存的数据
                int[] correctCounts = {data[0], data[2], data[4], data[6]};
                int[] totalCounts = {data[1], data[3], data[5], data[7]};

                // 绘制坐标轴
                g2d.setColor(new Color(200, 200, 200));
                g2d.drawLine(padding, height - padding, width - padding, height - padding);
                g2d.drawLine(padding, padding, padding, height - padding);

                // 绘制网格线
                g2d.setColor(new Color(240, 240, 240));
                for (int i = 1; i <= 4; i++) {
                    int y = padding + (height - 2 * padding) * i / 5;
                    g2d.drawLine(padding, y, width - padding, y);
                }

                // 绘制柱状图
                String[] labels = {"单选题", "多选题", "判断题", "填空题"};
                Color[] colors = {
                    new Color(52, 152, 219),
                    new Color(46, 204, 113),
                    new Color(155, 89, 182),
                    new Color(241, 196, 15)
                };

                int barWidth = (width - 2 * padding - 60) / 4;
                int chartHeight = height - 2 * padding;

                for (int i = 0; i < 4; i++) {
                    double accuracy = totalCounts[i] > 0 ? (correctCounts[i] * 100.0 / totalCounts[i]) : 0;
                    int barHeight = (int) (chartHeight * accuracy / 100);
                    int x = padding + 30 + i * (barWidth + 15);
                    int y = height - padding - barHeight;

                    // 绘制柱形
                    g2d.setColor(colors[i]);
                    g2d.fillRect(x, y, barWidth, barHeight);

                    // 绘制边框
                    g2d.setColor(colors[i].darker());
                    g2d.drawRect(x, y, barWidth, barHeight);

                    // 显示百分比
                    g2d.setFont(new Font("微软雅黑", Font.BOLD, 12));
                    String percentStr = String.format("%.1f%%", accuracy);
                    FontMetrics fm = g2d.getFontMetrics();
                    g2d.setColor(new Color(60, 60, 60));
                    g2d.drawString(percentStr, x + (barWidth - fm.stringWidth(percentStr)) / 2, y - 5);

                    // 显示标签
                    g2d.setColor(new Color(120, 120, 120));
                    g2d.setFont(new Font("微软雅黑", Font.PLAIN, 11));
                    g2d.drawString(labels[i], x + (barWidth - fm.stringWidth(labels[i])) / 2, height - padding + 20);
                }

                // Y轴刻度
                g2d.setColor(new Color(120, 120, 120));
                g2d.setFont(new Font("微软雅黑", Font.PLAIN, 11));
                for (int i = 0; i <= 5; i++) {
                    int percent = i * 20;
                    int y = height - padding - chartHeight * i / 5;
                    g2d.drawString(percent + "%", padding - 35, y + 5);
                }
            }
        };
        drawPanel.setBackground(Color.WHITE);
        chartPanel.add(drawPanel, BorderLayout.CENTER);

        return chartPanel;
    }
}