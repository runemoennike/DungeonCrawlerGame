/**
 * This class represents a tile in the map
 */

package map.tiles;

import engine.World;
import entities.Entity;

public abstract class MapTile extends Entity {
	public MapTile() {
		super();
	}

	public static int levelToTier(int level) {
		if (level == 0) {
			return 0;
		} else if (level <= World.LEVELS_PER_STYLE) {
			return 1;
		} else if (level <= World.LEVELS_PER_STYLE * 2) {
			return 2;
		} else if (level <= World.LEVELS_PER_STYLE * 3) {
			return 3;
		} else {
			return 4;
		}
	}
}
