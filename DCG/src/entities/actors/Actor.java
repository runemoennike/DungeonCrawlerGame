/**
 * The Actor class exteds Entity and represents all living actors, such as players, monsters, NPC and so on.
 * 
 * It contains a lot of information about the actor suck as animation, movement, stats, skills spells, 
 * equipped items, level, speed and much more.
 * 
 * It contains a series of different update methods used for movement, casting spells, attacking, updating stats
 * , interacting and other things.
 */

package entities.actors;

import infostore.DataNode;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import map.Map;
import misc.Attack;
import misc.Magic;
import misc.Stats;
import misc.Magic.MagicType;
import skills.DefensiveSkill;
import skills.MagicSkill;
import skills.OffensiveSkill;
import skills.Skill;
import skills.SkillTree;
import skills.StatsSkill;
import tasks.PathFinderTask;
import thirdParty.TextLabel2D;

import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.scene.BillboardNode;
import com.jme.scene.Node;

import engine.Game;
import engine.Profiler;
import engine.gfx.AnimatedNode.BoneType;
import engine.gfx.AnimatedNode.RepeatMode;
import engine.hud.ConsoleLog;
import entities.Entity;
import entities.doodads.Doodad;
import entities.items.Armor;
import entities.items.Item;
import entities.items.Weapon;
import entities.items.WearableItem;
import entities.items.Weapon.RangeType;
import entities.items.WearableItem.WearableType;

public abstract class Actor extends Entity {
	private static final long serialVersionUID = 5042625376381354713L;
	private final static float MOVEMENT_PRECISION_THRESHOLD = 1f;

	public enum Animation {
		IDLE, WALK, ATTACK, DIE, NONE, CAST, RECOVERING
	};

	public enum AttachPoint {
		HAND_L(WearableType.OFFHAND, BoneType.LEFT_HAND), HAND_R(WearableType.MAINHAND, BoneType.RIGHT_HAND), FEET(
				WearableType.FEET, null), CHEST(WearableType.CHEST, null), HEAD(WearableType.HEAD, null), RING1(
				WearableType.FINGER, null), RING2(WearableType.FINGER, null), HANDS(WearableType.HANDS, null);

		public WearableType allowed;
		public BoneType attachBone;
		private AttachPoint(WearableType allowed, BoneType attachBone) {
			this.allowed = allowed;
			this.attachBone = attachBone;
		}
	};

	protected Vector2f moveTowards;
	protected LinkedList<Vector2f> path;
	private float desiredAngle = 0.0f;
	private float prevDA = 0.0f;
	private Vector2f lastPos;
	protected Magic curMagic;
	protected Attack curAttack;

	protected Vector2f lookTowards;
	private Entity attackTarget;
	private float nextAttackTime;
	private float recoveringTime;

	private LinkedList<Magic> baseMagics;
	private LinkedList<Magic> curMagics;
	private LinkedList<DefensiveSkill> defensiveSkills;
	private LinkedList<OffensiveSkill> offensiveSkills;
	private LinkedList<MagicSkill> magicSkills;
	private LinkedList<StatsSkill> statsSkills;
	private HashMap<Skill, Float> tempSkills;

	private ArrayList<FloatingTextInfo> floatingTexts;
	private LinkedList<FloatingTextInfo> floatingTextQueue;
	private long lastFloatingTextTime;

	protected float speed;
	protected Stats baseStats;
	protected Stats totalStats;
	protected Stats curStats;
	protected int level;

	public long lastTimeSpellTarget;

	protected LinkedList<Item> items;

	protected HashMap<AttachPoint, WearableItem> wearingItems;
	protected HashMap<AttachPoint, Node> wearingNodes;

	private PathFinderTask pathingTask = null;

	public abstract void updateAI(float t);

	public Actor(Map map, DataNode model, Stats baseStats, int level) {

		super(map.getWorld(), model);
		this.map = map;
		this.baseStats = baseStats;
		this.level = level;
		this.addSubtype(Entity.EntitySubtype.ACTOR);

		this.lastPos = new Vector2f(this.getNode().getLocalTranslation().x, this.getNode().getLocalTranslation().y);

		this.lookTowards = new Vector2f(0, 0);

		this.items = new LinkedList<Item>();
		this.nextAttackTime = Game.getTimer().getTimeInSeconds();

		this.wearingItems = new HashMap<AttachPoint, WearableItem>();
		this.wearingNodes = new HashMap<AttachPoint, Node>();

		for (AttachPoint ap : AttachPoint.values()) {
			Node attachNode = new Node(ap.toString());
			attachNode.setLocalScale(new Vector3f(1, 1, 1).divideLocal(this.an.getNode().getLocalScale()));
			this.wearingNodes.put(ap, attachNode);
			this.an.attachToBone(ap.attachBone, attachNode);
		}

		this.totalStats = new Stats(this.baseStats);
		for (WearableItem w : this.wearingItems.values()) {
			this.totalStats.addStatsToThis(w.getStats());
		}
		this.curStats = new Stats(this.totalStats);
		this.offensiveSkills = new LinkedList<OffensiveSkill>();
		this.defensiveSkills = new LinkedList<DefensiveSkill>();
		this.statsSkills = new LinkedList<StatsSkill>();
		this.magicSkills = new LinkedList<MagicSkill>();
		this.curMagics = new LinkedList<Magic>();
		this.baseMagics = new LinkedList<Magic>();
		this.tempSkills = new HashMap<Skill, Float>();

		this.floatingTexts = new ArrayList<FloatingTextInfo>();
		this.floatingTextQueue = new LinkedList<FloatingTextInfo>();
		/*
		 * this.pathMarker1 = new PathMarker(w, 1); this.pathMarker2 = new
		 * PathMarker(w, 2); this.world.attachEntity(pathMarker1);
		 * this.world.attachEntity(pathMarker2);
		 */
	}

	public void setAnimation(Animation anim) {
		Profiler.start("Actor.setAnimation");

		switch (anim) {
			default :
			case IDLE :
				this.an.setRepeat(RepeatMode.LOOPED);
				this.an.doAnimation("Stand", 0.25f);
				break;
			case WALK :
				this.an.setRepeat(RepeatMode.LOOPED);
				this.an.doAnimation("Walk", 2.0f);
				break;
			case ATTACK :
				this.an.setRepeat(RepeatMode.NONE);
				this.an.doAnimation("Attack", 1.5f);
				break;
			case CAST :
				this.an.setRepeat(RepeatMode.NONE);
				if (this.an.hasAnimation("Cast")) {
					this.an.doAnimation("Cast");
				} else {
					this.an.doAnimation("Attack");
				}
				break;
			case RECOVERING :
				this.an.setRepeat(RepeatMode.NONE);
				this.an.doAnimation("Stand", 0.1f);
				break;
			case DIE :
				this.an.setRepeat(RepeatMode.NONE);
				this.an.doAnimation("Die");
		}

		Profiler.stop("Actor.setAnimation");
	}

	public Vector2f getMoveTowards() {
		return this.moveTowards;
	}

	public LinkedList<Vector2f> getPath() {
		return this.path;
	}

	public void setMoveTowards(Vector2f moveTowards) {
		this.moveTowards = moveTowards;
	}

	// We are certain on the objects type
	@SuppressWarnings("unchecked")
	@Override
	public void update(float t) {
		Profiler.start("Actor.update");

		super.update(t);

		this.isDead();

		if (this.curState == EntityState.DYING) {
			this.n.setLocalTranslation(this.n.getLocalTranslation().x, this.n.getLocalTranslation().y, this.n
					.getLocalTranslation().z
					- 1f * t);
			if (this.n.getLocalTranslation().z < -5 && !this.getClass().equals(Player.class)) {
				this.purge();
			}
		} else {
			updateAI(t);

			if (this.pathingTask != null) {
				if (this.pathingTask.isComplete()) {
					this.path = (LinkedList<Vector2f>) this.pathingTask.getResult();

					// Debug.dumpPathingMap(this.map.getWorld(), this);
					if (this.path != null && this.path.size() > 0) {
						this.moveTowards = this.path.getFirst();
						this.curState = EntityState.MOVE;
					} else {
						this.moveTowards = null;
					}
					this.pathingTask = null;
					// System.out.println(this.getIdent() + " " +
					// this.world.getCurFrame() + " Path calced.");
				}
			}

			switch (this.curState) {
				case RECOVERING :
					if (this.recoveringTime < Game.getTimer().getTimeInSeconds()) {
						this.setCurState(EntityState.IDLE);
					}
					break;
				case ATTACK_PRE :
				case ATTACK_APP :
				case ATTACK_POST :
				case CAST_PRE :
				case CAST_APP :
				case CAST_POST :
					updateAttackAndCast();
					break;
				case IDLE :
					this.setAnimation(Animation.IDLE);
			}

			updateAngle(t);

			for (Skill skill : this.tempSkills.keySet()) {
				if (this.tempSkills.get(skill) <= Game.getTimer().getTimeInSeconds()) {
					this.removeSkill(skill);
					this.tempSkills.remove(skill);
				}
			}
		}

		updateFloatingTexts(t);

		Profiler.stop("Actor.update");
		/*
		 * float newZ = -
		 * ((BoundingBox)this.an.getNode().getWorldBound()).zExtent / 2;
		 * this.n.setLocalTranslation(this.n.getLocalTranslation().x,
		 * this.n.getLocalTranslation().y, newZ);
		 */
	}

	public boolean isDead() {
		if (this.curStats.getTotalHp() <= 0) {
			this.removePathingFootprint();
			this.noPathing = true;
			this.setCurState(EntityState.DYING);
			this.setAnimation(Animation.DIE);
			if (this.room != null) {
				for (Item item : this.items) {
					this.room.addEntity(item);
					item.setPos(this.getPosition().x, this.getPosition().y);
					item.doSpawnFlip();
				}
			}
			return true;
		} else {
			return false;
		}
	}

	protected void updateWalk(float t) {
		Profiler.start("Actor.updateWalk");

		Vector2f curPos = new Vector2f(this.getNode().getLocalTranslation().x, this.getNode().getLocalTranslation().y);

		if (this.moveTowards == null && this.path != null) {
			if (this.path.size() == 0) {
				this.path = null;
				this.setCurState(EntityState.IDLE);
			} else {
				this.moveTowards = this.path.removeFirst();
			}
		}
		if (this.moveTowards != null) {
			Vector3f v = new Vector3f(moveTowards.x - curPos.x, moveTowards.y - curPos.y, 0);

			// TODO: Fix ugly hotfix
			if (this.path != null && this.path.size() > 0 && this.moveTowards.distanceSquared(curPos) < 3) {
				this.lookTowards = this.path.getFirst();
			} else {
				this.lookTowards = this.moveTowards;
			}

			if (v.length() < MOVEMENT_PRECISION_THRESHOLD * t * getMovementSpeed()) {
				this.moveTowards = null;
			} else {
				Vector3f oldPos = this.getNode().getLocalTranslation();

				v.normalizeLocal();
				v.multLocal(t * getMovementSpeed());

				if (!this.walkablePathSpot(v.x + this.getNode().getLocalTranslation().x, v.y
						+ this.getNode().getLocalTranslation().y)) {
					this.setPos(oldPos.x, oldPos.y);
					this.moveTowards = null;
					System.out.println("NOT WALKABLE PATH SPOT");
				} else {
					this.setPos(v.x + this.getNode().getLocalTranslation().x, v.y
							+ this.getNode().getLocalTranslation().y);
				}
			}
		}

		if (FastMath.abs(lastPos.subtract(this.getPosition()).length()) > 0.3f * t * getMovementSpeed()) {
			this.setAnimation(Animation.WALK);
		} else {
			this.setAnimation(Animation.IDLE);
		}

		this.lastPos = curPos;

		Profiler.stop("Actor.updateWalk");
	}

	public void updateAngle(float t) {
		Profiler.start("Actor.updateAngle");

		float da = FastMath.atan2(this.lookTowards.y - this.getPosition().y, this.lookTowards.x - this.getPosition().x)
				+ FastMath.HALF_PI;
		if (FastMath.abs(da - this.prevDA) < FastMath.HALF_PI) {
			this.desiredAngle = da;
		} else {
			this.prevDA = da;
		}

		float curA = this.getNode().getLocalRotation().toAngleAxis(new Vector3f(0, 0, 1f));

		if (FastMath.abs(this.desiredAngle - curA) > 0.01f) {
			Quaternion rot = new Quaternion();
			rot.fromAngleAxis(this.desiredAngle, new Vector3f(0, 0, 1f));
			// if(this.inherentRotation != null)
			// rot.addLocal(this.inherentRotation);
			this.getNode().setLocalRotation(rot);
		}

		Profiler.stop("Actor.updateAngle");
	}

	public void updateAttackAndCast() {
		Profiler.start("Actor.updateAttack");

		switch (this.curState) {
			case ATTACK_PRE :
				this.lookTowards = this.attackTarget.getPosition();
				if (this.an.animPercentage() >= 0.5f) {
					this.setCurState(EntityState.ATTACK_APP);
				}
				break;

			case ATTACK_APP :
				this.lookTowards = this.attackTarget.getPosition();
				if (attackTarget.isSubtype(EntitySubtype.ACTOR)) {
					this.curAttack.performAttack((Actor) this.attackTarget);
				} else if (this.attackTarget.isSubtype(EntitySubtype.DESTRUCTIBLE)) {
					Doodad d = ((Doodad) this.attackTarget);
					d.setCurState(EntityState.DYING);
					d.interactReact(this);
				}
				this.setCurState(EntityState.ATTACK_POST);
				break;

			case ATTACK_POST :
				if (this.an.animPercentage() >= 0.99f) {
					this.setAnimation(Animation.IDLE);
					this.setCurState(EntityState.IDLE);
				}
				break;
			case CAST_PRE :
				this.lookTowards = this.attackTarget.getPosition();
				if (this.an.animPercentage() >= 0.5f) {
					this.setCurState(EntityState.CAST_APP);
				}
				break;

			case CAST_APP :
				this.lookTowards = this.attackTarget.getPosition();
				if (this.curMagic.isValidTarget(this, this.attackTarget)) {
					if (this.getClass().equals(Player.class)
							&& (this.curMagic.getMagicType().equals(MagicType.HEAL) || this.curMagic.getMagicType()
									.equals(MagicType.BUFF))) {
						this.curMagic.cast(this, this);
					} else if (Actor.class.isAssignableFrom(this.attackTarget.getClass())) {
						this.curMagic.cast(this, (Actor) this.attackTarget);
					} else {
						this.curMagic.cast(this, this);
					}
				}
				this.setCurState(EntityState.CAST_POST);
				break;

			case CAST_POST :
				if (this.an.animPercentage() >= 0.99f) {
					this.setAnimation(Animation.IDLE);
					this.setCurState(EntityState.IDLE);
				}
				break;

			default :
				break;
		}

		Profiler.stop("Actor.updateAttack");
	}

	public boolean isInAttackRange(Entity target) {
		if (!this.getAttackRangeType().equals(RangeType.MELEE)) {
			// TODO: fix line-of-sight check for ranged attackers
			return (this.getPosition().distance(target.getPosition()) <= this.getAttackRange());
		} else {
			if (NPC.class.isAssignableFrom(target.getClass())) {
				NPC n = (NPC) target;
				return (this.getPosition().distance(target.getPosition()) <= this.getAttackRange() + n.getSize());
			}
			if (Doodad.class.isAssignableFrom(target.getClass())) {
				Doodad d = (Doodad) target;
				return (this.getPosition().distance(target.getPosition()) <= this.getAttackRange() + d.getSize());
			} else {
				return (this.getPosition().distance(target.getPosition()) <= this.getAttackRange());
			}
		}
	}

	public boolean isInLineForAttack(Entity target) {
		return this.getMap().lineOfAttack(this.getPosition(), target.getPosition());
	}

	public void attack(Entity target) {
		Profiler.start("Actor.attack");
		if (isInAttackRange(target) && (Game.getTimer().getTimeInSeconds() > this.nextAttackTime)) {
			this.path = null;
			this.moveTowards = null;
			this.setAnimation(Animation.ATTACK);
			this.setCurState(EntityState.ATTACK_PRE);
			this.attackTarget = target;
			this.nextAttackTime = Game.getTimer().getTimeInSeconds() + this.getAttackCooldown();
		}

		Profiler.stop("Actor.attack");
	}

	public void castMagic(Entity target) {
		Profiler.start("Actor.castSpell");
		if (Game.getTimer().getTimeInSeconds() > this.nextAttackTime) {
			if (this.curMagic != null && this.curMagic.isValidTarget(this, target)) {
				this.path = null;
				this.moveTowards = null;
				this.setAnimation(Animation.CAST);
				this.setCurState(EntityState.CAST_PRE);
				this.attackTarget = target;
				this.nextAttackTime = Game.getTimer().getTimeInSeconds() + this.getCastCooldown();
			}
		}

		Profiler.stop("Actor.castSpell");
	}

	public void createPathTo(Vector2f destWorld) {
		if (this.pathingTask == null) {
			Profiler.start("Actor.createPathTo");

			// System.out.println(this.getIdent() + " " +
			// this.world.getCurFrame() + " Path requested.");
			this.pathingTask = new PathFinderTask(this.map, this.getPosition(), destWorld, this);
			this.world.getTaskScheduler().addTask(this.pathingTask, this.isSubtype(EntitySubtype.PLAYER));

			Profiler.stop("Actor.createPathTo");
		}
	}

	public void updateStats() {
		this.totalStats = new Stats(this.baseStats);
		for (WearableItem w : this.wearingItems.values()) {
			this.totalStats.addStatsToThis(w.getStats());
		}
		for (StatsSkill s : this.statsSkills) {
			s.apply(this);
		}
		int curHp = this.curStats.getHp();
		int curMana = this.curStats.getMana();
		this.curStats = new Stats(this.totalStats);
		this.curStats.setHp(curHp);
		this.curStats.setMana(curMana);
		while (this.curStats.getTotalHp() < 1) {
			this.curStats.addHp(1);
		}
		if (this.curStats.getHp() > this.totalStats.getHp()) {
			this.curStats.setHp(this.totalStats.getHp());
		}
		while (this.curStats.getTotalMana() < 1) {
			this.curStats.addMana(1);
		}
		if (this.curStats.getMana() > this.totalStats.getMana()) {
			this.curStats.setMana(this.totalStats.getMana());
		}
		if (Game.getInstance().getHUD() != null) {
			Game.getInstance().getHUD().updateHpAndMana();
		}
		this.updateAttack();
	}

	public Item wearItem(Item i, AttachPoint ap) {
		if (WearableItem.class.isAssignableFrom(i.getClass())) {
			WearableItem item = (WearableItem) i;

			if (!ap.allowed.equals(item.getWearableType())) {
				return item;
			}

			Item result = null;
			if (this.wearingItems.get(ap) != null) {
				result = this.wearingItems.get(ap);
			}

			item.setNoPicking(true);

			item.getNode().removeFromParent();
			this.unlock();

			this.wearingItems.put(ap, item);
			this.wearingNodes.get(ap).detachAllChildren();
			this.wearingNodes.get(ap).attachChild(item.getNode());

			item.getNode().setLocalTranslation(0, 0, 0);
			updateStats();
			return result;
		} else {
			return null;
		}
	}

	public Item unwearItem(AttachPoint ap) {
		Item result = null;

		if (this.wearingItems.containsKey(ap) && this.wearingItems.get(ap) != null) {
			result = this.wearingItems.get(ap);
			this.wearingItems.remove(ap);
			this.wearingNodes.get(ap).detachAllChildren();
		}
		updateStats();
		return result;
	}

	public Stats getBaseStats() {
		return this.baseStats;
	}

	public void setBaseStats(Stats baseStats) {
		this.baseStats = baseStats;
	}

	public float getRotation() {
		return this.desiredAngle;
	}

	public boolean giveItem(Item i) {
		this.items.add(i);
		return true;
	}

	public void giveAllItems(LinkedList<Item> linkedList) {
		this.items.addAll(linkedList);
	}

	public Stats getTotalStats() {
		return this.totalStats;
	}

	public Stats getCurStats() {
		return this.curStats;
	}

	public float getNextAttackTime() {
		return this.nextAttackTime;
	}

	public void setNextAttackTime(float nextAttackTime) {
		this.nextAttackTime = nextAttackTime;
	}

	public float getAttackCooldown() {
		WearableItem w = this.wearingItems.get(AttachPoint.HAND_R);
		if (w == null) {
			return (2.0f * 100 / (this.getCurStats().getBonusAttackSpeed() + 100));
		} else {
			Weapon weapon = (Weapon) w;
			return (weapon.getCooldown() * 100 / (this.getCurStats().getBonusAttackSpeed() + 100));
		}
	}

	public float getCastCooldown() {
		return (2.0f * 100 / (this.getCurStats().getBonusAttackSpeed() + 100));
	}

	public Weapon getWeapon() {
		WearableItem w = this.wearingItems.get(AttachPoint.HAND_R);
		if (w != null) {
			return (Weapon) w;
		} else {
			return null;
		}
	}

	public LinkedList<DefensiveSkill> getDefensiveSkills() {
		return defensiveSkills;
	}

	public LinkedList<OffensiveSkill> getOffensiveSkills() {
		return offensiveSkills;
	}

	public LinkedList<MagicSkill> getMagicSkills() {
		return magicSkills;
	}

	public LinkedList<StatsSkill> getStatsSkills() {
		return statsSkills;
	}

	public Item getWearItem(AttachPoint ap) {
		return this.wearingItems.get(ap);
	}

	public int getPhysicalRndDmg() {
		return this.world.getRndInt(this.getPhysicalMinDmg(), this.getPhysicalMaxDmg());
	}

	public int getPhysicalMinDmg() {
		Weapon w = this.getWeapon();
		int bonus = 100;
		if (this.getAttackRangeType().equals(RangeType.MELEE)) {
			bonus += this.getCurStats().getMeleeDamageBonus();
		} else {
			bonus += this.getCurStats().getRangedDamageBonus();
		}
		if (w != null) {
			return w.getMinDmg() * (bonus) / 100;
		} else {
			return getMinBaseDmg() * (bonus) / 100;
		}
	}

	public int getPhysicalMaxDmg() {
		Weapon w = this.getWeapon();
		int bonus = 100;
		if (this.getAttackRangeType().equals(RangeType.MELEE)) {
			bonus += this.getCurStats().getMeleeDamageBonus();
		} else {
			bonus += this.getCurStats().getRangedDamageBonus();
		}
		if (w != null) {
			return w.getMaxDmg() * (bonus) / 100;
		} else {
			return getMaxBaseDmg() * (bonus) / 100;
		}
	}

	protected int getMinBaseDmg() {
		return 1;
	}

	protected int getMaxBaseDmg() {
		return 2;
	}

	public float getAttackRange() {
		Weapon w = this.getWeapon();
		if (w != null) {
			if (w.getRangeType().equals(RangeType.MELEE)) {
				return 4.0f;
			} else {
				return 100.0f;
			}
		}
		return 2.0f;
	}

	public RangeType getAttackRangeType() {
		Weapon w = this.getWeapon();
		if (w != null) {
			return w.getRangeType();
		} else {
			return RangeType.MELEE;
		}
	}

	public int getArmor() {
		int armor = 0;
		for (WearableItem w : this.wearingItems.values()) {
			if (!w.getWearableType().equals(WearableType.MAINHAND) && !w.getWearableType().equals(WearableType.HANDS)) {
				Armor a = (Armor) w;
				armor += a.getArmor();
			}
		}
		armor = armor * (this.curStats.getBonusArmor() + 100) / 100;
		return armor;
	}

	public abstract int getXpValue();

	public LinkedList<Skill> getAllSkills() {
		LinkedList<Skill> all = new LinkedList<Skill>();
		all.addAll(this.statsSkills);
		all.addAll(this.defensiveSkills);
		all.addAll(this.offensiveSkills);
		all.addAll(this.magicSkills);
		return all;
	}

	public void addMagicSkill(MagicSkill s) {
		removeMagicSkill(s);
		this.magicSkills.add(s);
		ConsoleLog.addLine("Skill added: " + s.getName() + " " + s.getLevel());
		updateMagics();
	}

	private void updateMagics() {
		this.curMagics.clear();
		for (Magic base : this.baseMagics) {
			Magic cur = new Magic(base);
			for (MagicSkill s : this.magicSkills) {
				s.apply(cur);
			}
			this.curMagics.add(cur);
		}
	}

	public void addStatsSkill(StatsSkill s) {
		removeStatsSkill(s);
		this.statsSkills.add(s);
		this.updateStats();
		ConsoleLog.addLine("Skill added: " + s.getName() + " " + s.getLevel());
	}

	public void addDefensiveSkill(DefensiveSkill s) {
		removeDefensiveSkill(s);
		this.defensiveSkills.add(s);
		ConsoleLog.addLine("Skill added: " + s.getName() + " " + s.getLevel());
	}

	public void addOffensiveSkill(OffensiveSkill s) {
		removeOffensiveSkill(s);
		this.offensiveSkills.add(s);
		ConsoleLog.addLine("Skill added: " + s.getName() + " " + s.getLevel());
	}

	public void addSkill(Skill s) {
		if (s.getClass().equals(OffensiveSkill.class)) {
			this.addOffensiveSkill((OffensiveSkill) s);
		} else if (s.getClass().equals(DefensiveSkill.class)) {
			this.addDefensiveSkill((DefensiveSkill) s);
		} else if (s.getClass().equals(StatsSkill.class)) {
			this.addStatsSkill((StatsSkill) s);
			this.updateStats();
		} else if (s.getClass().equals(MagicSkill.class)) {
			this.addMagicSkill((MagicSkill) s);
		}
	}

	protected void removeMagicSkill(MagicSkill s) {
		for (Skill o : SkillTree.getAllLevelsOfSkill(s)) {
			this.magicSkills.remove(o);
		}
		this.magicSkills.remove(s);
		updateMagics();
	}

	protected void removeStatsSkill(StatsSkill s) {
		for (Skill o : SkillTree.getAllLevelsOfSkill(s)) {
			this.statsSkills.remove(o);
		}
		this.statsSkills.remove(s);
		this.updateStats();
	}

	protected void removeDefensiveSkill(DefensiveSkill s) {
		for (Skill o : SkillTree.getAllLevelsOfSkill(s)) {
			this.defensiveSkills.remove(o);
		}
		this.defensiveSkills.remove(s);
	}

	protected void removeOffensiveSkill(OffensiveSkill s) {
		for (Skill o : SkillTree.getAllLevelsOfSkill(s)) {
			this.offensiveSkills.remove(o);
		}
		this.offensiveSkills.remove(s);
	}

	protected void removeSkill(Skill s) {
		if (s.getClass().equals(OffensiveSkill.class)) {
			this.removeOffensiveSkill((OffensiveSkill) s);
		} else if (s.getClass().equals(DefensiveSkill.class)) {
			this.removeDefensiveSkill((DefensiveSkill) s);
		} else if (s.getClass().equals(StatsSkill.class)) {
			this.removeStatsSkill((StatsSkill) s);
		} else if (s.getClass().equals(MagicSkill.class)) {
			this.removeMagicSkill((MagicSkill) s);
		}
	}

	public void setPath(LinkedList<Vector2f> p) {
		this.path = p;
	}

	public float getMovementSpeed() {
		return (this.getCurStats().getBonusMovementSpeed() + 100) * this.speed / 100;
	}

	public LinkedList<Magic> getMagics() {
		return this.curMagics;
	}

	public void addTempSkill(Skill s, float duration) {
		float expTime = duration + Game.getTimer().getTimeInSeconds();
		for (Skill o : SkillTree.getAllLevelsOfSkill(s)) {
			removeSkill(o);
			this.tempSkills.remove(o);
		}
		addSkill(s);
		this.tempSkills.put(s, expTime);
	}

	public void addMagic(Magic magic) {
		this.baseMagics.add(magic);
		Magic newM = new Magic(magic);
		this.curMagics.add(newM);
		for (MagicSkill s : this.magicSkills) {
			s.apply(newM);
		}
		this.curMagic = newM;
	}

	public void removeMagic(Magic magic) {
		this.curMagics.remove(magic);
		this.baseMagics.remove(magic);
		if (magic.equals(curMagic)) {
			this.curMagic = null;
		}
	}

	public Attack getCurAttack() {
		return curAttack;
	}

	public void updateAttack() {
		this.curAttack = new Attack(this);
	}

	public float getRecoveryTime() {
		return 0.2f;
	}

	public void setRecovering() {
		this.recoveringTime = Game.getTimer().getTimeInSeconds() + getRecoveryTime();
		this.setCurState(EntityState.RECOVERING);
		this.setAnimation(Animation.RECOVERING);
	}

	public Magic getCurMagic() {
		return this.curMagic;
	}

	@Override
	public int getLevel() {
		return this.level;
	}

	public void addFloatingTextQueued(String text, Color bg, Color fg, float speed) {
		this.floatingTextQueue.offer(new FloatingTextInfo(text, bg, fg, speed));
	}

	public void addFloatingText(String text, Color bg, Color fg, float speed) {
		FloatingTextInfo info = new FloatingTextInfo(text, bg, fg, speed);
		addFloatingText(info);
	}

	public void addFloatingText(FloatingTextInfo info) {
		TextLabel2D label = new TextLabel2D(info.text);
		label.setBackground(info.bg);
		label.setForeground(info.fg);
		BillboardNode bNode = label.getBillboard(0.5f);
		bNode.getLocalTranslation().z += 8;
		bNode.setLocalScale(3f);
		n.attachChild(bNode);
		info.bNode = bNode;
		this.floatingTexts.add(info);
	}

	private void updateFloatingTexts(float t) {
		if (this.floatingTextQueue.size() > 0 && System.currentTimeMillis() > this.lastFloatingTextTime + 250) {
			this.lastFloatingTextTime = System.currentTimeMillis();
			FloatingTextInfo info = this.floatingTextQueue.poll();
			addFloatingText(info);
		}

		ArrayList<BillboardNode> removeList = new ArrayList<BillboardNode>();

		for (FloatingTextInfo info : this.floatingTexts) {
			info.bNode.getLocalTranslation().z += info.speed * t;
			if (info.bNode.getLocalTranslation().z > 20f) {
				info.bNode.removeFromParent();
				removeList.add(info.bNode);
			}
		}

		this.floatingTexts.removeAll(removeList);
	}

	public void clearFloatingTexts() {
		this.floatingTextQueue.clear();
	}

	private class FloatingTextInfo {
		public String text;
		public Color bg;
		public Color fg;
		public BillboardNode bNode;
		public float speed;

		public FloatingTextInfo(String text, Color bg, Color fg, float speed) {
			this.text = text;
			this.bg = bg;
			this.fg = fg;
			this.speed = speed;
		}
	}

	public void markAsSpellTarget() {
		this.lastTimeSpellTarget = System.currentTimeMillis();
	}

	public long timeSinceSpellTargeted() {
		return System.currentTimeMillis() - this.lastTimeSpellTarget;
	}
}
