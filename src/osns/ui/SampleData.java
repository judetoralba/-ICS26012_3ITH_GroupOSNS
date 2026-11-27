package osns.ui;

/**
 * Data loaded by the "Load Sample Data" button.
 * Replace these values with the facilitator's practice problem sets.
 * Group OSNS - shared UI class.
 */
public class SampleData {

    // Priority (Non-Preemptive): {arrival, burst, priority}
    public static final int[][] PRIORITY = {
        {0, 4, 2}, {1, 3, 1}, {2, 1, 3}, {3, 5, 1}, {4, 2, 2}, {20, 2, 1}
    };

    // Multilevel Queue: {arrival, burst, queue}
    public static final int[][] MLQ = {
        {0, 5, 2}, {1, 3, 1}, {2, 4, 1}, {3, 2, 2}, {12, 3, 1}, {20, 2, 2}
    };
    public static final String[] MLQ_ALGORITHMS = {"RR", "FCFS"};
    public static final int[] MLQ_QUANTUM = {2, 2};

    // Disk scheduling (same data for SSTF and LOOK so they can be compared)
    public static final int[] DISK_REQUESTS = {98, 183, 37, 122, 14, 124, 65, 67};
    public static final int DISK_HEAD = 53;
    public static final int DISK_SIZE = 200;
    public static final boolean DISK_TOWARD_HIGHER = true;
}
