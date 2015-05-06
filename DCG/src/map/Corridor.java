/**
 * This class extends AbstractRoom and supplies methods for connecting two Rooms,
 * thus creating a Corridor.
 */

package map;

import java.awt.Point;
import java.util.ArrayList;

import com.jme.math.Vector2f;

import engine.World;
import entities.Entity;

public class Corridor extends AbstractRoom {

	AbstractRoom r1;
	AbstractRoom r2;
	ArrayList<Point> points = new ArrayList<Point>();
	CorridorStyle style;

	enum CorridorStyle {
		X_FIRST, Y_FIRST;
	}

	// Corridor(World world, Map map) {
	// super(world, map);
	// }

	// Corridor(World world, Map map, int width) {
	// super(world, map);
	// this.width = width;
	// }

	Corridor(Map map, AbstractRoom r1, AbstractRoom r2) {
		super(map);

		if (r1.isNeighbourByPosition(r2)) {
			r1.addNeighbour(r2);
			return;
		}

		// this.addNeighbour(r1);
		// this.addNeighbour(r2);
		this.r1 = r1;
		this.r2 = r2;
	}

	@Override
	int generate() {
		if (this.r1 == null || this.r2 == null) {
			return 0;
		}

		if (this.world.roll100(50)) {
			this.style = CorridorStyle.X_FIRST;
		} else {
			this.style = CorridorStyle.Y_FIRST;
		}

		Point curPos = this.r1.getCenter();
		Point nextPos = this.nextMove(curPos);

		boolean stop = false;

		while (!stop) {
			curPos = nextPos;
			nextPos = this.nextMove(curPos);

			boolean add = true;
			AbstractRoom room = this.map.getRoom(curPos);
			if (room != null) {
				add = false;
				stop = room.equals(this.r2);
			}
			if (add) {
				this.points.add(curPos);
				this.map.setRoom(curPos, this);
			}
		}

		if (points.isEmpty()) {
			return 0;
		}

		findAndSetNeighbours();

		// System.out.println("Corridor made: " + this + "Connecting: " + r1 +
		// ", " + r2 + "     points: " + points);
		// System.out.println(this + " has neighbours: " + getNeighbours());

		return this.points.size();
	}

	@Override
	public boolean contains(Point p) {
		return this.points.contains(p);
	}

	@Override
	boolean isNeighbourByPosition(AbstractRoom r) {
		for (Point p : this.points) {
			if (r.equals(this.map.getRoom(p.x, p.y))) {
				return true;
			}
			if (r.equals(this.map.getRoom(p.x + 1, p.y))) {
				return true;
			}
			if (r.equals(this.map.getRoom(p.x, p.y + 1))) {
				return true;
			}
			if (r.equals(this.map.getRoom(p.x - 1, p.y))) {
				return true;
			}
			if (r.equals(this.map.getRoom(p.x, p.y - 1))) {
				return true;
			}
		}
		return false;
	}

	private Point nextMove(Point curPos) {
		if (this.style == CorridorStyle.X_FIRST) {
			if (curPos.x < this.r2.getCenter().x) {
				return new Point(curPos.x + 1, curPos.y);
			} else if (curPos.x > this.r2.getCenter().x) {
				return new Point(curPos.x - 1, curPos.y);
			} else if (curPos.y < this.r2.getCenter().y) {
				return new Point(curPos.x, curPos.y + 1);
			} else if (curPos.y > this.r2.getCenter().y) {
				return new Point(curPos.x, curPos.y - 1);
			}
		} else if (this.style == CorridorStyle.Y_FIRST) {
			if (curPos.y < this.r2.getCenter().y) {
				return new Point(curPos.x, curPos.y + 1);
			} else if (curPos.y > this.r2.getCenter().y) {
				return new Point(curPos.x, curPos.y - 1);
			} else if (curPos.x < this.r2.getCenter().x) {
				return new Point(curPos.x + 1, curPos.y);
			} else if (curPos.x > this.r2.getCenter().x) {
				return new Point(curPos.x - 1, curPos.y);
			}
		}
		// System.out.println("returning center: curPos = " + curPos +
		// "    r2 = " + this.r2 + "  r2center = "
		// + this.r2.getCenter());
		return this.r2.getCenter();
	}

	@Override
	public Point getCenter() {
		return this.points.get(this.points.size() / 2);
	}

	// TODO: not used?
	// @Override
	// void createWallsAndFloor(int level) {
	// for (Point p : this.points) {
	// int x = p.x;
	// int y = p.y;
	// MapTile f = new BasicFloor(x + 0.5f, y + 0.5f, World.BLOCKSIZE,
	// MapTile.levelToTier(level));
	// this.addEntity(f);
	// //TODO: fix node attachment for floor and walls
	//			
	// if (this.map.getRoom(x + 1, y) == null) {
	// MapTile w = new Wall(x + 1 + 0.5f, y + 0.5f, World.BLOCKSIZE,
	// this.map.findOrientation(x + 1, y), MapTile.levelToTier(level));
	// this.addEntity(w);
	// }
	// if (this.map.getRoom(x - 1, y) == null) {
	// MapTile w = new Wall(x - 1 + 0.5f, y + 0.5f, World.BLOCKSIZE,
	// this.map.findOrientation(x - 1, y), MapTile.levelToTier(level));
	// this.addEntity(w);
	// }
	// if (this.map.getRoom(x, y + 1) == null) {
	// MapTile w = new Wall(x + 0.5f, y + 1 + 0.5f, World.BLOCKSIZE,
	// this.map.findOrientation(x, y + 1), MapTile.levelToTier(level));
	// this.addEntity(w);
	// }
	// if (this.map.getRoom(x, y - 1) == null) {
	// MapTile w = new Wall(x + 0.5f, y - 1 + 0.5f, World.BLOCKSIZE,
	// this.map.findOrientation(x, y - 1), MapTile.levelToTier(level));
	// this.addEntity(w);
	// }
	// }
	// }

	@Override
	public Vector2f getFreeSpot(Entity ent) {
		int idx = this.points.size() / 2;
		for (int i = 0; i < this.points.size() / 2; i++) {
			if (idx - i >= 0 && idx + i < this.points.size()) {
				System.out.println(this.points.get(idx - i));
				Point t = this.points.get(idx - i);
				if (ent.walkablePathSpot(World.tileToWorld(t.x), World.tileToWorld(t.y))) {
					return new Vector2f(World.tileToWorld(t.x), World.tileToWorld(t.y));
				}

				t = this.points.get(idx + i);
				if (ent.walkablePathSpot(World.tileToWorld(t.x), World.tileToWorld(t.y))) {
					return new Vector2f(World.tileToWorld(t.x), World.tileToWorld(t.y));
				}
			}
		}
		return null;
	}

	@Override
	public int getSize() {
		return this.points.size();
	}
}
