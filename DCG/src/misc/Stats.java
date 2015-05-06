/**
 * This very important class is used by all Actors and WearableItems represent all stats
 * an item or actor can have.
 * The class also contain a number of different constructers and different methods
 * for manipulating the stats. Also there are methods for getting hit chance, evasion chance, 
 * bonus damage and other things used in the Attack class. 
 */

package misc;

import infostore.DataNode;

import java.lang.reflect.Field;

import com.jme.math.FastMath;

import entities.items.Item.Quality;

public class Stats {

	private int str = 0;
	private int mag = 0;
	private int vit = 0;
	private int agi = 0;

	private int hp = 0;
	private int mana = 0;

	private int bonusDmg = 0;
	private int bonusHit = 0;
	private int bonusEva = 0;
	private int bonusArmor = 0;
	private int bonusMovementSpeed = 0;
	private int bonusAttackSpeed = 0;

	private int lifeSteal = 0;
	private int manaSteal = 0;

	public Stats() {

	}

	public Stats(Stats s) {
		this.str = s.str;
		this.mag = s.mag;
		this.vit = s.vit;
		this.agi = s.agi;
		this.hp = s.hp;
		this.mana = s.mana;
		this.bonusDmg = s.bonusDmg;
		this.bonusHit = s.bonusHit;
		this.bonusEva = s.bonusEva;
		this.bonusArmor = s.bonusArmor;
		this.lifeSteal = s.lifeSteal;
		this.manaSteal = s.manaSteal;
		this.bonusMovementSpeed = s.bonusMovementSpeed;
		this.bonusAttackSpeed = s.bonusAttackSpeed;
	}

	public Stats(DataNode n) {
		this.str = n.getPropI("str");
		this.mag = n.getPropI("mag");
		this.vit = n.getPropI("vit");
		this.agi = n.getPropI("agi");
		this.hp = n.getPropI("hp");
		this.mana = n.getPropI("mana");
		this.bonusDmg = n.getPropI("bonusDmg");
		this.bonusHit = n.getPropI("bonusHit");
		this.bonusEva = n.getPropI("bonusEva");
		this.bonusArmor = n.getPropI("bonusArmor");
		this.lifeSteal = n.getPropI("lifeSteal");
		this.manaSteal = n.getPropI("manaSteal");
		this.bonusMovementSpeed = n.getPropI("bonusmovementspeed");
		this.bonusAttackSpeed = n.getPropI("bonusattackspeed");
	}

	public Stats(DataNode n, int mult) {
		if (n == null) {
			// TODO: why?
			return;
		}
		mult = mult < 1 ? 1 : mult;
		this.str = n.getPropI("str") * mult;
		this.mag = n.getPropI("mag") * mult;
		this.vit = n.getPropI("vit") * mult;
		this.agi = n.getPropI("agi") * mult;
		this.hp = n.getPropI("hp") * mult;
		this.mana = n.getPropI("mana") * mult;
		this.bonusDmg = n.getPropI("bonusdmg") * mult;
		this.bonusHit = n.getPropI("bonusHit") * mult;
		this.bonusEva = n.getPropI("bonusEva") * mult;
		this.bonusArmor = n.getPropI("bonusarmor") * mult;
		this.lifeSteal = n.getPropI("lifeSteal") * mult;
		this.manaSteal = n.getPropI("manaSteal") * mult;
		this.bonusMovementSpeed = n.getPropI("bonusmovementspeed") * mult;
		this.bonusAttackSpeed = n.getPropI("bonusattackspeed") * mult;
	}

	public void addStatsToThis(Stats s) {
		this.str += s.str;
		this.mag += s.mag;
		this.vit += s.vit;
		this.agi += s.agi;
		this.hp += s.hp;
		this.mana += s.mana;
		this.bonusDmg += s.bonusDmg;
		this.bonusHit += s.bonusHit;
		this.bonusEva += s.bonusEva;
		this.bonusArmor += s.bonusArmor;
		this.lifeSteal += s.lifeSteal;
		this.manaSteal += s.manaSteal;
		this.bonusMovementSpeed += s.bonusMovementSpeed;
		this.bonusAttackSpeed += s.bonusAttackSpeed;
	}

	public void applyModifierStats(Stats s) {
		this.str *= (s.str + 100) / 100;
		this.mag *= (s.mag + 100) / 100;
		this.vit *= (s.vit + 100) / 100;
		this.agi *= (s.agi + 100) / 100;
		this.hp *= (s.hp + 100) / 100;
		this.mana *= (s.mana + 100) / 100;
		this.bonusDmg += s.bonusDmg;
		this.bonusHit += s.bonusHit;
		this.bonusEva += s.bonusEva;
		this.bonusArmor += s.bonusArmor;
		this.lifeSteal += s.lifeSteal;
		this.manaSteal += s.manaSteal;
		this.bonusMovementSpeed += s.bonusMovementSpeed;
		this.bonusAttackSpeed += s.bonusAttackSpeed;
	}

	public static Stats newPlayerStats() {
		Stats s = new Stats();
		s.setAgi(10);
		s.setVit(10);
		s.setStr(10);
		s.setMag(10);
		return s;
	}

	public static float qualityMultiplier(Quality quality) {
		if (quality.equals(Quality.EPIC)) {
			return 2.0f;
		} else if (quality.equals(Quality.RARE)) {
			return 1.5f;
		} else if (quality.equals(Quality.MAGIC)) {
			return 1.0f;
		} else if (quality.equals(Quality.COMMON)) {
			return 1.0f;
		} else if (quality.equals(Quality.POOR)) {
			return 0.5f;
		}
		return 1.0f;
	}

	public int getTotalHp() {
		return this.getVit() * 4 + this.hp;
	}

	public int getHp() {
		return this.hp;
	}

	public int getTotalMana() {
		return this.getMag() * 4 + this.mana;
	}

	public int getMana() {
		return this.mana;
	}

	public int getMeleeDamageBonus() {
		return this.getStr() + this.getAgi() / 2 + this.bonusDmg;
	}

	public int getRangedDamageBonus() {
		return this.getAgi() + this.getStr() / 2 + this.bonusDmg;
	}

	public int getHitChance() {
		return (int) (FastMath.log(this.getAgi(), 10) * 50 + this.bonusHit);
	}

	public int getMagicHitChance() {
		return (int) (FastMath.log(this.getMag(), 10) * 50 + this.bonusHit);
	}

	public void applyQulity(Quality q) {
		this.str = (int) (this.str * qualityMultiplier(q));
		this.mag = (int) (this.mag * qualityMultiplier(q));
		this.vit = (int) (this.vit * qualityMultiplier(q));
		this.agi = (int) (this.agi * qualityMultiplier(q));
		this.hp = (int) (this.hp * qualityMultiplier(q));
		this.mana = (int) (this.mana * qualityMultiplier(q));
		this.bonusDmg = (int) (this.bonusDmg * qualityMultiplier(q));
		this.bonusHit = (int) (this.bonusHit * qualityMultiplier(q));
		this.bonusEva = (int) (this.bonusEva * qualityMultiplier(q));
		this.bonusArmor = (int) (this.bonusArmor * qualityMultiplier(q));
		this.lifeSteal = (int) (this.lifeSteal * qualityMultiplier(q));
		this.manaSteal = (int) (this.manaSteal * qualityMultiplier(q));
		this.bonusAttackSpeed = (int) (this.bonusAttackSpeed * qualityMultiplier(q));
		this.bonusMovementSpeed = (int) (this.bonusMovementSpeed * qualityMultiplier(q));
	}

	public int getEvasionChance() {
		return (int) (FastMath.log(this.getAgi(), 10) * 15 + this.bonusEva);
	}

	public int getVit() {
		return this.vit;
	}

	public int getMag() {
		return this.mag;
	}

	public int getStr() {
		return this.str;
	}

	public int getAgi() {
		return this.agi;
	}

	public int getBonusDmg() {
		return bonusDmg;
	}

	public void setBonusDmg(int bonusDmg) {
		this.bonusDmg = bonusDmg;
	}

	public int getBonusHit() {
		return bonusHit;
	}

	public void setBonusHit(int bonusHit) {
		this.bonusHit = bonusHit;
	}

	public int getBonusDodge() {
		return bonusEva;
	}

	public void setBonusEva(int bonusEva) {
		this.bonusEva = bonusEva;
	}

	public int getLifeSteal() {
		return lifeSteal;
	}

	public void setLifeSteal(int lifeSteal) {
		this.lifeSteal = lifeSteal;
	}

	public int getManaSteal() {
		return manaSteal;
	}

	public void setManaSteal(int manaSteal) {
		this.manaSteal = manaSteal;
	}

	public void setStr(int str) {
		this.str = str;
	}

	public void setMag(int mag) {
		this.mag = mag;
	}

	public void setVit(int vit) {
		this.vit = vit;
	}

	public void setAgi(int agi) {
		this.agi = agi;
	}

	public void setHp(int hp) {
		this.hp = hp;
	}

	public void setMana(int mana) {
		this.mana = mana;
	}

	public void addMag() {
		this.mag++;
	}

	public void addVit() {
		this.vit++;
	}

	public void addStr() {
		this.str++;
	}

	public void addAgi() {
		this.agi++;
	}

	public int getBonusArmor() {
		return bonusArmor;
	}

	public void setBonusArmor(int bonusArmor) {
		this.bonusArmor = bonusArmor;
	}

	public int getBonusEva() {
		return bonusEva;
	}

	public int getBonusMovementSpeed() {
		return bonusMovementSpeed;
	}

	public void setBonusMovementSpeed(int movementSpeed) {
		this.bonusMovementSpeed = movementSpeed;
	}

	public int getBonusAttackSpeed() {
		return bonusAttackSpeed;
	}

	public void setBonusAttackSpeed(int attackSpeed) {
		this.bonusAttackSpeed = attackSpeed;
	}

	public void dump() {
		for (Field f : this.getClass().getDeclaredFields()) {
			try {
				System.out.print(f.getName() + "=" + f.getInt(this) + ", ");
			} catch (Exception e) {
			}
		}
		System.out.println("");
	}

	public void subtractHp(int amount) {
		this.hp -= amount;
	}

	public void subtractMana(int amount) {
		this.mana -= amount;
	}

	public void addHp(int amount) {
		this.hp += amount;
	}

	public void addMana(int amount) {
		this.mana += amount;
	}

	@Override
	public String toString() {
		String r = "";
		if (str != 0)
			r += "str=" + str + " ";
		if (vit != 0)
			r += "vit=" + vit + " ";
		if (agi != 0)
			r += "agi=" + agi + " ";
		if (mag != 0)
			r += "mag=" + mag + " ";
		if (hp != 0)
			r += "hp=" + hp + " ";
		if (mana != 0)
			r += "mana=" + mana + " ";
		if (lifeSteal != 0)
			r += "lifeSteal=" + lifeSteal + " ";
		if (manaSteal != 0)
			r += "manaSteal=" + manaSteal + " ";
		if (bonusDmg != 0)
			r += "bonusDmg=" + bonusDmg + " ";
		if (bonusHit != 0)
			r += "bonusHit=" + bonusHit + " ";
		if (bonusEva != 0)
			r += "bonusEva=" + bonusEva + " ";
		if (bonusArmor != 0)
			r += "bonusArmor=" + bonusArmor + " ";
		if (bonusAttackSpeed != 0)
			r += "bonusAttackSpeed=" + bonusAttackSpeed + " ";
		if (bonusMovementSpeed != 0)
			r += "bonusMovementSpeed=" + bonusMovementSpeed + " ";
		return r;
	}

	public String getInfoString() {
		String r = "";
		if (str != 0)
			r += getSign(str) + str + " to Strength\n";
		if (vit != 0)
			r += getSign(vit) + vit + " to Vitality\n";
		if (agi != 0)
			r += getSign(agi) + agi + " to Agility\n";
		if (mag != 0)
			r += getSign(mag) + mag + " to Magic\n";
		if (hp != 0)
			r += getSign(hp) + hp + " added Hit Point\n";
		if (mana != 0)
			r += getSign(mana) + mana + " added Mana\n";
		if (lifeSteal != 0)
			r += getSign(lifeSteal) + lifeSteal + "% HP stolen per hit\n";
		if (manaSteal != 0)
			r += getSign(manaSteal) + manaSteal + "% Mana stolen per hit\n";
		if (bonusDmg != 0)
			r += getSign(bonusDmg) + bonusDmg + "% to Damage\n";
		if (bonusHit != 0)
			r += getSign(bonusHit) + bonusHit + "% to Hit Chance\n";
		if (bonusEva != 0)
			r += getSign(bonusEva) + bonusEva + "% to Evasion Chance\n";
		if (bonusArmor != 0)
			r += getSign(bonusArmor) + bonusArmor + "% to Armor\n";
		if (bonusAttackSpeed != 0)
			r += getSign(bonusAttackSpeed) + bonusAttackSpeed + "% to Attack Speed\n";
		if (bonusMovementSpeed != 0)
			r += getSign(bonusMovementSpeed) + bonusMovementSpeed + "% to Movement Speed\n";
		return r.trim();
	}

	private String getSign(int value) {
		if (value == 0) {
			return "";
		} else if (value < 0) {
			return "-";
		} else {
			return "+";
		}
	}
}
