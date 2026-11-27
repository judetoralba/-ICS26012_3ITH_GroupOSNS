package osns.cpu;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import osns.model.CpuResult;
import osns.model.GanttBlock;
import osns.model.ProcessInfo;

/**
 * Multilevel Queue (MLQ) Scheduling - preemptive between queues
 * Owner: Ken
 *
 * How it works:
 *   - Every process is permanently assigned to one queue (Queue 1 = highest priority).
 *   - Each queue has its own algorithm: FCFS, SJF, or RR (with its own time quantum).
 *   - Between queues: fixed-priority PREEMPTIVE scheduling. A lower queue runs only when
 *     all higher queues are empty. If a process arrives in a higher queue, the running
 *     lower-queue process is preempted.
 *   - A process preempted by a higher queue goes back to the FRONT of its own queue,
 *     so it resumes first when its queue gets the CPU again (RR gives it a new quantum).
 *   - RR: when the quantum expires, the process goes to the back of its queue. A new
 *     arrival at the same instant is queued first.
 *   - Ties (FCFS / SJF): earlier arrival first, then lower PID.
 *
 * The simulation advances one time unit at a time (all times are whole numbers).
 */
public class MultilevelQueue implements CpuScheduler {

    public static final String FCFS = "FCFS";
    public static final String SJF = "SJF";
    public static final String RR = "RR";

    private final String[] queueAlgorithm;  // algorithm of each queue (index 0 = Queue 1)
    private final int[] quantum;            // time quantum of each queue (used only by RR)

    public MultilevelQueue(String[] queueAlgorithm, int[] quantum) {
        this.queueAlgorithm = queueAlgorithm;
        this.quantum = quantum;
    }

    public String getName() {
        return "Multilevel Queue";
    }

    public CpuResult schedule(List<ProcessInfo> processes) {
        int n = processes.size();
        int levels = queueAlgorithm.length;
        int[] remaining = new int[n];
        int[] completion = new int[n];
        int[] firstStart = new int[n];
        boolean[] started = new boolean[n];
        boolean[] resumeFirst = new boolean[n];   // true = was preempted by a higher queue
        for (int i = 0; i < n; i++) remaining[i] = processes.get(i).burst;

        // One ready queue per level; each holds process indexes.
        List<LinkedList<Integer>> ready = new ArrayList<LinkedList<Integer>>();
        for (int q = 0; q < levels; q++) ready.add(new LinkedList<Integer>());

        List<GanttBlock> gantt = CpuResult.newGantt();
        int running = -1;      // index of the running process, -1 = CPU free
        int quantumUsed = 0;
        int finished = 0;
        int time = 0;

        while (finished < n) {
            // 1. New arrivals join the back of their queue (lower PID first when they arrive together).
            for (int i = 0; i < n; i++) {
                ProcessInfo p = processes.get(i);
                if (p.arrival == time) ready.get(p.queue - 1).addLast(i);
            }

            // 2. RR quantum expired: back of its queue (after the new arrivals of this instant).
            if (running != -1) {
                int q = processes.get(running).queue - 1;
                if (RR.equals(queueAlgorithm[q]) && quantumUsed == quantum[q]) {
                    ready.get(q).addLast(running);
                    running = -1;
                }
            }

            // 3. Preemption: a higher queue has a ready process.
            if (running != -1) {
                int q = processes.get(running).queue - 1;
                int top = highestNonEmptyQueue(ready);
                if (top != -1 && top < q) {
                    resumeFirst[running] = true;
                    ready.get(q).addFirst(running);
                    running = -1;
                }
            }

            // 4. CPU free: take a process from the highest non-empty queue.
            if (running == -1) {
                int q = highestNonEmptyQueue(ready);
                if (q != -1) {
                    running = selectFromQueue(ready.get(q), queueAlgorithm[q], processes, resumeFirst);
                    resumeFirst[running] = false;
                    quantumUsed = 0;
                }
            }

            // 5. Run for one time unit (or stay idle).
            if (running == -1) {
                CpuResult.addToGantt(gantt, "Idle", time, time + 1);
            } else {
                if (!started[running]) {
                    started[running] = true;
                    firstStart[running] = time;
                }
                CpuResult.addToGantt(gantt, processes.get(running).pid, time, time + 1);
                remaining[running]--;
                quantumUsed++;
                if (remaining[running] == 0) {
                    completion[running] = time + 1;
                    finished++;
                    running = -1;
                }
            }
            time++;
        }
        return new CpuResult(processes, gantt, completion, firstStart);
    }

    /** Index of the highest-priority queue that is not empty, or -1 if all are empty. */
    private int highestNonEmptyQueue(List<LinkedList<Integer>> ready) {
        for (int q = 0; q < ready.size(); q++) {
            if (!ready.get(q).isEmpty()) return q;
        }
        return -1;
    }

    /** Removes and returns the next process of one queue according to that queue's algorithm. */
    private int selectFromQueue(LinkedList<Integer> queue, String algorithm,
                                List<ProcessInfo> processes, boolean[] resumeFirst) {
        // A process that was preempted by a higher queue always resumes first.
        if (resumeFirst[queue.getFirst()]) return queue.removeFirst();

        if (SJF.equals(algorithm)) {
            Integer best = null;
            for (Integer i : queue) {
                if (best == null || shorter(processes.get(i), processes.get(best))) best = i;
            }
            queue.remove(best);
            return best;
        }
        // FCFS and RR: take the front of the queue (queue is kept in arrival order).
        return queue.removeFirst();
    }

    /** SJF order: shorter burst; tie -> earlier arrival; tie -> lower PID. */
    private boolean shorter(ProcessInfo a, ProcessInfo b) {
        if (a.burst != b.burst) return a.burst < b.burst;
        if (a.arrival != b.arrival) return a.arrival < b.arrival;
        return a.number < b.number;
    }
}
