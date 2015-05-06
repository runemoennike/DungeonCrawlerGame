/**
 * This class represents the overlay map state. The overlay map is NOT meant for use in the current game
 * and is mainly used for testing.
 * 
 * It extends GameState.
 */

package engine.hud;

import com.jme.image.Texture;
import com.jme.image.Texture2D;
import com.jme.image.Texture.ApplyMode;
import com.jme.image.Texture.MagnificationFilter;
import com.jme.image.Texture.MinificationFilter;
import com.jme.image.Texture.WrapMode;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.shape.Box;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jmex.game.state.GameState;

import engine.Game;
import engine.Profiler;
import engine.Settings;
import engine.Settings.Fields;

public class MapOverlayState extends GameState {

	private Node rootNode;
	private Game game;

	private TextureState ts;
	private boolean inited = false;

	private MapOverlay moTex;

	private long lastUpd;

	public MapOverlayState(Game game) {
		super();
		game.setMapOverlay(this);

		this.game = game;

		rootNode = new Node();
		rootNode.setRenderQueueMode(Renderer.QUEUE_ORTHO);	
		rootNode.setLightCombineMode(Spatial.LightCombineMode.Off);
		rootNode.updateRenderState();
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub

	}

	@Override
	public void render(float tpf) {
		Profiler.start("MapOverlayState.render");
		DisplaySystem.getDisplaySystem().getRenderer().draw(this.rootNode);
		Profiler.stop("MapOverlayState.render");
	}

	@Override
	public void update(float tpf) {
		Profiler.start("MapOverlayState.update");

		if (!this.inited) {
			init();
			this.inited = true;
		} else {
			if (System.currentTimeMillis() - this.lastUpd > 200) {
				this.moTex.refreshImage();
				this.ts.deleteAll();
				this.lastUpd = System.currentTimeMillis();
			}
		}

		Profiler.stop("MapOverlayState.update");
	}

	private void init() {
		int scrw = Settings.get(Fields.SCR_W).i;
		int scrh = Settings.get(Fields.SCR_H).i;

		if (this.moTex == null) {
			this.moTex = new MapOverlay(scrw, scrh, true, this.game);
		} else {
			this.moTex.refreshImage();
		}

		Texture texture = new Texture2D();
		texture.setApply(ApplyMode.Replace);
		texture.setWrap(WrapMode.BorderClamp);
		texture.setBlendColor(new ColorRGBA(1, 1, 1, 1));
		texture.setImage(this.moTex);
		texture.setMagnificationFilter(MagnificationFilter.NearestNeighbor);
		texture.setMinificationFilter(MinificationFilter.NearestNeighborNoMipMaps);
		BlendState bs = DisplaySystem.getDisplaySystem().getRenderer().createBlendState();

		bs.setBlendEnabled(true);
		bs.setEnabled(true);

		this.ts = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();

		this.ts.setTexture(texture);
		this.ts.setEnabled(true);

		Box b = new Box("map overlay", new Vector3f(scrw / 2f, scrh / 2f, -20f), scrw / 2f, scrh / 2f, 20f);
		b.setRenderState(this.ts);
		b.setRenderState(bs);

		// b.addTextureCoordinates(textureCoords, coordSize)

		this.rootNode.attachChild(b);

		this.rootNode.updateGeometricState(0, false);
		this.rootNode.updateRenderState();

	}

	public void setGame(Game game) {
		this.game = game;
	}

}
