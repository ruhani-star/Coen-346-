/**
 * WorkerThread
 *
 * Each worker receives ONE log line from MasterThread.
 * It scans the line using a sliding window and compares
 * each substring with the vulnerability pattern using
 * LevenshteinDistance.java.
 *
 * If acceptable_change becomes true, it reports one
 * vulnerability to MasterThread.
 */
public class WorkerThread extends Thread {

    private final String logLine;
    private final String pattern;
    private final MasterThread master;

    public WorkerThread(String logLine, String pattern, MasterThread master) {
        this.logLine = (logLine == null) ? "" : logLine;
        this.pattern = pattern;
        this.master = master;
    }

    @Override
    public void run() {

        // Safety checks
        if (pattern == null || pattern.isEmpty())
            return;

        int patLen = pattern.length();

        if (logLine.length() < patLen)
            return;

        // IMPORTANT:
        // Calculate() is NON-STATIC → must create an object
        LevenshteinDistance ld = new LevenshteinDistance();

        // Sliding window search
        for (int i = 0; i <= logLine.length() - patLen; i++) {

            String window = logLine.substring(i, i + patLen);

            // Call Calculate() correctly (NOT static)
            ld.Calculate(pattern, window);

            // acceptable_change is set internally in LevenshteinDistance.java
            if (ld.isAcceptable_change()) {
                master.reportVulnerability(); // safely increments count
                return; // report once per line
            }
        }
    }
}

