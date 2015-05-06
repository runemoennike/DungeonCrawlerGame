/**
 * Room extends AbstractRoom and supplies methods for creating a room in a given sector.
 */

package map;

import infostore.DataManager;
import infostore.DataManager.DataType;

import java.awt.Point;

import com.jme.math.Vector2f;

import engine.World;
import entities.Entity;
import entities.doodads.Stairs;
import entities.doodads.Stairs.StairDirection;

public class Room extends AbstractRoom {

	Point sector;
	Point p0;
	Point p1;
	boolean entrance;
	boolean exit;
	boolean special;

	Room(Map map, Point sector) {
		super(map);
		this.sector = sector;
		this.p0 = new Point();
		this.p1 = new Point();
	}

	Room(Map map, Point sector, Point p0, Point p1) {
		super(map);
		this.sector = sector;
		this.p0 = p0;
		this.p1 = p1;
	}

	@Override
	int generate() {
		this.p0.x = this.world.getRndInt(this.sector.x * this.map.sectorSize.x, this.map.sectorSize.x
				- this.map.minRoomSize.x + this.sector.x * this.map.sectorSize.x);
		this.p1.x = this.world.getRndInt(this.map.minRoomSize.x + this.p0.x, this.map.sectorSize.x + this.sector.x
				* this.map.sectorSize.x) - 1;
		this.p0.y = this.world.getRndInt(this.sector.y * this.map.sectorSize.y, this.map.sectorSize.y
				- this.map.minRoomSize.y + this.sector.y * this.map.sectorSize.y);
		this.p1.y = this.world.getRndInt(this.map.minRoomSize.y + this.p0.y, this.map.sectorSize.y + this.sector.y
				* this.map.sectorSize.y) - 1;

		// System.out.println("Room created: u" + this + " x0 = " + p0.x +
		// ", x1 = " + p1.x + ", y0 = " + p0.y + ", y1 = "
		// + p1.y);

		// if (p0.x == 0) {
		// p0.x++;
		// p1.x++;
		// }
		// if (p0.y == 0) {
		// p0.y++;
		// p1.y++;
		// }

		return (this.p1.x - this.p0.x) * (this.p1.y - this.p0.y);
	}

	@Override
	public Point getCenter() {
		return new Point((p1.x + p0.x) / 2, (p1.y + p0.y) / 2);
	}

	@Override
	public boolean contains(Point p) {
		return (p.x >= p0.x && p.x <= p1.x && p.y >= p0.y && p.y <= p1.y);
	}

	// public Vector2f[] getWorldCoords() {
	// Vector2f start = new Vector2f(this.p0.x * World.BLOCKSIZE * 2, this.p0.y
	// * World.BLOCKSIZE * 2);
	// Vector2f end = new Vector2f(this.p1.x * World.BLOCKSIZE * 2, this.p1.y *
	// World.BLOCKSIZE * 2);
	//		
	// return new Vector2f[] {start, end};
	// }

	public Point[] getPathingCoords() {
		Point start = new Point(this.p0.x * World.PATHING_GRANULARITY, this.p0.y * World.PATHING_GRANULARITY);
		Point end = new Point(this.p1.x * World.PATHING_GRANULARITY, this.p1.y * World.PATHING_GRANULARITY);

		return new Point[]{start, end};
	}

	@Override
	boolean isNeighbourByPosition(AbstractRoom r) {
		// for (int x = p0.x - 1; x <= p1.x + 1; x++) {
		// for (int y = p0.y - 1; y <= p1.y + 1; y++) {
		// if (r.contains(new Point(x, y))) {
		// return true;
		// }
		// }
		// }
		for (int x = this.p0.x; x <= this.p1.x; x++) {
			if (r.equals(this.map.getRoom(x, this.p0.y - 1)) || r.equals(this.map.getRoom(x, this.p1.y + 1))) {
				return true;
			}
		}
		for (int y = this.p0.y; y <= this.p1.y; y++) {
			if (r.equals(this.map.getRoom(this.p0.x - 1, y)) || r.equals(this.map.getRoom(this.p1.x + 1, y))) {
				return true;
			}
		}

		return false;
	}

	// TODO: not used?
	// @Override
	// void createWallsAndFloor(int level) {
	// for (int x = p0.x - 1; x <= p1.x + 1; x++) {
	// for (int y = p0.y - 1; y <= p1.y + 1; y++) {
	// MapTile t = null;
	//
	// if (this.map.getRoom(x, y) == null) {
	// t = new Wall(x + 0.5f, y + 0.5f, World.BLOCKSIZE,
	// this.map.findOrientation(x, y), MapTile.levelToTier(level));
	// } else {
	// t = new BasicFloor(x + 0.5f, y + 0.5f, World.BLOCKSIZE,
	// MapTile.levelToTier(level));
	// }
	// this.addEntity(t);
	// }
	// }
	// }

	void placeOnMap() {
		for (int x = this.p0.x; x <= this.p1.x; x++) {
			for (int y = this.p0.y; y <= this.p1.y; y++) {
				this.map.setRoom(x, y, this);
			}
		}
	}

	@Override
	public Vector2f getFreeSpot(Entity ent) {
		// TODO: Change to path-spot stepping instead of tile stepping if too
		// much fail
		for (int x = this.p0.x; x <= this.p1.x; x++) {
			for (int y = this.p0.y; y <= this.p1.y; y++) {
				if (ent.walkablePathSpot(World.tileToWorld(x), World.tileToWorld(y))) {
					return new Vector2f(World.tileToWorld(x), World.tileToWorld(y));
				}
			}
		}
		return null;
	}

	public void placeStairsUp() {
		Stairs stairs = new Stairs(this.map, DataManager.findByNameAndType(DataType.DOODAD, "doodad_stairsup"),
				StairDirection.UP);
		stairs.placeInRoom(this);
		this.addEntity(stairs);
	}

	public void placeStairsDown() {
		Stairs stairs = new Stairs(this.map, DataManager.findByNameAndType(DataType.DOODAD, "doodad_stairsdown"),
				StairDirection.DOWN);
		stairs.placeInRoom(this);
		this.addEntity(stairs);
	}

	@Override
	public int getSize() {
		return (this.p1.x - this.p0.x) * (this.p1.y - this.p0.y);
	}
}
