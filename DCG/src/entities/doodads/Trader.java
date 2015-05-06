/**
 * The trader extends doodad and overrides the interactReact method. Opens the vendoring window
 * on interaction.
 */

package entities.doodads;

import infostore.DataManager;
import infostore.DataManager.DataType;

import java.awt.Point;

import map.AbstractRoom;
import map.Map;

import com.jme.math.Vector2f;

import engine.Game;
import engine.World;
import entities.actors.Actor;

public class Trader extends Doodad {

	private static final long serialVersionUID = 422869661944838829L;

	public Trader(Map map) {
		super(map, DataManager.findByNameAndType(DataType.DOODAD, "doodad_trader"));
	}

	@Override
	public void interactReact(Actor interactor) {
		Game.getInstance().getHUD().showTraderWindow();
	}

	@Override
	public void placeInRoom(AbstractRoom r) {
		Point pos = r.getCenter();
		pos.y += 0;
		pos.x += -1;
		this.setPos(new Vector2f(World.tileToWorld(pos.x), World.tileToWorld(pos.y)));
	}

}
