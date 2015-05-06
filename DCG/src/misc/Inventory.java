/**
 * The Inventory class is used to store items and potions. The players inventory is
 * created by the newBag static method. The class is the datastructure 
 * behind the InventoryWindow in the HUD.
 */

package misc;

import entities.items.Gold;
import entities.items.Item;

public class Inventory {
	public enum BELTSPOT {
		HPOT, HPOT_LARGE, MPOT, MPOT_LARGE
	};

	public static final int BAG_WIDTH = 10;
	public static final int BAG_HEIGHT = 4;
	public static final int STASH_WIDTH = 10;
	public static final int STASH_HEIGHT = 10;

	private Item[][] bagSpots;
	private int[] beltSpots;
	private int gold;
	private boolean wasUpdated = false;

	public Inventory(int width, int height) {
		this.bagSpots = new Item[width][height];
		this.beltSpots = new int[4];
		this.gold = 0;
	}

	public static Inventory newBag() {
		return new Inventory(BAG_WIDTH, BAG_HEIGHT);
	}

	public static Inventory newStash() {
		return new Inventory(STASH_WIDTH, STASH_HEIGHT);
	}

	public Item get(int x, int y) {
		if (x >= 0 && x < BAG_WIDTH && y >= 0 && y < BAG_HEIGHT) {
			return this.bagSpots[x][y];
		} else {
			return null;
		}
	}

	public boolean bagItem(Item item) {
		if (item.getName().equals("item_hpot")) {
			beltSpots[BELTSPOT.HPOT.ordinal()] += 1;
			return true;
		} else if (item.getName().equals("item_hpot_large")) {
			beltSpots[BELTSPOT.HPOT_LARGE.ordinal()] += 1;
			return true;
		} else if (item.getName().equals("item_mpot")) {
			beltSpots[BELTSPOT.MPOT.ordinal()] += 1;
			return true;
		} else if (item.getName().equals("item_mpot_large")) {
			beltSpots[BELTSPOT.MPOT_LARGE.ordinal()] += 1;
			return true;
		} else if (item.getClass().equals(Gold.class)) {
			if (addGold(((Gold) item).getQuantity())) {
				return true;
			} else {
				// not room for gold = integer overflow or gold has negative
				// quantity
				return false;
			}
		}

		for (int i = 0; i < BAG_WIDTH; i++) {
			for (int j = 0; j < BAG_HEIGHT; j++) {
				if (bagItem(item, i, j)) {
					System.out.println("bagged to slot " + i + ", " + j);
					return true;
				}
			}
		}

		return false;
	}

	public boolean bagItem(Item item, int x, int y) {

		// Check
		for (int i = 0; i < item.getBagW(); i++) {
			for (int j = 0; j < item.getBagH(); j++) {
				if (x + i < BAG_WIDTH && y + j < BAG_HEIGHT) {
					if (this.bagSpots[x + i][y + j] != null) {
						return false;
					}
				} else {
					return false;
				}
			}
		}

		// Place
		for (int i = 0; i < item.getBagW(); i++) {
			for (int j = 0; j < item.getBagH(); j++) {
				this.bagSpots[x + i][y + j] = item;
			}
		}

		this.wasUpdated = true;

		return true;
	}

	public void clearWasUpdated() {
		this.wasUpdated = false;
	}

	public boolean wasUpdated() {
		return wasUpdated;
	}

	public void removeItem(Item pickedItem) {
		for (int i = 0; i < BAG_WIDTH; i++) {
			for (int j = 0; j < BAG_HEIGHT; j++) {
				if (pickedItem.equals(this.bagSpots[i][j])) {
					this.bagSpots[i][j] = null;
				}
			}
		}
		// this.wasUpdated = true;
	}

	public int getBeltSpotCount(BELTSPOT spot) {
		return this.beltSpots[spot.ordinal()];
	}

	public boolean decreaseBeltSpotCount(BELTSPOT spot) {
		if (this.beltSpots[spot.ordinal()] > 0) {
			this.beltSpots[spot.ordinal()] -= 1;
			return true;
		} else {
			return false;
		}
	}

	public boolean addGold(int amount) {
		if (this.gold + amount < this.gold) {
			return false;
		}
		this.gold += amount;
		this.wasUpdated = true;
		return true;
	}

	public boolean subtractGold(int amount) {
		if (this.gold < amount) {
			return false;
		}
		this.gold -= amount;
		this.wasUpdated = true;
		return true;
	}

	public boolean canAfford(int amount) {
		return this.gold >= amount;
	}

	public int getGold() {
		return this.gold;
	}
}
