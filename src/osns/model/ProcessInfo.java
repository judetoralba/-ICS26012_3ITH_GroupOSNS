package osns.model;

/**
 * One process entered by the user (shared by all CPU modules).
 * Group OSNS - shared model class.
 */
public class ProcessInfo {
    public final int number;     // 1, 2, 3 ... used for the "lower PID first" tie-break
    public final String pid;     // "P1", "P2", ...
    public final int arrival;
    public final int burst;
    public final int priority;   // lower number = higher priority (Priority only)
    public final int queue;      // queue number, 1 = highest (Multilevel Queue only)

    public ProcessInfo(int number, int arrival, int burst, int priority, int queue) {
        this.number = number;
        this.pid = "P" + number;
        this.arrival = arrival;
        this.burst = burst;
        this.priority = priority;
        this.queue = queue;
    }
}
