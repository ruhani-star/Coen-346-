/**
 * WorkerThread
 * -Accept One log line, Vulnerability pattern, Reference to MasterThread
 * -Slide over log lines using substring windows
 * -Levenshtein functions
 * -Detect acceptable change
 * -Notify Master when vulnerability is found
 */
public class WorkerThread extends Thread { // Worker = Thread that searches one log line

    private final String logLine; // single assigned log line
    private final String pattern; // vulnerability pattern from master
    private final MasterThread master; // reference to Master thread
    // Constructor
    
    public WorkerThread(String logLine, String pattern, MasterThread master) {
        this.logLine = (logLine == null) ? "" : logLine;
        this.pattern = pattern;
        this.master = master;
    }

    @Override
    public void run() {

        // error check
        if (pattern == null || pattern.isEmpty())
            return;

        int patternLength = pattern.length();

        if (logLine.length() < patternLength)
            return;

        LevenshteinDistance ld = new LevenshteinDistance(); // each worker has its own object

        // Sliding over log line
        for (int i = 0; i <= logLine.length() - patternLength; i++) {

            String substring = logLine.substring(i, i + patternLength);

            // Apply Levenshtein function
            ld.Calculate(pattern, substring);

            // acceptable_change from LevenshteinDistance.java
            if (ld.isAcceptable_change()) {
                master.reportVulnerability(); // tell master to increment count
                return;
            }
        }
    }
}