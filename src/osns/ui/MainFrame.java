package osns.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;

/**
 * Main window: main menu plus one screen per algorithm (CardLayout).
 * Group OSNS - integration (Leader).
 */
public class MainFrame extends JFrame {
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);

    public MainFrame() {
        super("CPU and Disk Scheduling Simulator - Group OSNS");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        content.add(buildMenu(), "menu");
        content.add(new CpuPanel(this, false), "priority");
        content.add(new CpuPanel(this, true), "mlq");
        content.add(new DiskPanel(this, false), "sstf");
        content.add(new DiskPanel(this, true), "look");
        setContentPane(content);

        setSize(1150, 820);
        setLocationRelativeTo(null);
    }

    public void showMenu() {
        cards.show(content, "menu");
    }

    private JPanel buildMenu() {
        JPanel menu = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("CPU and Disk Scheduling Simulator", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
        JLabel sub = new JLabel("ICS26012 - Operating Systems  |  Group OSNS  |  3ITH", SwingConstants.CENTER);
        sub.setFont(sub.getFont().deriveFont(16f));

        JPanel cpu = new JPanel(new GridLayout(2, 1, 8, 8));
        cpu.setBorder(new TitledBorder("CPU Scheduling"));
        cpu.add(menuButton("Priority (Non-Preemptive)", "priority"));
        cpu.add(menuButton("Multilevel Queue (MLQ)", "mlq"));

        JPanel disk = new JPanel(new GridLayout(2, 1, 8, 8));
        disk.setBorder(new TitledBorder("Disk Scheduling"));
        disk.add(menuButton("Shortest Seek Time First (SSTF)", "sstf"));
        disk.add(menuButton("LOOK", "look"));

        JButton about = new JButton("About");
        about.addActionListener(e -> showAbout());
        JButton exit = new JButton("Exit");
        exit.addActionListener(e -> {
            int ans = JOptionPane.showConfirmDialog(this, "Exit the program?", "Exit", JOptionPane.YES_NO_OPTION);
            if (ans == JOptionPane.YES_OPTION) System.exit(0);
        });
        JPanel bottom = new JPanel(new GridLayout(1, 2, 8, 8));
        bottom.add(about);
        bottom.add(exit);

        menu.add(title, c);
        menu.add(sub, c);
        menu.add(cpu, c);
        menu.add(disk, c);
        menu.add(bottom, c);
        return menu;
    }

    private JButton menuButton(String text, final String card) {
        JButton b = new JButton(text);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 17f));
        b.setPreferredSize(new Dimension(420, 48));
        b.addActionListener(e -> cards.show(content, card));
        return b;
    }

    private void showAbout() {
        String html = "<html><h2>Group OSNS</h2>"
                + "ICS26012 - Operating Systems<br>Section: 3ITH<br>1st Semester, AY 2026-2027<br><br>"
                + "<b>Members and assigned algorithms</b><br>"
                + "Jude - Priority (Non-Preemptive)<br>"
                + "Ken - Multilevel Queue<br>"
                + "Rylev - Shortest Seek Time First (SSTF)<br>"
                + "Mark - LOOK<br><br>"
                + "Built with Java Swing (standard library only).</html>";
        JOptionPane.showMessageDialog(this, html, "About", JOptionPane.INFORMATION_MESSAGE);
    }
}
