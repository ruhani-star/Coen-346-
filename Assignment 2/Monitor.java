/**
 * Monitor.java
 *
 * Controls synchronization between philosophers.
 * Ensures safe access to shared resources (chopsticks, talking, pepper
 * shakers).
 *
 * How it works:
 * - pickUp(): philosopher becomes HUNGRY and waits until allowed to eat
 * - test(): checks if a philosopher can eat (neighbors not eating + fairness
 * using hungerCounter)
 * - putDown(): philosopher finishes eating, becomes THINKING, and allows
 * neighbors to try eating
 * - requestTalk()/endTalk(): ensures only one philosopher talks at a time
 * - requestPepperShakers()/releasePepperShakers(): ensures only 2 pepper
 * shakers are used safely
 *
 * Key ideas:
 * - Deadlock prevention: implemented in test() by allowing eating only if
 * neighbors are not eating
 * - Starvation prevention: implemented in test() using hungerCounter to
 * prioritize waiting philosophers
 * - Uses synchronized, wait(), and notifyAll() for thread coordination
 */
public class Monitor {

	// Constants for philosophers' states
	private static final int THINKING = 0;
	private static final int HUNGRY = 1;
	private static final int EATING = 2;

	private int[] state; // Array to track each philosopher's state
	private int numPhilosophers; // Number of philosophers
	private int[] hungerCounter; // For starvation prevention: track hunger count
	private boolean isTalking = false; // For talking synchronization

	// Task 5 variables
	private int availablePepperShakers = 2;
	private boolean[] hasPepperShaker;

	/**
	 * Constructor
	 */
	public Monitor(int piNumberOfPhilosophers) {

		numPhilosophers = piNumberOfPhilosophers; // Save the number of philosophers
		// Initialize arrays with size = number of philosophers
		state = new int[numPhilosophers];
		hungerCounter = new int[numPhilosophers];
		hasPepperShaker = new boolean[numPhilosophers];

		// Set initial state for all philosophers
		for (int i = 0; i < numPhilosophers; i++) {
			state[i] = THINKING; // All start THINKING
			hungerCounter[i] = 0; // No one has been hungry yet
			hasPepperShaker[i] = false; // No one has pepper shakers
		}

		System.out.println("Monitor initialized with " + numPhilosophers + " philosophers.");
	}

	/*
	 * pickUp() steps:
	 * 1. convert Thread ID. to correct array position
	 * 2. mark it as hungry
	 * 3. increase its hunger count
	 * 4. check whether it can eat now
	 * 5. if not, wait until notified
	 * 6. once allowed, reset hunger count and start eating
	 */
	public synchronized void pickUp(final int piTID) {
		// Convert TID to array index (TIDs start at 1, aarray indexes start at 0)
		int philosopherId = piTID - 1;
		// Philosopher is now hungry
		state[philosopherId] = HUNGRY;
		hungerCounter[philosopherId]++; // Increment hunger count for starvation prevention

		System.out.println("Philosopher " + piTID + " is hungry (waited " + hungerCounter[philosopherId] + " times)");

		// Try to let this philosopher eat
		test(philosopherId);

		// If test didn't change state to EATING, wait
		if (state[philosopherId] != EATING) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println("Monitor.pickUp():");
				DiningPhilosophers.reportException(e);
				System.exit(1);
			}
		}
		// Reset hunger counter when finally eating
		hungerCounter[philosopherId] = 0;
		System.out.println("Philosopher " + piTID + " starts eating.");
	}

	/**
	 * Test if philosopher with given ID can eat
	 * This implements our deadlock-free and starvation-free strategy
	 */
	private void test(int philosopherId) {
		// Get neighbors (circular table)
		int leftNeighbor = (philosopherId + numPhilosophers - 1) % numPhilosophers;
		int rightNeighbor = (philosopherId + 1) % numPhilosophers;

		// Check if philosopher can eat:
		// 1. They are hungry
		// 2. Neither neighbor is eating (this prevents deadlock)
		if (state[philosopherId] == HUNGRY &&
				state[leftNeighbor] != EATING &&
				state[rightNeighbor] != EATING) {
			// STARVATION-FREE STRATEGY:
			// Check if neighbors are also hungry and have waited LONGER
			boolean shouldEat = true;

			// If left neighbor is hungry and has waited more times, let them eat first
			if (state[leftNeighbor] == HUNGRY &&
					hungerCounter[leftNeighbor] > hungerCounter[philosopherId]) {
				shouldEat = false; // Left neighbor is hungrier
			}

			// If right neighbor is hungry and has waited more times, let them eat first
			if (state[rightNeighbor] == HUNGRY &&
					hungerCounter[rightNeighbor] > hungerCounter[philosopherId]) {
				shouldEat = false; // Right neighbor is hungrier
			}

			if (shouldEat) {
				state[philosopherId] = EATING;
			}
		}
	}

	/**
	 * When a given philosopher's done eating, they put the chopstiks down
	 * and let others know they are available.
	 */
	public synchronized void putDown(final int piTID) {
		int philosopherId = piTID - 1;

		// Task 5: Release pepper shaker if held
		if (hasPepperShaker[philosopherId]) {
			hasPepperShaker[philosopherId] = false;
			availablePepperShakers++;
		}

		// Philosopher is now thinking
		state[philosopherId] = THINKING;
		System.out.println("Philosopher " + piTID + " finished eating, is now thinking.");

		// Check who are my neighbors
		int leftNeighbor = (philosopherId + numPhilosophers - 1) % numPhilosophers;
		int rightNeighbor = (philosopherId + 1) % numPhilosophers;

		// Check if my neighbors can eat now that I'm done
		test(leftNeighbor);
		test(rightNeighbor);

		// Notify all waiting philosophers that chopsticks might be available
		notifyAll();
	}

	/**
	 * Only one philopher at a time is allowed to philosophy
	 * (while she is not eating).
	 */
	public synchronized void requestTalk() {
		// Wait if someone else is talking
		while (isTalking) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println("Monitor.requestTalk():");
				DiningPhilosophers.reportException(e);
				System.exit(1);
			}
		}

		// No one is talking, so this philosopher can talk
		isTalking = true;
		System.out.println("A philosopher started talking.");
	}

	/**
	 * When one philosopher is done talking stuff, others
	 * can feel free to start talking.
	 */
	public synchronized void endTalk() {
		// Philosopher is done talking
		isTalking = false;
		System.out.println("A philosopher finished talking.");

		// Notify waiting philosophers that they can talk now
		notifyAll();
	}

	/**
	 * Task 5: Request pepper shakers for eating
	 */
	public synchronized void requestPepperShakers(final int piTID) {
		int philosopherId = piTID - 1;

		// Need 2 pepper shakers to eat
		while (availablePepperShakers < 2) {
			try {
				wait();
			}

			catch (InterruptedException e) {
				System.err.println("Monitor.requestPepperShakers():");
				DiningPhilosophers.reportException(e);
				System.exit(1);
			}
		}

		// Take both pepper shakers
		availablePepperShakers -= 2;
		hasPepperShaker[philosopherId] = true;
		System.out.println("Philosopher " + piTID + " got pepper shakers to eat.");
	}

	/** Task 5: Release pepper shakers after eating */
	public synchronized void releasePepperShakers(final int piTID) {
		int philosopherId = piTID - 1;

		if (hasPepperShaker[philosopherId]) {
			hasPepperShaker[philosopherId] = false;
			availablePepperShakers += 2;
			System.out.println("Philosopher " + piTID + " released pepper shakers.");

			// Notify waiting philosophers that pepper shakers are now available
			notifyAll();
		}
	}
}

/**
 * Task 4: Dynamic Modification of the Number of Philosophers
 *
 * SUMMARY: Dynamic modification is NOT feasible in this implementation.
 *
 * WHY NOT?:
 * - The monitor uses fixed-size arrays (state[], hungerCounter[]) that
 * cannot be resized at runtime
 * - Neighbor relationships are fixed using circular indexing and cannot be
 * updated safely during execution
 * - All threads are created once in main(), with no mechanism to add or remove
 * threads dynamically
 * - Synchronization using wait()/notifyAll() assumes a fixed set of threads
 * - The starvation prevention (hungerCounter) would become unfair if new
 * philosophers are added
 *
 * Therefore, supporting philosophers joining or leaving during execution would
 * require a complete redesign.
 * 
 * ----------------------------------------------------------------------
 * Reason 1: FIXED-SIZE ARRAYS IN MONITOR
 * ----------------------------------------------------------------------
 * Our Monitor uses fixed-size arrays (state[], hungerCounter[],
 * hasPepperShaker[]) that are initialized in the constructor with a
 * specific size. Java arrays CANNOT be resized after creation. If we
 * wanted to add a new philosopher mid-execution, we would need a larger
 * array, but this is impossible without stopping all threads and recreating
 * new arrays - which would cause data corruption and race conditions.
 * 
 * ----------------------------------------------------------------------
 * Reason 2: CHOPSTICK COUNT IS FIXED
 * ----------------------------------------------------------------------
 * In our implementation, the number of chopsticks equals the number of
 * philosophers, set at startup. Philosopher i uses chopstick i and
 * chopstick (i+1)%N. If we add a philosopher, we would need a new chopstick,
 * but our monitor has no mechanism to create new resources dynamically.
 * If we remove a philosopher, their chopstick becomes orphaned and neighbor
 * relationships break.
 * 
 * ----------------------------------------------------------------------
 * Reason 3: STATIC NEIGHBOR RELATIONSHIPS
 * ----------------------------------------------------------------------
 * Our synchronization logic depends on fixed neighbor calculations:
 * 
 * int leftNeighbor = (philosopherId + numPhilosophers - 1) % numPhilosophers;
 * int rightNeighbor = (philosopherId + 1) % numPhilosophers;
 * 
 * This formula assumes consecutive IDs and a complete circle. Adding or
 * removing philosophers would require recalculating ALL neighbor relationships,
 * which is impossible while philosophers are actively eating/thinking/talking.
 * 
 * ----------------------------------------------------------------------
 * Reason 4: THREAD LIFECYCLE MANAGEMENT
 * ----------------------------------------------------------------------
 * All philosopher threads are created and started at once in main(), and main()
 * waits for ALL to finish using join(). There is no mechanism to:
 *
 * - Create and start new threads mid-execution
 * - Safely stop and clean up existing threads
 * - Handle threads that might be holding resources when removed
 *
 * ----------------------------------------------------------------------
 * Reason 5: WAIT()/NOTIFYALL() SYNCHRONIZATION
 * ----------------------------------------------------------------------
 * Our monitor uses wait() and notifyAll() for synchronization. This mechanism
 * assumes a FIXED set of waiting threads. If we add or remove philosophers
 * dynamically, the wait set becomes unpredictable, and notifyAll() would wake
 * up an inconsistent number of threads, breaking the synchronization logic.
 * 
 * ----------------------------------------------------------------------
 * Reason 6: STARVATION-FREE MECHANISM BREAKS
 * ----------------------------------------------------------------------
 * Our starvation-free solution uses hungerCounter[] where philosophers with
 * higher counts get priority. If we add a NEW philosopher mid-execution, their
 * hungerCounter starts at 0 while existing philosophers may have high counts
 * (5,6,7...). The new philosopher would ALWAYS have the lowest priority =
 * STARVATION.
 * 
 * If we remove a philosopher, their hunger data is lost and fairness
 * comparisons
 * become inconsistent.
 * 
 * ----------------------------------------------------------------------
 * Reason 7: TALKING MUTUAL EXCLUSION
 * ----------------------------------------------------------------------
 * Our talk synchronization uses a boolean flag (isTalking) and wait() for
 * mutual exclusion. If we try to remove a philosopher who is currently talking,
 * or add a new philosopher while someone is talking, the waiting queue becomes
 * unpredictable and the mutual exclusion guarantee breaks.
 *
 */

// EOF
