/**
 * The message payload returned from the AI thread when a pathfinding has
 * completed.
 */
package threadMessaging;

public class MTMsgPathMap {
	private int[][] map;
	private int level;

	public MTMsgPathMap(int level, int[][] map) {
		this.map = (int[][]) map.clone();
		this.level = level;
	}

	public int getLevel() {
		return level;
	}

	public int[][] getMap() {
		return map;
	}

}
