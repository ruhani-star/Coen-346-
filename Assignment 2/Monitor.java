/**
 * Class Monitor
 * To synchronize dining philosophers.
 *
 * @author Serguei A. Mokhov, mokhov@cs.concordia.ca
 */
public class Monitor {
	/*
	 * ------------
	 * Data members
	 * ------------
	 */

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
	 * -------------------------------
	 * User-defined monitor procedures
	 * You may need to add more procedures for task 5
	 * -------------------------------
	 */

	/**
	 * Grants request (returns) to eat when both chopsticks/forks are available.
	 * Else forces the philosopher to wait()
	 */

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

		// If test didn't change stae to EATING, wait
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
	 * When a given philosopher's done eating, they put the chopstiks/forks down
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

		// Check if neighbors can now eat
		int leftNeighbor = (philosopherId + numPhilosophers - 1) % numPhilosophers;
		int rightNeighbor = (philosopherId + 1) % numPhilosophers;

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

// EOF
