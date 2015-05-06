/**
 * The abstract Item class is used for all objects, which can be picked up b the player.
 * It contains information on type, size and quility of the item.
 * 
 * This class contains some very important methods used for items generation. These are 
 * generateFromNode() and generateFromLootTable().
 */

package entities.items;

import infostore.DataManager;
import infostore.DataNode;
import infostore.DataManager.DataType;

import java.util.LinkedList;

import map.Map;
import misc.Stats;

import com.jme.math.FastMath;
import com.jme.renderer.ColorRGBA;

import engine.Game;
import engine.World;
import entities.Entity;
import entities.actors.Player;
import entities.items.WearableItem.WearableType;

public abstract class Item extends Entity {
	private static final long serialVersionUID = 1890079766447739263L;

	public enum ItemType {
		CONSUMABLE, WEARABLE, GOLD
	};
	public enum Quality {
		POOR, COMMON, MAGIC, RARE, EPIC, UNIQUE, QUEST
	};

	private ItemType type;
	private int monetaryValue;
	private int level;

	public int getBagW() {
		return bagW;
	}

	public int getBagH() {
		return bagH;
	}

	public String getBagImage() {
		return bagImage;
	}

	private Quality quality;
	private int bagW = 1;
	private int bagH = 1;
	private String bagImage = "";
	private boolean isInShop;

	public Item(World world, ItemType type, int monetaryValue, String name, DataNode model, Quality quality, DataNode n) {
		super(world, model);
		this.addSubtype(EntitySubtype.ITEM);
		this.type = type;
		this.monetaryValue = monetaryValue;
		this.caption = name;
		this.noPathing = true;
		this.quality = quality;
		this.name = n.getProp("name");

		if (n != null) {
			this.bagW = n.getChild("bag").getPropI("w");
			this.bagH = n.getChild("bag").getPropI("h");
			this.bagImage = n.getChild("bag").getProp("image");
		}
	}

	public Item(World world, DataNode model) {
		super(world, model);
		this.noPathing = true;
	}

	public void interactReact(Entity interactor) {
		if (interactor.isSubtype(EntitySubtype.PLAYER)) {
			if (((Player) interactor).giveItem(this)) {
				this.n.removeFromParent();
				this.setCurState(EntityState.PURGED);
				System.out.println("bagged item " + this);
			} else {
				this.doSpawnFlip();
				System.out.println("Could not bag item " + this);
			}
		}
	}

	public ItemType getType() {
		return type;
	}

	public void update(float t) {
		super.update(t);

		if (this.n.getLocalTranslation().z > 0) {
			this.velocity.z -= 0.05f;
		}
		if (this.n.getLocalTranslation().z < 0) {
			this.n.getLocalTranslation().z = 0;
			this.velocity.z = 0;
			this.velocity.y = 0;
			this.velocity.x = 0;
		}
	}

	public void setType(ItemType type) {
		this.type = type;
	}

	public int getMonetaryValue() {
		if (this.isInShop) {
			return monetaryValue * 2;
		} else {
			return monetaryValue;
		}
	}

	public void setIsInShop(boolean b) {
		this.isInShop = b;
	}

	public void setMonetaryValue(int monetaryValue) {
		this.monetaryValue = monetaryValue;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getLevel() {
		return level;
	}

	public Quality getItemQuality() {
		return this.quality;
	}

	// public static Quality getRndQuality(int bonusChance, World w,
	// HashMap<Integer, Quality> chanceOfQuality) {
	// int roll = w.getRndInt(0, 100);
	// for (Integer i : chanceOfQuality.keySet()) {
	// if (roll <= i + bonusChance) {
	// return chanceOfQuality.get(i);
	// }
	// }
	// //bad input
	// return Quality.POOR;
	// }

	public static Quality getRndQuality(int bonusChance, World w) {
		int roll = w.getRndInt(0, 99);
		if (roll <= 0 + bonusChance) {
			return Quality.EPIC;
		} else if (roll < 10 + bonusChance) {
			return Quality.RARE;
		} else if (roll < 30 + bonusChance) {
			return Quality.MAGIC;
		} else if (roll < 60 + bonusChance) {
			return Quality.COMMON;
		} else {
			return Quality.POOR;
		}
	}

	public static LinkedList<Item> generateFromLootTable(DataNode tableData, int bonusQualityChance, Map map) {
		LinkedList<Item> result = new LinkedList<Item>();
		LinkedList<DataNode> itemsInTable = tableData.getList("entry");

		for (DataNode tableEntry : itemsInTable) {
			if (map.getWorld().roll100(tableEntry.getPropI("chance"))) {
				int level = map.getLevel();
				int roll = map.getWorld().getRndInt(0, 100);

				if (roll < 30) {
					level++;
				} else if (roll < 50 && level > 1) {
					level--;
				}

				result.add(generateFromNode(DataManager.findByNameAndType(DataType.ITEM, tableEntry.getProp("item")),
						level, bonusQualityChance, map.getWorld()));
			}
		}
		return result;
	}

	public static Item generateFromNode(DataNode n, int level, int bonusQualityChance, World world) {
		return generateFromNode(n, level, Item.getRndQuality(bonusQualityChance, world), world);
	}

	public static Item generateFromNode(DataNode n, int level, Quality qual, World world) {
		Item item = null;
		ClassLoader loader = Item.class.getClassLoader();

		try {
			item = (Item) loader.loadClass("entities.items." + n.getProp("class")).getConstructor(
					new Class[]{World.class, DataNode.class, int.class}).newInstance(
					new Object[]{world, n, (int) (level)});
		} catch (Exception e) {
			System.out.println("Item.generateFromNode: Error loading class " + n.getProp("class"));
			e.printStackTrace();
			System.exit(-1);
		}

		item.quality = qual;
		item.monetaryValue = (int) (n.getPropI("price") * (0.25f * level) * Stats.qualityMultiplier(qual));
		item.setCaption(n.getProp("caption"));
		item.level = level;
		item.bagW = n.getChild("bag").getPropI("w");
		item.bagH = n.getChild("bag").getPropI("h");
		item.bagImage = n.getChild("bag").getProp("image");

		if (WearableItem.class.isAssignableFrom(item.getClass())) {
			WearableItem witem = (WearableItem) item;
			witem.setStats(new Stats());

			LinkedList<DataNode> prefixes = DataManager.findAllByTypeWhereContains(DataType.PREFIX, "quality", qual
					.name().toLowerCase());

			LinkedList<DataNode> remove = new LinkedList<DataNode>();
			for (DataNode pre : prefixes) {
				boolean keep = false;
				for (String string : pre.getProp("slot").split(",")) {
					if (witem.getWearableType().equals(WearableType.valueOf(string.trim().toUpperCase()))) {
						keep = true;
					}
				}
				if (!keep) {
					remove.add(pre);
				}
			}
			prefixes.removeAll(remove);

			if (prefixes.size() > 0) {

				int element = world.getRndInt(0, prefixes.size() - 1);

				DataNode prefix = prefixes.get(element);
				witem.setCaption(prefix.getProp("caption") + " " + witem.getCaption());

				Stats stats = new Stats(prefix.getChild("stats"));
				Stats scale = new Stats(prefix.getChild("stats").getChild("scale"), level);
				stats.addStatsToThis(scale);
				stats.applyQulity(qual);

				witem.getStats().addStatsToThis(stats);
			}

			LinkedList<DataNode> surfixes = DataManager.findAllByTypeWhereContains(DataType.SURFIX, "quality", qual
					.name().toLowerCase());

			remove.clear();
			for (DataNode sur : surfixes) {
				boolean keep = false;
				for (String string : sur.getProp("slot").split(",")) {
					if (witem.getWearableType().equals(WearableType.valueOf(string.trim().toUpperCase()))) {
						keep = true;
					}
				}
				if (!keep) {
					remove.add(sur);
				}
			}
			surfixes.removeAll(remove);

			if (surfixes.size() > 0) {
				int element = world.getRndInt(0, surfixes.size() - 1);

				DataNode surfix = surfixes.get(element);
				witem.setCaption(witem.getCaption() + " " + surfix.getProp("caption"));

				Stats stats = new Stats(surfix.getChild("stats"));
				Stats scale = new Stats(surfix.getChild("stats").getChild("scale"), level);
				stats.addStatsToThis(scale);
				stats.applyQulity(qual);

				witem.getStats().addStatsToThis(stats);
			}
		} else if (ConsumableItem.class.equals(item.getClass())) {
			if (item.getCaption().equals("Book")) {
				LinkedList<DataNode> bookfixes = DataManager.findAllByType(DataType.BOOKFIX);
				int element = world.getRndInt(0, bookfixes.size() - 1);
				ConsumableItem citem = (ConsumableItem) item;
				citem.addConsumeEffect(bookfixes.get(element));
				item.quality = Quality.COMMON;
			}
		}

		return item;
	}

	public String getInfoString() {
		return "(Level " + this.getLevel() + " " + this.getItemQuality().toString().toLowerCase() + ")\n" + "Value: "
				+ this.getMonetaryValue() + " Gold\n";
	}

	public ColorRGBA getQualityColor() {
		switch (this.quality) {
			case COMMON :
				return ColorRGBA.white;
			case POOR :
				return ColorRGBA.gray;
			case MAGIC :
				return ColorRGBA.blue;
			case RARE :
				return ColorRGBA.red;
			case EPIC :
				return ColorRGBA.orange;
			case UNIQUE :
				return ColorRGBA.brown;
			case QUEST :
				return ColorRGBA.magenta;
		}
		return ColorRGBA.white;
	}

	public void doSpawnFlip() {
		this.unlock();
		this.setCurState(EntityState.IDLE);
		this.velocity.z = 0.01f / Game.tpf;
		float a = this.world.getRndFloat(0, FastMath.TWO_PI);
		this.velocity.x = FastMath.cos(a) / 60f;
		this.velocity.y = FastMath.sin(a) / 60f;
	}
}
