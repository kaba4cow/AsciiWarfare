package kaba4cow.warfare.game;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import kaba4cow.ascii.core.Display;
import kaba4cow.ascii.drawing.drawers.Drawer;
import kaba4cow.ascii.drawing.glyphs.Glyphs;
import kaba4cow.ascii.drawing.gui.GUIColorText;
import kaba4cow.ascii.toolbox.Printer;
import kaba4cow.ascii.toolbox.files.DataFile;
import kaba4cow.ascii.toolbox.maths.Maths;
import kaba4cow.ascii.toolbox.rng.RNG;
import kaba4cow.ascii.toolbox.rng.RandomLehmer;
import kaba4cow.warfare.Camera;
import kaba4cow.warfare.Game;
import kaba4cow.warfare.files.BiomeFile;
import kaba4cow.warfare.files.TerrainFile;
import kaba4cow.warfare.files.VegetationFile;
import kaba4cow.warfare.files.WeaponFile;
import kaba4cow.warfare.game.controllers.AIController;
import kaba4cow.warfare.game.controllers.ClientController;
import kaba4cow.warfare.game.controllers.Controller;
import kaba4cow.warfare.game.controllers.PlayerController;
import kaba4cow.warfare.game.world.Generator;
import kaba4cow.warfare.game.world.TerrainTile;
import kaba4cow.warfare.game.world.VegetationTile;
import kaba4cow.warfare.game.world.Viewport;
import kaba4cow.warfare.gui.ActionFrame;
import kaba4cow.warfare.gui.CurrentUnitFrame;
import kaba4cow.warfare.gui.InfoFrame;
import kaba4cow.warfare.gui.SelectedUnitFrame;
import kaba4cow.warfare.network.Message;
import kaba4cow.warfare.network.tcp.Client;
import kaba4cow.warfare.pathfinding.Node;
import kaba4cow.warfare.states.State;

public class World {

	private final int size;

	private final TerrainTile[][] terrainMap;
	private final VegetationTile[][] vegetationMap;
	private final float[][] temperatureMap;
	private final ArrayList<Village> villages;

	private final Node[][] nodeMap;

	private ArrayList<Player> players;

	private Camera camera;

	private CurrentUnitFrame currentUnitFrame;
	private SelectedUnitFrame selectedUnitFrame;
	private InfoFrame worldFrame;
	private ActionFrame actionFrame;

	private Viewport viewport;

	private int turn;
	private int turnPlayer;
	private int currentPlayer;

	private Client client;

	private final DataFile data;

	public World(float size, int season) {
		this(size, season, RNG.randomLong());
	}

	public World(float size, int season, long seed) {
		this.size = 10 * (int) Maths.mapLimit(size, 0f, 1f, Game.MIN_WORLD_SIZE, Game.MAX_WORLD_SIZE);
		this.terrainMap = new TerrainTile[this.size][this.size];
		this.vegetationMap = new VegetationTile[this.size][this.size];
		this.temperatureMap = new float[this.size][this.size];
		this.data = new DataFile();

		State.PROGRESS = 0f;

		Printer.println("Generating new world [size = " + size + ", seed = " + seed + "]");
		Generator generator = new Generator(this.size, season % 4, seed);
		generator.generate();
		State.PROGRESS = 0.2f;
		this.villages = generator.populate(terrainMap, vegetationMap, temperatureMap);
		State.PROGRESS = 0.4f;

		this.nodeMap = createNodeMap();
		State.PROGRESS = 0.5f;

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
		State.PROGRESS = 0.7f;

		this.players = new ArrayList<>();
		for (int i = 0; i < 2; i++)
			players.add(new Player(this));
		RandomLehmer rng = new RandomLehmer(seed);
		for (int i = 0; i < players.size(); i++)
			players.get(i).createUnits(playerVillages[i], rng);
		this.turn = 0;
		this.turnPlayer = 0;
		this.currentPlayer = 0;

		State.PROGRESS = 0.8f;

		this.camera = new Camera(this);
		createGUI();

		State.PROGRESS = 1f;
	}

	public World() throws IOException {
		State.PROGRESS = 0f;

		File file = new File("SAVE");
		if (!file.exists() || !file.isFile())
			throw new IOException();

		data = DataFile.read(file);
		DataFile node;

		State.PROGRESS = 0.2f;

		this.size = data.node("Size").getInt();
		this.turn = data.node("Turn").getInt();
		this.turnPlayer = 0;
		this.currentPlayer = data.node("Player").getInt();

		State.PROGRESS = 0.3f;

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

		State.PROGRESS = 0.6f;

		this.villages = new ArrayList<>();
		node = data.node("Villages");
		for (int i = 0; i < node.objectSize(); i++)
			villages.add(new Village(node.node(i)));

		State.PROGRESS = 0.7f;

		this.nodeMap = createNodeMap();

		State.PROGRESS = 0.8f;

		node = data.node("Players");
		players = new ArrayList<>();
		for (int i = 0; i < node.objectSize(); i++)
			players.add(new Player(this, node.node(i)));

		State.PROGRESS = 0.9f;

		this.camera = new Camera(this);
		createGUI();
		setCurrentPlayer(currentPlayer, true);

		State.PROGRESS = 1f;
	}

	public void setCurrentPlayer(int currentPlayer, boolean ai) {
		this.currentPlayer = currentPlayer;
		getCurrentPlayer().setController(new PlayerController());
		setCameraTarget(getCurrentPlayer().getCurrentUnit());
		Controller enemyController = ai ? new AIController() : new ClientController();
		getEnemyPlayer().setController(enemyController);
	}

	public DataFile save(boolean save) {
		DataFile node;

		State.PROGRESS = 0f;

		int x, y, index;

		data.node("Size").setInt(size);
		data.node("Turn").setInt(turn);
		data.node("Player").setInt(currentPlayer);

		State.PROGRESS = 0.2f;

		node = data.node("Players");
		for (index = 0; index < players.size(); index++)
			players.get(index).save(node.node(Integer.toString(index)));

		State.PROGRESS = 0.4f;

		node = data.node("Villages").clear();
		index = 0;
		for (index = 0; index < villages.size(); index++)
			villages.get(index).save(node.node(Integer.toString(index)));

		State.PROGRESS = 0.5f;

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

		State.PROGRESS = 0.8f;

		if (save)
			DataFile.write(data, new File("SAVE"));

		State.PROGRESS = 1f;

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

		for (int i = 0; i < players.size(); i++)
			players.get(i).update(dt);
		players.get(turnPlayer).updateController(dt);

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

		Player player = getCurrentPlayer();

		int x, y, ix, iy;
		for (y = 0; y < viewport.height; y++) {
			iy = y + offY;
			if (iy < 0 || iy >= size)
				continue;
			for (x = 0; x < viewport.width; x++) {
				ix = x + offX;
				if (ix < 0 || ix >= size)
					continue;
				if (!player.isVisible(ix, iy))
					Drawer.draw(x, y, Glyphs.SPACE, 0x000000);
				else if (vegetationMap[ix][iy] == null)
					terrainMap[ix][iy].render(x, y);
				else
					vegetationMap[ix][iy].render(x, y);
			}
		}
		for (int i = 0; i < players.size(); i++)
			players.get(i).render(offX, offY);
		getCurrentPlayer().renderController(offX, offY);

		Drawer.resetFrame();

		viewport.render();
		currentUnitFrame.render(player.getCurrentUnit());
		Unit mouseUnit = getUnit(camera.getMouseX(), camera.getMouseY());
		if (mouseUnit != null)
			selectedUnitFrame.render(player.getCurrentUnit(), mouseUnit);
		else
			worldFrame.render(this);
		actionFrame.render();
		if (player.isAiming())
			player.getCurrentUnit().renderWeaponFrame();

		if (!camera.isMouseInViewport())
			Display.setDrawCursor(true);
		Display.setCursorWaiting(!isPlayerTurn());
	}

	public Unit getUnit(int x, int y) {
		for (int i = 0; i < players.size(); i++) {
			Unit unit = players.get(i).getUnit(x, y);
			if (unit != null)
				return unit;
		}
		return null;
	}

	public void setCameraTarget(Unit unit) {
		camera.setPosition(unit.getX(), unit.getY());
	}

	public void createExplosion(int x, int y, float radius, boolean send) {
		vegetationMap[x][y] = null;
		if (terrainMap[x][y].allowsCrater())
			terrainMap[x][y] = new TerrainTile(TerrainFile.getCrater(), terrainMap[x][y].getBiome(),
					temperatureMap[x][y]);
		if (radius > 0f) {
			int range = (int) (1f + radius);
			int ix, iy;
			float dist;
			for (iy = y - range; iy <= y + range; iy++)
				for (ix = x - range; ix <= x + range; ix++) {
					if (ix == x && iy == y || ix < 0 || ix >= size || iy < 0 || iy >= size)
						continue;
					dist = Maths.dist(x, y, ix, iy);
					if (dist <= radius)
						vegetationMap[ix][iy] = null;
				}
		}
	}

	public void createProjectile(int index, int weapon, int x, int y, boolean send) {
		if (client == null)
			return;
		if (send)
			client.send(Message.PROJECTILE, index, weapon, x, y);
		else {
			Unit unit = getEnemyPlayer().getUnit(index);
			unit.setCurrentWeapon(weapon);
			unit.createAttackPath(x, y);
			if (!unit.isShooting())
				unit.createAttackPath(x, y);
		}
	}

	public void moveUnit(int index, int x, int y, boolean send) {
		if (client == null)
			return;
		if (send)
			client.send(Message.MOVE, index, x, y);
		else
			getEnemyPlayer().getUnit(index).setPos(x, y);
	}

	public void damageUnits(int x, int y, Unit source, WeaponFile weapon) {
		if (weapon.createsCrater())
			createExplosion(x, y, weapon.getRadius(), true);

		damageUnit(x, y, source, weapon);
		float radius = weapon.getRadius();
		if (radius > 0f) {
			int range = (int) (1f + radius);
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
		Unit unit = getUnit(x, y);
		if (unit == null)
			return;
		unit.damage(source);
	}

	public void newTurn(Player player, boolean send) {
		if (client != null && send)
			client.send(Message.TURN);
		player.onNewTurn();
		turnPlayer++;
		if (turnPlayer >= players.size()) {
			turnPlayer = 0;
			turn++;
		}
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
		return vegetationMap[x][y] != null || getUnit(x, y) != null;
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

	public boolean isPlayerTurn() {
		return turnPlayer == currentPlayer;
	}

	public int getTurnPlayer() {
		return turnPlayer;
	}

	public Player getCurrentPlayer() {
		return players.get(currentPlayer);
	}

	public Player getEnemyPlayer() {
		return players.get(players.size() - currentPlayer - 1);
	}

	public void setClient(Client client) {
		this.client = client;
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
