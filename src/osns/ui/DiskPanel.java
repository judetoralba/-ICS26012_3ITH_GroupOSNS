package osns.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import osns.disk.DiskScheduler;
import osns.disk.LOOK;
import osns.disk.SSTF;
import osns.model.DiskResult;

/**
 * Screen for one disk scheduling algorithm (used by both SSTF and LOOK).
 * Only reads input, calls the scheduler, and shows the result; all scheduling logic
 * lives in the osns.disk package.
 * Group OSNS - shared UI class.
 */
public class DiskPanel extends JPanel {
    public static final int MAX_REQUESTS = 50;
    public static final int MAX_DISK_SIZE = 10000;

    private final boolean look;   // false = SSTF, true = LOOK
    private final MainFrame frame;

    private final JTextField requestsField = new JTextField(40);
    private final JTextField headField = new JTextField(5);
    private final JTextField sizeField = new JTextField("200", 5);
    private final JComboBox<String> directionBox =
            new JComboBox<String>(new String[] {"Toward higher cylinders", "Toward lower cylinders"});

    private final JLabel sequenceLabel = new JLabel(" ");
    private final DefaultTableModel stepModel;
    private final JLabel summary = new JLabel(" ");
    private final HeadPathPanel pathPanel = new HeadPathPanel();

    public DiskPanel(MainFrame frame, boolean look) {
        this.frame = frame;
        this.look = look;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel(look ? "Disk Scheduling - LOOK (Owner: Mark)"
                                       : "Disk Scheduling - SSTF (Owner: Rylev)");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));

        // ---------- Input section ----------
        JPanel input = new JPanel(new GridBagLayout());
        input.setBorder(new TitledBorder("Input"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0; c.gridy = 0;
        input.add(new JLabel("Request queue (1-" + MAX_REQUESTS + " cylinders, separated by commas or spaces):"), c);
        c.gridy = 1; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1; c.gridwidth = 4;
        input.add(requestsField, c);
        c.gridwidth = 1; c.fill = GridBagConstraints.NONE; c.weightx = 0;
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        row.add(new JLabel("Initial head position:"));
        row.add(headField);
        row.add(new JLabel("   Disk size N (cylinders 0 to N-1):"));
        row.add(sizeField);
        if (look) {
            row.add(new JLabel("   Initial direction:"));
            row.add(directionBox);
        }
        c.gridy = 2; c.gridwidth = 4;
        input.add(row, c);

        // ---------- Output section ----------
        stepModel = new DefaultTableModel(new Object[] {"Step", "From", "To", "Head Movement"}, 0) {
            public boolean isCellEditable(int r, int col) {
                return false;
            }
        };
        JTable stepTable = new JTable(stepModel);
        stepTable.setRowHeight(22);
        JScrollPane stepScroll = new JScrollPane(stepTable);
        stepScroll.setPreferredSize(new Dimension(380, 300));
        stepScroll.setBorder(new TitledBorder("Head movement per step"));

        JScrollPane pathScroll = new JScrollPane(pathPanel);
        pathScroll.setBorder(new TitledBorder("Head path graph"));
        pathScroll.getVerticalScrollBar().setUnitIncrement(16);

        sequenceLabel.setFont(sequenceLabel.getFont().deriveFont(Font.BOLD, 14f));
        summary.setFont(summary.getFont().deriveFont(Font.BOLD, 15f));
        JPanel texts = new JPanel(new GridLayout(2, 1));
        texts.add(sequenceLabel);
        texts.add(summary);

        JPanel output = new JPanel(new BorderLayout(6, 6));
        output.setBorder(new TitledBorder("Output"));
        output.add(texts, BorderLayout.NORTH);
        output.add(stepScroll, BorderLayout.WEST);
        output.add(pathScroll, BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.add(input, BorderLayout.NORTH);
        center.add(output, BorderLayout.CENTER);

        // ---------- Buttons ----------
        JButton sampleBtn = new JButton("Load Sample Data");
        JButton computeBtn = new JButton("Compute");
        JButton resetBtn = new JButton("Reset / Clear");
        JButton backBtn = new JButton("Back to Menu");
        sampleBtn.addActionListener(e -> loadSample());
        computeBtn.addActionListener(e -> compute());
        resetBtn.addActionListener(e -> reset());
        backBtn.addActionListener(e -> frame.showMenu());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 4));
        buttons.add(sampleBtn);
        buttons.add(computeBtn);
        buttons.add(resetBtn);
        buttons.add(backBtn);

        add(title, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    private void loadSample() {
        reset();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < SampleData.DISK_REQUESTS.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(SampleData.DISK_REQUESTS[i]);
        }
        requestsField.setText(sb.toString());
        headField.setText(String.valueOf(SampleData.DISK_HEAD));
        sizeField.setText(String.valueOf(SampleData.DISK_SIZE));
        directionBox.setSelectedIndex(SampleData.DISK_TOWARD_HIGHER ? 0 : 1);
    }

    private void reset() {
        requestsField.setText("");
        headField.setText("");
        sizeField.setText("200");
        directionBox.setSelectedIndex(0);
        sequenceLabel.setText(" ");
        summary.setText(" ");
        stepModel.setRowCount(0);
        pathPanel.clear();
    }

    private void compute() {
        sequenceLabel.setText(" ");
        summary.setText(" ");
        stepModel.setRowCount(0);
        pathPanel.clear();
        try {
            int size = InputCheck.parse(sizeField.getText(), "Disk size", 2, MAX_DISK_SIZE);
            int head = InputCheck.parse(headField.getText(), "Initial head position", 0, size - 1);
            int[] requests = readRequests(size);
            DiskScheduler scheduler = look ? new LOOK() : new SSTF();
            boolean up = directionBox.getSelectedIndex() == 0;
            showResult(scheduler.schedule(requests, head, size, up), size);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int[] readRequests(int size) {
        String text = requestsField.getText().trim();
        if (text.isEmpty()) throw new IllegalArgumentException("Request queue is empty. Please enter at least one cylinder.");
        String[] parts = text.split("[,\\s]+");
        if (parts.length > MAX_REQUESTS) {
            throw new IllegalArgumentException("Too many requests: enter at most " + MAX_REQUESTS + ".");
        }
        int[] requests = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            requests[i] = InputCheck.parse(parts[i], "Request #" + (i + 1), 0, size - 1);
        }
        return requests;
    }

    private void showResult(DiskResult res, int size) {
        StringBuilder seq = new StringBuilder("Order of service: " + res.initialHead);
        int from = res.initialHead;
        for (int i = 0; i < res.sequence.length; i++) {
            seq.append(" -> ").append(res.sequence[i]);
            stepModel.addRow(new Object[] {i + 1, from, res.sequence[i], res.movement[i]});
            from = res.sequence[i];
        }
        sequenceLabel.setText("<html>" + seq + "</html>");
        summary.setText(String.format("Total head movement = %d cylinders     Average seek distance = %d / %d = %.2f cylinders",
                res.totalMovement, res.totalMovement, res.sequence.length, res.averageSeek));
        pathPanel.setResult(res, size);
    }
}
