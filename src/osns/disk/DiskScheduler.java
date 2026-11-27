package osns.disk;

import osns.model.DiskResult;

/**
 * Shared interface for every disk scheduling module (agreed by the group).
 * Cylinders run from 0 to diskSize - 1.
 * Group OSNS.
 */
public interface DiskScheduler {
    String getName();

    /**
     * @param requests  request queue (cylinder numbers, in the order received)
     * @param head      initial head position
     * @param diskSize  number of cylinders (N)
     * @param towardHigher initial direction: true = toward higher cylinders (ignored by SSTF)
     */
    DiskResult schedule(int[] requests, int head, int diskSize, boolean towardHigher);
}
