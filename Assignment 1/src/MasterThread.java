import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class MasterThread extends Thread {

    private final String VulnerabilityPattern = "V04K4B63CL5BK0B";
    private int worker_number = 2;
    private int Count = 2;
    private double Avg = 0.0;
    private double approximate_avg;

    private final String filename;
    private String[] lines;
    private int totalLines = 0;

    private int nextIndex = 0;

    public MasterThread(String filename) {
        this.filename = filename;
        loadFileIntoArray();
    }

    private void loadFileIntoArray() {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            totalLines = (int) br.lines().count();
        } catch (IOException e) {
            System.out.println("I/O ERROR: " + e.getMessage());
            lines = null;
            totalLines = 0;
            return;
        }

        if (totalLines == 0) {
            lines = null;
            return;
        }

        lines = new String[totalLines];
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            int i = 0;
            while ((line = br.readLine()) != null && i < totalLines) {
                lines[i++] = line;
            }
        } catch (IOException e) {
            lines = null;
            totalLines = 0;
        }
    }

    public synchronized void incrementCount() {
        Count++;
    }


    public synchronized void reportVulnerability() {
        // Fixed: report to increment Count safely
        incrementCount();
    }

    @Override
    public void run() {
        if (lines == null || totalLines == 0) {
            System.out.println("MasterThread exiting: no data loaded.");
            return;
        }

        while (nextIndex < totalLines) {

            int remaining = totalLines - nextIndex;
            int workersThisRound = Math.min(worker_number, remaining);

            WorkerThread[] workers = new WorkerThread[workersThisRound];

            for (int i = 0; i < workersThisRound; i++) {
                String oneLine = lines[nextIndex++];
                workers[i] = new WorkerThread(oneLine, VulnerabilityPattern, this);
                workers[i].start();
            }

            for (int i = 0; i < workersThisRound; i++) {
                try {
                    workers[i].join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            approximate_avg = (double) Count / totalLines;

            if (Avg == 0.0) {
                Avg = approximate_avg;
            } else if ((approximate_avg - Avg) / Avg > 0.20) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                worker_number += 2;
                Avg = approximate_avg;
            }
        }

        System.out.println("MasterThread finished.");
        System.out.println("Final Count = " + Count);
        System.out.println("Final worker_number used = " + worker_number);
    }
}

 

