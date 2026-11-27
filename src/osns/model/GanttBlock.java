package osns.model;

/**
 * One bar in the Gantt chart: a process (or idle time) from start to end.
 * Group OSNS - shared model class.
 */
public class GanttBlock {
    public final String label;   // "P1" or "Idle"
    public final int start;
    public int end;              // not final so consecutive units of the same process can be merged

    public GanttBlock(String label, int start, int end) {
        this.label = label;
        this.start = start;
        this.end = end;
    }

    public boolean isIdle() {
        return "Idle".equals(label);
    }
}
