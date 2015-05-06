/**
 * This class represents the world in the game. It contains an array of all the Map
 * objects, and calls update on the active map.
 * 
 * The class also manages handing out uids and contains methods for getting random numbers 
 * and rollong.
 * 
 * The World class also manages all attacks currently active, by calling updateAttack() on
 * all Attacks in the attack list.
 */

package engine;

import java.awt.Point;
import java.util.LinkedList;
import java.util.Random;

import map.Map;
import map.Sanctuary;
import misc.Attack;
import tasks.TaskScheduler;

import com.jme.math.Vector2f;

import entities.actors.Player;

public class World {

	public static final int BLOCKSIZE = 5;
	public static final int PATHING_GRANULARITY = 10;
	public static float PATHING_BLOCKSIZE = (float) BLOCKSIZE * 2 / PATHING_GRANULARITY;
	public static final int STARTING_MAP_LEVEL = 0;

	public static final int LEVELS_PER_STYLE = 5;
	public static final int LEVELS = 20 + 1;

	private Map[] maps;
	private Map activeMap;

	private int nextUid = 10;
	private Player localPlayer;
	private Random rnd;

	private TaskScheduler taskScheduler;
	private long curFrame = 0;

	private LinkedList<Attack> attacks;
	private LinkedList<Attack> attacksToRemove;
	private Sanctuary sanctuary;

	public World() {
		this.taskScheduler = new TaskScheduler();
		this.rnd = new Random();

		this.maps = new Map[LEVELS];
		// this.maps[0] = new Sanctuary(this);

		this.attacks = new LinkedList<Attack>();
		this.attacksToRemove = new LinkedList<Attack>();
	}

	public void update(float t) {
		Profiler.start("World.update");

		// for (Map m : this.activeMaps) {
		// if (m != null) {
		// m.update(t);
		// }
		// }
		// single player = one active map
		this.activeMap.update(t);

		for (Attack a : this.attacks) {
			if (a.updateAttack(t)) {
				this.attacksToRemove.add(a);
			}
		}
		this.attacks.removeAll(this.attacksToRemove);
		this.attacksToRemove.clear();

		this.taskScheduler.run(500, 50, 10);

		this.curFrame++;

		Profiler.stop("World.update");
	}

	private void prepareMap(int level) {
		if (level == 0) {
			this.sanctuary = new Sanctuary(this);
			this.maps[level] = this.sanctuary;
		} else {
			this.maps[level] = new Map(this, level);
		}
	}

	public Sanctuary getSanctuary() {
		if (this.sanctuary == null) {
			this.sanctuary = new Sanctuary(this);
		}
		return this.sanctuary;
	}

	// public void generateMap(int level) {
	// this.maps[level].generateMap();
	// }

	public void generateStartingMap() {
		this.maps[STARTING_MAP_LEVEL].generateMap();
	}

	public Map getMap(int level) {
		if (level < LEVELS) {
			if (this.maps[level] == null) {
				prepareMap(level);
			}
			this.activeMap = this.maps[level];
			return this.maps[level];
		} else {
			return null;
		}
	}

	public Map getLowerMap(int currentLevel) {
		if (this.maps[currentLevel + 1] == null) {
			prepareMap(currentLevel + 1);
		}
		this.activeMap = this.maps[currentLevel + 1];
		return this.maps[currentLevel + 1];
	}

	public Map getUpperMap(int currentLevel) {
		if (this.maps[currentLevel - 1] == null) {
			prepareMap(currentLevel - 1);
		}
		this.activeMap = this.maps[currentLevel - 1];
		return this.maps[currentLevel - 1];
	}

	public Map getStartingMap() {
		if (this.maps[STARTING_MAP_LEVEL] == null) {
			prepareMap(STARTING_MAP_LEVEL);
		}
		this.activeMap = this.maps[STARTING_MAP_LEVEL];
		return this.maps[STARTING_MAP_LEVEL];
	}

	public int getNextUid() {
		this.nextUid++;
		return this.nextUid;
	}

	public void setLocalPlayer(Player localPlayer) {
		this.localPlayer = localPlayer;
	}

	public Player getLocalPlayer() {
		return localPlayer;
	}

	public int getRndInt(int min, int max) {
		return (this.rnd.nextInt(max - min + 1)) + min;
	}

	public boolean roll100(int chance) {
		return (this.rnd.nextInt(100) < chance);
	}

	public Map getLocalPlayerMap() {
		return this.maps[this.localPlayer.getMap().getLevel()];
	}

	public float getRndFloat(float min, float max) {
		return (this.rnd.nextFloat() * (max - min)) + min;
	}

	public TaskScheduler getTaskScheduler() {
		return taskScheduler;
	}

	public long getCurFrame() {
		return curFrame;
	}

	public static int worldToPathing(float x) {
		return (int) (x);
	}

	public static float pathingToWorld(int x) {
		return (float) (x) + 0.5f;
	}

	public static Point worldToPathing(Vector2f pos) {
		return new Point(World.worldToPathing(pos.x), World.worldToPathing(pos.y));
	}

	public static Vector2f pathingToWorld(Point p) {
		return new Vector2f(World.pathingToWorld(p.x), World.pathingToWorld(p.y));
	}

	public static int worldToTile(float x) {
		return (worldToPathing(x)) / PATHING_GRANULARITY;
	}

	public static float tileToWorld(int t) {
		return pathingToWorld(t * PATHING_GRANULARITY) + (float) BLOCKSIZE;
	}

	public static Vector2f tileToWorld(Point t) {
		return new Vector2f(tileToWorld(t.x), tileToWorld(t.y));
	}

	public void addAttack(Attack attack) {
		this.attacks.add(attack);
	}
}
