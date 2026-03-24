import java.util.concurrent.Semaphore;

// Process.java
// 1. Each process is a real Java thread
// 2. On start, immediately blocks on runSignal waiting for scheduler
// 3. When woken, subtracts quantum from remaining time
// 4. Signals scheduler it's done via doneSignal, then blocks again
// 5. Repeats until remaining time hits zero

public class Process implements Runnable {

    // --- fields ---
    private int processId;
    private int arrivalTime;
    private int burstTime;
    private int remainingTime;
    private boolean hasStarted;
    private boolean isFinished;
    private int quantumToRun; // set by scheduler just before waking this thread
    private int finishTime;

    // runSignal: scheduler releases to wake process
    // doneSignal: process releases to wake scheduler
    private Semaphore runSignal;
    private Semaphore doneSignal;

    // --- constructor: initialize all fields and semaphores ---
    public Process(int id, int arrival, int burst) {
        this.processId = id;
        this.arrivalTime = arrival;
        this.burstTime = burst;
        this.remainingTime = burst;
        this.hasStarted = false;
        this.isFinished = false;
        this.quantumToRun = 0;
        this.finishTime = 0;

        // both start at 0 so both threads start blocked
        this.runSignal = new Semaphore(0);
        this.doneSignal = new Semaphore(0);
    }

    // --- process thread body ---
    @Override
    public void run() {
        while (remainingTime > 0) {
            try {
                // block here until scheduler calls runSignal.release()
                runSignal.acquire();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            // subtract the quantum the scheduler assigned
            remainingTime -= quantumToRun;

            // mark done if no time left
            if (remainingTime <= 0)
                isFinished = true;

            // wake the scheduler — quantum is done
            doneSignal.release();
        }
    }

    // --- called by scheduler thread: set quantum, wake process, wait for it to
    // finish ---
    public void runQuantum(int quantum) {
        // tell process how long to run
        this.quantumToRun = quantum;

        // wake the process thread
        runSignal.release();

        try {
            // block until process finishes its quantum
            doneSignal.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // --- getters and setters ---
    public int getProcessId() {
        return processId;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getBurstTime() {
        return burstTime;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public boolean hasStarted() {
        return hasStarted;
    }

    public void markStarted() {
        hasStarted = true;
    }

    public void setFinishTime(int time) {
        this.finishTime = time;
    }

    public int getFinishTime() {
        return finishTime;
    }
}