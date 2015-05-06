/**
 * Extend this thread to create a new task type.
 * Defines the methods that should be overwritten by tasks.
 */
package tasks;

public abstract class AbstractTask {
	/**
	 * Task should set this to true when completed, or overwrite
	 * {@link #isComplete() isComplete()}
	 */
	protected boolean isComplete = false;

	/**
	 * Do task initializations here. Should be minimal, put more expensive code
	 * in {@link #run(int) run()}
	 * 
	 * @return Success status. Returning false will cause the task to never be
	 *         run.
	 */
	abstract boolean init();

	/**
	 * Do task calculations and operations here. Task should do only "units"
	 * number of iterations or time consuming operations so as to be fair to
	 * other scheduled tasks.
	 * 
	 * @param units
	 *            The units available for this call
	 * @return The actual number of units used
	 */
	abstract int run(int units);

	/**
	 * Return task results as any form of object. May return null if not yet
	 * completed.
	 * 
	 * @return Any object containing the result
	 */
	public abstract Object getResult();

	/**
	 * Overwrite this in the task if using the {@link isComplete isComplete}
	 * boolean is not enough. If this returns true, {@link #getResult()
	 * getResult()} should be used to get the result.
	 * 
	 * @return Whether the task is completed.
	 */
	public boolean isComplete() {
		return this.isComplete;
	}
}
