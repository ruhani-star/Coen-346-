import common.BaseThread;

/**
 * Class Philosopher.
 * Outlines main subrutines of our virtual philosopher.
 *
 * @author Serguei A. Mokhov, mokhov@cs.concordia.ca
 */
public class Philosopher extends BaseThread {
	/**
	 * Max time an action can take (in milliseconds)
	 */
	public static final long TIME_TO_WASTE = 1000;

	
	// The act of eating.
	public void eat() {
		try {
			DiningPhilosophers.soMonitor.requestPepperShakers(getTID());
			System.out.println("Philospher " + getTID() + " has started eating."); // Print the fact that a given phil
																					// (their TID) has started eating.
			Thread.yield(); // yield
			sleep((long) (Math.random() * TIME_TO_WASTE));
			Thread.yield(); // yield
			System.out.println("Philospher " + getTID() + " is done eating."); // print that they are done eating.
			DiningPhilosophers.soMonitor.releasePepperShakers(getTID());

		} catch (InterruptedException e) {
			System.err.println("Philosopher.eat():");
			DiningPhilosophers.reportException(e);
			System.exit(1);
		}
	}

	
	// The act of thinking 
	public void think() {
		try {
			System.out.println("Philospher " + getTID() + " has started thinking."); // Print the fact that a given phil
																					// (their TID) has started thinking
			Thread.yield(); // yield
			sleep((long) (Math.random() * TIME_TO_WASTE));  // sleep() for a random interval.
			Thread.yield(); // yield
			System.out.println("Philospher " + getTID() + " is done thinking."); // print that they are done thinking.

		} catch (InterruptedException e) {
			System.err.println("Philosopher.eat():");
			DiningPhilosophers.reportException(e);
			System.exit(1);
		}

	}

	// The act of talking
	public void talk() {
		System.out.println("Philosopher " + getTID() + " has started talking."); //Print # phil started talking
		Thread.yield(); //yield
		saySomething(); //say something at random
		Thread.yield(); //yield
		System.out.println("Philosopher " + getTID() + " is done talking."); //print done talking
	}

	
	 //No, this is not the act of running, just the overridden Thread.run()
	public void run() {
		for (int i = 0; i < DiningPhilosophers.DINING_STEPS; i++) {
			DiningPhilosophers.soMonitor.pickUp(getTID());

			eat();

			DiningPhilosophers.soMonitor.putDown(getTID());

			think();


			// decision is made at random whether this particular philosopher is about to talk
			if (Math.random() < 0.5); // 50% chance philosopher will talk
			{
				// Some monitor ops down here...
				DiningPhilosophers.soMonitor.requestTalk();
				talk();
				DiningPhilosophers.soMonitor.endTalk();
			}

			Thread.yield();
		}
	} // run()

	
	 //Prints out a phrase from the array of phrases at random.
	public void saySomething() {
		String[] astrPhrases = {
				"Eh, it's not easy to be a philosopher: eat, think, talk, eat...",
				"You know, true is false and false is true if you think of it",
				"2 + 2 = 5 for extremely large values of 2...",
				"If thee cannot speak, thee must be silent",
				"I think, therefore I exist " + getTID() + ""
		};

		System.out.println(
				"Philosopher " + getTID() + " says: " +
						astrPhrases[(int) (Math.random() * astrPhrases.length)]);
	}
}

// EOF
