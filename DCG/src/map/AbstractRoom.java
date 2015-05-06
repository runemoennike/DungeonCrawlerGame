/**
 * This abstract class describes the common properties of the Room and
 * Corridor classes. Amongst these are neighbours, map and the entities that the room contains.
 * 
 * This class contains some important methods used for map generation. Also in calls update 
 * on all its entities and supplies methods for adding and removing entities and moving entities.
 * The clas also contains the methods used for monster and doodads generation.
 */

package map;

import infostore.DataManager;
import infostore.DataNode;
import infostore.DataManager.DataType;

import java.awt.Point;
import java.util.LinkedList;

import threadMessaging.MTMessageQueue;

import com.jme.math.Vector2f;

import ai.BoidGroup;
import ai.BoidUnit;
import engine.Game;
import engine.World;
import entities.Entity;
import entities.Entity.EntityState;
import entities.actors.Actor;
import entities.actors.Monster;
import entities.actors.Player;
import entities.doodads.Doodad;
import entities.items.Item;

public abstract class AbstractRoom {

	private boolean locked;
	private boolean doodadsGenerated;
	private boolean monstersGenerated;
	private LinkedList<AbstractRoom> neighbours;
	private LinkedList<Entity> entities;
	private LinkedList<Entity> actors;
	protected World world;
	protected Map map;

	private LinkedList<Entity> entitiesToMove;

	AbstractRoom(Map map) {
		this.locked = false;
		this.doodadsGenerated = false;
		this.monstersGenerated = false;
		this.world = map.getWorld();
		this.map = map;
		this.entitiesToMove = new LinkedList<Entity>();
		this.entities = new LinkedList<Entity>();
		this.actors = new LinkedList<Entity>();
		this.neighbours = new LinkedList<AbstractRoom>();
	}

	void addNeighbour(AbstractRoom room) {
		if (!this.neighbours.contains(room)) {
			this.neighbours.add(room);
			room.neighbours.add(this);
		}
	}

	boolean isNeighbour(AbstractRoom r) {
		return this.neighbours.contains(r);
	}

	public LinkedList<AbstractRoom> getNeighbours() {
		return this.neighbours;
	}

	public LinkedList<AbstractRoom> getNeighboursAndTheirNeighbours() {
		LinkedList<AbstractRoom> list = new LinkedList<AbstractRoom>(this.neighbours);
		list.add(this);
		for (AbstractRoom n1 : this.neighbours) {
			for (AbstractRoom n2 : n1.neighbours) {
				if (!list.contains(n2)) {
					list.add(n2);
				}
			}
		}
		return list;
	}

	public void findAndSetNeighbours() {
		for (AbstractRoom room : this.map.getRooms()) {
			if (this.isNeighbourByPosition(room) && !this.equals(room)) {
				this.addNeighbour(room);
			}
		}
	}

	public LinkedList<Entity> getEntities() {
		return this.entities;
	}

	// abstract void createWallsAndFloor(int level);
	// TODO: Use level when creating

	abstract int generate();

	public abstract boolean contains(Point p);

	abstract boolean isNeighbourByPosition(AbstractRoom r);

	public abstract Point getCenter();

	public boolean neighboursContain(Point p) {
		for (AbstractRoom n : this.neighbours) {
			if (n.contains(p)) {
				return true;
			}
		}
		return false;
	}

	public void update(float t) {
		LinkedList<Entity> remove = new LinkedList<Entity>();

		for (Entity e : this.entities) {
			e.update(t);
			if (e.getCurState().equals(EntityState.PURGED)) {
				remove.add(e);
			}
		}

		for (Entity e : this.actors) {
			e.update(t);
			if (e.getCurState().equals(EntityState.PURGED)) {
				remove.add(e);
			}
		}

		for (Entity e : remove) {
			this.removeEntity(e);
		}

		for (Entity e : this.entitiesToMove) {
			e.getRoom().actors.remove(e);
			e.setRoom(this);
			this.actors.add(e);
			if (this.locked) {
				e.lock();
				System.out.println("Moved " + e + " to locked room.");
			} else {
				e.unlock();
				System.out.println("Moved " + e + " to unlocked room.");
			}
			if (e.getClass().equals(Player.class)) {
				this.map.pruneAndGrowTask.start();
			}
		}
		this.entitiesToMove.clear();
	}

	public void removeEntity(Entity e) {
		e.unlock();
		this.map.detachEntity(e);
		this.entities.remove(e);
		this.actors.remove(e);
		e.setRoom(null);
		e.setMap(null);
	}

	public void addEntity(Entity e) {
		e.setMap(this.map);
		e.setRoom(this);
		this.entities.add(e);
		this.map.attachEntity(e);
		if (this.locked) {
			e.lock();
		} else {
			e.unlock();
		}
	}

	public void addActor(Actor a) {
		a.setRoom(this);
		a.setMap(this.map);
		this.actors.add(a);
		this.map.attachEntity(a);
		if (this.locked) {
			a.lock();
		} else {
			a.unlock();
		}
	}

	public void moveEntity(Entity e) {
		this.entitiesToMove.add(e);
	}

	public LinkedList<Entity> getActors() {
		return this.actors;
	}

	public void lockRoom() {
		this.locked = true;
		for (Entity e : this.actors) {
			// this.map.detachEntity(e);
			e.lock();
		}
	}

	public void unlockRoom() {
		this.locked = false;
		for (Entity e : this.actors) {
			e.unlock();
		}
	}

	public boolean isLocked() {
		return this.locked;
	}

	public boolean isDoodadsGenerated() {
		return this.doodadsGenerated;
	}

	public boolean isMonstersGenerated() {
		return this.monstersGenerated;
	}

	public void generateDoodads() {
		LinkedList<DataNode> types = DataManager.findAllByType(DataType.DOODAD);
		if (!this.isDoodadsGenerated()) {
			for (DataNode n : types) {
				if (n.isChild("placement")) {
					if (this.world.roll100(n.getChild("placement").getPropI("chance"))) {
						int count = this.world.getRndInt(n.getChild("placement").getPropI("countMin"), n.getChild(
								"placement").getPropI("countMax"))
								* this.getSize() / 9;
						for (int c = 0; c < count; c++) {
							Doodad d = new Doodad(this.map, n);
							// TODO: remove bonus chance again
							d.addAllContents(Item.generateFromLootTable(DataManager.findByNameAndType(
									DataType.LOOT_TABLE, n.getProp("lootTable")), 20, this.map));
							d.placeInRoom(this);
							this.addEntity(d);
						}
					}
				}
			}
			this.doodadsGenerated = true;
		}
	}

	public void generateMonsters() {
		if (this.getClass().equals(Room.class)) {
			Room r = (Room) this;
			if (!r.entrance && !r.exit) {
				BoidGroup boidg = new BoidGroup(World.tileToWorld(this.getCenter()), this.map, 100f);

				this.map.addBoidGroup(boidg);

				LinkedList<DataNode> types = DataManager.findAllByType(DataType.MONSTER);
				if (!this.isMonstersGenerated()) {
					for (DataNode n : types) {
						if (this.world.roll100(n.getChild("placement").getPropI("chance"))) {
							int count = this.world.getRndInt(n.getChild("placement").getPropI("countMin"), n.getChild(
									"placement").getPropI("countMax"))
									* this.getSize() / 9;
							for (int c = 0; c < count; c++) {
								spawnMonster(n, boidg);
							}
						}
					}
					this.monstersGenerated = true;
				}
			}
		}
	}

	public Monster spawnMonster(DataNode n, BoidGroup boidg) {
		Monster m = new Monster(this.map, n);
		m.setId(boidg.getId());
		MTMessageQueue msgq = Game.getInstance().getAIBrain().addNPC(m);
		m.giveAllItems(Item.generateFromLootTable(DataManager.findByNameAndType(DataType.LOOT_TABLE, n
				.getProp("lootTable")), 0, this.map));
		m.placeInRoom(this);
		this.addActor(m);

		BoidUnit boidu = new BoidUnit(m, msgq);
		boidg.addUnit(boidu);
		m.setBoidController(boidu);

		return m;
	}

	public abstract int getSize();

	public abstract Vector2f getFreeSpot(Entity ent);

	public Map getMap() {
		return this.map;
	}

}
