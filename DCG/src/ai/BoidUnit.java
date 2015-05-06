/**
 * A single Boid. Reacts to other boids in its group via pushing
 * and being pushed. Has a state system to control its behavior to
 * either align within the group or attack the player (in greater
 * detail).
 * 
 */
package ai;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;

import threadMessaging.MTMessage;
import threadMessaging.MTMessageQueue;
import threadMessaging.MTMessage.MessageType;

import com.jme.math.Vector2f;

import engine.World;
import entities.Entity;
import entities.actors.NPC;
import entities.actors.NPC.NPCType;

public class BoidUnit {
	public enum BoidUnitState {
		LFG, TARGET_HUNTING_LINE, TARGET_HUNTING_PATH, CATCHING_UP, PUSH_REACT, ALIGNING, MOVING, IDLE, WAITING_FOR_PATH_GROUP, WAITING_FOR_PATH_TARGET
	};

	private BoidGroup group;
	private BoidUnitState state = BoidUnitState.IDLE;
	private NPCType alignType;
	private Vector2f nextStep = new Vector2f();
	private float speed = 1.0f;
	private Vector2f curPush;
	private long stateTime;
	private int pushTime;
	private long lastPathingTime;
	private float lastStepVelocity;
	private MTMessageQueue msgq; // A is brain -> npc, B is npc -> brain

	private int uid;
	private NPC npc;
	private int hadCollision;

	public BoidUnit(NPC npc, MTMessageQueue msgq) {
		this.npc = npc;
		this.speed = npc.getMovementSpeed();
		this.alignType = npc.getNPCType();
		this.msgq = msgq;
		this.hadCollision = 0;
	}

	public void update(float t) {
		if (this.msgq.hasNextA()) {
			parseBrainMessage(this.msgq.nextA());
		}

		updateState();
		act();
		avoidGroupCollision(t);
		step(t);
	}

	protected void updateState() {
		if (this.state.equals(BoidUnitState.PUSH_REACT) && !(this.timeInState() > this.pushTime)) {
			return;
		} else {
			this.curPush = null;
		}

		if (this.needAlignment() && !this.getState().equals(BoidUnitState.WAITING_FOR_PATH_GROUP)) {
			this.setState(BoidUnitState.CATCHING_UP);
			// } else if (this.needAlignment()) {
			// this.setState(BoidUnitState.TARGET_HUNTING_PATH);
		} else if (!this.npc.isInAttackRange(this.npc.getTarget())) {
			if (couldWalkToAndAttack(this.npc.getTarget()) && this.hadCollision < 30) {
				this.setState(BoidUnitState.TARGET_HUNTING_LINE);
			} else {
				if (!this.getState().equals(BoidUnitState.WAITING_FOR_PATH_TARGET)) {
					this.setState(BoidUnitState.TARGET_HUNTING_PATH);
				}
			}
		} else {
			this.setState(BoidUnitState.IDLE);
			this.npc.setPath(null);
		}
	}

	private boolean couldWalkToAndAttack(Entity ent) {
		// TODO: should check if the npc can get *close enough* to attack
		return this.npc.getMap().lineOfWalk(this.npc.getPosition(), ent.getPosition(), this.npc, ent.getId());
	}

	private boolean needAlignment() {
		return this.getPos().distance(this.getGroup().getAlignPoint(this.alignType)) > this.getGroup()
				.getRadiusOfAlign(this.alignType);
	}

	private long timeInState() {
		return System.currentTimeMillis() - this.stateTime;
	}

	protected void act() {
		switch (this.state) {
			case PUSH_REACT :
				this.addStepInfluence(this.curPush);
				break;

			case CATCHING_UP :
				float dist = this.pathEndPoint().distance(this.getGroup().getAlignPoint(this.alignType));
				if (dist > this.group.getRadius() || this.hadCollision > 0) {
					this.makePathToGroup();
				}
				this.followPath();
				break;

			case WAITING_FOR_PATH_GROUP :
				this.seek(this.getGroup().getAlignPoint(this.alignType));
				break;

			case TARGET_HUNTING_LINE :
				this.seek(this.npc.getTarget().getPosition());
				break;

			case TARGET_HUNTING_PATH :
				if (this.npc.getPath() == null || this.npc.getPath().size() == 0 || this.hadCollision > 0) {
					this.makePathToTarget();
				}
				this.followPath();
				break;

			case WAITING_FOR_PATH_TARGET :
			case IDLE :
				this.curPush = null;
				break;
		}
	}

	private void avoidGroupCollision(float t) {
		for (BoidUnit other : this.group.getUnits()) {
			if (!other.equals(this)) {
				if (this.getPos().distance(other.getPos()) < (this.getRadius() + other.getRadius()) + this.speed * t) {
					avoid(other.getPos());
					this.receivePush(this.getPos().subtract(other.getPos()).mult(other.getLastStepVelocity()));
					other.receivePush(other.getPos().subtract(this.getPos()).mult(this.getLastStepVelocity()));
				}
			}
		}
	}

	private Vector2f getPos() {
		return this.npc.getPosition();
	}

	private void receivePush(Vector2f vec) {
		if (this.curPush == null) {
			this.curPush = new Vector2f(vec).normalize();

			this.curPush.rotateAroundOrigin(this.group.getWorld().getRndFloat(-0.5f, 0.5f), false);
			this.pushTime = 300 + this.group.getWorld().getRndInt(0, 200);
			this.setState(BoidUnitState.PUSH_REACT);
			this.npc.setPath(null);
		}
	}

	private void avoid(Vector2f pos) {
		Vector2f v = this.getPos().subtract(pos);
		this.addStepInfluence(v.normalize());
	}

	private void seek(Vector2f pos) {
		Vector2f v = this.getPos().subtract(pos);
		this.addStepInfluence(v.normalize().negate());
	}

	private void setState(BoidUnitState state) {
		if (!this.state.equals(state)) {
			this.state = state;
			this.stateTime = System.currentTimeMillis();
		}
	}

	public void setGroup(BoidGroup group) {
		this.group = group;
		this.npc.setId(group.getId());
	}

	public BoidGroup getGroup() {
		return group;
	}

	public float getRadius() {
		// return 7f;
		return this.npc.getSize();
	}

	protected float distanceToGroupCenter() {
		return (float) this.getPos().distance(this.group.getPos());
	}

	protected Vector2f pathEndPoint() {
		if (this.npc.getPath() != null && this.npc.getPath().size() > 0) {
			return new Vector2f(this.npc.getPath().peekLast().x, this.npc.getPath().peekLast().y);
		} else {
			return this.getPos();
		}
	}

	protected void followPath() {
		if (this.npc.getPath() != null && this.npc.getPath().size() > 0) {
			Vector2f ppv = this.npc.getPath().peekFirst();

			if (ppv.distance(this.getPos()) < 0.3f) {
				this.npc.getPath().removeFirst();
			}

			this.addStepInfluence(ppv.subtract(this.getPos()).normalize());
		} else {
			this.seek(this.getGroup().getAlignPoint(this.alignType));
		}
	}

	protected void step(float t) {
		Vector2f step = this.nextStep.normalize().multLocal(this.speed * t);

		Vector2f newPos = this.getPos().add(step);

		if (this.npc.walkablePathSpot(newPos.x, newPos.y)) {
			this.npc.setPos(newPos);
			this.hadCollision = 0;
		} else {
			this.hadCollision++;
		}

		this.lastStepVelocity = this.nextStep.length();
		this.nextStep.zero();
	}

	protected void makePathToGroup() {
		if ((this.state.equals(BoidUnitState.WAITING_FOR_PATH_GROUP) && this.timeInState() < 1000)
				|| (System.currentTimeMillis() < this.lastPathingTime + 1000)) {
			return;
		}

		this.sendMsgToAI(new MTMessage(MessageType.ENTID, (Integer) this.npc.getId()));
		this.sendMsgToAI(new MTMessage(MessageType.TARGETID, (Integer) this.npc.getTarget().getId()));
		this.sendMsgToAI(new MTMessage(MessageType.FOOTPRINT, this.npc.getFootprint()));
		this.sendMsgToAI(new MTMessage(MessageType.CURPOS, this.getPos()));
		this.sendMsgToAI(new MTMessage(MessageType.MAPLEVEL, this.npc.getMap().getLevel()));
		// this.sendMsgToAI(new MTMessage(MessageType.FINDPATH,
		// this.getGroup().getAlignPoint(this.alignType)));

		Vector2f spot = null;

		if (this.npc.walkablePathSpot(this.npc.getTarget().getPosition().x, this.npc.getTarget().getPosition().y,
				this.npc.getTarget().getId())) {
			spot = this.group.getAlignPoint(this.getAlignType());
		} else {
			// Try spots around target (target might be too close to a
			// wall/object)
			boolean found = false;

			int px = World.worldToPathing(this.group.getAlignPoint(this.getAlignType()).x);
			int py = World.worldToPathing(this.group.getAlignPoint(this.getAlignType()).y);
			for (int x = px - 2; x <= px + 2; x++) {
				for (int y = py - 2; y <= py + 2; y++) {
					if (this.npc.walkablePathSpot(x, y, this.npc.getTarget().getId())) {
						found = true;
						spot = new Vector2f(World.pathingToWorld(x), World.pathingToWorld(y));
						break;
					}
				}
			}

			if (!found) {
				// Find a spot in the room the target is in and walk there
				spot = this.npc.getTarget().getRoom().getFreeSpot(this.npc);
			}
		}

		if (spot != null) {
			this.sendMsgToAI(new MTMessage(MessageType.FINDPATH, spot));

			this.setState(BoidUnitState.WAITING_FOR_PATH_GROUP);
		} else {
			System.out.println("No free spot in target room!");
		}

		this.lastPathingTime = System.currentTimeMillis();
	}

	protected void makePathToTarget() {
		if ((this.state.equals(BoidUnitState.WAITING_FOR_PATH_TARGET) && this.timeInState() < 1000)
				|| (System.currentTimeMillis() < this.lastPathingTime + 1000)) {
			return;
		}

		Vector2f spot = null;
		// Attempt to pathfind to target
		if (this.npc.walkablePathSpot(this.npc.getTarget().getPosition().x, this.npc.getTarget().getPosition().y,
				this.npc.getTarget().getId())) {
			spot = this.npc.getTarget().getPosition();
			System.out.println("FOUND DIRECT");
		} else {
			// Try spots around target (target might be too close to a
			// wall/object)
			boolean found = false;

			int px = World.worldToPathing(this.npc.getTarget().getPosition().x);
			int py = World.worldToPathing(this.npc.getTarget().getPosition().y);
			for (int x = px - 2; x <= px + 2; x++) {
				for (int y = py - 2; y <= py + 2; y++) {
					if (this.npc.walkablePathSpot(x, y, this.npc.getTarget().getId())) {
						found = true;
						System.out.println("FOUND NEARBY");
						spot = new Vector2f(World.pathingToWorld(x), World.pathingToWorld(y));
						break;
					}
				}
			}

			if (!found) {
				// Find a spot in the room the target is in and walk there
				spot = this.npc.getTarget().getRoom().getFreeSpot(this.npc);
				if (spot != null)
					System.out.println("FOUND IN ROOM");
			}
		}

		if (spot != null) {
			this.sendMsgToAI(new MTMessage(MessageType.ENTID, (Integer) this.npc.getId()));
			this.sendMsgToAI(new MTMessage(MessageType.TARGETID, (Integer) this.npc.getTarget().getId()));
			this.sendMsgToAI(new MTMessage(MessageType.FOOTPRINT, this.npc.getFootprint()));
			this.sendMsgToAI(new MTMessage(MessageType.CURPOS, this.getPos()));
			this.sendMsgToAI(new MTMessage(MessageType.MAPLEVEL, this.npc.getMap().getLevel()));
			// this.sendMsgToAI(new MTMessage(MessageType.FINDPATH,
			// this.getGroup().getAlignPoint(this.alignType)));

			this.sendMsgToAI(new MTMessage(MessageType.FINDPATH, spot));

			this.setState(BoidUnitState.WAITING_FOR_PATH_TARGET);
		} else {
			System.out.println("No way to get to target, no free spot in room.");
		}

		this.lastPathingTime = System.currentTimeMillis();
	}

	public void addStepInfluence(Vector2f influence) {
		this.nextStep.addLocal(influence);
	}

	public int getUid() {
		return this.uid;
	}

	public float getLastStepVelocity() {
		return lastStepVelocity;
	}

	public NPCType getAlignType() {
		return this.alignType;
	}

	public BoidUnitState getState() {
		return this.state;
	}

	private void parseBrainMessage(MTMessage msg) {
		switch (msg.type) {
			case PATHLIST :
				LinkedList<Vector2f> path = (LinkedList<Vector2f>) msg.obj;
				this.npc.setPath(path);
				// System.out.println("path: " + path);
				if (this.state.equals(BoidUnitState.WAITING_FOR_PATH_GROUP)) {
					this.setState(BoidUnitState.CATCHING_UP);
				} else if (this.state.equals(BoidUnitState.WAITING_FOR_PATH_TARGET)) {
					this.setState(BoidUnitState.TARGET_HUNTING_PATH);
				}
				break;
			// case STATE :
			// this.npc.setCurState((EntityState) msg.obj);
			// break;
			case SEARCHEDPATH :
				this.npc.markSearchedPath((ArrayList<Point>) msg.obj);
				break;
		}
	}

	public void sendMsgToAI(MTMessage msg) {
		// System.out.println("Sending :" + msg);
		this.msgq.addB(msg);
	}

	public NPC getNpc() {
		return this.npc;
	}
}

/*
 * Monstrene skal pathfinde til spilleren HVIS lineofwalk(npc.pos, target.poc,
 * npc) == false
 * 
 * ellers skal de gå straight line mod ham
 * 
 * måden de pathfindet på er: 1. check om
 * npc.walkablepathspot(target.pos.x,target.pos.y) == true, hvis det er da
 * pathding der 2. hvis npc.walkablepathspot(target.pos.x,target.pos.y) ==
 * false, check de omkringliggende 24 med npc.walkablepathspot, virker ingen, 3.
 * find position i spillerens rum og prøv igen (det som sker nu)
 */
