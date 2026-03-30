import java.io.*;
import java.util.*;

// Main.java
// 1. Read processes from input.txt
// 2. Create a thread for each process + one for the scheduler
// 3. Start all process threads (they block immediately)
// 4. Start scheduler, wait for it to finish
// 5. Write event log + waiting times to output.txt

public class Main {

    public static List<Process> readInput(String path) throws IOException {
        // create empty list to hold all processes
        List<Process> processes = new ArrayList<>();

        // open the file for reading line by line
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line;
        int id = 1;

        // read each line until end of file
        while ((line = br.readLine()) != null) {
            // split line into [arrivalTime, burstTime]
            String[] parts = line.split("\\s+");
            int arrival = Integer.parseInt(parts[0]);
            int burst = Integer.parseInt(parts[1]);

            // create process and add to list
            processes.add(new Process(id, arrival, burst));
            id++;
        }

        // close file and return list
        br.close();
        return processes;
    }

    public static void writeOutput(List<String> log, List<Process> processes) throws IOException {
        // open output.txt for writing (creates it if it doesn't exist)
        BufferedWriter bw = new BufferedWriter(new FileWriter("output.txt"));

        // write every event log line
        for (String entry : log) {
            bw.write(entry);
            bw.newLine();
        }

        // write separator and waiting times header
        bw.write("--------------------------------------------------");
        bw.newLine();
        bw.write("Waiting Times:");
        bw.newLine();

        // compute and write each process waiting time
        // formula: finishTime - arrivalTime - burstTime
        for (Process p : processes) {
            int waiting = p.getFinishTime() - p.getArrivalTime() - p.getBurstTime();
            bw.write("Process " + p.getProcessId() + ": " + waiting);
            bw.newLine();
        }

        // close the file
        bw.close();
    }

    public static void main(String[] args) throws Exception {
        // read all processes from input.txt
        List<Process> processes = readInput("input.txt");

        // create scheduler and wrap it in a thread
        Scheduler scheduler = new Scheduler(processes);
        Thread schedulerThread = new Thread(scheduler);

        // create and start a thread for each process
        // each one immediately blocks on runSignal waiting for the scheduler
        List<Thread> processThreads = new ArrayList<>();

        for (Process p : processes) {
            Thread t = new Thread(p);
            processThreads.add(t);
            t.start();
        }

        // start the scheduler thread
        schedulerThread.start();

        // wait for scheduler to fully finish before writing output
        schedulerThread.join();
        for (Thread t : processThreads) {
            t.join();
        }
        // write results to output.txt
        writeOutput(scheduler.getEventLog(), processes);
    }
}