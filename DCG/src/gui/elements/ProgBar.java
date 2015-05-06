/**
 * A progress bar -type element. Only vertical. Uses two 
 * textured areas to visually represent the percentage set
 * using setPercentage().
 * Used for HP and Mana.
 */
package gui.elements;

import com.jme.image.Texture;
import com.jme.image.Texture.WrapMode;
import com.jme.math.Vector3f;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;

public class ProgBar extends AbstractElement {

	private String bgTex = "pane.png";
	private String borderTex = "border.png";
	private int borderWidth = 6;
	private float percentage = 1;

	public ProgBar(String id, float left, float top, float width, float height) {
		super(id, left, top, width, height);
	}

	@Override
	public void rebuild() {
		int x = this.getScrLeft() + this.getScrWidth() - this.getScrWidth() / 2;
		int y = this.getScrTop() + this.getScrHeight() - this.getScrHeight() / 2;
		int w = this.getScrWidth();
		int h = this.getScrHeight();

		Quad q = new Quad(this.getId(), w, h * this.percentage);

		q.setLocalTranslation(x, y - (h - h * this.percentage) / 2, 0);

		TextureState ts = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
		ts.setTexture(TextureManager.loadTexture(getClass().getClassLoader().getResource(
				"dcg/data/textures/" + this.bgTex), Texture.MinificationFilter.NearestNeighborNearestMipMap,
				Texture.MagnificationFilter.NearestNeighbor, 1.0f, true));
		ts.getTexture().setWrap(WrapMode.Repeat);
		ts.getTexture().setScale(
				new Vector3f((float) w / ts.getTexture().getImage().getWidth() * 2, (float) h
						/ ts.getTexture().getImage().getWidth() * 2, 1f));
		ts.setEnabled(true);

		q.setRenderState(ts);
		q.updateRenderState();

		Quad b = new Quad(this.getId(), w + this.borderWidth, h + this.borderWidth);

		b.setLocalTranslation(x, y, 0);

		TextureState tsb = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
		tsb.setTexture(TextureManager.loadTexture(getClass().getClassLoader().getResource(
				"dcg/data/textures/" + this.borderTex), Texture.MinificationFilter.NearestNeighborNearestMipMap,
				Texture.MagnificationFilter.NearestNeighbor, 1.0f, true));
		tsb.setEnabled(true);
		tsb.getTexture().setWrap(WrapMode.Repeat);

		b.setRenderState(tsb);
		b.updateRenderState();

		this.getNode().detachAllChildren();
		this.getNode().attachChild(b);
		this.getNode().attachChild(q);

		super.rebuild();
	}

	public void setPercentage(int percentage) {
		if (this.percentage != (float) percentage / 100f) {
			this.percentage = (float) percentage / 100f;

			this.rebuild();
		}
	}

	public int getPercentage() {
		return (int) (this.percentage / 100);
	}

	public void setBorderWidth(int borderWidth) {
		this.borderWidth = borderWidth;
	}

	public void setBgTex(String bgTex) {
		this.bgTex = bgTex;
	}

	public void setBorderTex(String borderTex) {
		this.borderTex = borderTex;
	}

}
