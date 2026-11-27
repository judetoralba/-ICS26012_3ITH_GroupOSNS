package osns.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of a CPU scheduling run. Every CPU module returns this class, so all
 * metrics are computed with the SAME formulas (Section 4.3 of the guidelines):
 *   Turnaround = Completion - Arrival
 *   Waiting    = Turnaround - Burst
 *   Response   = First CPU time - Arrival
 *   Utilization = busy time / total time x 100
 *   Throughput  = processes / total time
 * Total time = from time 0 until the last process completes.
 * Group OSNS - shared model class.
 */
public class CpuResult {
    public final List<ProcessInfo> processes;
    public final List<GanttBlock> gantt;
    public final int[] completion, turnaround, waiting, response;
    public final double avgTurnaround, avgWaiting, avgResponse;
    public final int totalTime, busyTime;
    public final double utilization, throughput;

    public CpuResult(List<ProcessInfo> processes, List<GanttBlock> gantt, int[] completion, int[] firstStart) {
        this.processes = processes;
        this.gantt = gantt;
        this.completion = completion;
        int n = processes.size();
        turnaround = new int[n];
        waiting = new int[n];
        response = new int[n];
        double sumTat = 0, sumWt = 0, sumRt = 0;
        for (int i = 0; i < n; i++) {
            ProcessInfo p = processes.get(i);
            turnaround[i] = completion[i] - p.arrival;
            waiting[i] = turnaround[i] - p.burst;
            response[i] = firstStart[i] - p.arrival;
            sumTat += turnaround[i];
            sumWt += waiting[i];
            sumRt += response[i];
        }
        avgTurnaround = sumTat / n;
        avgWaiting = sumWt / n;
        avgResponse = sumRt / n;

        int busy = 0, end = 0;
        for (GanttBlock b : gantt) {
            if (!b.isIdle()) busy += b.end - b.start;
            end = Math.max(end, b.end);
        }
        busyTime = busy;
        totalTime = end;
        utilization = totalTime == 0 ? 0 : busyTime * 100.0 / totalTime;
        throughput = totalTime == 0 ? 0 : (double) n / totalTime;
    }

    /** Helper used by the schedulers: adds a time slice and merges it with the previous one if same label. */
    public static void addToGantt(List<GanttBlock> gantt, String label, int start, int end) {
        if (!gantt.isEmpty()) {
            GanttBlock last = gantt.get(gantt.size() - 1);
            if (last.label.equals(label) && last.end == start) {
                last.end = end;
                return;
            }
        }
        gantt.add(new GanttBlock(label, start, end));
    }

    public static List<GanttBlock> newGantt() {
        return new ArrayList<GanttBlock>();
    }
}
