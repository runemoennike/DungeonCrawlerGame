/**
 * The Porter extends the doodad and overrides interactReact, which here opens the teleport window.
 */

package entities.doodads;

import infostore.DataManager;
import infostore.DataManager.DataType;

import java.awt.Point;

import map.AbstractRoom;
import map.Map;
import map.Room;

import com.jme.math.Vector2f;

import engine.Game;
import engine.World;
import entities.actors.Actor;

public class Porter extends Doodad {

	private static final long serialVersionUID = 422869661944838829L;

	private long lastTimeHideCheck;

	public Porter(Map map) {
		super(map, DataManager.findByNameAndType(DataType.DOODAD, "doodad_porter"));
	}

	@Override
	public void interactReact(Actor interactor) {
		Game.getInstance().getHUD().showPorterWindow();
	}

	@Override
	public void update(float t) {
		super.update(t);

		if (System.currentTimeMillis() > lastTimeHideCheck + 1000
				&& this.map.getWorld().getLocalPlayer().getPosition().distance(this.getPosition()) > 10f) {
			Game.getInstance().getHUD().hidePorterWindow();
			lastTimeHideCheck = System.currentTimeMillis();
		}
	}

	@Override
	public void placeInRoom(AbstractRoom r) {
		Point pos = r.getCenter();
		pos.y += +1;
		pos.x += +2;
		this.setPos(new Vector2f(World.tileToWorld(pos.x), World.tileToWorld(pos.y)));
	}

	public void placeInMap(Map map) {
		AbstractRoom r = null;
		int c = 0;
		do {
			c++;
			r = map.getRooms().get(this.world.getRndInt(0, map.getRooms().size() - 1));

			if (c > 100) {
				return;
			}
		} while (r != null && !r.getClass().equals(Room.class));

		r.addEntity(this);
		this.placeInRoomCluster(r, 100);
	}

}
