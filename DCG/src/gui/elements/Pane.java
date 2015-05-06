/**
 * A textured area with a border. Used for windows or
 * for marking areas within windows.
 */
package gui.elements;

import com.jme.image.Texture;
import com.jme.image.Texture.WrapMode;
import com.jme.math.Vector3f;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;

public class Pane extends AbstractElement {

	private String bgTex = "pane.png";
	private String borderTex = "border.png";
	private int borderWidth = 6;
	private TextureState tsBg = null;
	private TextureState tsBorder = null;
	private static BlendState blendState;
	private boolean alpha = false;

	public Pane(String id, float left, float top, float width, float height) {
		super(id, left, top, width, height);
	}

	@Override
	public void rebuild() {
		int x = this.getScrLeft() + this.getScrWidth() - this.getScrWidth() / 2;
		int y = this.getScrTop() + this.getScrHeight() - this.getScrHeight() / 2;
		int w = this.getScrWidth();
		int h = this.getScrHeight();

		Quad q = new Quad(this.getId(), w, h);

		q.setLocalTranslation(x, y, 0);

		if (this.tsBg == null) {
			this.tsBg = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
			this.tsBg.setTexture(TextureManager.loadTexture(getClass().getClassLoader().getResource(
					"dcg/data/textures/" + this.bgTex), Texture.MinificationFilter.NearestNeighborNearestMipMap,
					Texture.MagnificationFilter.NearestNeighbor, 1.0f, true));
			this.tsBg.getTexture().setWrap(WrapMode.Repeat);
			this.tsBg.getTexture().setScale(
					new Vector3f((float) w / this.tsBg.getTexture().getImage().getWidth() * 2, (float) h
							/ this.tsBg.getTexture().getImage().getWidth() * 2, 1f));
			this.tsBg.setEnabled(true);
		}

		q.setRenderState(this.tsBg);
		q.updateRenderState();

		Quad b = new Quad(this.getId(), w + this.borderWidth, h + this.borderWidth);

		b.setLocalTranslation(x, y, 0);

		if (this.tsBorder == null) {
			this.tsBorder = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
			this.tsBorder.setTexture(TextureManager.loadTexture(getClass().getClassLoader().getResource(
					"dcg/data/textures/" + this.borderTex), Texture.MinificationFilter.NearestNeighborNearestMipMap,
					Texture.MagnificationFilter.NearestNeighbor, 1.0f, true));
			this.tsBorder.setEnabled(true);
			this.tsBorder.getTexture().setWrap(WrapMode.Repeat);
		}

		b.setRenderState(this.tsBorder);

		if (this.alpha) {
			if (Pane.blendState == null) {
				Pane.blendState = DisplaySystem.getDisplaySystem().getRenderer().createBlendState();
				Pane.blendState.setEnabled(true);
				Pane.blendState.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
				Pane.blendState.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
				Pane.blendState.setBlendEnabled(true);
				Pane.blendState.setTestEnabled(true);
				Pane.blendState.setTestFunction(BlendState.TestFunction.GreaterThan);
				Pane.blendState.setReference(0.1f);
			}
			b.setRenderState(Pane.blendState);
			q.setRenderState(Pane.blendState);
		}

		b.updateRenderState();
		q.updateRenderState();

		this.getNode().detachAllChildren();
		this.getNode().attachChild(b);
		this.getNode().attachChild(q);

		super.rebuild();
	}

	public void setAlpha(boolean b) {
		this.alpha = b;
	}

	public void setBorderWidth(int borderWidth) {
		this.borderWidth = borderWidth;
	}

	public void setBgTex(String bgTex) {
		this.bgTex = bgTex;
		this.tsBg = null;
	}

	public void setBorderTex(String borderTex) {
		this.borderTex = borderTex;
		this.tsBorder = null;
	}

	public void setTexs(String border, String bg) {
		setBorderTex(border);
		setBgTex(bg);
	}

}
