package osns.cpu;

import java.util.List;
import osns.model.CpuResult;
import osns.model.ProcessInfo;

/**
 * Shared interface for every CPU scheduling module (agreed by the group).
 * Group OSNS.
 */
public interface CpuScheduler {
    String getName();

    CpuResult schedule(List<ProcessInfo> processes);
}
