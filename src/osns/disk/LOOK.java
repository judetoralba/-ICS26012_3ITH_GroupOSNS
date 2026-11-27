package osns.disk;

import java.util.Arrays;
import osns.model.DiskResult;

/**
 * LOOK Disk Scheduling
 * Owner: Mark
 *
 * How it works:
 *   The head moves in the initial direction and serves every request on the way,
 *   in cylinder order. It turns around at the LAST pending request in that direction
 *   (it does not travel to the end of the disk like SCAN), then serves the remaining
 *   requests in the opposite direction.
 *   A request at the current head position is served first (movement 0).
 *   Duplicate requests are served consecutively (movement 0).
 */
public class LOOK implements DiskScheduler {

    public String getName() {
        return "LOOK";
    }

    public DiskResult schedule(int[] requests, int head, int diskSize, boolean towardHigher) {
        int[] sorted = requests.clone();
        Arrays.sort(sorted);
        int[] sequence = new int[sorted.length];
        int k = 0;

        if (towardHigher) {
            // Going up: requests >= head in ascending order, then the rest descending.
            for (int i = 0; i < sorted.length; i++) if (sorted[i] >= head) sequence[k++] = sorted[i];
            for (int i = sorted.length - 1; i >= 0; i--) if (sorted[i] < head) sequence[k++] = sorted[i];
        } else {
            // Going down: requests <= head in descending order, then the rest ascending.
            for (int i = sorted.length - 1; i >= 0; i--) if (sorted[i] <= head) sequence[k++] = sorted[i];
            for (int i = 0; i < sorted.length; i++) if (sorted[i] > head) sequence[k++] = sorted[i];
        }
        return new DiskResult(head, sequence);
    }
}
