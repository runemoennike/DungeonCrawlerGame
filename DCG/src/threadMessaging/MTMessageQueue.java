/**
 * Thread-safe none-blocking two-way message queue Intended to be used between
 * two classes residing in different threads, in both directions. Supports
 * adding and polling of messages on two channels, A and B.
 */
package threadMessaging;

import java.util.LinkedList;

public class MTMessageQueue {

	private LinkedList<MTMessage> queueA, queueB;

	public MTMessageQueue() {
		this.queueA = new LinkedList<MTMessage>();
		this.queueB = new LinkedList<MTMessage>();
	}

	public synchronized boolean hasNextA() {
		return this.queueA.size() > 0;
	}

	public synchronized MTMessage nextA() {
		return this.queueA.poll();
	}

	public synchronized void addA(MTMessage obj) {
		this.queueA.offer(obj);
	}

	public synchronized boolean hasNextB() {
		return this.queueB.size() > 0;
	}

	public synchronized MTMessage nextB() {
		return this.queueB.poll();
	}

	public synchronized void addB(MTMessage obj) {
		this.queueB.offer(obj);
	}

}
