/**
 * Class DiningPhilosophers
 * The main starter.
 *
 * @author Serguei A. Mokhov, mokhov@cs.concordia.ca
 */
import java.util.Scanner;       //import scanner
public class DiningPhilosophers
{
	/*
	 * ------------
	 * Data members
	 * ------------
	 */
	
	// This default may be overridden from the command line
	public static final int DEFAULT_NUMBER_OF_PHILOSOPHERS = 5;

	 // Dining "iterations" per philosopher thread while they are socializing there
	public static final int DINING_STEPS = 10;
	
	// Our shared monitor for the philosphers to consult 
	public static Monitor soMonitor = null;
	/*
	 * -------
	 *Methods
	 * -------
	 */
	
	 // Main system starts up right here 
	public static void main(String[] argv)
	{
		try
		{		 
			//TASK 3:
			Scanner scanner = new Scanner(System.in);
			int iPhilosophers = 0;
			while(iPhilosophers <3){  // keep prompting the user until valid philosopher number is added
				System.out.println("Enter a philosopher number >=3: ");
				if (scanner.hasNextInt()){
					iPhilosophers = scanner.nextInt();

					if(iPhilosophers < 0){   //If negative number is entered
						System.out.println(iPhilosophers + " is not an acceptable number. Please enter a positive number >=3: ");
					}
				}
				else {  // if a not valid input is entered
					String invalidInput =scanner.next();
					System.out.println(invalidInput + " is not an acceptable number. Enter a number >=3. ");
				}
			}

			// Make the monitor aware of how many philosophers there are
			soMonitor = new Monitor(iPhilosophers);

			// Space for all the philosophers
			Philosopher aoPhilosophers[] = new Philosopher[iPhilosophers];

			// Let 'em sit down
			for(int j = 0; j < iPhilosophers; j++)
			{
				aoPhilosophers[j] = new Philosopher();
				aoPhilosophers[j].start();
			}

			System.out.println
			(
				iPhilosophers +
				" philosopher(s) came in for a dinner."
			);

			// Main waits for all philosophers to finish their dinner.
			for(int j = 0; j < iPhilosophers; j++)
				aoPhilosophers[j].join();

			System.out.println("All philosophers have left. System terminates normally.");
		}
		catch(InterruptedException e)
		{
			System.err.println("main():");
			reportException(e);
			System.exit(1);
		}
	} // main()

	/**
	 * Outputs exception information to STDERR
	 * @param poException Exception object to dump to STDERR
	 */
	public static void reportException(Exception poException)
	{
		System.err.println("Caught exception : " + poException.getClass().getName());
		System.err.println("Message          : " + poException.getMessage());
		System.err.println("Stack Trace      : ");
		poException.printStackTrace(System.err);
	}
}

// EOF
