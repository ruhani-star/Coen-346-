/*

MasterThread's Tasks:

1) Read the log file and store its lines in an array.
2) Send each line to WorkerThreads to be checked.
3) Decide how many workers run at the same time.
4) Start workers and wait for them to finish.
5) Receivce vulnerability reports from workers.
6) Keep track of the total vulnerability count.
7) Adjust the number of workers based on the results.
8) Print the final results after processing all lines.

*/

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

// Class Definition for MasterThread

public class MasterThread extends Thread {

    private final String VulnerabilityPattern = "V04K4B63CL5BK0B";
    private int worker_number = 2;
    private int Count = 2;
    private double Avg = 0.0;
    private double approximate_avg;

    private final String filename; // input file
    private String[] lines; // lines stores in array
    private int totalLines = 0; // # lines in the file

    private int nextIndex = 0; // index of the next line to process

    // Constructor (initialize filename and load file into array)
    public MasterThread(String filename) {
        this.filename = filename;
        loadFileIntoArray();
    }

    // Function to load the file into an array of strings (lines)
    private void loadFileIntoArray() {
        // First, count the total number of lines in the file
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            totalLines = (int) br.lines().count();
        } catch (IOException e) {
            // Handle file I/O exceptions if file can't be read
            System.out.println("I/O ERROR: " + e.getMessage());
            lines = null;
            totalLines = 0;
            return;
        }

        // If file's empty -> nothing to prcess
        if (totalLines == 0) {
            lines = null;
            return;
        }

        lines = new String[totalLines]; // Allocate array to hold lines
        // Read the file again and store each line in the array
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            int i = 0;
            while ((line = br.readLine()) != null && i < totalLines) {
                lines[i++] = line;
            }
        } catch (IOException e) { // If fail mid-load, treat as no data
            lines = null;
            totalLines = 0;
        }
    }

    /*
     * Synchronized method to increment the vulnerability count safely from multiple
     * threads (one update at a time)
     */
    public synchronized void incrementCount() {
        Count++;
    }

    // Method called by WorkerThreads to report a vulnerability found
    public synchronized void reportVulnerability() {
        incrementCount();
    }

    // Main run method for MasterThread
    @Override
    public void run() {
        // If file failed to load/empty -> exit
        if (lines == null || totalLines == 0) {
            System.out.println("MasterThread exiting: no data loaded.");
            return;
        }
        // Process until all lines assigned to workers
        while (nextIndex < totalLines) {

            int remaining = totalLines - nextIndex; // how many lines left to process
            int workersThisRound = Math.min(worker_number, remaining); // how many workers to start the round

            WorkerThread[] workers = new WorkerThread[workersThisRound]; // array to hold worker threads for the round
            // Start the worker threads for this round
            for (int i = 0; i < workersThisRound; i++) {
                String oneLine = lines[nextIndex++]; // Grab the next line for the worker
                workers[i] = new WorkerThread(oneLine, VulnerabilityPattern, this); // Each worker gets one line and the
                                                                                    // pattern
                workers[i].start();
            }
            // Wait for all workers to finish the round
            for (int i = 0; i < workersThisRound; i++) {
                try {
                    workers[i].join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // if interrupted, flag and exit
                    return;
                }
            }

            approximate_avg = (double) Count / totalLines; // Average vulnerabilities found per line so far

            /*
             * If the average number of vulnerabilities found per line
             * increases by more than 20%, add more worker threads.
             */
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

        // Final results
        System.out.println("MasterThread finished.");
        System.out.println("Final Count = " + Count);
        System.out.println("Final worker_number used = " + worker_number);
    }
}