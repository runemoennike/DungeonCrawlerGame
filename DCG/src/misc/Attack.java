/**
 * This class is used for all attacks, and damaging spells. 
 * It uses a vast variety of different stats from the actors 
 * involved to calculate the outcome of the attack. 
 * Attacks can either miss, hit a wall, be evaded or hit. 
 * Healing is also done through the Attack class by negating the damage.
 */

package misc;

import java.awt.Color;

import misc.Magic.MagicType;

import skills.DefensiveSkill;
import skills.OffensiveSkill;

import com.jme.image.Texture;
import com.jme.math.FastMath;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.shape.Arrow;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;

import engine.Game;
import engine.World;
import engine.gfx.EffectFactory;
import engine.gfx.EffectFactory.EffectType;
import engine.hud.ConsoleLog;
import entities.Entity;
import entities.actors.Actor;
import entities.actors.Player;
import entities.items.Armor;
import entities.items.Weapon.RangeType;

public class Attack extends Entity {

	public enum DamageType {
		PHYSICAL, MAGIC
	};

	private final static float RANGED_VELOCITY = 1f;

	private DamageType type;
	private Magic magic;
	private Actor attacker;
	private Actor target;
	private int maxDmg;
	private int minDmg;
	private int finalDmg;
	private float rangeModifyer;
	private float damagePoint;
	private int hitChance;
	private int hitRoll;
	private boolean miss;
	private float rangeSquared;

	private int attackerHpChange = 0;
	private int attackerManaChange = 0;
	private int targetHpChange = 0;
	private int targetManaChange = 0;

	/**
	 * Overload constructor for the Attack class. Calls the stardard constructor
	 * with Magic param null.
	 * 
	 * @param The
	 *            actor which attacks.
	 */
	public Attack(Actor attacker) {
		this(attacker, null);
	}

	/**
	 * Standard constructor for the Attack class. This should be used for all
	 * kinds of attacks. The hitchance, min dmg and max dmg of the attack it set
	 * if the attack is not a magic/spell.
	 * 
	 * @param The
	 *            actor which attacks.
	 * @param The
	 *            magic associated with the attack. If this is a physical
	 *            attack, this should be null.
	 */
	public Attack(Actor attacker, Magic magic) {
		super();
		super.noPathing = true;
		super.noPicking = true;
		this.magic = magic;
		this.map = attacker.getMap();
		this.world = attacker.getWorld();
		this.finalDmg = 0;
		this.attacker = attacker;

		if (this.magic == null) {
			this.type = DamageType.PHYSICAL;

			this.hitChance = attacker.getCurStats().getHitChance();
			this.maxDmg = attacker.getPhysicalMaxDmg();
			this.minDmg = attacker.getPhysicalMinDmg();

			for (OffensiveSkill s : attacker.getOffensiveSkills()) {
				s.apply(this);
			}

		} else {
			this.type = DamageType.MAGIC;
		}
	}

	/**
	 * Method for performing this Attack on target Actor. The attack is added to
	 * the attack list is the World class, if the attack is ranged or not a
	 * miss.
	 * 
	 * @param Actor
	 *            to attack.
	 */
	public void performAttack(Actor target) {
		this.attackerHpChange = 0;
		this.attackerManaChange = 0;
		this.targetHpChange = 0;
		this.targetManaChange = 0;
		this.target = target;
		this.hitRoll = this.world.getRndInt(0, 99);
		this.rangeSquared = this.attacker.getPosition().distanceSquared(target.getPosition());
		if (this.type.equals(DamageType.PHYSICAL)) {
			this.hitChance = this.attacker.getCurStats().getHitChance();
			this.maxDmg = this.attacker.getPhysicalMaxDmg();
			this.minDmg = this.attacker.getPhysicalMinDmg();
			this.finalDmg = this.world.getRndInt(this.minDmg, this.maxDmg);
			if (attacker.getAttackRangeType().equals(RangeType.MELEE)) {
				this.damagePoint = Game.getTimer().getTimeInSeconds();
				this.rangeModifyer = 1.0f;
			} else {
				setRangedAttackValuesAndAnimation();
			}
			this.hitChance = (int) ((this.hitChance) * this.rangeModifyer);
			for (OffensiveSkill s : attacker.getOffensiveSkills()) {
				s.apply(this);
			}
		} else {
			this.hitChance = this.attacker.getCurStats().getMagicHitChance();
			this.maxDmg = this.magic.getMaxDmgHeal();
			this.minDmg = this.magic.getMinDmgHeal();
			if (this.magic.getMagicType().equals(MagicType.HEAL)) {
				this.finalDmg = -this.world.getRndInt(this.minDmg, this.maxDmg);
			} else {
				this.finalDmg = this.world.getRndInt(this.minDmg, this.maxDmg);
			}
			setRangedAttackValuesAndAnimation();
			this.hitChance = (int) ((this.hitChance) * this.rangeModifyer);
		}
		if (!this.target.getClass().equals(this.attacker.getClass())) {
			this.miss = this.hitChance <= this.hitRoll;
		} else {
			this.miss = false;
		}
		if (this.miss) {
			ConsoleLog.addLine(attacker.getName() + " misses " + target.getName());
			this.attacker.addFloatingText("*miss*", Color.white, Color.yellow, 4f);
			if (!attacker.getAttackRangeType().equals(RangeType.MELEE)) {
				int roll = this.world.getRndInt(1, 2);
				float offset = roll == 1 ? -0.3f : 0.3f;
				if (this.velocity.x < this.velocity.y) {
					this.velocity.x += offset;
				} else {
					this.velocity.y += offset;
				}
			}
		}

		if (!this.miss || attacker.getAttackRangeType().equals(RangeType.RANGED) || this.type.equals(DamageType.MAGIC)) {
			this.attacker.getWorld().addAttack(this);
		}
	}

	private void setRangedAttackValuesAndAnimation() {
		Vector2f tmp = target.getPosition().subtract(attacker.getPosition()).normalize();
		super.velocity.x = tmp.x * RANGED_VELOCITY;
		super.velocity.y = tmp.y * RANGED_VELOCITY;
		this.map.getEffectNode().attachChild(this.n);
		this.n.setLocalTranslation(attacker.getPosition().x, attacker.getPosition().y, 4);
		if (this.type.equals(DamageType.PHYSICAL)) {
			Arrow ar = new Arrow("Arrow", 0.7f, 0.2f);
			TextureState ts = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
			ts.setTexture(TextureManager.loadTexture(Game.class.getClassLoader().getResource(
					"dcg/data/textures/" + "simple.png"), Texture.MinificationFilter.NearestNeighborLinearMipMap,
					Texture.MagnificationFilter.Bilinear));
			ts.setEnabled(true);
			ar.setDefaultColor(ColorRGBA.black);
			ar.setRenderState(ts);
			ar.rotateUpTo(new Vector3f(tmp.x, tmp.y, 0f));
			ar.updateRenderState();
			this.n.attachChild(ar);
		} else {
			this.n.attachChild(this.magic.getNode());
		}
		this.rangeModifyer = 1.0f - 1.0f / 1.0e8f * FastMath.pow(this.rangeSquared, 2.0f);
		this.damagePoint = (attacker.getPosition().distance(target.getPosition())) / (100f * RANGED_VELOCITY)
				+ Game.getTimer().getTimeInSeconds();
	}

	public int getMaxDmg() {
		return maxDmg;
	}

	public int getMinDmg() {
		return minDmg;
	}

	public int getAvgDmg() {
		return (this.maxDmg + this.minDmg) / 2;
	}

	public float getRangeSquared() {
		return this.rangeSquared;
	}

	public void setMaxDmg(int dmg) {
		this.maxDmg = dmg;
	}

	public void setMinDmg(int dmg) {
		this.minDmg = dmg;
	}

	public int getHitChance() {
		return hitChance;
	}

	public void setHitChance(int hitChance) {
		this.hitChance = hitChance;
	}

	public Actor getAttacker() {
		return attacker;
	}

	public Actor getTarget() {
		return target;
	}

	public boolean miss() {
		return this.miss;
	}

	public boolean hit() {
		return !this.miss;
	}

	public void setHitRoll(int roll) {
		this.hitRoll = roll;
	}

	public DamageType getDamageType() {
		return this.type;
	}

	public boolean updateAttack(float t) {
		super.update(t);
		if (Game.getTimer().getTimeInSeconds() >= this.damagePoint && !this.miss) {
			applyAttack();
			this.purge();
			return true;
		} else if (this.map.getRoom(World.worldToTile(this.getPosition().x), World.worldToTile(this.getPosition().y)) == null) {
			ConsoleLog.addLine(attacker.getName() + "'s attack hit the wall");
			this.purge();
			return true;
		} else {
			return false;
		}
	}

	public int getFinalDmg() {
		return finalDmg;
	}

	public void setFinalDmg(int finalDmg) {
		this.finalDmg = finalDmg;
	}

	public void subAttackerHp(int amount) {
		this.attackerHpChange -= amount;
	}

	public void addAttackerHp(int amount) {
		this.attackerHpChange += amount;
	}

	public void subAttackerMana(int amount) {
		this.attackerManaChange -= amount;
	}

	public void addAttackerMana(int amount) {
		this.attackerManaChange += amount;
	}

	public void subTargetHp(int amount) {
		this.targetHpChange -= amount;
	}

	public void addTargetHp(int amount) {
		this.targetHpChange += amount;
	}

	public void subTargetMana(int amount) {
		this.targetManaChange -= amount;
	}

	public void addTargetMana(int amount) {
		this.targetManaChange += amount;
	}

	private void applyAttack() {

		this.hitChance -= this.target.getCurStats().getEvasionChance();

		if (this.type.equals(DamageType.PHYSICAL)) {
			this.finalDmg = Armor.reduceDmgByArmor(this.finalDmg, target.getArmor());
		}

		if (!this.target.getClass().equals(this.attacker.getClass())) {
			for (DefensiveSkill s : target.getDefensiveSkills()) {
				s.apply(this);
			}
		}

		if (this.hitChance <= this.hitRoll && !this.target.getClass().equals(this.attacker.getClass())) {
			ConsoleLog.addLine(target.getName() + " evaded " + this.attacker.getName() + "'s attack.");
			target.addFloatingText("*evade*", Color.white, Color.blue, 4f);
		} else {
			this.attackerHpChange *= (this.attacker.getCurStats().getLifeSteal() + 100) / 100;
			this.attackerManaChange *= (this.attacker.getCurStats().getManaSteal() + 100) / 100;

			this.target.getCurStats().subtractHp(this.finalDmg);

			this.attacker.getCurStats().addHp(this.attackerHpChange);
			this.attacker.getCurStats().addMana(this.attackerManaChange);
			this.target.getCurStats().addHp(this.targetHpChange);
			this.target.getCurStats().addMana(this.targetManaChange);

			if (this.finalDmg < 0) {
				ConsoleLog.addLine(attacker.getName() + " heals " + target.getName() + " for " + -this.finalDmg + ".");
				ConsoleLog.addLine(target.getName() + " has " + target.getCurStats().getTotalHp() + " HP.");
				target.addFloatingTextQueued("" + -this.finalDmg, Color.white, Color.green, 2f);
				EffectFactory.spawnEffect(EffectType.HEAL, this.target.getNode().getLocalTranslation().add(0, 0, 1.5f),
						this.attacker.getRotation(), 1, "simple.png");
			} else {
				EffectFactory.spawnEffect(EffectType.HIT_BLOOD, this.target.getNode().getLocalTranslation().add(0, 0,
						1.5f), this.attacker.getRotation(), 1, "simple.png");
				EffectFactory.spawnEffect(EffectType.HIT_BLOOD, this.target.getNode().getLocalTranslation()
						.add(0, 0, 3), this.attacker.getRotation(), 1, "simple.png");
				EffectFactory.spawnEffect(EffectType.HIT_BLOOD, this.target.getNode().getLocalTranslation().add(0, 0,
						4.5f), this.attacker.getRotation(), 1, "simple.png");

				ConsoleLog.addLine(attacker.getName() + " hits " + target.getName() + " for " + this.finalDmg
						+ " damage.");
				ConsoleLog.addLine(target.getName() + " has " + target.getCurStats().getTotalHp() + " HP left.");

				target.addFloatingTextQueued("" + this.finalDmg, Color.white, Color.red, 2f);

			}
			if (this.attacker.getCurStats().getMana() > this.attacker.getTotalStats().getMana()) {
				this.attacker.getCurStats().setMana(this.attacker.getTotalStats().getMana());
			}
			if (this.attacker.getCurStats().getHp() > this.attacker.getTotalStats().getHp()) {
				this.attacker.getCurStats().setHp(this.attacker.getTotalStats().getHp());
			}
			if (this.target.getCurStats().getMana() > this.target.getTotalStats().getMana()) {
				this.target.getCurStats().setMana(this.target.getTotalStats().getMana());
			}
			if (this.target.getCurStats().getHp() > this.target.getTotalStats().getHp()) {
				this.target.getCurStats().setHp(this.target.getTotalStats().getHp());
			}

			this.target.setRecovering();

			if (this.target.isDead()) {
				if (attacker.getClass().equals(Player.class)) {
					Player p = (Player) attacker;
					p.addXp(target.getXpValue());
				}
				if (target.getClass().equals(Player.class)) {
					Game.getInstance().getHUD().showDeathWindow();
				}
				ConsoleLog.addLine(attacker.getName() + " killed " + target.getName() + ".");
			}
			Game.getInstance().getHUD().updateHpAndMana();
		}
	}
}
