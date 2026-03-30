import java.util.*;

// Scheduler.java
// 1. Loop until all processes are finished
// 2. Build ready queue — arrived + not done, sorted by remaining time
// 3. Pick next process using fairness rule
// 4. Compute quantum — 10% of remaining time, min 1
// 5. Hand CPU to process, block until it's done
// 6. Log Started / Resumed / Paused / Finished

public class Scheduler implements Runnable {

    // --- fields ---
    private List<Process> processes; // all processes from Main
    private List<String> eventLog; // log of all events in order
    private int currentTime; // simulated clock
    private Process lastRan; // tracks last ran process for fairness

    // --- constructor ---
    public Scheduler(List<Process> processes) {
        this.processes = processes;
        this.eventLog = new ArrayList<>();
        this.currentTime = 0;
        this.lastRan = null;
    }

    // --- getter for Main to retrieve log after scheduling is done ---
    public List<String> getEventLog() {
        return eventLog;
    }

    // --- check if every process is done ---
    private boolean allFinished() {
        for (Process p : processes) {
            if (!p.isFinished())
                return false;
        }
        return true;
    }

    // --- build and append one log string ---
    private void logEvent(int t, int pid, String type) {
        eventLog.add("Time " + t + ", Process " + pid + ", " + type);
    }

    // --- 10% of remaining time, rounded up, never below 1 ---
    private int computeQuantum(Process p) {
        return Math.max(1, (int) Math.ceil(p.getRemainingTime() * 0.10));
    }

    // --- filter to arrived + unfinished processes, then sort ---
    private List<Process> buildReadyQueue() {
        List<Process> ready = new ArrayList<>();

        // only include processes that have arrived and aren't finished
        for (Process p : processes) {
            if (p.getArrivalTime() <= currentTime && !p.isFinished()) {
                ready.add(p);
            }
        }

        // sort by remaining time, break ties by arrival time
        ready.sort((a, b) -> {
            if (a.getRemainingTime() != b.getRemainingTime())
                return a.getRemainingTime() - b.getRemainingTime();
            return a.getArrivalTime() - b.getArrivalTime();
        });

        return ready;
    }

    // --- pick shortest process, but skip it if it just ran and others are waiting
    // ---
    private Process pickNext(List<Process> ready) {
        if (ready.isEmpty())
            return null;

        // default: pick the shortest remaining time
        Process shortest = ready.get(0);

        // fairness rule: if shortest just ran and others are waiting, skip it
        if (lastRan != null && shortest == lastRan && ready.size() > 1) {
            return ready.get(1);
        }

        return shortest;
    }

    // --- main scheduler loop ---
    @Override
    public void run() {
        while (!allFinished()) {
            List<Process> ready = buildReadyQueue();

            // if no process has arrived yet, tick clock and retry
            if (ready.isEmpty()) {
                currentTime++;
                continue;
            }

            // pick next process and compute its quantum
            Process next = pickNext(ready);
            int quantum = computeQuantum(next);
            int actualRun = Math.min(quantum, next.getRemainingTime());

            // log Started on first access, Resumed on all subsequent ones
            if (!next.hasStarted()) {
                next.markStarted();
                logEvent(currentTime, next.getProcessId(), "Started");
            } else {
                logEvent(currentTime, next.getProcessId(), "Resumed");
            }

            // hand CPU to process — blocks here until process finishes quantum
            next.runQuantum(actualRun);

            // advance clock and record finish time
            currentTime += actualRun;
            next.setFinishTime(currentTime);
            lastRan = next;

            // log Finished if done, Paused if more time remains
            if (next.isFinished()) {
                logEvent(currentTime, next.getProcessId(), "Finished");
            } else {
                logEvent(currentTime, next.getProcessId(), "Paused");
            }
        }
    }
}