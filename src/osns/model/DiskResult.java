package osns.model;

/**
 * Result of a disk scheduling run (shared by all disk modules).
 *   Head movement = sum of |next - current|, starting from the initial head position.
 *   Average seek distance = total head movement / number of requests.
 * Group OSNS - shared model class.
 */
public class DiskResult {
    public final int initialHead;
    public final int[] sequence;     // order of service (cylinders), without the initial head
    public final int[] movement;     // head movement of each step
    public final int totalMovement;
    public final double averageSeek;

    public DiskResult(int initialHead, int[] sequence) {
        this.initialHead = initialHead;
        this.sequence = sequence;
        this.movement = new int[sequence.length];
        int current = initialHead, total = 0;
        for (int i = 0; i < sequence.length; i++) {
            movement[i] = Math.abs(sequence[i] - current);
            total += movement[i];
            current = sequence[i];
        }
        totalMovement = total;
        averageSeek = sequence.length == 0 ? 0 : (double) total / sequence.length;
    }
}
