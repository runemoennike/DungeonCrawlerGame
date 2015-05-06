/**
 * Provides a static method to pathfind using A* between two
 * points in the world, and should be supplied with a pathing map,
 * the pathing footprint of the pathfinding entity, the target
 * entity, and a reference to the invoking Ai cell.
 */
package ai;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;

import threadMessaging.MTMessage;
import threadMessaging.MTMessage.MessageType;

import map.Map;

import com.jme.math.Vector2f;

import engine.Game;
import engine.World;
import entities.Entity;

public class PathFinder {
	private static final int ASTAR_MAXITERS = 1000;

	public static LinkedList<Vector2f> find(int[][] pathingMap, Vector2f origin, Vector2f dest, int entId,
			boolean[][] footprint, int targetId, AIWorkerCell cell) {

		LinkedList<Vector2f> path;

		LinkedList<AStarNode> openList = new LinkedList<AStarNode>();
		LinkedList<AStarNode> closedList = new LinkedList<AStarNode>();

		AStarNode destNode = new AStarNode();
		destNode.px = World.worldToPathing(dest.x);
		destNode.py = World.worldToPathing(dest.y);

		AStarNode startNode = new AStarNode();
		startNode.px = World.worldToPathing(origin.x);
		startNode.py = World.worldToPathing(origin.y);
		openList.add(startNode);

		boolean found = false;
		int totalIters = 0;

		AStarNode curNode;

		// for(int py = 0; py < pathingMap[0].length; py ++) {
		// for(int px = 0; px < pathingMap.length; px ++) {
		// System.out.print(Map.getPathingValue(px, py, pathingMap));
		// }
		// System.out.println();
		// }

		ArrayList<Point> visitedPathPoints = new ArrayList<Point>();

		while (!found) {

			totalIters++;
			if (totalIters > ASTAR_MAXITERS) {
				break;
			}

			if (!openList.isEmpty()) {
				curNode = openList.getFirst();

				for (AStarNode node : openList) {
					if (node.F() <= curNode.F()) {
						curNode = node;
					}
				}

				if (Game.markPathing) {
					visitedPathPoints.add(new Point(curNode.px, curNode.py));
				}

				if (curNode.equals(destNode)) {
					destNode = curNode;
					found = true;
					break;
				} else {
					openList.remove(curNode);
					closedList.add(curNode);
					// System.out.println("node added: "+curNode.px+","+curNode.py);

					expand(openList, closedList, curNode, destNode, curNode.px + 1, curNode.py, entId, pathingMap,
							footprint, targetId);
					expand(openList, closedList, curNode, destNode, curNode.px - 1, curNode.py, entId, pathingMap,
							footprint, targetId);
					expand(openList, closedList, curNode, destNode, curNode.px, curNode.py + 1, entId, pathingMap,
							footprint, targetId);
					expand(openList, closedList, curNode, destNode, curNode.px, curNode.py - 1, entId, pathingMap,
							footprint, targetId);
				}

				if (openList.size() == 0) {
					break;
				}
			}
		}

		if (found) {
			path = new LinkedList<Vector2f>();

			curNode = destNode;
			while (!curNode.equals(startNode)) {
				path.addFirst(new Vector2f(World.pathingToWorld(curNode.px), World.pathingToWorld(curNode.py)));
				curNode = curNode.parent;
			}

			if (path.size() == 0) {
				path = null;
			} else {
				path = simplifyPath(origin.clone(), path, entId, pathingMap, footprint, targetId);
			}
		} else {
			path = null;
			System.out.println("fail: " + totalIters);
		}
		// System.out.println("interations: " + totalIters);
		// System.out.println("openlist size = " + openList.size());
		// System.out.println("closedlist size = " + closedList.size());

		if (Game.markPathing) {
			cell.sendMessage(new MTMessage(MessageType.SEARCHEDPATH, visitedPathPoints));
		}

		return path;
	}

	private static void expand(LinkedList<AStarNode> openList, LinkedList<AStarNode> closedList, AStarNode curNode,
			AStarNode destNode, int i, int j, int entId, int[][] pathingMap, boolean[][] footprint, int ignoreId) {
		AStarNode neighbour = new AStarNode();
		neighbour.px = i;
		neighbour.py = j;
		if (!closedList.contains(neighbour)) {
			if (Entity.walkablePathSpot(neighbour.px, neighbour.py, footprint, pathingMap, entId, ignoreId)) {
				if (openList.contains(neighbour)) {
					neighbour = openList.get(openList.lastIndexOf(neighbour));
					if (curNode.G + 1 < neighbour.G) {
						neighbour.G = curNode.G + 1;
						neighbour.parent = curNode;
					}
				} else {
					neighbour.G = curNode.G + 1;
					int h1 = Math.abs(neighbour.px - destNode.px);
					int h2 = Math.abs(neighbour.py - destNode.py);
					neighbour.H = h1 + h2;
					neighbour.parent = curNode;
					openList.add(neighbour);
				}
			}
		}
	}

	private static class AStarNode {
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
			return G + H;
		}

		@Override
		public boolean equals(Object other) {
			return (px == ((AStarNode) other).px && py == ((AStarNode) other).py);
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

	private static LinkedList<Vector2f> simplifyPath(Vector2f origin, LinkedList<Vector2f> path, int entId,
			int[][] pathingMap, boolean[][] footprint, int ignoreId) {
		LinkedList<Vector2f> newList = new LinkedList<Vector2f>();

		Vector2f prev = origin;
		newList.addLast(prev);

		for (Vector2f cur : path) {
			if (!Map.lineOfWalk(newList.getLast(), cur, footprint, pathingMap, entId, ignoreId)) {
				newList.addLast(prev);
			}
			prev = cur;
		}

		newList.addLast(path.getLast());

		newList.removeFirst();

		return newList;
	}
}
