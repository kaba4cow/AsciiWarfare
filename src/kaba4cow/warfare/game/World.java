package kaba4cow.warfare.game;

import java.io.File;
import java.util.ArrayList;

import kaba4cow.ascii.core.Display;
import kaba4cow.ascii.drawing.drawers.Drawer;
import kaba4cow.ascii.drawing.glyphs.Glyphs;
import kaba4cow.ascii.drawing.gui.GUIColorText;
import kaba4cow.ascii.toolbox.Printer;
import kaba4cow.ascii.toolbox.files.DataFile;
import kaba4cow.ascii.toolbox.maths.Maths;
import kaba4cow.ascii.toolbox.rng.RNG;
import kaba4cow.warfare.Camera;
import kaba4cow.warfare.files.BiomeFile;
import kaba4cow.warfare.files.TerrainFile;
import kaba4cow.warfare.files.VegetationFile;
import kaba4cow.warfare.files.WeaponFile;
import kaba4cow.warfare.game.players.EnemyPlayer;
import kaba4cow.warfare.game.players.HumanPlayer;
import kaba4cow.warfare.game.players.Player;
import kaba4cow.warfare.game.world.Generator;
import kaba4cow.warfare.game.world.TerrainTile;
import kaba4cow.warfare.game.world.VegetationTile;
import kaba4cow.warfare.game.world.Viewport;
import kaba4cow.warfare.gui.ActionFrame;
import kaba4cow.warfare.gui.CurrentUnitFrame;
import kaba4cow.warfare.gui.InfoFrame;
import kaba4cow.warfare.gui.SelectedUnitFrame;
import kaba4cow.warfare.pathfinding.Node;

public class World {

	public static float PROGRESS = 0f;

	private final int size;

	private final TerrainTile[][] terrainMap;
	private final VegetationTile[][] vegetationMap;
	private final float[][] temperatureMap;
	private final ArrayList<Village> villages;

	private final Node[][] nodeMap;

	private HumanPlayer humanPlayer;
	private EnemyPlayer aiPlayer;

	private Camera camera;

	private CurrentUnitFrame currentUnitFrame;
	private SelectedUnitFrame selectedUnitFrame;
	private InfoFrame worldFrame;
	private ActionFrame actionFrame;

	private Viewport viewport;

	private int turn;
	private boolean playerTurn;

	private final DataFile data;

	public World(int size, int season) {
		this.size = size;
		this.terrainMap = new TerrainTile[size][size];
		this.vegetationMap = new VegetationTile[size][size];
		this.temperatureMap = new float[size][size];
		this.data = new DataFile();

		PROGRESS = 0f;

		long seed = RNG.randomLong();
		Printer.println("Generating new world [size = " + size + ", seed = " + seed + "]");
		Generator generator = new Generator(size, season, seed);
		generator.generate();
		PROGRESS = 0.2f;
		this.villages = generator.populate(terrainMap, vegetationMap, temperatureMap);
		PROGRESS = 0.4f;

		this.nodeMap = createNodeMap();
		PROGRESS = 0.5f;

		Village[] playerVillages = new Village[2];
		float maxVillageDistSq = 0f;
		for (int i = 0; i < villages.size(); i++) {
			Village village1 = villages.get(i);
			for (int j = 0; j < villages.size(); j++) {
				if (i == j)
					continue;
				Village village2 = villages.get(j);
				float distSq = Maths.distSq(village1.x, village1.y, village2.x, village2.y);
				if (distSq > maxVillageDistSq) {
					maxVillageDistSq = distSq;
					playerVillages[0] = village1;
					playerVillages[1] = village2;
				}
			}
		}
		PROGRESS = 0.7f;

		this.humanPlayer = new HumanPlayer(this);
		this.aiPlayer = new EnemyPlayer(this);
		this.humanPlayer.createUnits(playerVillages[0]);
		this.aiPlayer.createUnits(playerVillages[1]);
		this.turn = 0;
		this.playerTurn = true;

		PROGRESS = 0.8f;

		this.camera = new Camera(this);
		createGUI();
		setCameraTarget(humanPlayer.getCurrentUnit());

		PROGRESS = 1f;
	}

	public World() {
		PROGRESS = 0f;

		data = DataFile.read(new File("SAVE"));
		DataFile node;

		PROGRESS = 0.2f;

		this.size = data.node("Size").getInt();
		this.turn = data.node("Turn").getInt();
		this.playerTurn = true;

		PROGRESS = 0.3f;

		this.terrainMap = new TerrainTile[size][size];
		this.vegetationMap = new VegetationTile[size][size];
		this.temperatureMap = new float[size][size];
		node = data.node("Map");
		for (int i = 0; i < node.objectSize(); i++) {
			DataFile tile = node.node(i);
			String biomeID = tile.getString(0);
			float temperature = tile.getFloat(1);
			String terrainID = tile.getString(2);
			String vegetationID = tile.getString(3);

			BiomeFile biome = BiomeFile.get(biomeID);
			TerrainFile terrain = TerrainFile.get(terrainID);
			VegetationFile vegetation = vegetationID.equals("-") ? null : VegetationFile.get(vegetationID);

			int x = i % size;
			int y = i / size;
			temperatureMap[x][y] = temperature;
			terrainMap[x][y] = new TerrainTile(terrain, biome, temperature);
			vegetationMap[x][y] = vegetation == null ? null : new VegetationTile(vegetation, temperature);
		}

		PROGRESS = 0.6f;

		this.villages = new ArrayList<>();
		node = data.node("Villages");
		for (int i = 0; i < node.objectSize(); i++)
			villages.add(new Village(node.node(i)));

		PROGRESS = 0.7f;

		this.nodeMap = createNodeMap();

		PROGRESS = 0.8f;

		node = data.node("Players");
		this.humanPlayer = new HumanPlayer(this, node.node("PLAYER"));
		this.aiPlayer = new EnemyPlayer(this, node.node("ENEMY"));

		PROGRESS = 0.9f;

		this.camera = new Camera(this);
		createGUI();
		setCameraTarget(humanPlayer.getCurrentUnit());

		PROGRESS = 1f;
	}

	public DataFile save(boolean save) {
		DataFile node;

		PROGRESS = 0f;

		int x, y, index;

		data.node("Size").setInt(size);
		data.node("Turn").setInt(turn);

		PROGRESS = 0.2f;

		node = data.node("Players");
		humanPlayer.save(node.node("PLAYER"));
		aiPlayer.save(node.node("ENEMY"));

		PROGRESS = 0.4f;

		node = data.node("Villages").clear();
		index = 0;
		for (index = 0; index < villages.size(); index++)
			villages.get(index).save(node.node(Integer.toString(index)));

		PROGRESS = 0.5f;

		node = data.node("Map");
		index = 0;
		for (y = 0; y < size; y++)
			for (x = 0; x < size; x++) {
				node.node(Integer.toString(index))//
						.setString(terrainMap[x][y].getBiome().getID())//
						.setFloat(temperatureMap[x][y])//
						.setString(terrainMap[x][y].getFile().getID())//
						.setString(vegetationMap[x][y] == null ? "-" : vegetationMap[x][y].getFile().getID());
				index++;
			}

		PROGRESS = 0.8f;

		if (save)
			DataFile.write(data, new File("SAVE"));

		PROGRESS = 1f;

		return data;
	}

	private void createGUI() {
		currentUnitFrame = new CurrentUnitFrame();
		selectedUnitFrame = new SelectedUnitFrame();
		worldFrame = new InfoFrame();
		actionFrame = new ActionFrame();
		viewport = new Viewport(Display.getWidth() / 4, Display.getHeight() / 5,
				Display.getWidth() - Display.getWidth() / 4, Display.getHeight() - Display.getHeight() / 5);
	}

	public void update(float dt) {
		camera.update(dt);

		if (playerTurn)
			humanPlayer.update(dt);
		else
			aiPlayer.update(dt);

		currentUnitFrame.update();
		selectedUnitFrame.update();
		worldFrame.update();
		actionFrame.update();
	}

	public void render() {
		if (viewport.width != Display.getWidth() || viewport.height != Display.getHeight())
			viewport = new Viewport(Display.getWidth() / 4, Display.getHeight() / 5,
					Display.getWidth() - Display.getWidth() / 4, Display.getHeight() - Display.getHeight() / 5);

		int offX = (int) camera.getX();
		int offY = (int) camera.getY();

		Drawer.setFrame(viewport);
		viewport.fill(Glyphs.SPACE, 0);

		int x, y, ix, iy;
		for (y = 0; y < viewport.height; y++) {
			iy = y + offY;
			if (iy < 0 || iy >= size)
				continue;
			for (x = 0; x < viewport.width; x++) {
				ix = x + offX;
				if (ix < 0 || ix >= size || !humanPlayer.isVisible(ix, iy))
					continue;

				if (vegetationMap[ix][iy] == null)
					terrainMap[ix][iy].render(x, y);
				else
					vegetationMap[ix][iy].render(x, y);
			}
		}
		aiPlayer.render(offX, offY);
		humanPlayer.render(offX, offY);

		Drawer.resetFrame();

		viewport.render();
		currentUnitFrame.render(humanPlayer.getCurrentUnit());
		Unit mouseUnit = humanPlayer.getUnit(camera.getMouseX(), camera.getMouseY());
		if (mouseUnit == null)
			mouseUnit = aiPlayer.getUnit(camera.getMouseX(), camera.getMouseY());
		if (mouseUnit != null)
			selectedUnitFrame.render(humanPlayer.getCurrentUnit(), mouseUnit);
		else
			worldFrame.render(this);
		actionFrame.render();
		if (humanPlayer.isAiming())
			humanPlayer.getCurrentUnit().renderWeaponFrame();

		if (!camera.isMouseInViewport())
			Display.setDrawCursor(true);
		Display.setCursorWaiting(!playerTurn);
	}

	public void setCameraTarget(Unit unit) {
		camera.setPosition(unit.getX(), unit.getY());
	}

	public void createTrack(int x, int y) {
		if (terrainMap[x][y].allowsTrack())
			terrainMap[x][y] = new TerrainTile(TerrainFile.getTrack(), terrainMap[x][y].getBiome(),
					temperatureMap[x][y]);
	}

	public void createCrater(int x, int y) {
		vegetationMap[x][y] = null;
		if (terrainMap[x][y].allowsCrater())
			terrainMap[x][y] = new TerrainTile(TerrainFile.getCrater(), terrainMap[x][y].getBiome(),
					temperatureMap[x][y]);
	}

	public void damageUnits(int x, int y, Unit source, WeaponFile weapon) {
		if (weapon.createsCrater() && vegetationMap[x][y] == null)
			createCrater(x, y);

		damageUnit(x, y, source, weapon);
		float radius = weapon.getRadius();
		if (radius > 0f) {
			int range = 1 + (int) radius;
			int ix, iy;
			float dist;
			for (iy = y - range; iy <= y + range; iy++)
				for (ix = x - range; ix <= x + range; ix++) {
					if (ix == x && iy == y || ix < 0 || ix >= size || iy < 0 || iy >= size)
						continue;
					dist = Maths.dist(x, y, ix, iy);
					if (dist <= radius)
						damageUnit(ix, iy, source, weapon);
				}
		}
	}

	private void damageUnit(int x, int y, Unit source, WeaponFile weapon) {
		if (weapon.createsCrater())
			vegetationMap[x][y] = null;
		Unit unit = humanPlayer.getUnit(x, y);
		if (unit == null)
			unit = aiPlayer.getUnit(x, y);
		if (unit == null)
			return;
		unit.damage(source);
	}

	public void newTurn(Player player) { // TODO
		player.onNewTurn();
//		playerTurn = !playerTurn;
//		if (playerTurn)
		turn++;
	}

	public GUIColorText addAction() {
		return actionFrame.addAction(this);
	}

	public int getSize() {
		return size;
	}

	public boolean isObstacle(int x, int y) {
		if (x < 0 || x >= size || y < 0 || y >= size)
			return true;
		return vegetationMap[x][y] != null || humanPlayer.getUnit(x, y) != null || aiPlayer.getUnit(x, y) != null;
	}

	public boolean isVisible(Player player, int x, int y) {
		if (x < 0 || x >= size || y < 0 || y >= size)
			return false;
		return player.isVisible(x, y);
	}

	public float getPenalty(int x, int y) {
		if (x < 0 || x >= size || y < 0 || y >= size)
			return 0f;
		return terrainMap[x][y].getPenalty();
	}

	public TerrainTile getTerrain(int x, int y) {
		if (x < 0 || x >= size || y < 0 || y >= size)
			return null;
		return terrainMap[x][y];
	}

	public VegetationTile getVegetation(int x, int y) {
		if (x < 0 || x >= size || y < 0 || y >= size)
			return null;
		return vegetationMap[x][y];
	}

	public float getTemperature(int x, int y) {
		if (x < 0 || x >= size || y < 0 || y >= size)
			return 0.5f;
		return temperatureMap[x][y];
	}

	public Node[][] getNodeMap() {
		return nodeMap;
	}

	private Node[][] createNodeMap() {
		Node[][] map = new Node[size][size];
		for (int y = 0; y < size; y++)
			for (int x = 0; x < size; x++)
				map[x][y] = new Node(x, y, getPenalty(x, y));
		return map;
	}

	public HumanPlayer getHumanPlayer() {
		return humanPlayer;
	}

	public EnemyPlayer getAIPlayer() {
		return aiPlayer;
	}

	public boolean isPlayerTurn() {
		return playerTurn;
	}

	public Camera getCamera() {
		return camera;
	}

	public Viewport getViewport() {
		return viewport;
	}

	public int getTurns() {
		return turn;
	}

	public int getWorldWeek() {
		return turn / (24 * 7);
	}

	public int getWorldDay() {
		return (turn / 24) % 7;
	}

	public int getWorldHour() {
		return turn % 24;
	}

}
