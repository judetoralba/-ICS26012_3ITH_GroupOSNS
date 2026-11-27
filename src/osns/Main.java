package osns;

import java.awt.Font;
import java.util.Enumeration;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import osns.ui.MainFrame;

/**
 * Program entry point: CPU and Disk Scheduling Simulator.
 * Group OSNS - 3ITH - ICS26012 Operating Systems.
 */
public class Main {
    public static void main(String[] args) {
        // Larger default font so the screen is readable in the video presentation.
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                FontUIResource f = (FontUIResource) value;
                UIManager.put(key, new FontUIResource(f.getFamily(), f.getStyle(), 14));
            }
        }
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
