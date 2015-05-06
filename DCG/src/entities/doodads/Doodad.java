/**
 * The Doodad class extends the Entity class and represents different kinds of non-licing/moving objects.
 * This class fields for size, entities contained in the doodad and whether its destructible.
 * 
 * Methods for dropping content and interacting with actors are also contained.
 * 
 */

package entities.doodads;

import infostore.DataManager;
import infostore.DataNode;
import infostore.DataManager.DataType;

import java.awt.Point;
import java.util.LinkedList;

import map.AbstractRoom;
import map.Corridor;
import map.Map;
import map.Room;

import com.jme.math.FastMath;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;

import engine.World;
import engine.gfx.EffectFactory;
import engine.gfx.EffectFactory.EffectType;
import entities.Entity;
import entities.actors.Actor;
import entities.items.Item;

public class Doodad extends Entity {

	private static final long serialVersionUID = -51929794185660405L;
	private boolean destructible = false;
	private LinkedList<Entity> contains;
	private DataNode info;
	private float size;

	public Doodad(Map map, DataNode node) {
		super(map.getWorld(), DataManager.findByNameAndType(DataType.MODEL_ID, node.getProp("modelID")));
		this.map = map;
		this.contains = new LinkedList<Entity>();
		this.addSubtype(EntitySubtype.DOODAD);
		this.setCaption(node.getProp("caption"));
		this.info = node;

		if (node.getPropB("destructible")) {
			this.destructible = true;
			this.addSubtype(EntitySubtype.DESTRUCTIBLE);
		}

		this.size = 2f + FastMath.sqrt(FastMath.sqr(node.getChild("collision").getPropI("w") / 2f)
				+ FastMath.sqr(node.getChild("collision").getPropI("h") / 2f));

		this.lock();
		this.makePathingFootprint(node.getChild("collision").getPropI("w"), node.getChild("collision").getPropI("h"));
	}

	public void interactReact(Actor interactor) {
		if (this.getCurState() == EntityState.DYING) {
			EffectFactory.spawnEffect(EffectType.DESTRUCTION, this.getNode().getWorldTranslation().addLocal(0, 0, 2),
					0, 1.0f, "barrel_planks02.jpg");
			for (Entity ent : this.contains) {
				this.room.addEntity(ent);
				ent.setPos(this.getPosition().x, this.getPosition().y);
				if (ent.isSubtype(EntitySubtype.ITEM)) {
					((Item) ent).doSpawnFlip();
				}
			}

			this.purge();
		} else {

			float a = this.getNode().getLocalRotation().toAngleAxis(new Vector3f(0, 0, 1f)) - FastMath.HALF_PI;

			for (Entity ent : this.contains) {
				this.room.addEntity(ent);
				ent.setPos(this.getPosition().x + FastMath.cos(a) * 2f, this.getPosition().y + FastMath.sin(a) * 2f);
				if (ent.isSubtype(EntitySubtype.ITEM)) {
					((Item) ent).doSpawnFlip();
				}
			}
		}
		this.contains.clear();
	}

	public void setDestructible(boolean destructible) {
		this.destructible = destructible;
	}

	public boolean isDestructible() {
		return destructible;
	}

	public LinkedList<Entity> getContains() {
		return contains;
	}

	public void addContent(Entity ent) {
		this.contains.add(ent);
	}

	public void addAllContents(LinkedList<Item> linkedList) {
		this.contains.addAll(linkedList);

	}

	public void placeInRoom(AbstractRoom r) {
		if (this.info.getChild("placement").getProp("type").equals("cluster")) {
			placeInRoomCluster(r, Integer.parseInt(this.info.getChild("placement").getProp("typeParameters")));
		} else if (this.info.getChild("placement").getProp("type").equals("nearWall")) {
			placeInRoomNearWall(r);
		}
	}

	protected void placeInRoomNearWall(AbstractRoom r) {

		int x = 0, y = 0;
		float angle = 0;

		int c = 0;
		boolean backToNeighbour;
		do {
			backToNeighbour = false;
			if (r.getClass().equals(Room.class)) {
				Room room = (Room) r;
				Point rs = room.getPathingCoords()[0];
				Point re = room.getPathingCoords()[1];

				int side = this.world.getRndInt(1, 4);

				switch (side) {
					case 1 :
						y = (int) (re.y + World.PATHING_GRANULARITY - World.PATHING_BLOCKSIZE);
						x = this.world.getRndInt(rs.x, re.x);
						angle = 0;
						backToNeighbour = room.neighboursContain(new Point(x / 10, y / 10 + 1));
						break;
					case 2 :
						y = (int) (rs.y + World.PATHING_BLOCKSIZE);
						x = this.world.getRndInt(rs.x, re.x);
						angle = FastMath.PI;
						backToNeighbour = room.neighboursContain(new Point(x / 10, y / 10 - 1));
						break;
					case 3 :
						y = this.world.getRndInt(rs.y, re.y);
						x = (int) (rs.x + World.PATHING_BLOCKSIZE);
						angle = FastMath.HALF_PI;
						backToNeighbour = room.neighboursContain(new Point(rs.x / 10 - 1, y / 10));
						break;
					case 4 :
						y = this.world.getRndInt(rs.y, re.y);
						x = (int) (re.x + World.PATHING_GRANULARITY - World.PATHING_BLOCKSIZE);
						backToNeighbour = room.neighboursContain(new Point(re.x / 10 + 1, y / 10));
						angle = -FastMath.HALF_PI;
						break;
				}
			} else if (r.getClass().equals(Corridor.class)) {
				// TODO implement this
				this.purge();
				return;
			}

			if (c++ > 100) {
				this.purge();
				System.out.println("Doodad.placeInRoomNearWall: Problems placing doodads in free spot");
				return;
			}
		} while (!this.walkablePathSpot(x, y) || backToNeighbour);
		this.setPos(x, y);
		this.rotate(angle);
	}

	protected void placeInRoomCluster(AbstractRoom r, int tries) {
		float x = 0, y = 0, angle = 0;

		int c = 0;
		do {
			if (r.getClass().equals(Room.class)) {
				Room room = (Room) r;
				Point rs = room.getPathingCoords()[0];
				Point re = room.getPathingCoords()[1];

				Vector2f pos = new Vector2f(0, 0);

				clusterFinderLoop : for (int i = 0; i < tries; i++) {
					pos.x = this.world.getRndFloat(rs.x, re.x);
					pos.y = this.world.getRndFloat(rs.y, re.y);

					for (Entity ent : r.getEntities()) {
						if (ent.isSubtype(EntitySubtype.DOODAD)) {
							if (ent.getPosition().distance(pos) < 8f) {
								break clusterFinderLoop;
							}
						}
					}
				}

				x = pos.x;
				y = pos.y;
			} else if (r.getClass().equals(Corridor.class)) {
				// TODO implement this
				this.purge();
				return;
			}

			if (c++ > 100) {
				this.purge();
				System.out.println("Doodad.placeInRoomCluster: Problems placing doodads in free spot");
				return;
			}
		} while (!this.walkablePathSpot(x, y));

		System.out.println("at " + x + ", " + y);

		this.setPos(x, y);
		this.rotate(angle);
	}

	public boolean isInInteractRange(Vector2f position) {
		return this.getPosition().distance(position) <= this.size;
	}
	
	public float getSize() {
		return this.size;
	}

}
