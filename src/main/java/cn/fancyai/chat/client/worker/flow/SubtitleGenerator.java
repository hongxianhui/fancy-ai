package cn.fancyai.chat.client.worker.flow;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class SubtitleGenerator {

    public void drawCenteredWrappedText(BufferedImage image, String subtitleText, int centerY, int maxWidth, int borderPadding, int bgPadding) {
        Graphics2D g2d = image.createGraphics();

        // 启用抗锯齿‌
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setFont(new Font("微软雅黑", Font.BOLD, 24));

        // 获取字体参数
        FontMetrics fm = g2d.getFontMetrics();
        int lineHeight = fm.getHeight();

        // 分割多行文本
        List<String> lines = splitTextToLines(g2d, subtitleText, maxWidth);

        // 计算整体尺寸
        int maxLineWidth = lines.stream().mapToInt(fm::stringWidth).max().orElse(0);

        // 计算整体高度和起始Y坐标
        int totalHeight = lines.size() * lineHeight;
        int startY = centerY - totalHeight / 2 + fm.getAscent();

        // 背景框参数计算‌
        int bgWidth = maxLineWidth + bgPadding * 2;
        int bgHeight = totalHeight + bgPadding * 2;
        int bgX = (maxWidth - bgWidth) / 2 + borderPadding;
        int bgY = centerY - bgHeight / 2;

        // 绘制统一背景框‌
        g2d.setColor(new Color(0, 0, 0, 178));
        g2d.fillRoundRect(bgX, bgY, bgWidth, bgHeight, 15, 15);
        g2d.setColor(new Color(255, 223, 0));

        // 绘制每行文字
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int textWidth = fm.stringWidth(line);
            int x = (maxWidth - textWidth) / 2 + borderPadding; // 水平居中‌
            int y = startY + (i * lineHeight);
            g2d.drawString(line, x, y);
        }
    }

    /**
     * 智能分割文本为多行（保持行宽均匀）
     */
    private List<String> splitTextToLines(Graphics2D g2d, String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        FontMetrics fm = g2d.getFontMetrics();

        // 最佳换行算法
        int currentLineStart = 0;
        int currentLineEnd = 0;
        int textLength = text.length();

        while (currentLineEnd < textLength) {
            int nextBreak = findBestBreakPoint(text, currentLineEnd, maxWidth, fm);

            if (nextBreak == currentLineEnd) { // 无法分割则强制换行
                nextBreak = currentLineEnd + 1;
            }

            lines.add(text.substring(currentLineStart, nextBreak));
            currentLineStart = nextBreak;
            currentLineEnd = nextBreak;
        }

        return lines;
    }

    /**
     * 查找最佳换行点（保持行宽均匀）
     */
    private int findBestBreakPoint(String text, int start, int maxWidth, FontMetrics fm) {
        int textLength = text.length();
        int bestBreak = start;
        int currentWidth = 0;

        for (int i = start; i < textLength; i++) {
            char c = text.charAt(i);
            currentWidth += fm.charWidth(c);

            if (currentWidth > maxWidth) {
                // 向前查找最近的可行分割点
                int breakPos = i;
                while (breakPos > start && !isBreakCharacter(text.charAt(breakPos))) {
                    breakPos--;
                }
                return (breakPos > start) ? breakPos : i;
            }

            if (isBreakCharacter(c)) {
                bestBreak = i + 1; // 在可分割字符后换行
            }
        }
        return textLength;
    }

    /**
     * 判断是否允许换行的字符（中文通常任意位置可换行）
     */
    private boolean isBreakCharacter(char c) {
        // 中文不需要特殊处理，默认每个字符都可换行
        return true;
    }

}
