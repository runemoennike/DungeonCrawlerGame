/**
 * The Magic class represent a spell and contains all relevant information about
 * the spell such as level, name, duration, damage and much more.
 * It also contains method for casting the spell and checking validity of 
 * targets for the spell, as not all spells can hit the same targets.
 */

package misc;

import infostore.DataNode;

import java.util.LinkedList;

import map.AbstractRoom;
import skills.Skill;
import skills.SkillTree;

import com.jme.image.Texture;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.shape.Sphere;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;

import engine.Game;
import engine.hud.ConsoleLog;
import entities.Entity;
import entities.actors.Actor;
import entities.actors.Monster;
import entities.actors.Player;

public class Magic {

	public enum MagicType {
		DAMAGE, HEAL, BUFF, DEBUFF
	};

	private String name;
	private MagicType type;
	private int level;
	private Node node;
	private LinkedList<Skill> skills = new LinkedList<Skill>();
	private float duration = 0f;
	private int minDmgHeal = 0;
	private int maxDmgHeal = 0;
	private float rangeSquared = 10000f;
	private float aoeRangeSquared = 0f;
	private boolean noTarget = false;
	private int manaCost = 0;

	public Magic(Magic m) {
		this.name = m.name;
		this.type = m.type;
		this.level = m.level;
		this.node = m.node;
		this.skills = m.skills;
		this.duration = m.duration;
		this.minDmgHeal = m.minDmgHeal;
		this.maxDmgHeal = m.maxDmgHeal;
		this.rangeSquared = m.rangeSquared;
		this.aoeRangeSquared = m.aoeRangeSquared;
		this.noTarget = m.noTarget;
		this.manaCost = m.manaCost;
	}

	public Magic(DataNode node, int level) {
		this.name = node.getProp("name");
		this.type = MagicType.valueOf(node.getProp("magictype").toUpperCase());
		this.level = level;
		this.node = new Node();
		Sphere sp = new Sphere("Magic Sphere", 8, 8, 0.5f);
		TextureState ts = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
		ts.setTexture(TextureManager.loadTexture(Game.class.getClassLoader().getResource(
				"dcg/data/textures/" + "simple.png"), Texture.MinificationFilter.NearestNeighborLinearMipMap,
				Texture.MagnificationFilter.Bilinear));
		ts.setEnabled(true);
		sp.setDefaultColor(new ColorRGBA(0.6f, 0.2f, 0f, 0.5f));
		sp.setRenderState(ts);
		sp.updateRenderState();
		this.node.attachChild(sp);
		DataNode stats = node.getChild("stats");
		if (!stats.getProp("skills").isEmpty()) {
			for (String string : stats.getProp("skills").split(", ")) {
				if (SkillTree.getSkillByString(string) != null) {
					this.skills.add(SkillTree.getSkillByString(string));
				} else {
					System.out.println("Problem adding skill to NPC. Skill by the name " + string + " not found.");
				}
			}
		}
		this.duration = stats.getPropF("duration");
		this.minDmgHeal = stats.getPropI("mindmgheal") * level;
		this.maxDmgHeal = stats.getPropI("maxdmgheal") * level;
		this.rangeSquared = stats.getPropF("rangesquared") != 0f ? stats.getPropF("rangesquared") : this.rangeSquared;
		this.aoeRangeSquared = stats.getPropF("aoerangesquared");
		if (!stats.getProp("notarget").isEmpty()) {
			this.noTarget = Boolean.parseBoolean(stats.getProp("notarget"));
		}
		this.manaCost = stats.getPropI("manacost");
	}

	public void cast(Actor caster, Actor target) {
		ConsoleLog.addLine(caster.getName() + " casts " + this.toString() + " on " + target.getName());
		if (this.manaCost <= caster.getCurStats().getTotalMana()
				&& caster.getPosition().distanceSquared(target.getPosition()) <= this.rangeSquared) {
			target.markAsSpellTarget();
			if (this.aoeRangeSquared == 0f) {
				apply(caster, target);
			} else {
				for (AbstractRoom r : target.getRoom().getNeighbours()) {
					for (Entity e : r.getActors()) {
						if (Monster.class.equals(e.getClass())) {
							if (target.getPosition().distanceSquared(e.getPosition()) <= this.aoeRangeSquared) {
								Actor a = ((Actor) e);
								apply(caster, a);
							}
						}
					}
				}
			}
		}
	}

	private void apply(Actor caster, Actor target) {
		caster.getCurStats().subtractMana(this.manaCost);
		for (Skill s : this.skills) {
			target.addTempSkill(s, duration);
		}
		if (this.minDmgHeal != 0 || this.maxDmgHeal != 0) {
			Attack attack = new Attack(caster, this);
			attack.performAttack(target);
		}
	}

	public boolean isValidTarget(Actor caster, Entity target) {

		if (this.manaCost > caster.getCurStats().getTotalMana()) {
			return false;
		}
		if (this.noTarget) {
			return true;
		}
		if (caster.getClass().equals(Player.class)
				&& (this.type.equals(MagicType.HEAL) || this.type.equals(MagicType.BUFF))) {
			return true;
		}

		if (!Actor.class.isAssignableFrom(target.getClass())) {
			return false;
		}

		if (!(((Actor) target).timeSinceSpellTargeted() > 500)) {
			return false;
		}

		if (caster.getPosition().distanceSquared(target.getPosition()) <= rangeSquared) {
			return true;
		}
		return false;
	}

	public int getLevel() {
		return this.level;
	}

	public boolean isNoTarget() {
		return this.noTarget;
	}

	public int getMinDmgHeal() {
		return this.minDmgHeal;
	}

	public int getMaxDmgHeal() {
		return this.maxDmgHeal;
	}

	public LinkedList<Skill> getSkills() {
		return this.skills;
	}

	public String getName() {
		return this.name;
	}

	public float getDuration() {
		return duration;
	}

	public void setDuration(float duration) {
		this.duration = duration;
	}

	public float getRangeSquared() {
		return rangeSquared;
	}

	public void setRangeSquared(float rangeSquared) {
		this.rangeSquared = rangeSquared;
	}

	public float getAoeRangeSquared() {
		return aoeRangeSquared;
	}

	public void setAoeRangeSquared(float aoeRangeSquared) {
		this.aoeRangeSquared = aoeRangeSquared;
	}

	public int getManaCost() {
		return manaCost;
	}

	public void setManaCost(int manaCost) {
		this.manaCost = manaCost;
	}

	public void setMinDmgHeal(int minDmgHeal) {
		this.minDmgHeal = minDmgHeal;
	}

	public void setMaxDmgHeal(int maxDmgHeal) {
		this.maxDmgHeal = maxDmgHeal;
	}

	public Node getNode() {
		return this.node;
	}

	public MagicType getMagicType() {
		return this.type;
	}

	@Override
	public String toString() {
		return this.name + " " + this.level;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this.getClass().equals(obj.getClass())) {
			return this.toString().equals(obj.toString());
		}
		return false;
	}
}
