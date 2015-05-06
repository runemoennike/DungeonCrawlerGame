/**
 * The current version of the class uses the AbstractRoom getNeighboursAndTheirNeighbours()
 * method to lock rooms away from the player and unlock rooms close by.
 * When unlocking rooms it also class the generate methods on the rooms, which
 * will generate the rooms content, has it not already been done. This is what creates the dynamic 
 * entity generation in the dungeons.
 */

package tasks;

import java.util.Iterator;
import java.util.LinkedList;

import map.AbstractRoom;
import map.Map;
import entities.actors.Actor;

public class GfxPruneAndGrowTask extends AbstractTask {

	private Map map;
	private Actor subject;
	private boolean running = false;
	private Iterator<AbstractRoom> iter;

	public GfxPruneAndGrowTask(Map map, Actor subject) {
		super();
		this.map = map;
		this.subject = subject;
	}

	@Override
	public Object getResult() {
		return null;
	}

	@Override
	boolean init() {
		return true;
	}

	@Override
	int run(int units) {
		int unitsUsed = 0;

		if (!this.running) {
			return 0;
		}

		if (this.iter == null || !this.iter.hasNext()) {
			this.iter = this.map.getRooms().iterator();
		}

		LinkedList<AbstractRoom> showList = this.map.getWorld().getLocalPlayer().getRoom()
				.getNeighboursAndTheirNeighbours();

		if (this.iter != null && this.iter.hasNext()) {
			do {
				AbstractRoom room = this.iter.next();
				
				if (room.isLocked() && showList.contains(room)) {
					room.unlockRoom();
					room.generateDoodads();
					room.generateMonsters();

				} else if (!room.isLocked() && !showList.contains(room)) {
					room.lockRoom();
				}

				unitsUsed = +room.getEntities().size();
				if (unitsUsed > units) {
					break;
				}
			} while (this.iter.hasNext());

			if (!this.iter.hasNext()) {
				this.running = false;
			}
			this.map.getEntityNode().updateGeometricState(0f, true);
		}

		return unitsUsed;
	}

	public void pause() {
		this.running = false;
	}

	public void start() {
		this.running = true;
	}

	public void unhideAll() {
		// for (Entity ent : this.map.getMapEntities()) {
		// if (ent.getCurState() == EntityState.HIDDEN) {
		// this.map.attachEntityDelayed(ent);
		// ent.setCurState(EntityState.IDLE);
		// }
		// }
		for (AbstractRoom room : this.map.getRooms()) {
			room.unlockRoom();
		}
	}

	public void fullPrune() {
		// for (Entity ent : this.map.getMapEntities()) {
		// float dist = ent.getPosition().distance(this.subject.getPosition());
		// if (dist > this.viewDist) {
		// ent.getNode().removeFromParent();
		// ent.setCurState(EntityState.HIDDEN);
		// }
		// }
		this.start();
		run(0x00FFFFFF);
	}

	public boolean isRunning() {
		return this.running;
	}
}
