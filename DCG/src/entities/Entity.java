/**
 * The Entiry class represents base class of all objects, which exist in the world.
 * 
 * The class contains information about the objects placement in the world, its state, graphic node, room,
 * map, world, caption, name, pathing, picking, footprint and other basic information.
 * 
 * The class contains a lot of important methods for changing and getting these values. Important methods 
 * are eg. walkablePathSpot, setPosition and setCurState.
 */

package entities;

import infostore.DataNode;

import java.io.IOException;
import java.util.LinkedList;

import map.AbstractRoom;
import map.Map;

import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.util.export.JMEExporter;
import com.jme.util.export.JMEImporter;
import com.jme.util.export.Savable;

import engine.Game;
import engine.Profiler;
import engine.Settings;
import engine.World;
import engine.gfx.AnimatedNode;
import engine.gfx.ModelFactory;

public abstract class Entity {

	private static final long serialVersionUID = -3087571736914444742L;

	// TODO: remove entire entity subtype system??
	public static enum EntitySubtype {
		ACTOR, ITEM, DOODAD, NPC, PLAYER, MONSTER, FRIENDLY, CONSUMABLE, WEARABLE, GOLD, DESTRUCTIBLE, DUMMY
	};

	public enum EntityState {
		IDLE, MOVE, ATTACK_PRE, ATTACK_APP, ATTACK_POST, DYING, BOID, PURGED, CAST_PRE, CAST_APP, CAST_POST, RECOVERING
	};

	protected EntityState curState = EntityState.IDLE;
	protected int id;
	protected World world;
	protected Map map;
	protected AbstractRoom room;
	protected AnimatedNode an;
	protected Node n;
	protected LinkedList<EntitySubtype> subtypes;
	protected String caption;
	protected String name;
	protected boolean noPathing = false;
	protected boolean noPicking = false;
	protected Vector3f velocity;

	protected boolean[][] pathingFootprint;
	protected boolean[][] pathingFootprintBackup;

	// protected int[][] prevPathingValues;

	private boolean locked = false;

	public Entity() {
		this.n = new Node();
		this.n.setUserData("entity", new EntityPtr(this));
		this.subtypes = new LinkedList<EntitySubtype>();
		this.velocity = new Vector3f(0, 0, 0);
	}

	public Entity(World world, DataNode m) {
		this.n = new Node();
		this.n.setUserData("entity", new EntityPtr(this));
		this.world = world;
		this.subtypes = new LinkedList<EntitySubtype>();
		this.id = this.world.getNextUid();
		this.velocity = new Vector3f(0, 0, 0);

		if (m != null && !Settings.NOGFX) {
			this.an = ModelFactory.getModel(m);
			this.an.setReusable(false);
			this.n.attachChild(this.an.getNode());
		}

		this.makePathingFootprint(1, 1);
	}

	/**
	 * Called to purge entity from map
	 */
	public void purge() {
		this.setCurState(EntityState.PURGED);
		this.n.removeFromParent();
		if (this.an != null) {
			this.an.setReusable(true);
		}
		if (!this.noPathing) {
			this.removePathingFootprint();
		}
		if (this.name != null && this.name.equals("monster_boss")) {
			Game.getInstance().getHUD().gameWon();
		}
	}

	// public void detachGfx() {
	// this.n.removeFromParent();
	// this.an.setReusable(true);
	// }
	//	
	// public void attachGfx() {
	// this.n.removeFromParent();
	// this.an.setReusable(false);
	// }

	/**
	 * Called every frame. Overriding methods should always call super.update(t)
	 * 
	 * @param t
	 *            Time since last call
	 */
	public void update(float t) {
		if (this.velocity != null && this.velocity.lengthSquared() > 0) {
			this.n.getLocalTranslation().addLocal(this.velocity.mult(t * 100f));
		}
	}

	public boolean isLocked() {
		return this.locked;
	}

	public void lock() {
		if (!this.locked) {
			this.getNode().lock();
			// this.n.removeFromParent();
			this.locked = true;
			// System.out.println("Locking: "+this);
		}
	}

	public void unlock() {
		if (this.locked) {
			this.getNode().unlock();
			// this.map.getNode().attachChild(this.n);
			this.locked = false;
			// System.out.println("Unlocking: "+this);
		}
	}

	protected void makePathingFootprint(int i, int j) {
		this.pathingFootprint = new boolean[i][j];
		// this.prevPathingValues = new int[i][j];

		for (int x = 0; x < i; x++) {
			for (int y = 0; y < j; y++) {
				this.pathingFootprint[x][y] = true;
			}
		}

		this.pathingFootprintBackup = this.pathingFootprint.clone();
	}

	public void rotate(float angle) {
		Profiler.start("Entity.rotate");

		if (angle < 0)
			angle += FastMath.TWO_PI;

		Quaternion rot = new Quaternion();
		rot.fromAngleAxis(angle, new Vector3f(0, 0, 1f));
		this.getNode().setLocalRotation(rot);

		if (this.pathingFootprint.length != this.pathingFootprint[0].length) {
			this.removePathingFootprint();
			this.pathingFootprint = this.pathingFootprintBackup.clone();
			if ((angle > FastMath.QUARTER_PI && angle < FastMath.PI - FastMath.QUARTER_PI)
					|| (angle > FastMath.PI + FastMath.QUARTER_PI && angle < FastMath.TWO_PI - FastMath.QUARTER_PI)) {
				boolean newFootprint[][] = new boolean[this.pathingFootprint[0].length][this.pathingFootprint.length];

				for (int i = 0; i < this.pathingFootprint.length; i++) {
					for (int j = 0; j < this.pathingFootprint[0].length; j++) {
						newFootprint[j][i] = this.pathingFootprint[i][j];
					}
				}

				this.pathingFootprint = newFootprint;
			}
			// this.prevPathingValues = new
			// int[this.pathingFootprint.length][this.pathingFootprint[0].length];
			this.createPathingFootprint();
		}

		Profiler.stop("Entity.rotate");
	}

	public int getId() {
		return this.id;
	}

	public Node getNode() {
		return this.n;
	}

	public Map getMap() {
		return this.map;
	}

	public void setCurState(EntityState curAction) {
		if (curAction.equals(EntityState.DYING)) {
			this.noPicking = true;
		}

		if (!this.curState.equals(EntityState.DYING) || curAction.equals(EntityState.PURGED)) {

			this.curState = curAction;
		}
	}

	public EntityState getCurState() {
		return curState;
	}

	public boolean isSubtype(EntitySubtype st) {
		return this.subtypes.contains(st);
	}

	public void addSubtype(EntitySubtype st) {
		this.subtypes.add(st);
	}

	/**
	 * Moves this entity to another position, updates World's pathing
	 * information and sets the current room
	 * 
	 * @param x
	 *            First coordinate, in World system
	 * @param y
	 *            Second coordinate, in World system
	 */
	public void setPos(float x, float y) {
		Vector3f newPos = new Vector3f(x, y, this.getNode().getLocalTranslation().z);
		if (!this.getNode().getLocalTranslation().equals(newPos)) {
			this.removePathingFootprint();
			this.getNode().setLocalTranslation(newPos);
			this.createPathingFootprint();
			if (this.room != null) {
				AbstractRoom curRoom = this.map.getRoom((int) x / World.PATHING_GRANULARITY, (int) y
						/ World.PATHING_GRANULARITY);
				if (!this.room.equals(curRoom) && curRoom != null) {
					curRoom.moveEntity(this);
				}
			}
		}
	}

	public void rebuildPathingFootprint() {
		createPathingFootprint();
	}

	/**
	 * Internal use within Entity and its extenders. Puts pathing footprint for
	 * this entity into the World's pathing map.
	 */
	protected void createPathingFootprint() {
		if (!this.noPathing) {
			Profiler.start("Entity.createPathingFootprint");

			int sx = World.worldToPathing(this.getPosition().x
					- ((float) (this.pathingFootprint.length - 1) * World.PATHING_BLOCKSIZE) / 2);
			int sy = World.worldToPathing(this.getPosition().y
					- ((float) (this.pathingFootprint[0].length - 1) * World.PATHING_BLOCKSIZE) / 2);

			for (int x = 0; x < this.pathingFootprint.length; x++) {
				for (int y = 0; y < this.pathingFootprint[0].length; y++) {
					if (this.pathingFootprint[x][y]) {
						// this.prevPathingValues[x][y] =
						// this.map.getPathingValue(sx + x, sy + y);
						this.map.setPathingMapAt(sx + x, sy + y, -this.getId());
					}
				}
			}

			Profiler.stop("Entity.createPathingFootprint");
		}
	}
	/**
	 * Internal use within Entity and its extenders. Removes pathing footprint
	 * from World's pathing map.
	 */
	protected void removePathingFootprint() {
		if (!this.noPathing) {
			Profiler.start("Entity.removePathingFootprint");

			int sx = World.worldToPathing(this.getPosition().x
					- ((float) (this.pathingFootprint.length - 1) * World.PATHING_BLOCKSIZE) / 2);
			int sy = World.worldToPathing(this.getPosition().y
					- ((float) (this.pathingFootprint[0].length - 1) * World.PATHING_BLOCKSIZE) / 2);

			for (int x = 0; x < this.pathingFootprint.length; x++) {
				for (int y = 0; y < this.pathingFootprint[0].length; y++) {
					if (this.pathingFootprint[x][y]) {
						this.map.setPathingMapAt(sx + x, sy + y, 0);
						// this.map.setPathingMapAt(sx + x, sy + y,
						// this.prevPathingValues[x][y]);
					}
				}
			}

			Profiler.stop("Entity.removePathingFootprint");
		}
	}

	public boolean walkablePathSpot(float worldx, float worldy) {
		return walkablePathSpot(World.worldToPathing(worldx), World.worldToPathing(worldy));
	}

	public boolean walkablePathSpot(float worldx, float worldy, int ignoreId) {
		return walkablePathSpot(World.worldToPathing(worldx), World.worldToPathing(worldy), ignoreId);
	}

	public boolean walkablePathSpot(int px, int py) {
		return walkablePathSpot(px, py, 0);
	}

	public boolean walkablePathSpot(int px, int py, int ignoreId) {
		if (this.noPathing) {
			return true;
		} else {
			return walkablePathSpot(px, py, this.pathingFootprint, this.map.getPathingMap(), this.id, ignoreId);
		}
	}

	public static boolean walkablePathSpot(int px, int py, boolean[][] footprint, int[][] pmap, int entId, int ignoreId) {
		int sx = World.worldToPathing(px - ((float) (footprint.length - 1) * World.PATHING_BLOCKSIZE) / 2);
		int sy = World.worldToPathing(py - ((float) (footprint[0].length - 1) * World.PATHING_BLOCKSIZE) / 2);

		if (ignoreId == 0) {
			ignoreId = entId; // dirty
		}

		for (int x = 0; x < (footprint.length); x++) {
			for (int y = 0; y < (footprint[0].length); y++) {
				int pval = Map.getPathingValue(sx + x, sy + y, pmap);
				if (footprint[x][y] && (pval < 0 && pval != -entId && pval != -ignoreId)) {
					return false;
				}
			}
		}
		return true;
	}

	public Vector2f getPosition() {
		return new Vector2f(this.getNode().getLocalTranslation().x, this.getNode().getLocalTranslation().y);
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String getCaption() {
		return caption;
	}

	public boolean getNoPicking() {
		return this.noPicking;
	}

	public void setNoPicking(boolean b) {
		this.noPicking = b;
	}

	public int getLevel() {
		return 0;
	}

	public AbstractRoom getRoom() {
		return this.room;
	}

	public void setRoom(AbstractRoom room) {
		this.room = room;
	}

	public String getName() {
		return this.name;
	}

	public World getWorld() {
		return this.world;
	}

	public void setPos(Vector2f v) {
		this.setPos(v.x, v.y);
	}

	public void setMap(Map map) {
		this.map = map;
	}

	public boolean[][] getFootprint() {
		return this.pathingFootprint;
	}

	public void setId(int id) {
		this.removePathingFootprint();
		this.id = id;
		this.createPathingFootprint();
	}

	/**
	 * Used to store a reference to the owning entity into jME nodes. Useful fx
	 * when picking to find the entity of a node. The Entity constructor should
	 * store a reference to itself into the Node userdata field "entity" using
	 * this class.
	 */
	public class EntityPtr implements Savable {

		private Entity ptr;

		public EntityPtr(Entity ptr) {
			this.ptr = ptr;
		}

		public Entity get() {
			return this.ptr;
		}

		@Override
		public Class<?> getClassTag() {
			return null;
		}

		@Override
		public void read(JMEImporter im) throws IOException {
		}

		@Override
		public void write(JMEExporter ex) throws IOException {
		}

	}
}
