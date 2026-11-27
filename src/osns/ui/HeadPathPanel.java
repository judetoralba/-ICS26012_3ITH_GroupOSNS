package osns.ui;

import java.awt.*;
import javax.swing.JPanel;
import osns.model.DiskResult;

/**
 * Draws the path of the disk head: the x-axis is the cylinder number (0 to N-1),
 * and each step of the seek sequence goes one row down.
 * Group OSNS - shared UI class.
 */
public class HeadPathPanel extends JPanel {
    private static final int LEFT = 40, RIGHT = 40, TOP = 50, ROW = 32;
    private DiskResult result;
    private int diskSize = 200;

    public HeadPathPanel() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(700, 150));
    }

    public void setResult(DiskResult result, int diskSize) {
        this.result = result;
        this.diskSize = diskSize;
        setPreferredSize(new Dimension(700, TOP + (result.sequence.length + 1) * ROW + 20));
        revalidate();
        repaint();
    }

    public void clear() {
        result = null;
        setPreferredSize(new Dimension(700, 150));
        revalidate();
        repaint();
    }

    private int xOf(int cylinder) {
        int width = getWidth() - LEFT - RIGHT;
        return LEFT + (int) Math.round((double) cylinder * width / (diskSize - 1));
    }

    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (result == null) {
            g.setColor(Color.GRAY);
            g.drawString("Head path graph will appear here after Compute.", LEFT, TOP);
            return;
        }
        g.setFont(getFont().deriveFont(12f));
        FontMetrics fm = g.getFontMetrics();

        // Cylinder axis at the top: 0, N-1 and every cylinder that is visited.
        g.setColor(Color.BLACK);
        g.drawLine(xOf(0), TOP - 20, xOf(diskSize - 1), TOP - 20);
        drawTick(g, fm, 0);
        drawTick(g, fm, diskSize - 1);

        int prevX = xOf(result.initialHead), prevY = TOP;
        drawPoint(g, fm, result.initialHead, prevX, prevY, true);
        for (int i = 0; i < result.sequence.length; i++) {
            int x = xOf(result.sequence[i]);
            int y = TOP + (i + 1) * ROW;
            g.setColor(new Color(0x4E79A7));
            g.setStroke(new BasicStroke(2f));
            g.drawLine(prevX, prevY, x, y);
            drawPoint(g, fm, result.sequence[i], x, y, false);
            prevX = x;
            prevY = y;
        }
    }

    private void drawTick(Graphics2D g, FontMetrics fm, int cylinder) {
        int x = xOf(cylinder);
        g.drawLine(x, TOP - 24, x, TOP - 16);
        String s = String.valueOf(cylinder);
        g.drawString(s, x - fm.stringWidth(s) / 2, TOP - 28);
    }

    private void drawPoint(Graphics2D g, FontMetrics fm, int cylinder, int x, int y, boolean start) {
        g.setColor(start ? new Color(0xE15759) : new Color(0x4E79A7));
        g.fillOval(x - 5, y - 5, 10, 10);
        g.setColor(Color.BLACK);
        String s = start ? "Head " + cylinder : String.valueOf(cylinder);
        g.drawString(s, x + 8, y + 4);
    }
}
