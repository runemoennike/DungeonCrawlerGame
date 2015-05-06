/**
 * The task scheduler. Accepts new tasks for queueing and
 * will run a number of tasks each time run() is called,
 * distributing available units among a number of tasks 
 * each time. Ensures that no task can block by running
 * for long times via putting tasks at the end of the queue
 * each time they have had execution time.
 */
package tasks;

import engine.Profiler;

public class TaskScheduler {
	private TaskQueueNode queueHead;
	private TaskQueueNode queueTail;

	public static float load = 0.0f;
	public static int total = 0;
	public static int running = 0;

	public TaskScheduler() {
	}

	/**
	 * Runs queued tasks. High priority tasks are run first, and if there are
	 * any units left, low priority ones next. The given amount of units are
	 * split equally within a priority grouping, however if there are too many
	 * tasks some will be postponed to a later run, ensuring a minimum number of
	 * units per task. When a task marks itself as completed, it is removed from
	 * the task list.
	 * 
	 * @param availableUnits
	 *            Maximum units to use
	 * @param minUnitsPerTask
	 *            Minimum units to give to a task
	 */
	public void run(int availableUnits, int minUnitsPerHighPrioTask, int minUnitsPerTask) {
		Profiler.start("TaskScheduler.run");

		TaskScheduler.running = 0;

		// queueDump();

		int unitsLeft = availableUnits;
		int unitsPerTask = (TaskScheduler.total == 0 ? 0 : availableUnits / TaskScheduler.total);

		if (unitsPerTask < minUnitsPerTask) {
			unitsPerTask = minUnitsPerTask;
		}

		TaskQueueNode iter = this.queueHead;
		while (iter != null) {
			if (unitsLeft < minUnitsPerTask && !iter.highPrio) {
				break;
			}

			int units = unitsPerTask;

			if (iter.highPrio && units < minUnitsPerHighPrioTask) {
				units = minUnitsPerHighPrioTask;
			}

			Profiler.start(iter.task.getClass().getSimpleName() + ".run");
			unitsLeft -= iter.task.run(units);
			Profiler.stop(iter.task.getClass().getSimpleName() + ".run");
			TaskScheduler.running++;

			if (iter.task.isComplete()) {
				// System.out.println("Task " + iter.id + " complete");
				queueRemove(iter);
				TaskScheduler.total--;
			} else {
				queueMoveLast(iter);
			}

			iter = iter.next;
		}

		TaskScheduler.load = Math
				.max(TaskScheduler.load - 0.01f, (float) (availableUnits - unitsLeft) / availableUnits);

		Profiler.stop("TaskScheduler.run");
	}

	/**
	 * Adds a task to the low priority list.
	 * 
	 * @param task
	 *            The task
	 */
	public void addTask(AbstractTask task) {
		addTask(task, false);
	}

	/**
	 * Adds a task to either the low or high priority list
	 * 
	 * @param task
	 *            The task
	 * @param highPriority
	 *            True to add to high priority list
	 */
	public void addTask(AbstractTask task, boolean highPriority) {
		if (task.init()) {
			TaskQueueNode n = new TaskQueueNode();
			n.task = task;
			n.highPrio = highPriority;
			// System.out.println("Task " + n.id + " inited.");
			queueAdd(n);
			TaskScheduler.total++;
		} else {
			// System.out.println("Task not added, init failed.");
		}
	}

	private void queueAdd(TaskQueueNode n) {
		if (this.queueHead == null) {
			this.queueHead = n;
			this.queueTail = n;
		} else if (n.highPrio) {
			n.next = this.queueHead;
			this.queueHead = n;
		} else {
			this.queueTail.next = n;
			this.queueTail = n;
		}

		// System.out.println("Task " + n.id + " added to queue.");
	}

	private void queueMoveLast(TaskQueueNode n) {
		if (n.highPrio) {
			return;
			// TODO move to end of high-prio part of queue?
		}

		queueRemove(n);

		this.queueTail.next = n;
		this.queueTail = n;
		this.queueTail.next = null;
	}

	private void queueRemove(TaskQueueNode n) {
		if (n.equals(this.queueHead)) {
			this.queueHead = null;
			if (n.next != null) {
				this.queueHead = n.next;
			} else {
				this.queueTail = null;
			}
		} else {
			TaskQueueNode it = this.queueHead;
			while (it.next != null) {
				if (it.next.equals(n)) {
					if (it.next.next != null) {
						it.next = it.next.next;
					} else {
						it.next = null;
						this.queueTail = it;
					}
					break;
				}
				it = it.next;
			}
		}
	}

	private void queueDump() {
		TaskQueueNode n = this.queueHead;

		String r = "";

		while (n != null) {
			r += n.id + (n.highPrio ? "*" : "") + " ";
			n = n.next;
		}

		System.out.println(r);
	}

}
