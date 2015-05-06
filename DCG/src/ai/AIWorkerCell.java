/**
 * A cell in the AI thread. Is connected with a one-to-one releationship
 * with the NPC it does work for. The central method is the update()
 * one which will parse incoming messages and act upon them.
 */

package ai;

import java.util.LinkedList;

import threadMessaging.MTMessage;
import threadMessaging.MTMessageQueue;
import threadMessaging.MTMessage.MessageType;

import com.jme.math.Vector2f;

import entities.actors.NPC;

public class AIWorkerCell {

	private MTMessageQueue msgq; // A is aiThread -> main, B is main -> aithread
	private NPC npc;

	private Vector2f npcPos;
	private int npcId;
	private boolean[][] npcFootprint;
	private int npcMaplevel;

	private int uid = nextUid++;
	private static int nextUid = 1;
	private AIWorkerMain workerMain;
	private Integer npcTargetId;

	public AIWorkerCell(NPC npc, AIWorkerMain workerMain) {
		this.npc = npc;
		this.msgq = new MTMessageQueue();
		this.npcPos = new Vector2f();
		this.workerMain = workerMain;
	}

	public MTMessageQueue getMsgQueue() {
		return this.msgq;
	}

	public void update() {
		parseIncommingMessages();

		// if(System.currentTimeMillis() - this.testTimer > 1000) {
		// LinkedList<Vector2f> test = new LinkedList<Vector2f>();
		// test.add(new Vector2f(World.pathingToWorld(this.npcPos.x + 1),
		// World.pathingToWorld(this.npcPos.y)));
		// this.msgq.addA(new MTMessage(MTMessage.MessageType.PATHLIST, test));
		//			
		// this.msgq.addA(new MTMessage(MTMessage.MessageType.STATE,
		// EntityState.MOVE));
		//			
		// this.testTimer = System.currentTimeMillis();
		// }
	}

	public void sendMessage(MTMessage msg) {
		this.msgq.addA(msg);
	}

	private void parseIncommingMessages() {
		while (this.msgq.hasNextB()) {
			MTMessage msg = this.msgq.nextB();
			// System.out.println("recv " + msg);
			switch (msg.type) {
				case CURPOS :
					this.npcPos = (Vector2f) msg.obj;
					break;
				case ENTID :
					this.npcId = (Integer) msg.obj;
					break;
				case TARGETID :
					this.npcTargetId = (Integer) msg.obj;
					break;
				case FOOTPRINT :
					this.npcFootprint = (boolean[][]) msg.obj;
					break;
				case MAPLEVEL :
					this.npcMaplevel = (Integer) msg.obj;
					break;
				case FINDPATH :
					LinkedList<Vector2f> path = PathFinder.find(this.workerMain.getPathingMap(this.npcMaplevel),
							this.npcPos, (Vector2f) msg.obj, this.npcId, this.npcFootprint, this.npcTargetId, this);
					this.msgq.addA(new MTMessage(MessageType.PATHLIST, path));
					break;
			}
		}
	}
}
