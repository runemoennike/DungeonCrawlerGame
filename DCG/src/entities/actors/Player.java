/**
 * The player class exstending the Actor class, constain information relevant to the player.
 * This is for example experince, gold, inventory, available stats and skill points.
 * 
 * It contains a set of methods for interaction with these properties and some overrides.
 */

package entities.actors;

import infostore.DataManager;
import infostore.DataManager.DataType;
import map.Map;
import misc.Inventory;
import misc.Magic;
import misc.Stats;
import misc.Inventory.BELTSPOT;
import engine.Game;
import engine.gfx.EffectFactory;
import engine.gfx.EffectFactory.EffectType;
import engine.hud.ConsoleLog;
import entities.Entity;
import entities.doodads.Doodad;
import entities.items.Item;
import entities.items.Weapon;
import entities.items.WearableItem;

public class Player extends Actor {

	private static final long serialVersionUID = -2936496315062080088L;

	private Entity interactionTarget;
	private float pathingTime;
	private boolean attemptedPathing;
	private int xp;
	private int xpForNextLevel;
	private Inventory inventory;
	private Inventory stash;
	private int availableStatPoints;
	private int availableSkillPoints;
	private int deepestLevelReached = 0;

	public Player(Map map, String name) {
		super(map, DataManager.findByNameAndType(DataType.MODEL_ID, "modelID_player"), Stats.newPlayerStats(), 1);
		this.addSubtype(Entity.EntitySubtype.PLAYER);

		this.speed = 10f;
		this.level = 1;
		this.name = name;

		WearableItem weap2 = new Weapon(map.getWorld(), DataManager.findByName("item_sword"), 1);
		weap2.setCaption("Sword");
		this.wearItem(weap2, AttachPoint.HAND_R);

		this.inventory = Inventory.newBag();
		this.stash = Inventory.newStash();

		this.xp = 0;
		this.xpForNextLevel = calcXp(this.level + 1);

		this.pathingTime = Game.getTimer().getTimeInSeconds();
	}

	public void interact(Entity target) {
		// Attempt to interact with the target
		boolean success = false;
		if (target.isSubtype(EntitySubtype.MONSTER)) {
			if (isInAttackRange(target)) {
				attack(target);
				success = true;
			}
		} else if (target.isSubtype(EntitySubtype.ITEM)) {
			if (this.getPosition().distanceSquared(target.getPosition()) < 9f) {
				((Item) target).interactReact(this);
				success = true;
			}
		} else if (target.isSubtype(EntitySubtype.DOODAD)) {
			if (((Doodad) target).isInInteractRange(this.getPosition())) {
				if (((Doodad) target).isDestructible()) {
					attack(target);
				} else {
					((Doodad) target).interactReact(this);
				}

				success = true;
				//System.out.println("in range");
			} /* else { System.out.println("not in range"); }*/ 
		} else if (target.isSubtype(EntitySubtype.FRIENDLY)) {

		} else if (target.isSubtype(EntitySubtype.DUMMY)) {
			if (this.getPosition().distanceSquared(target.getPosition()) < 4f) {
				success = true;
			}
		}

		if (success) {
			this.interactionTarget = null;
		} else {
			if (!this.attemptedPathing) {
				// If unable to interact, move towards the target
				if (this.getPosition().distanceSquared(this.interactionTarget.getPosition()) < 16f) {
					// LinkedList<Vector2f> list = new LinkedList<Vector2f>();
					// list.add(interactionTarget.getPosition());
					// setPath(list);
					this.moveTowards = this.interactionTarget.getPosition();
					this.curState = EntityState.MOVE;
				} else if (Game.getTimer().getTimeInSeconds() - this.pathingTime > 0.2f) {
					createPathTo(this.map.findValidSpotNear(this.getPosition(), target.getPosition(), this));
					this.setCurState(EntityState.MOVE);
					this.pathingTime = Game.getTimer().getTimeInSeconds();
				}
				this.attemptedPathing = true;
			}
		}
	}

	@Override
	public void updateAI(float t) {
		switch (this.curState) {
			case MOVE :
				updateWalk(t);
				break;
			case DYING :
				respawn();
				break;
		}
		if (this.interactionTarget != null) {
			interact(this.interactionTarget);
		}
	}

	public void respawn() {
		int xpForLvl = calcXp(this.level);
		int xpNeededForLvl = this.xpForNextLevel - xpForLvl;
		this.xp -= xpNeededForLvl * 10 / 100;
		if (this.xp < xpForLvl) {
			this.xp = xpForLvl;
		}
		this.noPathing = false;
		this.rebuildPathingFootprint();
		this.getNode().getLocalTranslation().z = 0f;
		this.curStats.setHp(this.totalStats.getHp());
		this.curState = EntityState.IDLE;
		Game.getInstance().movePlayerToMap(0);
	}

	private static int calcXp(int level) {
		return (int) Math.pow(level, (20f / 7f)) * 100 + 100;
	}

	public void setInteractionTarget(Entity interactionTarget) {
		this.interactionTarget = interactionTarget;
		this.attemptedPathing = false;
	}

	public Entity getInteractionTarget() {
		return interactionTarget;
	}

	public int getXp() {
		return this.xp;
	}

	public void addXp(int xp) {
		this.xp += xp;
		ConsoleLog.addLine(xp + " xp gained.");
		if (this.xp >= this.xpForNextLevel) {
			EffectFactory.spawnEffect(EffectType.LEVEL_UP, this.getNode().getLocalTranslation(), 0, 1, "simple.png");
			EffectFactory.spawnEffect(EffectType.LEVEL_UP, this.getNode().getLocalTranslation().add(0, 0, 3), 0, 1,
					"simple.png");
			EffectFactory.spawnEffect(EffectType.LEVEL_UP, this.getNode().getLocalTranslation().add(0, 0, 6), 0, 1,
					"simple.png");
			this.level++;
			ConsoleLog.addLine("Level " + level + " reached!");
			this.xpForNextLevel = calcXp(this.level + 1);
			this.availableSkillPoints++;
			this.availableStatPoints += 5;

			Game.getInstance().getHUD().requireSkillSheetUpdate();
		}
	}
	public int getXpForNextLevel() {
		return this.xpForNextLevel;
	}

	@Override
	public boolean giveItem(Item i) {
		return (this.inventory.bagItem(i));
	}

	public Inventory getInventory() {
		return this.inventory;
	}

	public void useBeltSlot(BELTSPOT spot) {
		if (this.inventory.decreaseBeltSpotCount(spot)) {
			switch (spot) {
				case HPOT :
					this.curStats.setHp((int) (this.curStats.getHp() + this.totalStats.getTotalHp() * 0.25));
					if (this.curStats.getHp() > this.totalStats.getHp()) {
						this.curStats.setHp(this.totalStats.getHp());
					}
					break;
				case MPOT :
					this.curStats.setMana((int) (this.curStats.getMana() + this.totalStats.getTotalMana() * 0.25));
					if (this.curStats.getMana() > this.totalStats.getMana()) {
						this.curStats.setMana(this.totalStats.getMana());
					}
					break;
				case HPOT_LARGE :
					this.curStats.setHp(this.totalStats.getHp());
					break;
				case MPOT_LARGE :
					this.curStats.setMana(this.totalStats.getMana());
					break;
			}
			Game.getInstance().getHUD().updateHpAndMana();
		}
	}

	public Inventory getStash() {
		return this.stash;
	}

	public boolean isInSanctuary() {
		return this.map.getLevel() == 0;
	}

	@Override
	public int getXpValue() {
		return 0;
	}

	public int getAvailableSkillPoints() {
		return this.availableSkillPoints;
	}

	public int getAvailableStatPoints() {
		return this.availableStatPoints;
	}

	public void removeAvailableSkillPoint() {
		this.availableSkillPoints--;
	}

	public void removeAvailableStatPoint() {
		this.availableStatPoints--;
	}

	public void setCurMagic(Magic m) {
		this.curMagic = m;
	}

	public void setDeepestLevelReached(int deepestLevelReached) {
		this.deepestLevelReached = deepestLevelReached;
	}

	public int getDeepestLevelReached() {
		return deepestLevelReached;
	}
}
