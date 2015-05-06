/**
 * This class represents the inventory window. Together with the inventory class, this manages
 * the contents of the inventory and shows it to the players.
 * 
 * It extends AbstractElement and implements GuiMouseListener.
 */

package engine.hud;

import engine.Game;
import entities.actors.Player;
import entities.actors.Actor.AttachPoint;
import entities.items.ConsumableItem;
import entities.items.Item;
import gui.MouseEvent;
import gui.elements.AbstractElement;
import gui.elements.Button;
import gui.elements.Image;
import gui.elements.Pane;
import gui.elements.TextArea;
import gui.implementables.GuiMouseListener;

import java.util.HashMap;
import java.util.LinkedList;

import misc.Inventory;

import com.jmex.angelfont.BitmapFont.Align;

public class InventoryWindow extends AbstractElement implements GuiMouseListener {

	private Player player;
	private boolean needsRebuild = false;
	private ItemInfoWindow wndInfo;

	private Item pickedItem;
	private Image pickedItemImg;

	private Image dropzone;

	public static final float bagX = 1.6f;
	public static final float bagY = 10f;
	public static final float bagSlotW = 4.7f;
	public static final float bagSlotH = 6f;

	public boolean hoveringItem = false;

	Pane paneMainHand, paneOffHand, paneHead, paneChest, paneRing1, paneRing2, paneFeet, paneHands;
	Pane bg;
	Pane paneGold;
	Image imgGold;
	TextArea txtGold;
	Pane bag;
	private Button btnClose;

	public InventoryWindow(Player player) {
		super("wndInventory", 50, 10, 48, 100);
		this.player = player;
		this.setMouseListener(this);
	}

	@Override
	public void rebuild() {
		this.removeAllElements();

		bg = new Pane("paneInventoryBG", 1, 1, 48, 88);
		this.addElement(bg);

		//

		// Main hand
		paneMainHand = new Pane("paneMainHand", 2, 55, 14, 25);
		paneMainHand.setTexs("border2.png", "pane2.png");
		paneMainHand.setBorderWidth(2);
		this.addElement(paneMainHand);

		Item itemMH = this.player.getWearItem(AttachPoint.HAND_R);
		if (itemMH != null) {
			Image img = new Image("imgInventoryItemMainHand", itemMH.getBagImage(), 5, 61, bagSlotW * itemMH.getBagW(),
					bagSlotH * itemMH.getBagH());
			img.setAttachment(itemMH);
			this.addElement(img);
		}

		// Off hand
		paneOffHand = new Pane("paneOffHand", 34, 55, 14, 25);
		paneOffHand.setTexs("border2.png", "pane2.png");
		paneOffHand.setBorderWidth(2);
		this.addElement(paneOffHand);

		Item itemOH = this.player.getWearItem(AttachPoint.HAND_L);
		if (itemOH != null) {
			Image img = new Image("imgInventoryItemOffHand", itemOH.getBagImage(), 37, 61, bagSlotW * itemOH.getBagW(),
					bagSlotH * itemOH.getBagH());
			img.setAttachment(itemOH);
			this.addElement(img);
		}

		// Hands
		paneHands = new Pane("paneHands", 5, 35, 10, 14);
		paneHands.setTexs("border2.png", "pane2.png");
		paneHands.setBorderWidth(2);
		this.addElement(paneHands);

		Item itemH = this.player.getWearItem(AttachPoint.HANDS);
		if (itemH != null) {
			Image img = new Image("imgInventoryItemHands", itemH.getBagImage(), 5, 36, bagSlotW * itemH.getBagW(),
					bagSlotH * itemH.getBagH());
			img.setAttachment(itemH);
			this.addElement(img);
		}

		// Feet
		paneFeet = new Pane("paneFeet", 35, 35, 10, 14);
		paneFeet.setTexs("border2.png", "pane2.png");
		paneFeet.setBorderWidth(2);
		this.addElement(paneFeet);

		Item itemF = this.player.getWearItem(AttachPoint.FEET);
		if (itemF != null) {
			Image img = new Image("imgInventoryItemFeet", itemF.getBagImage(), 35, 35, bagSlotW * itemF.getBagW(),
					bagSlotH * itemF.getBagH());
			img.setAttachment(itemF);
			this.addElement(img);
		}

		// Chest
		paneChest = new Pane("paneChest", 18, 45, 14, 25);
		paneChest.setTexs("border2.png", "pane2.png");
		paneChest.setBorderWidth(2);
		this.addElement(paneChest);

		Item itemC = this.player.getWearItem(AttachPoint.CHEST);
		if (itemC != null) {
			Image img = new Image("imgInventoryItemChest", itemC.getBagImage(), 21, 51, bagSlotW * itemC.getBagW(),
					bagSlotH * itemC.getBagH());
			img.setAttachment(itemC);
			this.addElement(img);
		}

		// Head
		paneHead = new Pane("paneHead", 20, 73, 10, 14);
		paneHead.setTexs("border2.png", "pane2.png");
		paneHead.setBorderWidth(2);
		this.addElement(paneHead);

		Item itemHd = this.player.getWearItem(AttachPoint.HEAD);
		if (itemHd != null) {
			Image img = new Image("imgInventoryItemHead", itemHd.getBagImage(), 20, 74, bagSlotW * itemHd.getBagW(),
					bagSlotH * itemHd.getBagH());
			img.setAttachment(itemHd);
			this.addElement(img);
		}

		// Ring 1
		paneRing1 = new Pane("paneRing1", 19, 35, 5, 7);
		paneRing1.setTexs("border2.png", "pane2.png");
		paneRing1.setBorderWidth(2);
		this.addElement(paneRing1);

		Item itemR1 = this.player.getWearItem(AttachPoint.RING1);
		if (itemR1 != null) {
			Image img = new Image("imgInventoryItemRing1", itemR1.getBagImage(), 19, 35, bagSlotW * itemR1.getBagW(),
					bagSlotH * itemR1.getBagH());
			img.setAttachment(itemR1);
			this.addElement(img);
		}

		// Ring 2
		paneRing2 = new Pane("paneRing2", 26, 35, 5, 7);
		paneRing2.setTexs("border2.png", "pane2.png");
		paneRing2.setBorderWidth(2);
		this.addElement(paneRing2);

		Item itemR2 = this.player.getWearItem(AttachPoint.RING2);
		if (itemR2 != null) {
			Image img = new Image("imgInventoryItemRing2", itemR2.getBagImage(), 26, 35, bagSlotW * itemR2.getBagW(),
					bagSlotH * itemR2.getBagH());
			img.setAttachment(itemR2);
			this.addElement(img);
		}

		// Bag
		bag = new Pane("paneInventoryBag", bagX, bagY, bagSlotW * Inventory.BAG_WIDTH, bagSlotH * Inventory.BAG_HEIGHT);
		bag.setBorderWidth(2);
		bag.setTexs("border2.png", "pane2.png");
		this.addElement(bag);

		HashMap<Item, Boolean> itemAdded = new HashMap<Item, Boolean>();
		LinkedList<Image> itemImages = new LinkedList<Image>();

		for (int i = 0; i < Inventory.BAG_WIDTH; i++) {
			for (int j = 0; j < Inventory.BAG_HEIGHT; j++) {
				float x = bagX + i * bagSlotW;
				float y = bagY + bagSlotH * (Inventory.BAG_HEIGHT - 1) - j * bagSlotH;

				if (this.player.getInventory().get(i, j) != null) {
					Item item = this.player.getInventory().get(i, j);

					if (!itemAdded.containsKey(item)) {
						Image img = new Image("imgInventoryItemBag", item.getBagImage(), x, y - bagSlotH
								* (item.getBagH() - 1), bagSlotW * item.getBagW(), bagSlotH * item.getBagH());
						img.setAttachment(item);
						itemImages.add(img);
						itemAdded.put(item, true);
						img.setMouseListener(this);
					}
				}
			}
		}

		for (Image img : itemImages) {
			this.addElement(img);
		}

		this.wndInfo = new ItemInfoWindow();
		this.addElement(this.wndInfo);

		// Picked item image
		if (this.pickedItemImg == null) {
			this.pickedItemImg = new Image("imgInventoryPickedItem", "simple.png", 0, 0, 0, 0);
		}
		this.addElement(this.pickedItemImg);
		if (this.pickedItem == null) {
			this.pickedItemImg.getNode().setLocalTranslation(0, 0, 6);
			this.pickedItemImg.hide();
		}

		// Item drop zone
		this.dropzone = new Image("imgInventoryDropzone", "transparent.png", -50, 0, 50, 100);
		this.addElement(this.dropzone);

		if (this.pickedItem == null) {
			this.dropzone.hide();
		}
		this.wndInfo.hide();

		// Close button
		btnClose = new Button("btnInvClose", 40, 3, 8, 5, "Close");
		btnClose.setMouseListener(this);
		this.addElement(btnClose);

		// Gold
		paneGold = new Pane("paneInvGold", 2, 3, 15, 5);
		paneGold.setTexs("border2.png", "pane2.png");
		paneGold.setBorderWidth(2);
		this.addElement(paneGold);

		imgGold = new Image("imgInvGold", "gold.png", 2, 3, 4, 5);
		this.addElement(imgGold);

		txtGold = new TextArea("txtInvGold", 8, 7, 10, 5, Integer.toString(this.player.getInventory().getGold()));
		txtGold.setAlign(Align.Left);
		this.addElement(txtGold);
	}

	@Override
	public boolean handleMouse(MouseEvent e) {
		return super.handleMouse(e);
	}

	@Override
	public void show() {
		super.show();
	}

	@Override
	public void hide() {
		this.dropItem();
		super.hide();
	}

	@Override
	public void update(float t) {
		if (this.player.getInventory().wasUpdated() || this.needsRebuild) {
			this.player.getInventory().clearWasUpdated();
			this.rebuild();
			this.needsRebuild = false;
		}

		this.hoveringItem = false;
	}

	@Override
	public void guiMouseClickEvent(AbstractElement element, MouseEvent e) {
		if (element.equals(this.btnClose)) {
			this.hide();
			return;
		}
		if (this.pickedItem != null) {
			if (this.bag.isInsideScrBounds(e.getX(), e.getY())) {
				int slotX = (int) ((this.bag.scrToPctX(e.getX()) - this.getLeft()) / InventoryWindow.bagSlotW);
				int slotY = (Inventory.BAG_HEIGHT - 1)
						- (int) ((this.bag.scrToPctY(e.getY()) - this.getTop()) / InventoryWindow.bagSlotH);

				if (this.player.getInventory().bagItem(this.pickedItem, slotX, slotY)) {
					this.pickedItem = null;
				}
			} else if (element.equals(this.dropzone)) {
				if (Game.getInstance().getHUD().isTrading()) {
					this.sellItem();
				} else {
					this.dropItem();
				}
			} else if (element.equals(this.paneMainHand)) {
				this.pickedItem = this.player.wearItem(this.pickedItem, AttachPoint.HAND_R);
			} else if (element.equals(this.paneOffHand)) {
				this.pickedItem = this.player.wearItem(this.pickedItem, AttachPoint.HAND_L);
			} else if (element.equals(this.paneChest)) {
				this.pickedItem = this.player.wearItem(this.pickedItem, AttachPoint.CHEST);
			} else if (element.equals(this.paneHead)) {
				this.pickedItem = this.player.wearItem(this.pickedItem, AttachPoint.HEAD);
			} else if (element.equals(this.paneRing1)) {
				this.pickedItem = this.player.wearItem(this.pickedItem, AttachPoint.RING1);
			} else if (element.equals(this.paneRing2)) {
				this.pickedItem = this.player.wearItem(this.pickedItem, AttachPoint.RING2);
			} else if (element.equals(this.paneFeet)) {
				this.pickedItem = this.player.wearItem(this.pickedItem, AttachPoint.FEET);
			} else if (element.equals(this.paneHands)) {
				this.pickedItem = this.player.wearItem(this.pickedItem, AttachPoint.HANDS);
			}

			System.out.println(this.pickedItem);

			if (this.pickedItem == null) {
				this.pickedItemImg.hide();
				this.dropzone.hide();
				this.needsRebuild = true;
			} else {
				this.pickedItemImg.setImage(this.pickedItem.getBagImage());
				this.needsRebuild = true;
			}

		} else {
			if (element.getId().equals("imgInventoryItemBag")) {
				if (e.getButton().equals(MouseEvent.MouseButton.BTN2)
						&& element.getAttachment().getClass().equals(ConsumableItem.class)) {
					ConsumableItem c = (ConsumableItem) element.getAttachment();
					c.consume(this.player);
					this.rebuild();
				} else {
					this.pickedItem = (Item) element.getAttachment();
					this.player.getInventory().removeItem(this.pickedItem);
				}
			} else if (element.equals(this.paneMainHand)) {
				this.pickedItem = this.player.unwearItem(AttachPoint.HAND_R);
			} else if (element.equals(this.paneOffHand)) {
				this.pickedItem = this.player.unwearItem(AttachPoint.HAND_L);
			} else if (element.equals(this.paneChest)) {
				this.pickedItem = this.player.unwearItem(AttachPoint.CHEST);
			} else if (element.equals(this.paneHead)) {
				this.pickedItem = this.player.unwearItem(AttachPoint.HEAD);
			} else if (element.equals(this.paneRing1)) {
				this.pickedItem = this.player.unwearItem(AttachPoint.RING1);
			} else if (element.equals(this.paneRing2)) {
				this.pickedItem = this.player.unwearItem(AttachPoint.RING2);
			} else if (element.equals(this.paneFeet)) {
				this.pickedItem = this.player.unwearItem(AttachPoint.FEET);
			} else if (element.equals(this.paneHands)) {
				this.pickedItem = this.player.unwearItem(AttachPoint.HANDS);
			}

			if (this.pickedItem != null) {
				this.rebuild();
				this.pickedItemImg.setWidth(bagSlotW * this.pickedItem.getBagW());
				this.pickedItemImg.setHeight(bagSlotH * this.pickedItem.getBagH());
				this.pickedItemImg.setImage(this.pickedItem.getBagImage());
				this.pickedItemImg.setTop(this.scrToPctY(e.getY()) - (this.pickedItem.getBagH() - 0.5f)
						* InventoryWindow.bagSlotH);
				this.pickedItemImg.setLeft(this.scrToPctX(e.getX()) - this.pickedItem.getBagW()
						* InventoryWindow.bagSlotW / 2);
				this.pickedItemImg.show();
				this.dropzone.show();
			}
		}
	}

	@Override
	public void guiMouseMoveEvent(AbstractElement element, MouseEvent e) {
		if (element.getId().startsWith("imgInventoryItem")) {
			Item item = (Item) element.getAttachment();
			if (item != null) {
				this.wndInfo.setTitleColor(item.getQualityColor());
				this.wndInfo.setTitle(item.getCaption());
				this.wndInfo.setContent(item.getInfoString());
				this.wndInfo.setLeft(this.scrToPctX(e.getX()) - this.wndInfo.getWidth() / 2);
				this.wndInfo.setTop(this.scrToPctY(e.getY()));
				if (this.wndInfo.getLeft() + this.wndInfo.getWidth() > this.width) {
					this.wndInfo.setLeft(this.getWidth() - this.wndInfo.getWidth());
				}
				if (this.wndInfo.getTop() + this.wndInfo.getHeight() > this.bg.getHeight()) {
					this.wndInfo.setTop(this.bg.getHeight() - this.wndInfo.getHeight());
				}
				this.wndInfo.show();
				this.hoveringItem = true;
			}
		} else if (!this.hoveringItem) {
			this.wndInfo.hide();
		}

		if (this.pickedItem != null) {
			this.pickedItemImg.setTop(this.scrToPctY(e.getY()) - (this.pickedItem.getBagH() - 0.5f)
					* InventoryWindow.bagSlotH);
			this.pickedItemImg.setLeft(this.scrToPctX(e.getX()) - this.pickedItem.getBagW() * InventoryWindow.bagSlotW
					/ 2);
		}
	}

	private void dropItem() {
		if (pickedItem != null) {
			this.player.getRoom().addEntity(this.pickedItem);
			this.pickedItem.doSpawnFlip();
			this.pickedItem.setNoPicking(false);
			this.pickedItem.setPos(this.player.getPosition().x, this.player.getPosition().y);
			this.pickedItem = null;
			this.pickedItemImg.hide();
			this.dropzone.hide();
			this.needsRebuild = true;
		}
	}

	private void sellItem() {
		this.player.getMap().getWorld().getSanctuary().sellItem(this.player, pickedItem);
		this.pickedItem = null;
		this.pickedItemImg.hide();
	}

}
