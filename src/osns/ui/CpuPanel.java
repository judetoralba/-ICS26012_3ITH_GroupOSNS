package osns.ui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import osns.cpu.CpuScheduler;
import osns.cpu.MultilevelQueue;
import osns.cpu.PriorityNonPreemptive;
import osns.model.CpuResult;
import osns.model.ProcessInfo;

/**
 * Screen for one CPU scheduling algorithm (used by both Priority and Multilevel Queue).
 * Only reads input, calls the scheduler, and shows the result; all scheduling logic
 * lives in the osns.cpu package.
 * Group OSNS - shared UI class.
 */
public class CpuPanel extends JPanel {
    public static final int MAX_PROCESSES = 20;
    public static final int MAX_TIME = 999;

    private final boolean mlq;               // false = Priority (Non-Preemptive), true = Multilevel Queue
    private final MainFrame frame;

    private final JTextField countField = new JTextField("5", 4);
    private final DefaultTableModel inputModel;
    private final JTable inputTable;

    // Multilevel Queue settings
    private final JComboBox<String> levelsBox = new JComboBox<String>(new String[] {"1", "2", "3"});
    private final JComboBox<String>[] algoBox;
    private final JTextField[] quantumField;

    private final DefaultTableModel outputModel;
    private final JLabel summary = new JLabel(" ");
    private final GanttChartPanel gantt = new GanttChartPanel();

    @SuppressWarnings("unchecked")
    public CpuPanel(MainFrame frame, boolean mlq) {
        this.frame = frame;
        this.mlq = mlq;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String extra = mlq ? "Queue" : "Priority";
        JLabel title = new JLabel(mlq ? "CPU Scheduling - Multilevel Queue (Owner: Ken)"
                                      : "CPU Scheduling - Priority Non-Preemptive (Owner: Jude)");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));

        // ---------- Input section ----------
        inputModel = new DefaultTableModel(new Object[] {"PID", "Arrival Time", "Burst Time", extra}, 0) {
            public boolean isCellEditable(int row, int col) {
                return col > 0;
            }
        };
        inputTable = new JTable(inputModel);
        inputTable.setRowHeight(24);
        inputTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        setRows(5);

        JPanel countRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        countRow.add(new JLabel("Number of processes (1-" + MAX_PROCESSES + "):"));
        countRow.add(countField);
        JButton setBtn = new JButton("Set");
        setBtn.addActionListener(e -> applyCount());
        countRow.add(setBtn);
        countRow.add(new JLabel(mlq ? "   Queue 1 = highest priority" : "   Lower number = higher priority"));

        algoBox = new JComboBox[3];
        quantumField = new JTextField[3];
        JPanel mlqRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        if (mlq) {
            mlqRow.add(new JLabel("Number of queues:"));
            levelsBox.setSelectedIndex(1);
            mlqRow.add(levelsBox);
            for (int q = 0; q < 3; q++) {
                algoBox[q] = new JComboBox<String>(new String[] {MultilevelQueue.FCFS, MultilevelQueue.SJF, MultilevelQueue.RR});
                quantumField[q] = new JTextField("2", 3);
                algoBox[q].addActionListener(e -> updateQueueControls());
                mlqRow.add(new JLabel("   Queue " + (q + 1) + ":"));
                mlqRow.add(algoBox[q]);
                mlqRow.add(new JLabel("Quantum (RR):"));
                mlqRow.add(quantumField[q]);
            }
            levelsBox.addActionListener(e -> updateQueueControls());
            updateQueueControls();
        }

        JPanel inputTop = new JPanel(new GridLayout(mlq ? 2 : 1, 1));
        inputTop.add(countRow);
        if (mlq) inputTop.add(mlqRow);

        JScrollPane inputScroll = new JScrollPane(inputTable);
        inputScroll.setPreferredSize(new Dimension(600, 190));
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(new TitledBorder("Input"));
        inputPanel.add(inputTop, BorderLayout.NORTH);
        inputPanel.add(inputScroll, BorderLayout.CENTER);

        // ---------- Output section ----------
        outputModel = new DefaultTableModel(new Object[] {"PID", "Arrival", "Burst", extra,
                "Completion", "Turnaround", "Waiting", "Response"}, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        JTable outputTable = new JTable(outputModel);
        outputTable.setRowHeight(24);
        JScrollPane outputScroll = new JScrollPane(outputTable);
        outputScroll.setPreferredSize(new Dimension(600, 190));
        summary.setFont(summary.getFont().deriveFont(Font.BOLD, 15f));

        JScrollPane ganttScroll = new JScrollPane(gantt);
        ganttScroll.setPreferredSize(new Dimension(600, 150));
        ganttScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        ganttScroll.setBorder(new TitledBorder("Gantt Chart"));

        JPanel outputPanel = new JPanel(new BorderLayout(5, 5));
        outputPanel.setBorder(new TitledBorder("Output"));
        outputPanel.add(ganttScroll, BorderLayout.NORTH);
        outputPanel.add(outputScroll, BorderLayout.CENTER);
        outputPanel.add(summary, BorderLayout.SOUTH);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.add(inputPanel, BorderLayout.NORTH);
        center.add(outputPanel, BorderLayout.CENTER);

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

    private void updateQueueControls() {
        int levels = levelsBox.getSelectedIndex() + 1;
        for (int q = 0; q < 3; q++) {
            algoBox[q].setEnabled(q < levels);
            quantumField[q].setEnabled(q < levels && MultilevelQueue.RR.equals(algoBox[q].getSelectedItem()));
        }
    }

    /** Resizes the input table to the given number of rows, keeping existing values. */
    private void setRows(int count) {
        while (inputModel.getRowCount() > count) inputModel.removeRow(inputModel.getRowCount() - 1);
        while (inputModel.getRowCount() < count) {
            inputModel.addRow(new Object[] {"P" + (inputModel.getRowCount() + 1), "", "", ""});
        }
    }

    private void applyCount() {
        try {
            int count = InputCheck.parse(countField.getText(), "Number of processes", 1, MAX_PROCESSES);
            setRows(count);
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void loadSample() {
        reset();
        int[][] data = mlq ? SampleData.MLQ : SampleData.PRIORITY;
        countField.setText(String.valueOf(data.length));
        setRows(data.length);
        for (int i = 0; i < data.length; i++) {
            for (int c = 0; c < 3; c++) inputModel.setValueAt(String.valueOf(data[i][c]), i, c + 1);
        }
        if (mlq) {
            levelsBox.setSelectedIndex(SampleData.MLQ_ALGORITHMS.length - 1);
            for (int q = 0; q < SampleData.MLQ_ALGORITHMS.length; q++) {
                algoBox[q].setSelectedItem(SampleData.MLQ_ALGORITHMS[q]);
                quantumField[q].setText(String.valueOf(SampleData.MLQ_QUANTUM[q]));
            }
        }
    }

    private void reset() {
        if (inputTable.isEditing()) inputTable.getCellEditor().cancelCellEditing();
        countField.setText("5");
        inputModel.setRowCount(0);
        setRows(5);
        if (mlq) {
            levelsBox.setSelectedIndex(1);
            for (int q = 0; q < 3; q++) {
                algoBox[q].setSelectedIndex(0);
                quantumField[q].setText("2");
            }
        }
        clearOutput();
    }

    private void clearOutput() {
        outputModel.setRowCount(0);
        summary.setText(" ");
        gantt.clear();
    }

    private void compute() {
        if (inputTable.isEditing()) inputTable.getCellEditor().stopCellEditing();
        clearOutput();
        try {
            int levels = mlq ? levelsBox.getSelectedIndex() + 1 : 0;
            List<ProcessInfo> processes = readProcesses(levels);
            CpuScheduler scheduler;
            if (mlq) {
                String[] algos = new String[levels];
                int[] quanta = new int[levels];
                for (int q = 0; q < levels; q++) {
                    algos[q] = (String) algoBox[q].getSelectedItem();
                    quanta[q] = MultilevelQueue.RR.equals(algos[q])
                            ? InputCheck.parse(quantumField[q].getText(), "Time quantum of Queue " + (q + 1), 1, MAX_TIME)
                            : 0;
                }
                scheduler = new MultilevelQueue(algos, quanta);
            } else {
                scheduler = new PriorityNonPreemptive();
            }
            showResult(scheduler.schedule(processes));
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private List<ProcessInfo> readProcesses(int levels) {
        List<ProcessInfo> list = new ArrayList<ProcessInfo>();
        for (int r = 0; r < inputModel.getRowCount(); r++) {
            String pid = "P" + (r + 1);
            int at = InputCheck.parse(inputModel.getValueAt(r, 1), pid + " Arrival Time", 0, MAX_TIME);
            int bt = InputCheck.parse(inputModel.getValueAt(r, 2), pid + " Burst Time", 1, MAX_TIME);
            int extra = mlq
                    ? InputCheck.parse(inputModel.getValueAt(r, 3), pid + " Queue", 1, levels)
                    : InputCheck.parse(inputModel.getValueAt(r, 3), pid + " Priority", 0, MAX_TIME);
            list.add(mlq ? new ProcessInfo(r + 1, at, bt, 0, extra) : new ProcessInfo(r + 1, at, bt, extra, 1));
        }
        return list;
    }

    private void showResult(CpuResult res) {
        for (int i = 0; i < res.processes.size(); i++) {
            ProcessInfo p = res.processes.get(i);
            outputModel.addRow(new Object[] {p.pid, p.arrival, p.burst, mlq ? p.queue : p.priority,
                    res.completion[i], res.turnaround[i], res.waiting[i], res.response[i]});
        }
        summary.setText(String.format("<html>Average Turnaround = %.2f &nbsp;&nbsp; Average Waiting = %.2f &nbsp;&nbsp; "
                + "Average Response = %.2f<br>CPU Utilization = %d / %d x 100 = %.2f%% &nbsp;&nbsp; "
                + "Throughput = %d / %d = %.4f processes per time unit</html>",
                res.avgTurnaround, res.avgWaiting, res.avgResponse,
                res.busyTime, res.totalTime, res.utilization,
                res.processes.size(), res.totalTime, res.throughput));
        gantt.setBlocks(res.gantt);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Invalid Input", JOptionPane.ERROR_MESSAGE);
    }
}
