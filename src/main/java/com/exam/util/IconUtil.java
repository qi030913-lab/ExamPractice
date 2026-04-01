package com.exam.util;

import javax.swing.*;
import java.awt.*;

/**
 * 鍥炬爣宸ュ叿绫?- 鎻愪緵缇庤鐨勭煝閲忓浘鏍?
 */
public class IconUtil {
    
    /**
     * 鍒涘缓涓婚〉鍥炬爣
     */
    public static Icon createHomeIcon(Color color, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                
                // 缁樺埗鎴垮瓙
                int[] xPoints = {x + size/2, x + size, x + size, x, x};
                int[] yPoints = {y + 2, y + size/3, y + size, y + size, y + size/3};
                g2d.fillPolygon(xPoints, yPoints, 5);
                
                // 缁樺埗闂?
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
     * 鍒涘缓鏂囨。/璇曞嵎鍥炬爣
     */
    public static Icon createDocumentIcon(Color color, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                
                // 缁樺埗鏂囨。澶栨
                g2d.fillRoundRect(x + 2, y, size - 4, size, 3, 3);
                
                // 缁樺埗鏂囨。绾挎潯
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
     * 鍒涘缓鍥捐〃/鎴愮哗鍥炬爣
     */
    public static Icon createChartIcon(Color color, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                
                // 缁樺埗鏌辩姸鍥?
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
     * 鍒涘缓濂栨澂/鎴愬氨鍥炬爣
     */
    public static Icon createTrophyIcon(Color color, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                
                // 缁樺埗濂栨澂鏉韩
                int cupWidth = size * 2 / 3;
                int cupHeight = size * 2 / 3;
                int cupX = x + (size - cupWidth) / 2;
                int cupY = y + 2;
                g2d.fillRoundRect(cupX, cupY, cupWidth, cupHeight, 5, 5);
                
                // 缁樺埗濂栨澂鎶婃墜锛堝乏锛?
                g2d.setStroke(new BasicStroke(2f));
                g2d.drawArc(x, cupY + 5, size/4, size/3, 270, 180);
                
                // 缁樺埗濂栨澂鎶婃墜锛堝彸锛?
                g2d.drawArc(x + size*3/4, cupY + 5, size/4, size/3, 90, 180);
                
                // 缁樺埗濂栨澂搴曞骇
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
     * 鍒涘缓鍦嗗舰鍥炬爣锛堢敤浜庣粺璁″崱鐗囷級
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
     * 鍒涘缓鍕鹃€夊浘鏍?
     */
    public static Icon createCheckIcon(Color color, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                g2d.setStroke(new BasicStroke(2f));
                
                // 缁樺埗鍕?
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
     * 鍒涘缓鐩爣/闈跺績鍥炬爣
     */
    public static Icon createTargetIcon(Color color, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                
                // 缁樺埗涓夊眰鍦嗙幆
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
     * 鍒涘缓涓婁紶/瀵煎叆鍥炬爣
     */
    public static Icon createUploadIcon(Color color, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                g2d.setStroke(new BasicStroke(2f));
                
                // 缁樺埗涓婄澶?
                int arrowWidth = size / 2;
                int arrowHeight = size * 2 / 3;
                int centerX = x + size / 2;
                int arrowY = y + 2;
                
                // 绠ご绔栫嚎
                g2d.drawLine(centerX, arrowY, centerX, arrowY + arrowHeight);
                
                // 绠ご宸︿晶
                g2d.drawLine(centerX, arrowY, centerX - arrowWidth/2, arrowY + arrowWidth/2);
                
                // 绠ご鍙充晶
                g2d.drawLine(centerX, arrowY, centerX + arrowWidth/2, arrowY + arrowWidth/2);
                
                // 缁樺埗搴曢儴妯嚎
                g2d.drawLine(x + 2, y + size - 2, x + size - 2, y + size - 2);
                
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() { return size; }
            
            @Override
            public int getIconHeight() { return size; }
        };
    }
    
    /**
     * 鍒涘缓涓婂崌瓒嬪娍鍥炬爣
     */
    public static Icon createTrendUpIcon(Color color, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                g2d.setStroke(new BasicStroke(2f));
                
                // 缁樺埗涓婂崌鎶樼嚎
                g2d.drawLine(x + 2, y + size - 2, x + size/3, y + size*2/3);
                g2d.drawLine(x + size/3, y + size*2/3, x + size*2/3, y + size/3);
                g2d.drawLine(x + size*2/3, y + size/3, x + size - 2, y + 2);
                
                // 缁樺埗绠ご
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
    
    /**
     * 鍒涘缓鐢ㄦ埛鍥炬爣
     */
    public static Icon createUserIcon(Color color, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                
                // 缁樺埗鐢ㄦ埛澶村儚锛堝渾褰級
                int headSize = size / 2;
                int headX = x + (size - headSize) / 2;
                int headY = y + 2;
                g2d.fillOval(headX, headY, headSize, headSize);
                
                // 缁樺埗鐢ㄦ埛韬綋锛堢煩褰級- 璋冩暣浣嶇疆浣垮ご鍍忓拰韬綋涔嬮棿璺濈鏇村皬
                int bodyWidth = size * 2 / 3;
                int bodyHeight = size / 2;
                int bodyX = x + (size - bodyWidth) / 2;
                int bodyY = y + headY + headSize - 10; // 鍑忓皬澶村儚鍜岃韩浣撲箣闂寸殑闂磋窛
                g2d.fillRect(bodyX, bodyY, bodyWidth, bodyHeight);
                
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() { return size; }
            
            @Override
            public int getIconHeight() { return size; }
        };
    }
}
