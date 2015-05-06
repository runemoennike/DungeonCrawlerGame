/**
 * Used as the node of the task queue in TaskScheduler.
 */
package tasks;

public class TaskQueueNode {
	private static int nextId;
	public int id;

	public AbstractTask task;
	public TaskQueueNode next;
	public boolean highPrio;

	public TaskQueueNode() {
		this.id = TaskQueueNode.nextId++;
	}

	@Override
	public boolean equals(Object o) {
		return (this.id == ((TaskQueueNode) o).id);
	}
}
