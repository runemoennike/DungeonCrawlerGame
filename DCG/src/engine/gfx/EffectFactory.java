/**
 * Caches particle effects because they are expensive to instantiate.
 * Preloads a set number of each type of effect, and is called when
 * a new effect should be spawned. It will search for a free, preloaded
 * effect, or else create a new one (and add it to the pool of preloaded
 * effects). Use spawnEffect.
 */
package engine.gfx;

import java.util.ArrayList;
import java.util.HashMap;

import com.jme.image.Texture;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Controller;
import com.jme.scene.Node;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import com.jmex.effects.particles.ParticleFactory;
import com.jmex.effects.particles.ParticleMesh;
import com.jmex.effects.particles.SimpleParticleInfluenceFactory;

import engine.Game;
import engine.Profiler;

public class EffectFactory {
	public enum EffectType {
		DESTRUCTION(3), HIT_BLOOD(10), LEVEL_UP(3), HEAL(3);

		public int preloadCount;
		private EffectType(int preloadCount) {
			this.preloadCount = preloadCount;
		}
	}

	private static Node rootNode;

	private static HashMap<EffectType, ArrayList<ParticleMesh>> pool;

	public static void init(Node root) {
		EffectFactory.rootNode = root;

		EffectFactory.pool = new HashMap<EffectType, ArrayList<ParticleMesh>>();

		for (EffectType t : EffectType.values()) {
			EffectFactory.pool.put(t, new ArrayList<ParticleMesh>());
		}

		EffectFactory.preload();
	}

	public static void spawnEffect(EffectType type, Vector3f pos, float angle, float scale, String tex) {
		Profiler.start("EffectFactory.spawnEffect");

		ParticleMesh m = getEffect(type);

		if (tex != null) {
			TextureState ts = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
			ts.setTexture(TextureManager.loadTexture(Game.class.getClassLoader()
					.getResource("dcg/data/textures/" + tex), Texture.MinificationFilter.NearestNeighborLinearMipMap,
					Texture.MagnificationFilter.Bilinear));
			ts.setEnabled(true);
			m.setRenderState(ts);

			m.updateRenderState();
		}

		Quaternion rot = new Quaternion();
		rot.fromAngleAxis(angle + FastMath.HALF_PI, new Vector3f(0, 0, 1f));
		m.setLocalRotation(rot);

		m.getLocalTranslation().set(pos);
		m.updateGeometricState(0, true);
		m.forceRespawn();

		Profiler.stop("EffectFactory.spawnEffect");
	}

	public static void preload() {
		for (EffectType t : EffectType.values()) {
			for (int i = 0; i < t.preloadCount; i++) {
				ParticleMesh trash = constructEffect(t);
				EffectFactory.pool.get(t).add(trash);
			}
		}
	}

	public static ParticleMesh getEffect(EffectType type) {
		Profiler.start("EffectFactory.getEffect");

		for (ParticleMesh m : EffectFactory.pool.get(type)) {
			if (!m.isActive()) {
				Profiler.stop("EffectFactory.getEffect");
				return m;
			}
		}

		ParticleMesh newEffect = constructEffect(type);
		EffectFactory.pool.get(type).add(newEffect);

		Profiler.stop("EffectFactory.getEffect");
		return newEffect;
	}

	private static ParticleMesh constructEffect(EffectType type) {
		ParticleMesh e = null;
		switch (type) {
			case DESTRUCTION :
				e = ParticleFactory.buildParticles("", 100);
				e.addInfluence(SimpleParticleInfluenceFactory.createBasicGravity(new Vector3f(0, 0, -1), true));
				e.setEmissionDirection(new Vector3f(0.0f, 0.0f, 1.0f));
				e.setMaximumAngle(FastMath.PI);
				e.setSpeed(0.1f);
				e.getParticleController().setSpeed(0.1f);
				e.getParticleController().setControlFlow(false);
				e.getParticleController().setRepeatType(Controller.RT_CLAMP);
				e.setMinimumLifeTime(100);
				e.setMaximumLifeTime(100);
				e.setStartSize(0.1f);
				e.setEndSize(0.2f);
				e.setStartColor(new ColorRGBA(1, 1, 1, 1.0f));
				e.setEndColor(new ColorRGBA(1, 1, 1, 0.0f));
				e.setControlFlow(false);
				e.setInitialVelocity(0.02f);
				e.setParticleSpinSpeed(0.1f);
				break;
			case HIT_BLOOD :
				e = ParticleFactory.buildParticles("", 20);
				e.addInfluence(SimpleParticleInfluenceFactory.createBasicGravity(new Vector3f(0, 0, -1), true));
				e.addInfluence(SimpleParticleInfluenceFactory
						.createBasicWind(1.0f, new Vector3f(-1, 0, 0), false, true));
				e.setEmissionDirection(new Vector3f(0.0f, 0.0f, 1.0f));
				e.setMaximumAngle(FastMath.PI);
				e.setSpeed(0.1f);
				e.getParticleController().setSpeed(0.1f);
				e.getParticleController().setControlFlow(false);
				e.getParticleController().setRepeatType(Controller.RT_CLAMP);
				e.setMinimumLifeTime(100);
				e.setMaximumLifeTime(100);
				e.setStartSize(0.1f);
				e.setEndSize(0.2f);
				e.setStartColor(new ColorRGBA(1, 0, 0, 1.0f));
				e.setEndColor(new ColorRGBA(1, 0, 0, 0.0f));
				e.setControlFlow(false);
				e.setInitialVelocity(0.02f);
				e.setParticleSpinSpeed(0.1f);
				break;
			case LEVEL_UP :

				e = ParticleFactory.buildParticles("", 30);
				e
						.addInfluence(SimpleParticleInfluenceFactory.createBasicWind(1.0f, new Vector3f(0, 0, 1),
								false, true));
				e.setEmissionDirection(new Vector3f(0.0f, 0.0f, 1.0f));
				e.setMaximumAngle(FastMath.HALF_PI);
				e.setSpeed(0.1f);
				e.getParticleController().setSpeed(0.1f);
				e.getParticleController().setControlFlow(false);
				e.getParticleController().setRepeatType(Controller.RT_CLAMP);
				e.setMinimumLifeTime(200);
				e.setMaximumLifeTime(400);
				e.setStartSize(0.1f);
				e.setEndSize(0.1f);
				e.setStartColor(new ColorRGBA(1, 1, 0, 1.0f));
				e.setEndColor(new ColorRGBA(1, 1, 0, 0.0f));
				e.setControlFlow(false);
				e.setInitialVelocity(0.02f);
				e.setParticleSpinSpeed(0.1f);
				break;

			case HEAL :

				e = ParticleFactory.buildParticles("", 90);
				e
						.addInfluence(SimpleParticleInfluenceFactory.createBasicWind(1.0f, new Vector3f(0, 0, 1),
								false, true));
				e.setEmissionDirection(new Vector3f(0.0f, 0.0f, 1.0f));
				e.setMaximumAngle(FastMath.HALF_PI);
				e.setSpeed(0.1f);
				e.getParticleController().setSpeed(0.1f);
				e.getParticleController().setControlFlow(false);
				e.getParticleController().setRepeatType(Controller.RT_CLAMP);
				e.setMinimumLifeTime(200);
				e.setMaximumLifeTime(400);
				e.setStartSize(0.1f);
				e.setEndSize(0.2f);
				e.setStartColor(new ColorRGBA(0.3f, 0.3f, 1, 1.0f));
				e.setEndColor(new ColorRGBA(0, 0, 1, 0.0f));
				e.setControlFlow(false);
				e.setInitialVelocity(0.02f);
				e.setParticleSpinSpeed(0.1f);
				break;

		}

		e.setRotateWithScene(true);
		e.setParticlesInWorldCoords(true);

		e.updateRenderState();
		e.warmUp(200);

		if (e != null) {
			BlendState tpState = DisplaySystem.getDisplaySystem().getRenderer().createBlendState();
			tpState.setEnabled(true);
			tpState.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
			tpState.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
			tpState.setBlendEnabled(true);
			tpState.setTestEnabled(true);
			tpState.setTestFunction(BlendState.TestFunction.GreaterThan);
			tpState.setReference(0.1f);
			e.setRenderState(tpState);

			MaterialState ms = DisplaySystem.getDisplaySystem().getRenderer().createMaterialState();
			ms.setEmissive(new ColorRGBA(1f, 1f, 1f, 1));

			e.setRenderState(ms);
		}

		EffectFactory.rootNode.attachChild(e);
		EffectFactory.rootNode.updateRenderState();

		return e;
	}

	public static void setRootNode(Node n) {
		rootNode = n;
	}
}
