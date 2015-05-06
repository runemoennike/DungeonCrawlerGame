/**
 * Class which is used when no other Entity is targeted by the player. Used for moving.
 */

package entities;

import map.Map;

public class WalkTargetEntity extends Entity {

	private static final long serialVersionUID = -488262068866018708L;

	public WalkTargetEntity(Map map) {
		super(map.getWorld(), null);
		this.map = map;
		this.noPathing = true;
		this.noPicking = true;
		this.addSubtype(EntitySubtype.DUMMY);
		// TODO Auto-generated constructor stub
	}
}
