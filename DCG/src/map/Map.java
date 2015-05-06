/**
 * The Map class represents an entire dungeon, and contains a lot of info
 * about this and well a graphical nodes attached to the root node. The Map contains all the
 * AbstractRooms in a list, as well and the Maps room map and pathing map.
 * 
 * Everything which are to by shown in the map needs to be attached to one of the maps graphical nodes.
 * The Map also contains some entities, such as BasicFloor and BasicWall, as well as
 * MarkerEnrity general effects.
 * 
 * This class contains a lot of methods for reading and manipulating the pathing map 
 * and much else. Also the update method here calls update on all rooms and entities in the map.
 */

package map;

import infostore.DataManager;
import infostore.DataManager.DataType;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import map.tiles.Floor;
import map.tiles.MapTile;
import map.tiles.Wall;
import tasks.GfxPruneAndGrowTask;
import threadMessaging.MTMessage;
import threadMessaging.MTMsgPathMap;
import threadMessaging.MTMessage.MessageType;
import ai.BoidGroup;

import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.shape.Box;

import engine.Game;
import engine.Profiler;
import engine.World;
import entities.Entity;
import entities.Entity.EntityState;
import entities.Entity.EntitySubtype;
import entities.actors.Actor;
import entities.actors.Monster;
import entities.doodads.Porter;
import entities.doodads.Trader;

public class Map {

	private int[][] pathingMap;
	private Node sceneNode;
	private Node entityNode;
	private Node effectNode;
	private HashMap<Actor, Node> showPathingMap;
	private LinkedList<Entity> mapEntities;
	private boolean generated;
	private long sendPathMapTimer;

	public GfxPruneAndGrowTask pruneAndGrowTask;

	int totalRoomSpace = 1;
	int width = 1;
	int height = 1;

	public static final int ORIENT_N = 1 << 0;
	public static final int ORIENT_S = 1 << 1;
	public static final int ORIENT_E = 1 << 2;
	public static final int ORIENT_W = 1 << 3;

	protected int level;
	protected World world;

	private AbstractRoom[][] roomMap;
	private LinkedList<AbstractRoom> rooms;
	private Room entrance;
	private Room exit;
	private Point entrancePos;
	private Point exitPos;

	Point sectorSize;
	Point minRoomSize;
	private LinkedList<BoidGroup> boidGroups;

	public Map(World w, int level) {
		this.generated = false;
		this.sceneNode = new Node();
		this.entityNode = new Node();
		this.effectNode = new Node();
		this.sceneNode.attachChild(this.entityNode);
		this.sceneNode.attachChild(this.effectNode);

		this.mapEntities = new LinkedList<Entity>();
		// this.entitiesToAdd = new LinkedList<Entity>();
		this.level = level;
		this.world = w;

		this.sectorSize = new Point();
		this.minRoomSize = new Point();

		this.rooms = new LinkedList<AbstractRoom>();
		// this.corridors = new TreeSet<Corridor>();

		setRoomParameters();
		// createLevelStage1();

		this.pruneAndGrowTask = new GfxPruneAndGrowTask(this, this.world.getLocalPlayer());
		this.world.getTaskScheduler().addTask(this.pruneAndGrowTask, true);

		this.boidGroups = new LinkedList<BoidGroup>();
	}

	public void generateMap() {
		if (this.level == 0) {
			Room r = new Room(this, new Point(0, 0), new Point(1, 1), new Point(4, 4));
			this.rooms.add(r);
			this.width = 7;
			this.height = 7;
			this.entrance = r;
			r.entrance = true;
			this.exit = r;
			r.exit = true;
			this.roomMap = new AbstractRoom[this.getWidth()][this.getHeight()];
			r.placeOnMap();
			this.pathingMap = new int[this.getWidth() * World.PATHING_GRANULARITY][this.getHeight()
					* World.PATHING_GRANULARITY];

			Trader trader = new Trader(this);
			trader.placeInRoom(r);
			r.addEntity(trader);

			Porter porter = new Porter(this);
			porter.placeInRoom(r);
			r.addEntity(porter);

			this.exit.placeStairsDown();

		} else if (this.level == 20) {
			Room r = new Room(this, new Point(0, 0), new Point(1, 1), new Point(10, 10));
			this.rooms.add(r);
			this.width = 20;
			this.height = 20;
			this.entrance = r;
			r.entrance = true;
			this.exit = r;
			r.exit = true;
			this.roomMap = new AbstractRoom[this.getWidth()][this.getHeight()];
			r.placeOnMap();
			this.pathingMap = new int[this.getWidth() * World.PATHING_GRANULARITY][this.getHeight()
					* World.PATHING_GRANULARITY];

			BoidGroup boidg = new BoidGroup(World.tileToWorld(r.getCenter()), this, 100f);
			this.addBoidGroup(boidg);

			Monster boss = r.spawnMonster(DataManager.findByNameAndType(DataType.MONSTER, "monster_boss"), boidg);
			boss.setPos(World.tileToWorld(r.getCenter().x + 3), World.tileToWorld(r.getCenter().y + 3));

			for (int i = 0; i < 10; i++) {
				r.spawnMonster(DataManager.findByNameAndType(DataType.MONSTER, "monster_melee"), boidg);
				r.spawnMonster(DataManager.findByNameAndType(DataType.MONSTER, "monster_caster"), boidg);
			}

			this.entrance.placeStairsUp();

		} else {
			createRandomLevel();

			this.pathingMap = new int[this.getWidth() * World.PATHING_GRANULARITY][this.getHeight()
					* World.PATHING_GRANULARITY];

			this.rebuildPathingMap();

			this.exit.placeStairsDown();
			this.entrance.placeStairsUp();

			Porter porter = new Porter(this);
			porter.placeInMap(this);

			System.out.println("porter guy is at " + porter.getPosition());

			for (AbstractRoom room : this.entrance.getNeighboursAndTheirNeighbours()) {
				room.generateDoodads();
				room.generateMonsters();
			}
		}

		this.rebuildPathingMap();

		this.createWallsAndFloor();

		this.generated = true;
	}

	private void createRandomLevel() {
		int totalSize = 0;
		int count = 0;
		int over9000counter = 0;
		Point sector = new Point(0, 0);
		while (totalSize < this.totalRoomSpace) {
			if (this.world.roll100(50)) {
				Room r = new Room(this, new Point(sector.x, sector.y));
				totalSize += r.generate();
				this.rooms.add(r);
			}
			if (count == sector.x) {
				count++;
				sector.x = 0;
				sector.y = count;
			} else {
				sector.x++;
				sector.y--;
			}
			if (over9000counter++ > 9000) {
				System.out.println("Problem places rooms. While loop run over 9000 times.");
				// TODO: Throw over9000exception
			}
			this.width = this.width < (sector.x + 1) * sectorSize.x ? (sector.x + 1) * sectorSize.x : this.width;
			this.height = this.height < (sector.y + 1) * sectorSize.y ? (sector.y + 1) * sectorSize.y : this.height;
		}
		this.height += 2;
		this.width += 2;
		this.roomMap = new AbstractRoom[this.getWidth()][this.getHeight()];
		for (AbstractRoom room : this.rooms) {
			room.findAndSetNeighbours();
			((Room) room).placeOnMap();
		}
		int roll = this.world.getRndInt(0, this.rooms.size() - 1);
		Room room = (Room) this.rooms.get(roll);
		this.entrance = room;
		room.entrance = true;

		int dist = 0;

		// picking room furthest away from entrance for exit
		for (AbstractRoom r : this.rooms) {
			int nextDist = Math.abs(r.getCenter().x - this.entrance.getCenter().x)
					+ Math.abs(r.getCenter().y - this.entrance.getCenter().y);
			if (nextDist > dist) {
				room = (Room) r;
				dist = nextDist;
			}
		}
		this.exit = room;
		room.exit = true;

		this.connectRooms();
	}

	@SuppressWarnings("unchecked")
	private void connectRooms() {
		for (AbstractRoom r1 : (LinkedList<AbstractRoom>) this.rooms.clone()) {
			LinkedList<AbstractRoom> list = getRoomsCloseBy(r1);
			for (AbstractRoom r2 : list) {
				if (!r1.isNeighbour(r2)) {
					Corridor cor = new Corridor(this, r1, r2);
					int size = cor.generate();
					if (size != 0) {
						this.rooms.add(cor);
					}
				}
			}
		}

		int over9000counter = 0;
		int conndConns = 0xFFFFFF;
		while (conndConns > 1) {
			if (over9000counter++ > 9000) {
				System.out.println("Problem in connectRooms(). Over 9000 tries.");
				// TODO: Throw over9000exception
			}
			conndConns = this.ensureAllRoomsConnected();
		}

		if (conndConns == -1) {
			System.out.println("Failed while unifying connected components");
		}
	}

	private int ensureAllRoomsConnected() {

		// Count number connected components
		LinkedList<AbstractRoom> closedList = new LinkedList<AbstractRoom>();
		LinkedList<LinkedList<AbstractRoom>> conndComps = new LinkedList<LinkedList<AbstractRoom>>();

		for (AbstractRoom r : this.rooms) {
			if (!closedList.contains(r)) {
				LinkedList<AbstractRoom> reachable = new LinkedList<AbstractRoom>();
				findReachableRooms(reachable, r);
				conndComps.add(reachable);
				closedList.addAll(reachable);
			}
		}

		System.out.println("Connected components: " + conndComps.size());

		if (conndComps.size() > 1) {

			// Find closest pair of connected components
			LinkedList<AbstractRoom> closestPair = null;
			int lowDist = 0xFFFFFF;
			for (LinkedList<AbstractRoom> cc1 : conndComps) {
				for (LinkedList<AbstractRoom> cc2 : conndComps) {
					if (!cc1.equals(cc2)) {
						LinkedList<AbstractRoom> resultPair = new LinkedList<AbstractRoom>();
						int dist = findClosedRoomPair(cc1, cc2, resultPair);
						if (dist < lowDist) {
							lowDist = dist;
							closestPair = resultPair;
						}
					}
				}
			}

			AbstractRoom closest1, closest2;
			closest1 = closestPair.pop();
			closest2 = closestPair.pop();

			if (closest1 != null && closest2 != null) {
				// Connect the two connected components that were closest
				Corridor cor = new Corridor(this, closest1, closest2);
				int size = cor.generate();
				if (size != 0) {
					this.rooms.add(cor);
				}
				return conndComps.size() - 1;
			} else {
				System.out.println("WARNING: Something went wrong when unifying connected components!");
				return -1;
			}
		} else {
			return 1;
		}
	}

	private int findClosedRoomPair(LinkedList<AbstractRoom> cc1, LinkedList<AbstractRoom> cc2,
			LinkedList<AbstractRoom> results) {
		int lowDist = 0xFFFFFF;

		AbstractRoom result1 = null, result2 = null;

		for (AbstractRoom r1 : cc1) {
			for (AbstractRoom r2 : cc2) {
				int dist = Math.abs(r1.getCenter().x - r2.getCenter().x)
						+ Math.abs(r1.getCenter().y - r2.getCenter().y);
				if (dist < lowDist) {
					lowDist = dist;
					result1 = r1;
					result2 = r2;
				}
			}
		}

		results.push(result1);
		results.push(result2);

		// System.out.println(result1 + " " + result2);

		return lowDist;
	}

	private void findReachableRooms(LinkedList<AbstractRoom> list, AbstractRoom r) {
		list.add(r);

		for (AbstractRoom neighbour : r.getNeighbours()) {
			if (!list.contains(neighbour)) {
				findReachableRooms(list, neighbour);
			}
		}
	}

	private LinkedList<AbstractRoom> getRoomsCloseBy(AbstractRoom r) {
		LinkedList<AbstractRoom> closeby = new LinkedList<AbstractRoom>();
		int count = 0;
		int dist = 1;
		int over9000counter = 0;
		int max = this.world.getRndInt(1, this.rooms.size() < 4 ? this.rooms.size() - 1 : 3);
		while (count < max) {
			for (AbstractRoom n : this.rooms) {
				if (dist == Math.abs(n.getCenter().x - r.getCenter().x) + Math.abs(n.getCenter().y - r.getCenter().y)) {
					closeby.add(n);
					count++;
				}
			}
			dist++;
			if (over9000counter++ > 9000) {
				System.out.println("OVER 9000 IN connectRooms() always:" + count + " < " + max);
				// TODO: Throw over9000exception
			}
		}
		return closeby;
	}

	// public void generateTileMap() {
	// this.roomMap = new AbstractRoom[this.width][this.height];
	// int[][] tempTilemap = new int[this.width][this.height];
	// System.out.println("w: " + width + "  h: " + height);
	// for (int i = 0; i < this.width; i++) {
	// for (int j = 0; j < this.height; j++) {
	// tempTilemap[i][j] = 1;
	// }
	// }
	// for (AbstractRoom room : this.rooms) {
	// if (room.getClass().equals(Room.class)) {
	// for (int i = ((Room) room).p0.x; i <= ((Room) room).p1.x; i++) {
	// for (int j = ((Room) room).p0.y; j <= ((Room) room).p1.y; j++) {
	// tempTilemap[i][j] = 0;
	// this.roomMap[i][j] = room;
	// }
	// }
	// } else if (room.getClass().equals(Corridor.class)) {
	// for (Point p : ((Corridor) room).points) {
	// tempTilemap[p.x][p.y] = 0;
	// this.roomMap[p.x][p.y] = room;
	// }
	// }
	// }
	// this.tilemap = tempTilemap;
	// }

	int findOrientation(int x, int y) {
		int orient = 0;
		if (this.getRoom(x, y - 1) != null) {
			orient |= Map.ORIENT_S;
		}
		if (this.getRoom(x, y + 1) != null) {
			orient |= Map.ORIENT_N;
		}
		if (this.getRoom(x - 1, y) != null) {
			orient |= Map.ORIENT_W;
		}
		if (this.getRoom(x + 1, y) != null) {
			orient |= Map.ORIENT_E;
		}
		return orient;
	}

	private void createWallsAndFloor() {
		// this.scene = new Node("Map node");

		// Create tiles
		for (int x = -1; x < this.width; x++) {
			for (int y = -1; y < this.height; y++) {
				MapTile t = null;

				if (this.exitPos == null || this.exitPos.x != x || this.exitPos.y != y) {
					if (this.getRoom(x, y) == null) {
						// t = new BasicWall(x, y, World.BLOCKSIZE);
						t = new Wall(x + 0.5f, y + 0.5f, World.BLOCKSIZE, findOrientation(x, y), MapTile
								.levelToTier(this.level));
					} else {
						t = new Floor(x + 0.5f, y + 0.5f, World.BLOCKSIZE, MapTile.levelToTier(this.level));
					}

					this.addEntity(t);
					t.lock();
				}
			}
		}
	}

	public Node getSceneNode() {
		return this.sceneNode;
	}

	public AbstractRoom getRoom(int x, int y) {
		if (x >= 0 && y >= 0 && x < this.getWidth() && y < this.getHeight()) {
			return this.roomMap[x][y];
		} else {
			return null;
		}
	}

	public AbstractRoom getRoom(Point p) {
		return this.getRoom(p.x, p.y);
	}

	void setRoom(int x, int y, AbstractRoom room) {
		this.roomMap[x][y] = room;
	}

	void setRoom(Point p, AbstractRoom room) {
		this.setRoom(p.x, p.y, room);
	}

	public int getHeight() {
		return this.height;
	}

	public int getWidth() {
		return this.width;
	}

	public int getSize() {
		return this.width * this.height;
	}

	public Room getEntrance() {
		return this.entrance;
	}

	public Room getExit() {
		return this.exit;
	}

	public World getWorld() {
		return this.world;
	}

	private void setRoomParameters() {
		if (true) {
			// this.sectorSize.x = 5;
			// this.sectorSize.y = 5;
			// this.minRoomSize.x = 3;
			// this.minRoomSize.y = 3;
			// // this.maxCorridorWidth = 1;
			// this.totalRoomSpace = 100;
			// // TODO check error: index out of bounds for large space
			// return;
		}
		if (World.LEVELS_PER_STYLE >= this.level) {
			this.sectorSize.x = 7;
			this.sectorSize.y = 7;
			this.minRoomSize.x = 4;
			this.minRoomSize.y = 4;
			// this.maxCorridorWidth = 2;
			this.totalRoomSpace = 150;
		} else if (World.LEVELS_PER_STYLE * 2 >= this.level) {
			this.sectorSize.x = 5;
			this.sectorSize.y = 5;
			this.minRoomSize.x = 3;
			this.minRoomSize.y = 3;
			// this.maxCorridorWidth = 1;
			this.totalRoomSpace = 125;
		} else if (World.LEVELS_PER_STYLE * 3 >= this.level) {
			this.sectorSize.x = 9;
			this.sectorSize.y = 9;
			this.minRoomSize.x = 4;
			this.minRoomSize.y = 4;
			// this.maxCorridorWidth = 2;
			this.totalRoomSpace = 200;
		} else {
			this.sectorSize.x = 10;
			this.sectorSize.y = 10;
			this.minRoomSize.x = 6;
			this.minRoomSize.y = 6;
			// this.maxCorridorWidth = 2;
			this.totalRoomSpace = 300;
		}
	}

	public LinkedList<Entity> getAllRoomEntities() {
		LinkedList<Entity> all = new LinkedList<Entity>();
		for (AbstractRoom r : this.rooms) {
			all.addAll(r.getEntities());
			all.addAll(r.getActors());
		}
		return all;
	}

	public LinkedList<Entity> getAllRoomActors() {
		LinkedList<Entity> all = new LinkedList<Entity>();
		for (AbstractRoom r : this.rooms) {
			all.addAll(r.getActors());
		}
		return all;
	}

	public void update(float t) {
		Profiler.start("Map.update");
		for (Entity e : this.mapEntities) {
			e.update(t);
			if (e.getCurState().equals(EntityState.PURGED)) {
				this.removeEntity(e);
			}
		}

		for (AbstractRoom room : this.rooms) {
			room.update(t);
		}

		ArrayList<BoidGroup> boidGroupRemove = new ArrayList<BoidGroup>();
		for (BoidGroup boidg : this.boidGroups) {
			if (!boidGroupRemove.contains(boidg)) {
				boidg.update(t);

				for (BoidGroup boidg2 : this.boidGroups) {
					if (boidg.getPos().distance(boidg2.getPos()) < boidg.getRadius() + boidg2.getRadius()
							&& !boidg.equals(boidg2)) {
						boidGroupRemove.add(boidg2);
						boidg.consumeGroup(boidg2);
					}
				}
			}
		}
		this.boidGroups.removeAll(boidGroupRemove);

		if (System.currentTimeMillis() > this.sendPathMapTimer + 500) {
			this.sendPathMapTimer = System.currentTimeMillis();

			Game.getInstance().getAIBrainMsgq().addB(
					new MTMessage(MessageType.PATHMAP, new MTMsgPathMap(this.level, this.pathingMap)));
		}

		Profiler.stop("Map.update");
	}

	public void addEntity(Entity e) {
		this.attachEntity(e);
		this.mapEntities.add(e);
		e.setMap(this);
	}

	public void removeEntity(Entity e) {
		this.attachEntity(e);
		this.mapEntities.remove(e);
		e.setMap(null);
	}

	void attachEntity(Entity e) {
		this.entityNode.attachChild(e.getNode());
		e.getNode().updateRenderState();
	}

	void detachEntity(Entity e) {
		this.entityNode.detachChild(e.getNode());
	}

	public Node getEntityNode() {
		return this.entityNode;
	}

	public Node getEffectNode() {
		return this.effectNode;
	}

	public int[][] getPathingMap() {
		return pathingMap;
	}

	public void setPathingMapAt(int x, int y, int val) {
		if (x >= 0 && x < this.getWidth() * World.PATHING_GRANULARITY && y >= 0
				&& y < this.getHeight() * World.PATHING_GRANULARITY) {
			this.pathingMap[x][y] = val;
		}
	}

	public int getPathingValue(float worldx, float worldy) {
		return getPathingValue(World.worldToPathing(worldx), World.worldToPathing(worldy));
	}

	public int getPathingValue(int x, int y) {
		return getPathingValue(x, y, this.pathingMap);
	}

	public static int getPathingValue(int x, int y, int[][] pmap) {
		if (pmap != null && x >= 0 && x < pmap.length && y >= 0 && y < pmap[0].length) {
			return pmap[x][y];
		} else {
			return -1;
		}
	}

	public void rebuildPathingMap() {
		for (int x = -1; x < this.getWidth() * World.PATHING_GRANULARITY; x++) {
			for (int y = -1; y < this.getHeight() * World.PATHING_GRANULARITY; y++) {
				this.setPathingMapAt(x, y, 0);
			}
		}

		for (int x = -1; x < this.getWidth(); x++) {
			for (int y = -1; y < this.getHeight(); y++) {
				int px = x * World.PATHING_GRANULARITY;
				int py = y * World.PATHING_GRANULARITY;

				if (this.getRoom(x, y) == null) {
					for (int i = px - 1; i < px + World.PATHING_GRANULARITY + 1; i++) {
						for (int j = py - 1; j < py + World.PATHING_GRANULARITY + 1; j++) {
							this.setPathingMapAt(i, j, -1);
						}
					}
				}
			}
		}

		for (Entity e : this.mapEntities) {
			e.rebuildPathingFootprint();
		}

		for (Entity e : this.getAllRoomEntities()) {
			e.rebuildPathingFootprint();
		}
	}

	public boolean lineOfWalk(Vector2f a, Vector2f b, Entity ent) {
		return lineOfWalk(a, b, ent, 0);
	}

	public boolean lineOfWalk(Vector2f a, Vector2f b, Entity ent, int ignoreId) {
		return lineOfWalk(a, b, ent.getFootprint(), this.pathingMap, ent.getId(), ignoreId);
	}

	public static boolean lineOfWalk(Vector2f a, Vector2f b, boolean[][] footprint, int[][] pmap, int entId,
			int ignoreId) {
		Vector2f stepV = new Vector2f(a.x - b.x, a.y - b.y);
		float l = stepV.length();
		stepV.normalizeLocal().divideLocal(20f);
		Vector2f p = new Vector2f(b.x, b.y);

		for (float t = 0; t < l; t += stepV.length()) {
			p.addLocal(stepV);
			if (!Entity.walkablePathSpot(World.worldToPathing(p.x), World.worldToPathing(p.y), footprint, pmap, entId,
					ignoreId)) {
				return false;
			}
		}
		return true;
	}

	public boolean lineOfSight(Vector2f a, Vector2f b) {
		Vector2f v = new Vector2f(a.x - b.x, a.y - b.y);
		Vector2f stepV = v.clone().divideLocal(v.length() / World.PATHING_BLOCKSIZE);
		Vector2f p = new Vector2f(b.x, b.y);

		for (float t = 0; t < v.length(); t += stepV.length()) {
			p.addLocal(stepV);
			if (getPathingValue(p.x, p.y) < 0) {
				return false;
			}
		}
		return true;
	}

	public boolean lineOfSight(Entity a, Entity b) {
		Vector2f v = new Vector2f(a.getPosition().x - b.getPosition().x, a.getPosition().y - b.getPosition().y);
		Vector2f stepV = v.clone().divideLocal(v.length() / World.PATHING_BLOCKSIZE);
		Vector2f p = new Vector2f(b.getPosition().x, b.getPosition().y);

		for (float t = 0; t < v.length(); t += stepV.length()) {
			p.addLocal(stepV);
			if (getPathingValue(p.x, p.y) < 0 && a.getId() != getPathingValue(p.x, p.y)
					&& b.getId() != getPathingValue(p.x, p.y)) {
				return false;
			}
		}
		return true;
	}

	public boolean lineOfAttack(Vector2f a, Vector2f b) {
		Vector2f v = new Vector2f(a.x - b.x, a.y - b.y);
		Vector2f stepV = v.clone().divideLocal(v.length() / World.PATHING_BLOCKSIZE);
		Vector2f p = new Vector2f(b.x, b.y);

		for (float t = 0; t < v.length(); t += stepV.length()) {
			p.addLocal(stepV);
			if (this.getRoom(World.worldToTile(p.x), World.worldToTile(p.y)) == null) {
				return false;
			}
		}
		return true;
	}

	public Vector2f findValidSpotNear(Vector2f start, Vector2f goal, Entity ent) {
		Vector2f v = new Vector2f(start.x - goal.x, start.y - goal.y);
		Vector2f stepV = v.clone().divideLocal(v.length() / World.PATHING_BLOCKSIZE);
		Vector2f p = new Vector2f(goal.x, goal.y);

		for (int t = 0; t < v.length(); t += World.PATHING_BLOCKSIZE) {
			p.addLocal(stepV);
			if (ent.walkablePathSpot(p.x, p.y)) {
				return new Vector2f(p.x, p.y);
			}
		}

		return null;
	}

	public int getLevel() {
		return this.level;
	}

	public LinkedList<Entity> getMapEntities() {
		return this.mapEntities;
	}

	public LinkedList<AbstractRoom> getRooms() {
		return this.rooms;
	}

	public void killAllMobs() {
		for (AbstractRoom room : this.rooms) {
			for (Entity ent : room.getActors()) {
				if (ent.isSubtype(EntitySubtype.MONSTER)) {
					ent.purge();
				}
			}
		}
		this.boidGroups.clear();
	}

	public boolean isGenerated() {
		return this.generated;
	}

	public void addBoidGroup(BoidGroup boidg) {
		this.boidGroups.add(boidg);
	}

	public void clearShowPathing(Actor a) {
		if (Game.markPathing) {
			if (this.showPathingMap == null) {
				this.showPathingMap = new HashMap<Actor, Node>();
			}
			Node newN = new Node();
			Node oldN = this.showPathingMap.get(a);
			if (oldN != null) {
				oldN.removeFromParent();
				if (oldN.getChildren() != null && oldN.getChildren().size() >= 1000) {
					System.out.println("Running garbage collection.");
					System.gc();
				}
			}
			this.showPathingMap.put(a, newN);
			this.sceneNode.attachChild(newN);
		}
	}

	public void clearAllShowPathing() {
		for (Actor a : this.showPathingMap.keySet()) {
			this.showPathingMap.get(a).removeFromParent();
		}
	}

	public void markExploredPathing(Actor a, int x, int y) {
		if (Game.markPathing) {
			Box b = new Box("pathing box", new Vector3f(x, y, 0), 0.5f, 0.5f, 0.1f);
			//
			// TextureState ts =
			// DisplaySystem.getDisplaySystem().getRenderer()
			// .createTextureState();
			// ts.setEnabled(true);
			// ts.setTexture(TextureManager.loadTexture(Game.class.getClassLoader()
			// .getResource(
			// "dcg/data/textures/"
			// + Settings.get(Fields.TEXTURE_QUALITY).str
			// + "/marker.png"),
			// Texture.MinificationFilter.NearestNeighborLinearMipMap,
			// Texture.MagnificationFilter.Bilinear));

			// b.setRenderState(ts);

			// MaterialState ms =
			// DisplaySystem.getDisplaySystem().getRenderer().createMaterialState();
			// ms.setEmissive(new ColorRGBA(0f, 0.2f, 0.5f, 1));

			// b.setRenderState(ms);
			this.showPathingMap.get(a).attachChild(b);
		}
	}

	public void setEntrancePos(Point entrancePos) {
		this.entrancePos = entrancePos;
	}

	public Point getEntrancePos() {
		return entrancePos;
	}

	public void setExitPos(Point exitPos) {
		this.exitPos = exitPos;
	}

	public Point getExitPos() {
		return exitPos;
	}
}
