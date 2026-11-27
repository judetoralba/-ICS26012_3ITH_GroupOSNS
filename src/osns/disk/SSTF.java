package osns.disk;

import osns.model.DiskResult;

/**
 * Shortest Seek Time First (SSTF)
 * Owner: Rylev
 *
 * How it works:
 *   From the current head position, always serve the pending request that is
 *   closest (smallest |cylinder - head|). Repeat until no requests are left.
 *   Tie (same distance on both sides): the LOWER cylinder number goes first.
 *   Duplicate requests have distance 0, so they are served consecutively.
 *   Direction is not used by SSTF.
 */
public class SSTF implements DiskScheduler {

    public String getName() {
        return "Shortest Seek Time First (SSTF)";
    }

    public DiskResult schedule(int[] requests, int head, int diskSize, boolean towardHigher) {
        int n = requests.length;
        boolean[] served = new boolean[n];
        int[] sequence = new int[n];
        int current = head;

        for (int step = 0; step < n; step++) {
            int best = -1;
            for (int i = 0; i < n; i++) {
                if (served[i]) continue;
                if (best == -1) {
                    best = i;
                    continue;
                }
                int d = Math.abs(requests[i] - current);
                int bestD = Math.abs(requests[best] - current);
                // Closer wins; on equal distance the lower cylinder wins.
                if (d < bestD || (d == bestD && requests[i] < requests[best])) best = i;
            }
            served[best] = true;
            sequence[step] = requests[best];
            current = requests[best];
        }
        return new DiskResult(head, sequence);
    }
}
