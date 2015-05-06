/**
 * The main class of the Ai thread. Calls update on each
 * cell that is attached to it, yielding to other threads
 * after each update call. Is also connected with the Game
 * class in order to receive messages from it, for now
 * only pathing map and stat request.
 */
package ai;

import java.util.ArrayList;
import java.util.HashMap;

import threadMessaging.MTMessage;
import threadMessaging.MTMessageQueue;
import threadMessaging.MTMsgAIStat;
import threadMessaging.MTMsgPathMap;
import threadMessaging.MTMessage.MessageType;

import entities.actors.NPC;

public class AIWorkerMain implements Runnable {

	private MTMessageQueue msgq;
	private ArrayList<AIWorkerCell> cells;
	private HashMap<Integer, int[][]> pathingMaps;
	private Boolean running = true;
	private float cps;
	private float cpsavg = 100;

	public AIWorkerMain(MTMessageQueue msgq) {
		this.msgq = msgq;
		this.cells = new ArrayList<AIWorkerCell>();
		this.pathingMaps = new HashMap<Integer, int[][]>();
	}

	@Override
	public void run() {
		System.out.println("AI thread running.");

		int cpsc = 0;
		long cpst = System.currentTimeMillis();
		while (running) {
			parseIncommingMessages();

			synchronized (this.cells) {
				for (AIWorkerCell cell : this.cells) {
					cell.update();
					Thread.yield();
				}
			}

			// Thread.yield();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			cpsc++;
			if (System.currentTimeMillis() >= cpst + 1000) {
				this.cps = cpsc;
				this.cpsavg = this.cpsavg * 0.95f + cpsc * 0.05f;
				cpsc = 0;
				cpst = System.currentTimeMillis();
			}
		}

		System.out.println("AI thread terminating.");
	}

	private void parseIncommingMessages() {
		while (this.msgq.hasNextB()) {
			MTMessage msg = this.msgq.nextB();

			switch (msg.type) {
				case PATHMAP :
					this.pathingMaps.put(((MTMsgPathMap) msg.obj).getLevel(), ((MTMsgPathMap) msg.obj).getMap());
					break;
				case KILL :
					this.running = false;
					break;
				case SENDSTAT :
					this.msgq.addA(new MTMessage(MessageType.AISTAT, new MTMsgAIStat(this.cps, this.cpsavg, this.cells
							.size(), this.pathingMaps.size(), this.running)));
					break;
			}
		}
	}

	public MTMessageQueue addNPC(NPC npc) {
		AIWorkerCell cell = new AIWorkerCell(npc, this);
		synchronized (this.cells) {
			this.cells.add(cell);
		}
		return cell.getMsgQueue();
	}

	public int[][] getPathingMap(int level) {
		if (this.pathingMaps.containsKey(level)) {
			return this.pathingMaps.get(level);
		} else {
			System.out.println("NO PATHING MAP FOR LEVEL " + level);
			return null;
		}
	}

}
