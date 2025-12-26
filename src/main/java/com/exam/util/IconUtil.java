package com.exam.util;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * 图标工具类 - 提供美观的矢量图标
 */
public class IconUtil {
    
    /**
     * 创建主页图标
     */
    public static Icon createHomeIcon(Color color, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                
                // 绘制房子
                int[] xPoints = {x + size/2, x + size, x + size, x, x};
                int[] yPoints = {y + 2, y + size/3, y + size, y + size, y + size/3};
                g2d.fillPolygon(xPoints, yPoints, 5);
                
                // 绘制门
                g2d.setColor(Color.WHITE);
                g2d.fillRect(x + size/3, y + size*2/3, size/3, size/3);
                
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() { return size; }
            
            @Override
            public int getIconHeight() { return size; }
        };
    }
    
    /**
     * 创建文档/试卷图标
     */
    public static Icon createDocumentIcon(Color color, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                
                // 绘制文档外框
                g2d.fillRoundRect(x + 2, y, size - 4, size, 3, 3);
                
                // 绘制文档线条
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(1.5f));
                for (int i = 1; i <= 3; i++) {
                    int lineY = y + size * i / 4;
                    g2d.drawLine(x + 5, lineY, x + size - 5, lineY);
                }
                
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() { return size; }
            
            @Override
            public int getIconHeight() { return size; }
        };
    }
    
    /**
     * 创建图表/成绩图标
     */
    public static Icon createChartIcon(Color color, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                
                // 绘制柱状图
                int barWidth = size / 5;
                int[] heights = {size*2/3, size*4/5, size/2, size};
                for (int i = 0; i < 4; i++) {
                    int barX = x + i * (barWidth + 1);
                    int barHeight = heights[i];
                    g2d.fillRect(barX, y + size - barHeight, barWidth, barHeight);
                }
                
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() { return size; }
            
            @Override
            public int getIconHeight() { return size; }
        };
    }
    
    /**
     * 创建奖杯/成就图标
     */
    public static Icon createTrophyIcon(Color color, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                
                // 绘制奖杯杯身
                int cupWidth = size * 2 / 3;
                int cupHeight = size * 2 / 3;
                int cupX = x + (size - cupWidth) / 2;
                int cupY = y + 2;
                g2d.fillRoundRect(cupX, cupY, cupWidth, cupHeight, 5, 5);
                
                // 绘制奖杯把手（左）
                g2d.setStroke(new BasicStroke(2f));
                g2d.drawArc(x, cupY + 5, size/4, size/3, 270, 180);
                
                // 绘制奖杯把手（右）
                g2d.drawArc(x + size*3/4, cupY + 5, size/4, size/3, 90, 180);
                
                // 绘制奖杯底座
                int baseWidth = size * 3 / 4;
                int baseHeight = size / 5;
                g2d.fillRect(x + (size - baseWidth) / 2, y + size - baseHeight, baseWidth, baseHeight);
                
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() { return size; }
            
            @Override
            public int getIconHeight() { return size; }
        };
    }
    
    /**
     * 创建圆形图标（用于统计卡片）
     */
    public static Icon createCircleIcon(Color color, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                g2d.fillOval(x, y, size, size);
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() { return size; }
            
            @Override
            public int getIconHeight() { return size; }
        };
    }
    
    /**
     * 创建勾选图标
     */
    public static Icon createCheckIcon(Color color, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                g2d.setStroke(new BasicStroke(2f));
                
                // 绘制勾
                g2d.drawLine(x + 2, y + size/2, x + size/3, y + size - 3);
                g2d.drawLine(x + size/3, y + size - 3, x + size - 2, y + 2);
                
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() { return size; }
            
            @Override
            public int getIconHeight() { return size; }
        };
    }
    
    /**
     * 创建目标/靶心图标
     */
    public static Icon createTargetIcon(Color color, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                
                // 绘制三层圆环
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawOval(x + 2, y + 2, size - 4, size - 4);
                g2d.drawOval(x + size/4, y + size/4, size/2, size/2);
                g2d.fillOval(x + size*3/8, y + size*3/8, size/4, size/4);
                
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() { return size; }
            
            @Override
            public int getIconHeight() { return size; }
        };
    }
    
    /**
     * 创建上升趋势图标
     */
    public static Icon createTrendUpIcon(Color color, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                g2d.setStroke(new BasicStroke(2f));
                
                // 绘制上升折线
                g2d.drawLine(x + 2, y + size - 2, x + size/3, y + size*2/3);
                g2d.drawLine(x + size/3, y + size*2/3, x + size*2/3, y + size/3);
                g2d.drawLine(x + size*2/3, y + size/3, x + size - 2, y + 2);
                
                // 绘制箭头
                g2d.drawLine(x + size - 2, y + 2, x + size - 6, y + 2);
                g2d.drawLine(x + size - 2, y + 2, x + size - 2, y + 6);
                
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() { return size; }
            
            @Override
            public int getIconHeight() { return size; }
        };
    }
}
