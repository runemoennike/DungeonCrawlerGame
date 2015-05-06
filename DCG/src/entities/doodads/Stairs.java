/**
 * Stairs extends doodad, contains the stair graphics and overrides interactReact.
 */

package entities.doodads;

import infostore.DataNode;

import java.awt.Point;

import map.AbstractRoom;
import map.Corridor;
import map.Map;

import com.jme.math.Vector2f;

import engine.Game;
import engine.World;
import engine.Game.Elevation;
import entities.actors.Actor;

public class Stairs extends Doodad {

	private static final long serialVersionUID = 8504040389352724565L;

	public enum StairDirection {
		UP, DOWN
	}

	private StairDirection direction;

	public Stairs(Map map, DataNode type, StairDirection direction) {
		super(map, type);
		this.direction = direction;
	}

	@Override
	public void interactReact(Actor interactor) {
		switch (this.direction) {
			case DOWN :
				Game.getInstance().queueElevatePlayer(Elevation.DOWN);
				break;
			case UP :
				Game.getInstance().queueElevatePlayer(Elevation.UP);
				break;
		}
	}

	@Override
	public void placeInRoom(AbstractRoom r) {
		Point pos = new Point(r.getCenter());

		switch (this.direction) {
			case DOWN :
				pos.y++;
				if (this.map.getRoom(pos.x, pos.y + 1) != null
						&& Corridor.class.equals(this.map.getRoom(pos.x, pos.y + 1).getClass())) {
					pos.x++;
				}
				r.getMap().setExitPos(pos);
				break;
			case UP :
				pos.y++;
				if (this.map.getRoom(pos.x, pos.y + 1) != null
						&& Corridor.class.equals(this.map.getRoom(pos.x, pos.y + 1).getClass())) {
					pos.x++;
				}
				r.getMap().setEntrancePos(pos);
				break;
		}

		this.setPos(new Vector2f(World.tileToWorld(pos.x), World.tileToWorld(pos.y)));
	}

}
