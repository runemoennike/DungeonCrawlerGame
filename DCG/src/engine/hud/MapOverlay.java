/**
 * This class represents the overlay map. The overlay map is NOT meant for use in the current game
 * and is mainly used for testing.
 * 
 * It extends MapOverlay and implements thirdParty.PaintableImage.
 */


package engine.hud;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.LinkedList;

import map.Map;
import engine.Game;
import engine.Profiler;
import engine.Settings;
import engine.World;
import engine.Settings.Fields;
import entities.Entity;
import entities.Entity.EntitySubtype;

public class MapOverlay extends thirdParty.PaintableImage {
	private static final long serialVersionUID = 576232659997516698L;

	private Game game;

	public MapOverlay(int width, int height, boolean hasAlpha, Game game) {
		super(width, height, hasAlpha);
		this.game = game;
		refreshImage();
	}

	@Override
	public void paint(Graphics2D g) {
		Profiler.start("MapOverlay.paint");

		g.setBackground(new Color(0, 0, 0, 0));
		g.clearRect(0, 0, getWidth(), getHeight());

		Map map = this.game.getWorld().getLocalPlayerMap();

		int mw = map.getWidth();
		int mh = map.getWidth();

		int pmw = mw * World.PATHING_GRANULARITY;
		int pmh = mh * World.PATHING_GRANULARITY;

		int scrw = Settings.get(Fields.SCR_W).i;
		int scrh = Settings.get(Fields.SCR_H).i;

		float bw = (float) scrw / pmw;
		float bh = (float) scrh / pmh;

		g.setComposite(makeComposite(0.7f));
		g.setColor(Color.WHITE);

		for (int x = 0; x < pmw; x++) {
			for (int y = 0; y < pmh; y++) {
				if (map.getPathingValue(x, y) >= 0) {
					g.fillRect((int) (x * bw), (int) (y * bh), (int) bw + 1, (int) bh + 1);
				}
			}
		}

		float plx = this.game.getWorld().getLocalPlayer().getPosition().x * bw;
		float ply = this.game.getWorld().getLocalPlayer().getPosition().y * bh;
		g.setColor(Color.BLUE);
		g.fillOval((int) plx - 5, (int) ply - 5, (int) 10, (int) 10);

		LinkedList<Entity> ents = this.game.getWorld().getLocalPlayerMap().getAllRoomActors();
		for (Entity e : ents) {
			float ex = e.getPosition().x * bw;
			float ey = e.getPosition().y * bh;

			if (e.isSubtype(EntitySubtype.MONSTER)) {
				g.setColor(Color.RED);
				g.fillOval((int) ex - 5, (int) ey - 5, (int) 10, (int) 10);
			}
		}

		Profiler.stop("MapOverlay.paint");

	}

	private AlphaComposite makeComposite(float alpha) {
		int type = AlphaComposite.SRC_OVER;
		return (AlphaComposite.getInstance(type, alpha));
	}

}
