/**
 * This class represent non-person-characters in the game. It extends Actor and contains information
 * needed about NPCs, such as type, quility, rangetype size, ai/boid controller, damage and such. 
 * 
 * It contains a series of methods overriding methods from th Actor class.
 */

package entities.actors;

import infostore.DataManager;
import infostore.DataNode;
import infostore.DataManager.DataType;

import java.awt.Point;
import java.util.ArrayList;

import map.Map;
import misc.Magic;
import misc.Stats;
import misc.Magic.MagicType;
import skills.SkillTree;
import ai.BoidUnit;
import ai.BoidUnit.BoidUnitState;

import com.jme.math.Vector2f;

import entities.Entity;
import entities.items.Weapon.RangeType;

public abstract class NPC extends Actor {

	private static final long serialVersionUID = -2339359365864261458L;

	public enum NPCQuality {
		NORMAL, SPECIAL, BOSS
	};

	public enum NPCType {
		MELEE, MAGIC, RANGED
	};

	protected RangeType rangeType;
	protected NPCQuality npcQuality;
	protected NPCType npcType;

	private BoidUnit boidController;

	private float size;
	private Entity target;
	private int minDmg;
	private int maxDmg;

	public NPC(Map map, DataNode node) {
		super(map, DataManager.findByNameAndType(DataType.MODEL_ID, node.getProp("modelID")), new Stats(node
				.getChild("stats"), map.getWorld().getRndInt(map.getLevel() - 2, map.getLevel())), map.getLevel());

		// TODO: implement/remove ai
		node.getProp("ai");

		this.npcType = NPCType.valueOf(node.getProp("npctype").toUpperCase());
		this.npcQuality = NPCQuality.valueOf(node.getProp("npcquality").toUpperCase());
		this.speed = node.getPropF("speed");
		this.size = .5f * Math
				.max(node.getChild("collision").getPropI("w"), (node.getChild("collision").getPropI("h")));

		this.minDmg = node.getPropI("mindmg") * this.level;
		this.maxDmg = node.getPropI("maxdmg") * this.level;

		this.makePathingFootprint(node.getChild("collision").getPropI("w"), node.getChild("collision").getPropI("h"));
		this.addSubtype(Entity.EntitySubtype.NPC);

		if (npcType.equals(NPCType.MELEE)) {
			this.rangeType = RangeType.MELEE;
		} else {
			this.rangeType = RangeType.RANGED;
		}
		if (node.isChild("skills")) {
			if (!node.getChild("skills").getProp("skills").isEmpty()) {
				for (String string : node.getChild("skills").getProp("skills").split(", ")) {
					if (SkillTree.getSkillByString(string) != null) {
						this.addSkill(SkillTree.getSkillByString(string));
					} else {
						System.out.println("Problem adding skill to NPC. Skill by the name " + string + " not found.");
					}
				}
			}
			if (!node.getChild("skills").getProp("magics").isEmpty()) {
				for (String string : node.getChild("skills").getProp("magics").split(", ")) {
					if (DataManager.findByNameAndType(DataType.MAGIC, string) != null) {
						this.addMagic(new Magic(DataManager.findByNameAndType(DataType.MAGIC, string), this.level));
					} else {
						System.out.println("Problem adding magic to NPC. Magic by the name " + string + " not found.");

					}
				}
			}
		}
		updateStats();
	}

	public NPCType getNPCType() {
		return this.npcType;
	}

	// Serious java magic is black.
	// try {
	// this.ai = (AbstractAI) loader.loadClass("entities.ai." +
	// ai).getConstructor(
	// new Class[]{World.class, NPC.class}).newInstance(new Object[]{this.world,
	// this});
	// } catch (Exception e) {
	// System.out.println("NPC: Serious black java magic failure. Eject.");
	// e.printStackTrace();
	// System.exit(-1);
	// }

	// public void setAI(AbstractAI newAI) {
	// this.ai = newAI;
	// }

	@Override
	public void updateAI(float t) {
		if (this.target == null) {
			this.target = this.map.getWorld().getLocalPlayer();
		}

		if (this.boidController != null) {
			if (this.getCurState().equals(EntityState.IDLE)) {
				this.setCurState(EntityState.BOID);
			}

			if (this.isInAttackRange(this.target)) {
				this.attack(this.target);
			}

			if (this.getCurState().equals(EntityState.BOID)) {
				Vector2f oldP = this.getPosition();

				this.boidController.update(t);

				if (this.boidController.getState().equals(BoidUnitState.IDLE)) {
					this.lookTowards = this.getWorld().getLocalPlayer().getPosition();
					this.setAnimation(Animation.IDLE);
				} else {
					Vector2f d = this.getPosition().subtract(oldP).normalize();
					this.lookTowards = this.getPosition().add(d);
					this.setAnimation(Animation.WALK);
				}
			}
		}

		if (this.npcType.equals(NPCType.MAGIC)) {
			Magic healSpell = null;
			for (Magic m : this.getMagics()) {
				if (m.getMagicType().equals(MagicType.HEAL)) {
					healSpell = m;
					break;
				}
			}

			if (healSpell != null) {
				for (BoidUnit bu : this.boidController.getGroup().getUnits()) {
					if (healSpell.isValidTarget(this, bu.getNpc())
							&& bu.getNpc().getCurStats().getTotalHp() > 0
							&& 100f * bu.getNpc().getCurStats().getTotalHp() / bu.getNpc().getTotalStats().getTotalHp() < 50f) {
						healSpell.cast(this, bu.getNpc());
						break;
					}
				}
			}
		}
	}

	@Override
	protected int getMinBaseDmg() {
		return this.minDmg;
	}

	@Override
	protected int getMaxBaseDmg() {
		return this.maxDmg;
	}

	@Override
	public float getAttackRange() {
		if (this.rangeType.equals(RangeType.MELEE)) {
			if (this.getSize() + 1f > 5.0f) {
				return this.getSize() + 1f;
			}
			return 5.0f;
		} else {
			return 30.0f;
		}
	}

	@Override
	public RangeType getAttackRangeType() {
		return this.rangeType;
	}

	@Override
	public int getXpValue() {
		int value = this.level * this.level * 50;
		if (this.npcQuality.equals(NPCQuality.NORMAL)) {
			return value;
		} else if (this.npcQuality.equals(NPCQuality.SPECIAL)) {
			return value * 5;
		} else {
			return value * 10;
		}
	}

	public void setBoidController(BoidUnit boidController) {
		this.boidController = boidController;
	}

	public BoidUnit getBoidController() {
		return boidController;
	}

	public float getSize() {
		return this.size;
	}

	public Entity getTarget() {
		return this.target;
	}

	public float getAcquisitionRange() {
		return 50f;
	}

	public void markSearchedPath(ArrayList<Point> obj) {
		this.map.clearShowPathing(this);
		for (Point p : obj) {
			this.map.markExploredPathing(this, p.x, p.y);
		}
	}
}