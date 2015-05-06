/**
 * This class extends the CameraGameState class of jME represents the game as a whole.
 * It contains the World and the HUD, camera and other important objects.
 * 
 * The Game class manages mouse and keyboard input, as well as loading and contains a series of methods
 * for moving the player between Map objects stored in World.
 * 
 * This class calls update on the hud and the world.
 */

package engine;

import infostore.DataManager;
import infostore.DataNode;
import infostore.DataManager.DataType;

import java.awt.Color;
import java.awt.Point;
import java.util.logging.Logger;

import map.Map;
import map.Room;
import misc.Inventory.BELTSPOT;
import skills.SkillTree;
import threadMessaging.MTMessage;
import threadMessaging.MTMessageQueue;
import threadMessaging.MTMsgAIStat;
import threadMessaging.MTMessage.MessageType;
import ai.AIWorkerMain;

import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;
import com.jme.input.MouseInput;
import com.jme.intersection.BoundingPickResults;
import com.jme.intersection.PickData;
import com.jme.intersection.PickResults;
import com.jme.light.PointLight;
import com.jme.light.SpotLight;
import com.jme.math.FastMath;
import com.jme.math.Plane;
import com.jme.math.Ray;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.Spatial.CullHint;
import com.jme.scene.Spatial.LightCombineMode;
import com.jme.scene.state.CullState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.ZBufferState;
import com.jme.system.DisplaySystem;
import com.jme.util.Timer;
import com.jme.util.resource.ClasspathResourceLocator;
import com.jme.util.resource.ResourceLocator;
import com.jme.util.resource.ResourceLocatorTool;
import com.jmex.game.state.CameraGameState;

import engine.gfx.EffectFactory;
import engine.gfx.ModelFactory;
import engine.hud.ConsoleLog;
import engine.hud.HUD;
import engine.hud.MapOverlayState;
import entities.WalkTargetEntity;
import entities.Entity;
import entities.Marker;
import entities.Entity.EntityState;
import entities.actors.Monster;
import entities.actors.Player;

public class Game extends CameraGameState {
	public static final Logger logger = Logger.getLogger(Game.class.getName());

	private static final boolean CHASE_CAM = true;

	public enum Elevation {
		UP, DOWN, QUEUE_UP, QUEUE_DOWN, NOWHERE
	};

	private float camAngle = FastMath.PI / 4f, camHeight = 35.0f, camDist = 20.0f;
	private int camPresetAngle = 0;
	private static final float[] camPresetAngles = {FastMath.QUARTER_PI, FastMath.QUARTER_PI + FastMath.HALF_PI,
			-FastMath.QUARTER_PI - FastMath.HALF_PI, -FastMath.QUARTER_PI};

	PointLight pl;
	private LightState lights;

	SpotLight hlLight;
	private LightState hlLightState;

	protected static Timer timer;

	static public float tpf;

	World world;

	Player player;
	Monster testMonster;
	Marker selectionMarker;

	private int loadProgress = 0;
	private String loadText = "";
	private int loadStage = 0;

	private float fps = 0;

	private boolean devMode = false;

	private Entity selectedEntity = null;
	private WalkTargetEntity walkTargetEntity;
	private HUD hud;
	private MapOverlayState mapOverlay;
	private boolean mapOverlayOn;

	private Thread aiBrainThread;
	private MTMessageQueue aiBrainMsgq = new MTMessageQueue();
	private AIWorkerMain aiBrain = new AIWorkerMain(aiBrainMsgq);

	private Elevation elevatePlayer = Elevation.NOWHERE;

	private static Game instance;

	public static boolean markPathing = false;

	public Game() {
		super("game");
	}

	public static Game getInstance() {
		return Game.instance;
	}

	public void loadAll() {
		prepareLoad();
		while (this.loadProgress < 100)
			load();
	}

	public void prepareLoad() {
		this.loadProgress = 0;
		this.loadText = "Loading...";
		this.loadStage = 0;
	}

	// TODO: fix starting map stuff
	public void load() {
		switch (this.loadStage) {
			case 0 :
				Game.instance = this;
				setLoadProgress(5, "Loading, please wait... initializing system");
				break;

			case 1 :
				initSystem();
				setLoadProgress(10, "Loading, please wait... initializing map");
				break;

			case 2 :
				initWorld();
				setLoadProgress(20, "Loading, please wait... initializing graphics");
				break;

			case 3 :
				initGfx(this.world.getStartingMap());
				setLoadProgress(30, "Loading, please wait... generating map");
				break;

			case 4 :
				this.world.generateStartingMap();
				setLoadProgress(40, "Loading, please wait... initializing input");
				break;

			case 5 :
				initInput();
				setLoadProgress(50, "Loading, please wait... initializing mouse");
				break;

			case 6 :
				initMouse();
				setLoadProgress(60, "Loading, please wait... initializing player");
				break;

			case 7 :
				initPlayer(this.world.getStartingMap());
				setLoadProgress(70, "Loading, please wait... initializing doodads");
				break;

			case 8 :
				initDoodads();
				setLoadProgress(80, "Loading, please wait... initializing monsters");
				break;

			case 9 :
				initMonsters();
				setLoadProgress(90, "Loading, please wait... initializing lights");
				break;

			case 10 :
				initLights();
				initAI();
				setLoadProgress(95, "Loading, please wait... finalizing");
				break;

			case 11 :
				initFinal(world.getStartingMap());
				world.getLocalPlayerMap().pruneAndGrowTask.fullPrune();
				setLoadProgress(100, "Done!");
				break;
		}

		this.loadStage++;
	}

	private void initAI() {
		this.aiBrainThread = new Thread(this.aiBrain);
		this.aiBrainThread.start();
	}

	public void onActivate() {
		super.onActivate();
	}

	public void stateUpdate(float interpolation) {
		Profiler.start("Game.stateUpdate");

		Game.tpf = interpolation;

		if (!this.hud.isConsoleOpen()) {
			if (KeyBindingManager.getKeyBindingManager().isValidCommand("exit", false)) {
				this.hud.toggleConfirmExit();
			}
			if (KeyBindingManager.getKeyBindingManager().isValidCommand("scrUp")) {
				if (this.devMode) {
					this.camHeight++;
				} else {
					if (this.camHeight < 40f) {
						this.camHeight++;
					}
				}
			}
			if (KeyBindingManager.getKeyBindingManager().isValidCommand("scrDown")) {
				if (this.devMode) {
					this.camHeight--;
				} else {
					if (this.camHeight > 5f) {
						this.camHeight--;
					}
				}
			}
			if (KeyBindingManager.getKeyBindingManager().isValidCommand("scrLeft", false)) {
				// this.camAngle -= 0.1f;
				this.camPresetAngle -= 1;
				if (this.camPresetAngle < 0)
					this.camPresetAngle = 3;
			}
			if (KeyBindingManager.getKeyBindingManager().isValidCommand("scrRight", false)) {
				// this.camAngle += 0.1f;
				this.camPresetAngle += 1;
				if (this.camPresetAngle > 3)
					this.camPresetAngle = 0;
			}
			if (this.devMode && KeyBindingManager.getKeyBindingManager().isValidCommand("camZoomin")) {
				this.camDist -= 1f;
			}
			if (this.devMode && KeyBindingManager.getKeyBindingManager().isValidCommand("camZoomout")) {
				this.camDist += 1f;
			}
			if (KeyBindingManager.getKeyBindingManager().isValidCommand("debugPathingMap", false)) {
				// Debug.dumpPathingMap(this.world, this.player);
			}
			if (KeyBindingManager.getKeyBindingManager().isValidCommand("debugKillMobs", false)) {
				this.world.getLocalPlayerMap().killAllMobs();
			}
			if (KeyBindingManager.getKeyBindingManager().isValidCommand("debugProfileTotal", false)) {
				Profiler.dumpTotals();
			}
			if (KeyBindingManager.getKeyBindingManager().isValidCommand("debugMapOverlay", false)) {
				toggleMapOverlay();
			}
			if (KeyBindingManager.getKeyBindingManager().isValidCommand("hudInventory", false)) {
				this.hud.toggleInventory();
			}
			if (KeyBindingManager.getKeyBindingManager().isValidCommand("hudCharacterSheet", false)) {
				this.hud.toggleCharacterSheet();
			}
			if (KeyBindingManager.getKeyBindingManager().isValidCommand("hudSkillSheet", false)) {
				this.hud.toggleSkillSheet();
			}
			if (KeyBindingManager.getKeyBindingManager().isValidCommand("hudConsole", false)) {
				this.hud.toggleConsole();
			}
			if (KeyBindingManager.getKeyBindingManager().isValidCommand("hudMagics", false)) {
				this.hud.toggleMagics();
			}
			if (KeyBindingManager.getKeyBindingManager().isValidCommand("hudHideAll", false)) {
				this.hud.hideAll();
			}
			if (KeyBindingManager.getKeyBindingManager().isValidCommand("beltSpot1", false)) {
				this.player.useBeltSlot(BELTSPOT.HPOT);
			}
			if (KeyBindingManager.getKeyBindingManager().isValidCommand("beltSpot2", false)) {
				this.player.useBeltSlot(BELTSPOT.HPOT_LARGE);
			}
			if (KeyBindingManager.getKeyBindingManager().isValidCommand("beltSpot3", false)) {
				this.player.useBeltSlot(BELTSPOT.MPOT);
			}
			if (KeyBindingManager.getKeyBindingManager().isValidCommand("beltSpot4", false)) {
				this.player.useBeltSlot(BELTSPOT.MPOT_LARGE);
			}
			if (KeyBindingManager.getKeyBindingManager().isValidCommand("respawn", false)
					&& this.player.getCurState().equals(EntityState.DYING)) {
				this.player.respawn();
				this.player.clearFloatingTexts();
				this.hud.hideDeathWindow();
			}
		} else {
			if (KeyBindingManager.getKeyBindingManager().isValidCommand("hudConsole", false)
					|| KeyBindingManager.getKeyBindingManager().isValidCommand("exit", false)) {
				this.hud.toggleConsole();
			}
		}

		pl.setLocation(new Vector3f(this.player.getNode().getLocalTranslation().x, this.player.getNode()
				.getLocalTranslation().y, 9));

		if (!this.hud.getGui().handleJMEMouseInput(MouseInput.get())) {
			Vector2f mousePosition = new Vector2f(MouseInput.get().getXAbsolute(), MouseInput.get().getYAbsolute());
			Ray ray = DisplaySystem.getDisplaySystem().getPickRay(mousePosition, false, new Ray());

			PickResults pickResults = new BoundingPickResults();
			pickResults.setCheckDistance(true);
			this.world.getLocalPlayerMap().getEntityNode().findPick(ray, pickResults);

			int c = 0;
			this.selectedEntity = null;
			while (c < pickResults.getNumber()) {
				PickData picked = pickResults.getPickData(c);
				this.selectedEntity = ((Entity.EntityPtr) picked.getTargetMesh().getParent().getParent().getUserData(
						"entity")).get();
				if (!this.selectedEntity.getNoPicking()) {
					break;
				} else {
					this.selectedEntity = null;
				}
				c += 1;
			}

			if (this.selectedEntity != null
					&& !this.selectionMarker.getPosition().equals(this.selectedEntity.getPosition())) {
				// Somehow highlight
				// this.hlLight.setLocation(new
				// Vector3f(this.selectedEntity.getPosition().x,
				// this.selectedEntity.getPosition().y,
				// this.hlLight.getLocation().z));
				this.selectionMarker.getMap().getEntityNode().attachChild((this.selectionMarker.getNode()));
				this.selectionMarker.setPos(this.selectedEntity.getPosition().x, this.selectedEntity.getPosition().y);
				this.hud.updateSelectionText();
			} else if (this.selectedEntity == null) {
				// this.hlLight.setLocation(new Vector3f(-10, -10,
				// this.hlLight.getLocation().z));
				this.selectionMarker.getNode().removeFromParent();
				this.selectionMarker.getNode().setLocalTranslation(-100, -100, 0.1f);
				this.hud.updateSelectionText();
			}

			if (MouseInput.get().isButtonDown(0)) {

				if (this.selectedEntity == null) {
					this.selectedEntity = this.walkTargetEntity;

					Vector3f loc = new Vector3f();
					Plane p = new Plane();
					p.setPlanePoints(new Vector3f(0, 1000, 0), new Vector3f(1000, 1000, 0), new Vector3f(0, 0, 0));

					ray.intersectsWherePlane(p, loc);

					this.walkTargetEntity.setPos(loc.getX(), loc.getY());
				}

				this.player.setInteractionTarget(this.selectedEntity);
			} else if (MouseInput.get().isButtonDown(1)) {
				if (this.selectedEntity == null) {
					this.selectedEntity = this.walkTargetEntity;

					Vector3f loc = new Vector3f();
					Plane p = new Plane();
					p.setPlanePoints(new Vector3f(0, 1000, 0), new Vector3f(1000, 1000, 0), new Vector3f(0, 0, 0));

					ray.intersectsWherePlane(p, loc);

					this.walkTargetEntity.setPos(loc.getX(), loc.getY());
				}
				this.player.castMagic(this.selectedEntity);
			}
		}

		this.world.update(interpolation);

		updateAutoCam(tpf);

		if (Game.CHASE_CAM) {
		}

		cam.setLocation(new Vector3f(this.player.getNode().getLocalTranslation().x - this.camDist
				* FastMath.cos(this.camAngle), this.player.getNode().getLocalTranslation().y - this.camDist
				* FastMath.sin(this.camAngle), this.camHeight));
		cam
				.lookAt(new Vector3f(this.player.getNode().getLocalTranslation().x, this.player.getNode()
						.getLocalTranslation().y, 0), new Vector3f(FastMath.cos(this.camAngle), FastMath
						.sin(this.camAngle), 0));

		parseAIThreadMessages();

		if (!this.elevatePlayer.equals(Elevation.NOWHERE)) {
			switch (this.elevatePlayer) {
				case QUEUE_DOWN :
					movePlayerToMap(this.world.getLocalPlayerMap().getLevel() + 1);
					Main.getInstance().toggleLoadingScreen(false, "");
					this.elevatePlayer = Elevation.NOWHERE;
					break;
				case QUEUE_UP :
					movePlayerToMap(this.world.getLocalPlayerMap().getLevel() - 1);
					Main.getInstance().toggleLoadingScreen(false, "");
					this.elevatePlayer = Elevation.NOWHERE;
					break;
				case DOWN :
					this.elevatePlayer = Elevation.QUEUE_DOWN;
					Main.getInstance().toggleLoadingScreen(true, "Loading...");
					break;
				case UP :
					this.elevatePlayer = Elevation.QUEUE_UP;
					Main.getInstance().toggleLoadingScreen(true, "Loading...");
					break;
			}
		}

		Profiler.stop("Game.stateUpdate");

	}

	private void parseAIThreadMessages() {
		if (this.aiBrainMsgq.hasNextA()) {
			MTMessage msg = this.aiBrainMsgq.nextA();

			switch (msg.type) {
				case AISTAT :
					MTMsgAIStat stat = (MTMsgAIStat) msg.obj;
					ConsoleLog.addLine("Live Flag: " + stat.isLiveFlag());
					ConsoleLog.addLine("Cycles/second: " + stat.getCps());
					ConsoleLog.addLine("Cycles/second (average): " + stat.getCpsAvg());
					ConsoleLog.addLine("Number of cells: " + stat.getCells());
					ConsoleLog.addLine("Number of pathing maps: " + stat.getPathingMaps());
					break;
			}
		}
	}

	private void toggleMapOverlay() {
		this.mapOverlayOn = !this.mapOverlayOn;
		this.mapOverlay.setActive(this.mapOverlayOn);
	}

	private void updateAutoCam(float t) {
		float da = Game.camPresetAngles[this.camPresetAngle] - this.camAngle;

		if (da > FastMath.PI)
			da -= FastMath.TWO_PI;
		if (da < -FastMath.PI)
			da += FastMath.TWO_PI;

		if (da < -(t * 1.01f * 10f)) {
			this.camAngle += -(t * 1.01f /* + (da / 100f) */);
		} else if (da > (t * 1.01f * 10f)) {
			this.camAngle += (t * 1.01f /* + (da / 100f) */);
		}
	}

	public void dumpCamInfo() {
		ConsoleLog.addLine("-----------------------");
		ConsoleLog.addLine("Cam dist: " + this.camDist);
		ConsoleLog.addLine("Cam angle: " + this.camAngle);
		ConsoleLog.addLine("Cam height: " + this.camHeight);
		ConsoleLog.addLine("-----------------------");
	}

	public void stateRender(float interpolation) {
		Profiler.start("Game.stateRender");
		this.fps = Game.timer.getFrameRate();
		Profiler.stop("Game.stateRender");
	}

	protected void initSystem() {
		Profiler.start("Game.initSystem");

		SkillTree.init();

		ResourceLocator locator = new ClasspathResourceLocator();

		ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_MODEL, locator);

		ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, locator);

		/** Get a high resolution timer for FPS updates. */
		Game.timer = Timer.getTimer();

		Profiler.stop("Game.initSystem");
	}

	private void initInput() {
		Profiler.start("Game.initInput");

		KeyBindingManager.getKeyBindingManager().set("exit", KeyInput.KEY_ESCAPE);
		KeyBindingManager.getKeyBindingManager().set("scrUp", KeyInput.KEY_UP);
		KeyBindingManager.getKeyBindingManager().set("scrDown", KeyInput.KEY_DOWN);
		KeyBindingManager.getKeyBindingManager().set("scrLeft", KeyInput.KEY_LEFT);
		KeyBindingManager.getKeyBindingManager().set("scrRight", KeyInput.KEY_RIGHT);
		KeyBindingManager.getKeyBindingManager().set("camZoomin", KeyInput.KEY_A);
		KeyBindingManager.getKeyBindingManager().set("camZoomout", KeyInput.KEY_Z);
		KeyBindingManager.getKeyBindingManager().set("respawn", KeyInput.KEY_RETURN);

		// Belt usage
		KeyBindingManager.getKeyBindingManager().set("beltSpot1", KeyInput.KEY_1);
		KeyBindingManager.getKeyBindingManager().set("beltSpot2", KeyInput.KEY_2);
		KeyBindingManager.getKeyBindingManager().set("beltSpot3", KeyInput.KEY_3);
		KeyBindingManager.getKeyBindingManager().set("beltSpot4", KeyInput.KEY_4);

		// HUD keys
		KeyBindingManager.getKeyBindingManager().set("hudInventory", KeyInput.KEY_I);
		KeyBindingManager.getKeyBindingManager().set("hudCharacterSheet", KeyInput.KEY_C);
		KeyBindingManager.getKeyBindingManager().set("hudSkillSheet", KeyInput.KEY_T);
		KeyBindingManager.getKeyBindingManager().set("hudMagics", KeyInput.KEY_S);
		KeyBindingManager.getKeyBindingManager().set("hudConsole", 43);
		KeyBindingManager.getKeyBindingManager().add("hudConsole", KeyInput.KEY_F4);
		KeyBindingManager.getKeyBindingManager().set("hudHideAll", KeyInput.KEY_SPACE);

		// Debug keys
		KeyBindingManager.getKeyBindingManager().set("debugPathingMap", KeyInput.KEY_F12);
		KeyBindingManager.getKeyBindingManager().set("debugShowAllEntities", KeyInput.KEY_F11);
		KeyBindingManager.getKeyBindingManager().set("debugKillMobs", KeyInput.KEY_F10);
		KeyBindingManager.getKeyBindingManager().set("debugProfileTotal", KeyInput.KEY_F9);
		KeyBindingManager.getKeyBindingManager().set("debugDumpInfo", KeyInput.KEY_F8);
		KeyBindingManager.getKeyBindingManager().set("debugMapOverlay", KeyInput.KEY_F7);

		Profiler.stop("Game.initInput");
	}

	private void initMouse() {
		Profiler.start("Game.initMouse");

		MouseInput.get().setCursorVisible(true);

		MouseInput.get().setHardwareCursor(Game.class.getClassLoader().getResource("dcg/data/cursors/1.png"));

		Profiler.stop("Game.initMouse");
	}

	private void initWorld() {
		Profiler.start("Game.initMap");

		this.world = new World();

		this.rootNode = new Node("Scene graph node");

		Profiler.stop("Game.initMap");
	}

	private void initPlayer(Map map) {
		Profiler.start("Game.initPlayer");

		this.player = new Player(map, "Player 1");
		this.player.setCaption("Player");
		Point startRoomCenter = map.getEntrance().getCenter();
		this.player.setPos((float) startRoomCenter.x * World.PATHING_GRANULARITY + World.BLOCKSIZE,
				(float) startRoomCenter.y * World.PATHING_GRANULARITY + World.BLOCKSIZE);
		map.getEntrance().addActor(this.player);
		this.world.setLocalPlayer(this.player);
		this.player.setNoPicking(true);

		Profiler.stop("Game.initPlayer");
	}

	private void initMonsters() {
		Profiler.start("Game.initMonsters");
		ModelFactory.preload(DataManager.findByNameAndType(DataType.MODEL_ID, "modelID_devildog"), 5);
		ModelFactory.preload(DataManager.findByNameAndType(DataType.MODEL_ID, "modelID_seademon"), 5);
		Profiler.stop("Game.initMonsters");

	}

	private void initDoodads() {
		Profiler.start("Game.initDoodads");
		ModelFactory.preload(DataManager.findByNameAndType(DataType.MODEL_ID, "modelID_gold"), 20);
		ModelFactory.preload(DataManager.findByNameAndType(DataType.MODEL_ID, "modelID_bookshelf"), 20);
		ModelFactory.preload(DataManager.findByNameAndType(DataType.MODEL_ID, "modelID_barrel"), 20);
		Profiler.stop("Game.initDoodads");
	}

	private void initGfx(Map map) {
		Profiler.start("Game.initGfx");

		EffectFactory.init(map.getEffectNode());
		ModelFactory.init();

		ZBufferState buf = DisplaySystem.getDisplaySystem().getRenderer().createZBufferState();
		buf.setEnabled(true);
		buf.setFunction(ZBufferState.TestFunction.LessThan);
		this.rootNode.setRenderState(buf);

		CullState cull = DisplaySystem.getDisplaySystem().getRenderer().createCullState();
		cull.setEnabled(true);
		cull.setCullFace(CullState.Face.Back);

		this.rootNode.setCullHint(CullHint.Dynamic);
		this.rootNode.setRenderState(cull);

		this.rootNode.setRenderQueueMode(Renderer.QUEUE_OPAQUE);

		this.rootNode.updateGeometricState(0.0f, true);
		this.rootNode.updateRenderState();

		this.selectionMarker = new Marker(map);
		this.selectionMarker.getNode().setLocalTranslation(0, 0, 0.1f);
		map.addEntity(this.selectionMarker);

		Profiler.stop("Game.initGfx");
	}

	private void initLights() {
		Profiler.start("Game.initLights");

		this.lights = DisplaySystem.getDisplaySystem().getRenderer().createLightState();

		this.pl = new PointLight();
		this.pl.setLocation(new Vector3f(0, 0, 10));
		this.pl.setDiffuse(new ColorRGBA(0.5f, 0.4f, 0.4f, 0.4f));
		this.pl.setAmbient(new ColorRGBA(0.3f, 0.3f, 0.3f, 0.4f));
		this.pl.setEnabled(true);
		this.pl.setAttenuate(true);
		this.pl.setConstant(.0f);
		this.pl.setQuadratic(.005f);
		this.pl.setShadowCaster(true);

		lights.setEnabled(true);
		lights.attach(this.pl);

		// Highlight light state
		this.hlLightState = DisplaySystem.getDisplaySystem().getRenderer().createLightState();

		this.hlLight = new SpotLight();
		this.hlLight.setLocation(new Vector3f(0, 0, 10));
		this.hlLight.setDiffuse(new ColorRGBA(1f, 1f, 0f, 1.0f));
		this.hlLight.setSpecular(new ColorRGBA(1f, 1f, 0f, 1.0f));
		this.hlLight.setAngle(5);
		this.hlLight.setEnabled(true);
		this.hlLight.setAttenuate(false);
		this.hlLight.setDirection(new Vector3f(0, 0, -1));

		this.hlLightState.setEnabled(true);
		this.hlLightState.attach(this.hlLight);

		this.lights.attach(this.hlLight);
		this.lights.setGlobalAmbient(new ColorRGBA(1, 1, 1, 1));
		// this.rootNode.setRenderState(hlLightState);

		// General final settings
		this.rootNode.setRenderState(lights);
		this.rootNode.setLightCombineMode(LightCombineMode.Replace);

		Profiler.stop("Game.initLights");
	}

	private void initFinal(Map map) {
		Profiler.start("Game.initFinal");

		this.rootNode.attachChild(this.world.getStartingMap().getSceneNode());

		this.rootNode.updateGeometricState(0, true);
		this.rootNode.updateRenderState();
		this.walkTargetEntity = new WalkTargetEntity(map);
		this.walkTargetEntity.getNode().setLocalTranslation(0, 0, 0);
		this.walkTargetEntity.setCaption("");

		this.player.addFloatingTextQueued("", Color.white, Color.black, 4f);

		this.world.getSanctuary().generateVendorItems(this.world.getLocalPlayer());

		Profiler.stop("Game.initFinal");
	}

	public void cleanup() {
		this.aiBrainMsgq.addB(new MTMessage(MessageType.KILL, null));
		System.out.println("Waiting for AI thread to terminate...");
		try {
			this.aiBrainThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		super.cleanup();
	}

	public void setLoadProgress(int loadProgress) {
		this.loadProgress = loadProgress;
	}

	public void setLoadProgress(int loadProgress, String text) {
		this.loadProgress = loadProgress;
		this.loadText = text;
	}

	public int getLoadProgress() {
		return loadProgress;
	}

	public String getLoadText() {
		return loadText;
	}

	public static Timer getTimer() {
		return Game.timer;
	}

	public float getFPS() {
		return this.fps;
	}

	public World getWorld() {
		return this.world;
	}

	public String getSelectionText() {
		if (this.selectedEntity == null || this.selectedEntity.getCaption() == null) {
			return "";
		} else {
			return this.selectedEntity.getCaption();
		}
	}

	public void setHUD(HUD hud) {
		this.hud = hud;
	}

	public HUD getHUD() {
		return this.hud;
	}

	public void setMapOverlay(MapOverlayState mapOverlayState) {
		this.mapOverlay = mapOverlayState;
	}

	public AIWorkerMain getAIBrain() {
		return this.aiBrain;
	}

	public void queueElevatePlayer(Elevation el) {
		this.elevatePlayer = el;
	}

	public boolean movePlayerToMap(int level) {
		return movePlayerToMap(this.world.getMap(level));
	}

	public boolean movePlayerToMap(Map map) {
		if (map == null) {
			return false;
		}

		this.world.getLocalPlayer().getMap().removeEntity(this.walkTargetEntity);
		this.world.getLocalPlayer().getMap().removeEntity(this.selectionMarker);
		this.world.getLocalPlayer().getMap().getSceneNode().removeFromParent();

		int prevLvl = this.world.getLocalPlayer().getMap().getLevel();

		this.world.getLocalPlayer().getRoom().removeEntity(this.world.getLocalPlayer());

		this.world.getLocalPlayer().setMap(map);
		Game.getInstance().getRootNode().attachChild(this.world.getLocalPlayer().getMap().getSceneNode());
		EffectFactory.setRootNode(map.getEffectNode());
		if (!map.isGenerated()) {
			map.generateMap();
		}
		if (map.getLevel() == 0) {
			this.world.getSanctuary().generateVendorItems(this.world.getLocalPlayer());
		}

		Room spawnRoom = map.getEntrance();
		if (map.getLevel() < prevLvl) {
			spawnRoom = map.getExit();
		}
		spawnRoom.addActor(this.world.getLocalPlayer());
		this.world.getLocalPlayer().getRoom().unlockRoom();
		this.player.setPos(World.pathingToWorld(spawnRoom.getCenter().x * World.PATHING_GRANULARITY + World.BLOCKSIZE),
				World.pathingToWorld(spawnRoom.getCenter().y * World.PATHING_GRANULARITY + World.BLOCKSIZE));
		this.player.setCurState(EntityState.IDLE);
		map.addEntity(this.walkTargetEntity);
		map.addEntity(this.selectionMarker);
		map.pruneAndGrowTask.fullPrune();

		if (this.player.getDeepestLevelReached() < map.getLevel()) {
			this.player.setDeepestLevelReached(map.getLevel());
		}

		DataNode storyNode = DataManager.findByNameAndType(DataType.STORY, "story_entering_" + map.getLevel());
		if (storyNode != null) {
			this.hud.showStoryBox(storyNode.getProp("text"), true);
		}

		return true;
	}

	public MTMessageQueue getAIBrainMsgq() {
		return this.aiBrainMsgq;
	}

	public void savePlayer() {
		// FileOutputStream fos;
		// ObjectOutputStream oos;
		//		
		// try {
		// fos = new FileOutputStream("saved");
		// oos = new ObjectOutputStream(fos);
		// oos.writeObject(this.world.getLocalPlayer());
		//			
		// oos.flush();
		// oos.close();
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

	public void firstTime() {
		this.hud.showStoryBox(DataManager.findByNameAndType(DataType.STORY, "story_intro").getProp("text"), true);
		// this.hud.showStoryBox(
		// "\n\n\nWelcome to Divide!\n\n\n" +
		// "Use the left mouse to navigate, attack, interact and\n" +
		// "pick up items, and the right mouse button to cast\n" +
		// "spells (when you learn them from books). \n\n\n" +
		// "Use the interface buttons to open your inventory,\n" +
		// "character stats, skills and spells. \n\n\n" +
		// "The red area represents your hit points, and the\n" +
		// "blue your spell points.\n\n\n" +
		// "You can trade items with one of the guys in the \n" +
		// "sanctuary (this area), and port to previous areas\n" +
		// "using the other guy.\n\n\nWalk down the stairs to\n" +
		// "begin your adventure...\n");
	}

	public void setDevMode(boolean b) {
		this.devMode = b;
	}
}