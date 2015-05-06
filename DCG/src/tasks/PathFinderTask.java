/**
 * The pathfinder task uses the A* search algorithm to find a path between two position
 * for a given entity. When a path is found the simplify path method is used to remove 
 * redundant points in the path.
 */

package tasks;

import java.util.LinkedList;

import map.Map;

import com.jme.math.Vector2f;

import engine.World;
import entities.Entity;
import entities.Entity.EntitySubtype;
import entities.actors.Actor;

public class PathFinderTask extends AbstractTask {
	private static final int ASTAR_MAXITERS = 1000;

	private Map map;
	private Vector2f origin;
	private Vector2f dest;
	private Vector2f originalDest;
	private LinkedList<AStarNode> openList;
	private LinkedList<AStarNode> closedList;
	private AStarNode curNode;
	private AStarNode destNode;
	private AStarNode startNode;
	private boolean found;
	private int totalIters;
	private LinkedList<Vector2f> path;
	private int panicStep = 0;
	private Entity ent;

	public PathFinderTask(Map map, Vector2f origin, Vector2f dest, Entity ent) {
		super();
		this.map = map;
		this.dest = dest;
		this.originalDest = dest;
		this.origin = origin;
		this.ent = ent;
	}

	@Override
	public Object getResult() {
		return this.path;
	}

	@Override
	boolean init() {
		if (this.map == null || this.dest == null || this.origin == null) {
			return false;
		}

		this.openList = new LinkedList<AStarNode>();
		this.closedList = new LinkedList<AStarNode>();

		this.destNode = new AStarNode();
		this.destNode.px = World.worldToPathing(this.dest.x);
		this.destNode.py = World.worldToPathing(this.dest.y);

		this.startNode = new AStarNode();
		this.startNode.px = World.worldToPathing(this.origin.x);
		this.startNode.py = World.worldToPathing(this.origin.y);
		this.openList.add(this.startNode);

		this.found = false;
		this.totalIters = 0;

		return true;
	}

	@Override
	int run(int units) {
		if (totalIters == 0) {
			// System.out.println("new path");
		}
		int unitsUsed = 0;

		this.map.clearShowPathing((Actor) this.ent);
		while (!this.found) {
			unitsUsed++;
			if (unitsUsed >= units) {
				return unitsUsed;
			}

			this.totalIters++;
			if (this.totalIters > ASTAR_MAXITERS) {
				break;
			}

			/*
			 * TODO crash from line above: java.util.NoSuchElementException at
			 * java.util.LinkedList.getFirst(Unknown Source) at
			 * tasks.PathFinderTask.run(PathFinderTask.java:89)
			 */
			if (!this.openList.isEmpty()) {
				curNode = this.openList.getFirst();

				for (AStarNode node : openList) {
					if (node.F() <= curNode.F()) {
						curNode = node;
					}
				}
				this.map.markExploredPathing((Actor) this.ent, curNode.px, curNode.py);
				if (this.curNode.equals(this.destNode)) {
					this.destNode = this.curNode;
					this.found = true;
					// return unitsUsed;
					break;
				} else {
					this.openList.remove(curNode);
					this.closedList.add(curNode);

					// System.out.println("node added: "+curNode.px+","+curNode.py);

					expand(this.curNode.px + 1, this.curNode.py);
					expand(this.curNode.px - 1, this.curNode.py);
					expand(this.curNode.px, this.curNode.py + 1);
					expand(this.curNode.px, this.curNode.py - 1);
				}

				if (this.openList.size() == 0) {
					break;
				}
			}
		}

		if (found) {
			this.path = new LinkedList<Vector2f>();

			this.curNode = this.destNode;
			while (!this.curNode.equals(this.startNode)) {
				this.path.addFirst(new Vector2f(World.pathingToWorld(this.curNode.px), World
						.pathingToWorld(this.curNode.py)));
				this.curNode = this.curNode.parent;
			}

			if (this.path.size() == 0) {
				this.path = null;
			} else {
				unitsUsed += this.path.size() / 2;
				this.simplifyPath();
			}
			this.isComplete = true;
		} else if (this.ent.isSubtype(EntitySubtype.PLAYER)) {
			this.panicStep++;
			Vector2f v = new Vector2f(this.origin.x - this.originalDest.x, this.origin.y - this.originalDest.y);
			Vector2f stepV = v.clone().divideLocal(v.length() / World.PATHING_BLOCKSIZE);
			Vector2f p = null;

			boolean foundAlternative = false;

			if (this.panicStep == 1) {
				// Optimistic, go from spot towards actor
				p = new Vector2f(this.originalDest.x, this.originalDest.y);

				for (int t = 0; t < v.length(); t += World.PATHING_BLOCKSIZE) {
					p.addLocal(stepV);
					if (this.ent.walkablePathSpot(p.x, p.y)) {
						// System.out.println("Trying furthest valid spot.");
						foundAlternative = true;
						break;
					}
				}
			} else if (this.panicStep == 2) {
				// Pessimistic, go from actor towards spot

				p = new Vector2f(this.origin.x, this.origin.y);

				boolean hasValid = false;
				for (int t = 0; t < v.length(); t += World.PATHING_BLOCKSIZE) {
					p.subtractLocal(stepV);
					if (this.ent.walkablePathSpot(p.x, p.y)) {
						hasValid = true;
					} else if (hasValid) {
						p.addLocal(stepV);
						foundAlternative = true;
						break;
					}
				}
			} else {
				// Apparently everything failed. Bail.
				System.out.println("Rune failed to find path.");
				this.path = null;
				this.isComplete = true;
			}

			if (foundAlternative) {
				this.dest.x = p.x;
				this.dest.y = p.y;
				this.init();
			}

		} else {
			this.path = null;
			this.isComplete = true;
		}
		// System.out.println("units used: " + unitsUsed);
		return unitsUsed;
	}

	private void expand(int i, int j) {
		AStarNode neighbour = new AStarNode();
		neighbour.px = i;
		neighbour.py = j;
		if (!this.closedList.contains(neighbour)) {
			if (this.ent.walkablePathSpot(neighbour.px, neighbour.py)) {
				if (this.openList.contains(neighbour)) {
					neighbour = this.openList.get(this.openList.lastIndexOf(neighbour));
					if (this.curNode.G + 1 < neighbour.G) {
						neighbour.G = this.curNode.G + 1;
						neighbour.parent = this.curNode;
					}
				} else {
					neighbour.G = this.curNode.G + 1;
					int h1 = Math.abs(neighbour.px - this.destNode.px);
					int h2 = Math.abs(neighbour.py - this.destNode.py);
					neighbour.H = h1 + h2;
					neighbour.parent = this.curNode;
					this.openList.add(neighbour);
				}
			}
		}
	}

	private class AStarNode {
		public int G, H;
		public int px, py;
		public AStarNode parent;
		private int fHashCode;

		public AStarNode() {
			G = 0;
			H = 0;
			px = 0;
			py = 0;
		}

		public int F() {
			return this.G + this.H;
		}

		@Override
		public boolean equals(Object other) {
			return (this.px == ((AStarNode) other).px && this.py == ((AStarNode) other).py);
		}

		@Override
		public int hashCode() {
			if (fHashCode == 0) {
				fHashCode = px << 16 + py;
			}
			return fHashCode;
		}

		@Override
		public String toString() {
			return "Node at (" + px + ", " + py + ") G=" + G + " H=" + H + " Parent=" + (parent == null ? "NO" : "yes");
		}

	}

	private void simplifyPath() {
		LinkedList<Vector2f> newList = new LinkedList<Vector2f>();

		Vector2f prev = this.origin;
		newList.addLast(prev);

		for (Vector2f cur : this.path) {
			if (!this.map.lineOfWalk(newList.getLast(), cur, this.ent)) {
				newList.addLast(prev);
			}
			prev = cur;
		}

		newList.addLast(this.path.getLast());

		newList.removeFirst();

		this.path = newList;
	}
}
