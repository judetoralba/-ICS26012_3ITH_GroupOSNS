package osns.cpu;

import java.util.List;
import osns.model.CpuResult;
import osns.model.GanttBlock;
import osns.model.ProcessInfo;

/**
 * Priority Scheduling (Non-Preemptive)
 * Owner: Jude
 *
 * How it works:
 *   Whenever the CPU is free, pick the arrived process with the HIGHEST priority
 *   (lowest priority number) and run it until it finishes. No preemption, no aging.
 *   Ties: earlier arrival first, then lower PID.
 *   If no process has arrived yet, the CPU is idle until the next arrival.
 */
public class PriorityNonPreemptive implements CpuScheduler {

    public String getName() {
        return "Priority (Non-Preemptive)";
    }

    public CpuResult schedule(List<ProcessInfo> processes) {
        int n = processes.size();
        int[] completion = new int[n];
        int[] firstStart = new int[n];
        boolean[] done = new boolean[n];
        List<GanttBlock> gantt = CpuResult.newGantt();

        int time = 0;
        int finished = 0;
        while (finished < n) {
            int chosen = pickHighestPriority(processes, done, time);

            if (chosen == -1) {
                // Nobody is ready: CPU is idle until the next process arrives.
                int nextArrival = Integer.MAX_VALUE;
                for (int i = 0; i < n; i++) {
                    if (!done[i]) nextArrival = Math.min(nextArrival, processes.get(i).arrival);
                }
                CpuResult.addToGantt(gantt, "Idle", time, nextArrival);
                time = nextArrival;
                continue;
            }

            // Run the chosen process to completion (non-preemptive).
            ProcessInfo p = processes.get(chosen);
            firstStart[chosen] = time;
            CpuResult.addToGantt(gantt, p.pid, time, time + p.burst);
            time += p.burst;
            completion[chosen] = time;
            done[chosen] = true;
            finished++;
        }
        return new CpuResult(processes, gantt, completion, firstStart);
    }

    /** Returns the index of the best ready process at the given time, or -1 if none is ready. */
    private int pickHighestPriority(List<ProcessInfo> processes, boolean[] done, int time) {
        int best = -1;
        for (int i = 0; i < processes.size(); i++) {
            ProcessInfo p = processes.get(i);
            if (done[i] || p.arrival > time) continue;
            if (best == -1 || isBetter(p, processes.get(best))) best = i;
        }
        return best;
    }

    /** Lower priority number wins; tie -> earlier arrival; tie -> lower PID. */
    private boolean isBetter(ProcessInfo a, ProcessInfo b) {
        if (a.priority != b.priority) return a.priority < b.priority;
        if (a.arrival != b.arrival) return a.arrival < b.arrival;
        return a.number < b.number;
    }
}
