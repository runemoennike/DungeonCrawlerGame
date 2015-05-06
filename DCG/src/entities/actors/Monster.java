/**
 * The Monster class extends NPC, but really doesn't add any extra info at the moment. 
 * If non-monster NPC were added, it would however make us able to differentiate.
 * 
 * It contains a method for placing a monster in a room.
 */

package entities.actors;

import infostore.DataNode;

import java.awt.Point;

import map.AbstractRoom;
import map.Corridor;
import map.Map;
import map.Room;
import entities.Entity;

public class Monster extends NPC {

	private static final long serialVersionUID = -711715515227235220L;

	public Monster(Map map, DataNode node) {
		super(map, node);
		this.addSubtype(Entity.EntitySubtype.MONSTER);
		this.name = node.getProp("name");
		this.setCaption(node.getProp("caption"));
	}

	public void placeInRoom(AbstractRoom r) {
		float x = 0, y = 0;

		int c = 0;
		do {
			if (r.getClass().equals(Room.class)) {
				Room room = (Room) r;
				Point rs = room.getPathingCoords()[0];
				Point re = room.getPathingCoords()[1];

				x = this.world.getRndFloat(rs.x, re.x);
				y = this.world.getRndFloat(rs.y, re.y);

			} else if (r.getClass().equals(Corridor.class)) {
				// TODO implement this
				this.purge();
				return;
			}

			if (c++ > 100) {
				this.purge();
				System.out.println("Monster.placeInRoomCluster: Problems placing monster in free spot");
				return;
			}
		} while (!this.walkablePathSpot(x, y));

		this.room = r;
		this.setPos(x, y);

	}
}
