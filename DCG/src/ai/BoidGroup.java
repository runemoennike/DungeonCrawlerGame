/**
 * Represents a group of boid controlled units. Provides
 * alignment information for attached boids, and is able
 * to consume another boid group (adopting its boids).
 * Will change its position to be near the player if the
 * player is close enough to be acquired. 
 */
package ai;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import map.AbstractRoom;
import map.Map;

import com.jme.math.FastMath;
import com.jme.math.Vector2f;

import crap.ANode;
import engine.Profiler;
import engine.World;
import entities.actors.NPC.NPCType;

public class BoidGroup {
	public enum GroupState {
		REGROUPING, MOVING, WAITING_FOR_PATH, IDLE
	};

	private GroupState state = GroupState.IDLE;
	private float acquisitionRange;
	private long nextAcqCheckTime;
	private Vector2f pos;
	private float radius;
	private LinkedList<BoidUnit> units;
	private Map map;
	protected int grpId = 0;
	private static int nextId = 100000;
	private HashMap<NPCType, HashMap<Point, LinkedList<ANode>>> goalPaths;
	private int numMelee, numRanged, numSupport;
	private ArrayList<BoidUnit> unitsToAdd;

	public int getNumMelee() {
		return numMelee;
	}

	public int getNumRanged() {
		return numRanged;
	}

	public int getNumSupport() {
		return numSupport;
	}

	public BoidGroup(Vector2f pos, Map map, float acquisitionRange) {
		this.grpId = nextId++;
		this.pos = pos;
		this.map = map;
		this.units = new LinkedList<BoidUnit>();
		this.goalPaths = new HashMap<NPCType, HashMap<Point, LinkedList<ANode>>>();
		this.goalPaths.put(NPCType.MELEE, new HashMap<Point, LinkedList<ANode>>());
		this.goalPaths.put(NPCType.RANGED, new HashMap<Point, LinkedList<ANode>>());
		this.goalPaths.put(NPCType.MAGIC, new HashMap<Point, LinkedList<ANode>>());
		this.acquisitionRange = acquisitionRange;
		this.unitsToAdd = new ArrayList<BoidUnit>();
	}

	public void addUnit(BoidUnit unit) {
		this.units.add(unit);
		unit.setGroup(this);

		float area = 0;
		for (BoidUnit u : this.units) {
			area += FastMath.PI * FastMath.sqr(u.getRadius());
		}
		this.radius = FastMath.sqrt(area) * 4;

		switch (unit.getAlignType()) {
			case MELEE :
				this.numMelee++;
				break;
			case RANGED :
				this.numRanged++;
				break;
			case MAGIC :
				this.numSupport++;
				break;
		}
	}

	public void update(float t) {
		Profiler.start("BoidGroup.update");

		if (System.currentTimeMillis() > this.nextAcqCheckTime) {
			if (this.pos.distance(this.map.getWorld().getLocalPlayer().getPosition()) < this.acquisitionRange) {
				Vector2f lineToPlayer = this.pos.subtract(this.map.getWorld().getLocalPlayer().getPosition());
				this.pos.set(this.map.getWorld().getLocalPlayer().getPosition().add(lineToPlayer.normalize().mult(5f)));
			}

			this.nextAcqCheckTime = System.currentTimeMillis() + 500;
		}

		if (this.unitsToAdd != null && this.unitsToAdd.size() > 0) {
			for (BoidUnit bu : this.unitsToAdd) {
				this.addUnit(bu);
			}
			this.unitsToAdd.clear();
		}

		Profiler.stop("BoidGroup.update");
	}

	public Vector2f getPos() {
		return this.pos;
	}

	public LinkedList<BoidUnit> getUnits() {
		return this.units;
	}

	public World getWorld() {
		return map.getWorld();
	}

	public float getRadius() {
		return radius;
	}

	public int getId() {
		return this.grpId;
	}

	public void setPos(Vector2f pos) {
		this.pos = pos;
		this.goalPaths.get(NPCType.MELEE).clear();
		this.goalPaths.get(NPCType.RANGED).clear();
		this.goalPaths.get(NPCType.MAGIC).clear();
	}

	public Vector2f getAlignPoint(NPCType alignType) {
		Vector2f alignPoint = null;

		switch (alignType) {
			case MELEE :
				alignPoint = this.getPos();
				break;
			case RANGED :
				alignPoint = this.getPos().subtract(this.getFacingVec().mult(this.getRadius() / 6f));
				break;
			case MAGIC :
				alignPoint = this.getPos().subtract(this.getFacingVec().mult(this.getRadius() / 6f).mult(2f));
				break;
		}

		return alignPoint;
	}

	public float getRadiusOfAlign(NPCType alignType) {
		switch (alignType) {
			case MELEE :
				return this.radius * Math.min((float) this.numMelee / (float) this.units.size() * 1.5f, 1f);
			case RANGED :
				return this.radius * Math.min((float) this.numRanged / (float) this.units.size() * 1.5f, 1f);
			case MAGIC :
				return this.radius * Math.min((float) this.numSupport / (float) this.units.size() * 1.5f, 1f);
		}
		return 0;
	}

	public void addGoalPath(Point from, LinkedList<ANode> path, NPCType alignType) {
		this.goalPaths.get(alignType).put(from, (LinkedList<ANode>) path.clone());
	}

	/**
	 * If a unit in the group found a path from a point closer to the given
	 * point than the given point is to the center of this group, then this
	 * method will return the this path. Otherwise null.
	 * 
	 * @param from
	 * @return A key to a path or null
	 */
	public Point findGoalPath(Point from, NPCType alignType) {
		float distToGC = (float) from.distance(World.worldToPathing(this.pos));
		float shortestDist = 0x0FFFFFFF;
		Point shortestKey = null;

		for (Point key : this.goalPaths.get(alignType).keySet()) {
			float dist = (float) key.distance(from);
			float otherDistToGC = (float) key.distance(World.worldToPathing(this.pos));
			if (dist < shortestDist && otherDistToGC < distToGC) {
				shortestDist = dist;
				shortestKey = key;
			}
		}

		if (shortestKey != null && shortestDist < distToGC) {
			return shortestKey;
		} else {
			return null;
		}
	}

	public Vector2f getFacingVec() {
		Vector2f res = new Vector2f(this.map.getWorld().getLocalPlayer().getPosition());
		res.subtractLocal(this.pos);
		return res.normalize();
	}

	public LinkedList<ANode> getGoalPath(Point key, NPCType alignType) {
		return this.goalPaths.get(alignType).get(key);
	}

	public void removeGoalPath(Point key, NPCType alignType) {
		this.goalPaths.get(alignType).remove(key);
	}

	public AbstractRoom getRoom() {
		return this.map.getRoom(World.worldToTile(this.getPos().x), World.worldToTile(this.getPos().y));
	}

	public void consumeGroup(BoidGroup other) {
		System.out.println("Boid Group consuming other group.");
		for (BoidUnit bu : other.getUnits()) {
			this.unitsToAdd.add(bu);
		}
	}

}
