package kaba4cow.warfare.game;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import kaba4cow.ascii.core.Display;
import kaba4cow.ascii.drawing.drawers.Drawer;
import kaba4cow.ascii.drawing.glyphs.Glyphs;
import kaba4cow.ascii.drawing.gui.GUIColorText;
import kaba4cow.ascii.toolbox.Colors;
import kaba4cow.ascii.toolbox.files.DataFile;
import kaba4cow.ascii.toolbox.maths.Maths;
import kaba4cow.ascii.toolbox.maths.vectors.Vector2i;
import kaba4cow.ascii.toolbox.rng.RNG;
import kaba4cow.ascii.toolbox.rng.RandomLehmer;
import kaba4cow.warfare.Camera;
import kaba4cow.warfare.Controls;
import kaba4cow.warfare.files.TerrainFile;
import kaba4cow.warfare.files.WeaponFile;
import kaba4cow.warfare.game.controllers.AIController;
import kaba4cow.warfare.game.controllers.ClientController;
import kaba4cow.warfare.game.controllers.Controller;
import kaba4cow.warfare.game.controllers.PlayerController;
import kaba4cow.warfare.game.world.Generator;
import kaba4cow.warfare.game.world.TerrainTile;
import kaba4cow.warfare.game.world.VegetationTile;
import kaba4cow.warfare.gui.Viewport;
import kaba4cow.warfare.gui.game.ActionFrame;
import kaba4cow.warfare.gui.game.CurrentUnitFrame;
import kaba4cow.warfare.gui.game.GameOverFrame;
import kaba4cow.warfare.gui.game.HelpFrame;
import kaba4cow.warfare.gui.game.SelectedUnitFrame;
import kaba4cow.warfare.gui.game.WorldFrame;
import kaba4cow.warfare.gui.info.InfoFrame;
import kaba4cow.warfare.gui.shop.ShopFrame;
import kaba4cow.warfare.network.Message;
import kaba4cow.warfare.network.tcp.Client;
import kaba4cow.warfare.pathfinding.Node;
import kaba4cow.warfare.states.State;

public class World {

	public static final int SIZE = 250;
	public static final int ELEVATION = 5;

	private final long inputSeed;
	private final int inputSeason;

	private final Node[][] nodeMap;
	private final TerrainTile[][] terrainMap;
	private final VegetationTile[][] vegetationMap;
	private final int[][] elevationMap;
	private final boolean[][] topologyMap;
	private final float[][] temperatureMap;
	private final HashMap<Vector2i, String> terrain;

	private final ArrayList<Player> players;
	private final ArrayList<Village> villages;

	private Camera camera;

	private CurrentUnitFrame currentUnitFrame;
	private SelectedUnitFrame selectedUnitFrame;
	private WorldFrame worldFrame;
	private ActionFrame actionFrame;
	private HelpFrame helpFrame;
	private GameOverFrame gameOverFrame;
	private Viewport viewport;

	private ShopFrame shopFrame;
	private InfoFrame infoFrame;
	private boolean gui;

	private int turn;
	private int turnPlayer;
	private int currentPlayer;

	private Client client;

	public World(int season, long seed) {
		this.inputSeed = seed;
		this.inputSeason = season;

		this.terrainMap = new TerrainTile[SIZE][SIZE];
		this.vegetationMap = new VegetationTile[SIZE][SIZE];
		this.elevationMap = new int[SIZE][SIZE];
		this.topologyMap = new boolean[SIZE][SIZE];
		this.temperatureMap = new float[SIZE][SIZE];

		Generator generator = new Generator(inputSeason, seed);
		generator.generate();
		this.villages = generator.populate(terrainMap, vegetationMap, elevationMap, topologyMap, temperatureMap);

		this.nodeMap = createNodeMap();

		int[] playerVillages = new int[2];
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
					playerVillages[0] = i;
					playerVillages[1] = j;
				}
			}
		}

		State.PROGRESS = 0.7f;

		this.players = new ArrayList<>();
		for (int i = 0; i < 2; i++)
			players.add(new Player(this, i));
		for (int i = 0; i < players.size(); i++)
			players.get(i).createUnits(villages.get(playerVillages[i]));
		this.turn = 0;
		this.turnPlayer = 0;
		this.currentPlayer = 0;
		this.terrain = new HashMap<>();

		State.PROGRESS = 0.8f;

		this.camera = new Camera(this);
		createGUI();

		State.PROGRESS = 1f;
	}

	public World(DataFile data, int id) {
		DataFile node;

		this.inputSeed = data.node("Seed").getLong();
		this.inputSeason = data.node("Season").getInt();

		this.turn = data.node("Turn").getInt(0);
		this.turnPlayer = data.node("Turn").getInt(1);
		this.currentPlayer = id < 0 ? data.node("Player").getInt() : id;

		this.terrainMap = new TerrainTile[SIZE][SIZE];
		this.vegetationMap = new VegetationTile[SIZE][SIZE];
		this.elevationMap = new int[SIZE][SIZE];
		this.topologyMap = new boolean[SIZE][SIZE];
		this.temperatureMap = new float[SIZE][SIZE];

		Generator generator = new Generator(inputSeason, inputSeed);
		generator.generate();

		this.villages = generator.populate(terrainMap, vegetationMap, elevationMap, topologyMap, temperatureMap);
		this.terrain = new HashMap<>();

		node = data.node("Map");
		for (int i = 0; i < node.objectSize(); i++) {
			DataFile tile = node.node(i);
			int x = tile.getInt(0);
			int y = tile.getInt(1);
			String terrainID = tile.getString(2);
			TerrainFile terrainFile = TerrainFile.get(terrainID);

			terrainMap[x][y] = new TerrainTile(terrainFile, terrainMap[x][y].getBiome(), temperatureMap[x][y]);
			vegetationMap[x][y] = null;
			terrain.put(new Vector2i(x, y), terrainID);
		}

		State.PROGRESS = 0.7f;

		this.nodeMap = createNodeMap();

		node = data.node("Players");
		players = new ArrayList<>();
		for (int i = 0; i < node.objectSize(); i++)
			players.add(new Player(this, i, node.node(i)));

		State.PROGRESS = 1f;

		this.camera = new Camera(this);
		createGUI();
		setCurrentPlayer(currentPlayer, id < 0);
	}

	public void setCurrentPlayer(int currentPlayer, boolean ai) {
		this.currentPlayer = currentPlayer;
		getPlayer().setController(new PlayerController());
		if (getPlayer().hasUnits())
			setCameraTarget(getPlayer().getCurrentUnit());
		Controller enemyController = ai ? new AIController() : new ClientController();
		getPlayer(1 - currentPlayer).setController(enemyController);
	}

	public void setCurrentPlayer(int currentPlayer) {
		this.currentPlayer = currentPlayer;
		getPlayer(0).setController(null);
		getPlayer(1).setController(null);
	}

	public DataFile getDataFile() {
		DataFile data = new DataFile();
		DataFile node;
		State.PROGRESS = 0f;

		int index;

		data.node("Seed").clear().setLong(inputSeed);
		data.node("Season").clear().setInt(inputSeason);

		data.node("Turn").clear().setInt(turn).setInt(turnPlayer);
		data.node("Player").clear().setInt(currentPlayer);
		State.PROGRESS = 0.2f;

		node = data.node("Players");
		for (index = 0; index < players.size(); index++)
			players.get(index).save(node.node(Integer.toString(index)));
		State.PROGRESS = 0.4f;

		node = data.node("Map").clear();
		index = 0;
		for (Vector2i pos : terrain.keySet())
			node.node(Integer.toString(index++)).clear()//
					.setInt(pos.x).setInt(pos.y)//
					.setString(terrain.get(pos));
		State.PROGRESS = 0.7f;

		return data;
	}

	public void save() {
		DataFile data = getDataFile();
		DataFile.write(data, new File("SAVE"));
		State.PROGRESS = 1f;
	}

	private void createGUI() {
		currentUnitFrame = new CurrentUnitFrame();
		selectedUnitFrame = new SelectedUnitFrame();
		worldFrame = new WorldFrame();
		actionFrame = new ActionFrame();
		helpFrame = new HelpFrame();
		gui = false;
		createViewport();
	}

	private void openShop() {
		gui = true;
		shopFrame = new ShopFrame(getPlayer());
	}

	private void openInfo() {
		gui = true;
		infoFrame = new InfoFrame(this, getPlayer());
	}

	public boolean canExit() {
		return !gui && gameOverFrame == null;
	}

	private void createViewport() {
		viewport = new Viewport(Display.getWidth() / 4, Display.getHeight() / 5,
				Display.getWidth() - Display.getWidth() / 4, Display.getHeight() - Display.getHeight() / 5);
	}

	public void update(float dt) {
		if (!isGameOver())
			for (int i = 0; i < players.size(); i++)
				if (!players.get(i).hasUnits()) {
					gameOverFrame = new GameOverFrame(this, players.get(1 - i), getPlayer());
					getPlayer().setAiming(false);
					break;
				}
		if (isGameOver()) {
			gameOverFrame.update();
			return;
		}

		if (!gui && isPlayerTurn()) {
			if (Controls.SHOP.isKeyDown() && getPlayer().canAccessShop())
				openShop();
			else if (Controls.INFO.isKeyDown())
				openInfo();
		} else if (gui && Controls.PAUSE.isKeyDown() && (shopFrame == null || shopFrame.canExit())) {
			shopFrame = null;
			infoFrame = null;
			gui = false;
		}

		if (gui) {
			if (shopFrame != null)
				shopFrame.update();
			else if (infoFrame != null)
				infoFrame.update();
			return;
		}

		camera.update(dt);

		for (int i = 0; i < villages.size(); i++) {
			villages.get(i).update(this);
			if (villages.get(i).getHouses() <= 0)
				villages.remove(i);
		}
		for (int i = 0; i < players.size(); i++)
			players.get(i).update(dt);
		players.get(turnPlayer).updateController(dt);

		currentUnitFrame.update();
		selectedUnitFrame.update();
		worldFrame.update();
		actionFrame.update();
		helpFrame.update();
	}

	public void render() {
		if (gui) {
			if (shopFrame != null)
				shopFrame.render();
			else if (infoFrame != null)
				infoFrame.render();
			return;
		}

		if (viewport.width != Display.getWidth() - Display.getWidth() / 4
				|| viewport.height != Display.getHeight() - Display.getHeight() / 5)
			createViewport();

		int offX = (int) camera.getX();
		int offY = (int) camera.getY();

		Drawer.setFrame(viewport);

		Player player = getPlayer();

		int x, y, ix, iy;
		for (y = 0; y < viewport.height; y++) {
			iy = y + offY;
			if (iy < 0 || iy >= SIZE)
				continue;
			for (x = 0; x < viewport.width; x++) {
				ix = x + offX;
				if (ix < 0 || ix >= SIZE)
					continue;
				if (!player.isVisible(ix, iy))
					Drawer.draw(x, y, Glyphs.SPACE, 0x000000);
				else {
					if (vegetationMap[ix][iy] == null)
						terrainMap[ix][iy].render(x, y);
					else
						vegetationMap[ix][iy].render(x, y);

					if (topologyMap[ix][iy])
						Drawer.draw(x, y, Glyphs.SPACE, Drawer.IGNORE_FOREGROUND | Drawer.IGNORE_GLYPH
								| Colors.createBackground(elevationMap[ix][iy]));
				}
			}
		}

		for (int i = 0; i < players.size(); i++)
			players.get(i).render(offX, offY);
		player.renderController(offX, offY);

		Drawer.resetFrame();

		viewport.render();
		currentUnitFrame.render(player.getCurrentUnit());
		Unit mouseUnit = getUnit(camera.getMouseX(), camera.getMouseY());
		if (mouseUnit != null)
			selectedUnitFrame.render(player.getCurrentUnit(), mouseUnit);
		else
			worldFrame.render(this);
		actionFrame.render();
		if (player.isAiming() && player.hasUnits())
			player.getCurrentUnit().renderWeaponFrame();

		if (isGameOver())
			gameOverFrame.render();
		else if (Controls.HELP.isKey())
			helpFrame.render();

		if (!camera.isMouseInViewport())
			Display.setDrawCursor(true);
		Display.setCursorWaiting(!isPlayerTurn() && gameOverFrame == null);
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
		if (terrainMap[x][y].allowsCrater()) {
			terrainMap[x][y] = new TerrainTile(TerrainFile.getCrater(), terrainMap[x][y].getBiome(),
					temperatureMap[x][y]);
			terrain.put(new Vector2i(x, y), TerrainFile.getCrater().getID());
		}
		if (radius > 0f) {
			int range = (int) (1f + radius);
			int ix, iy;
			float dist;
			for (iy = y - range; iy <= y + range; iy++)
				for (ix = x - range; ix <= x + range; ix++) {
					if (ix == x && iy == y || ix < 0 || ix >= SIZE || iy < 0 || iy >= SIZE
							|| vegetationMap[ix][iy] == null)
						continue;
					dist = Maths.dist(x, y, ix, iy);
					if (dist <= radius) {
						vegetationMap[ix][iy] = null;
						terrain.put(new Vector2i(ix, iy), terrainMap[ix][iy].getFile().getID());
					}
				}
		}
	}

	public void createProjectile(int player, int index, int weapon, int x, int y, long seed, boolean send) {
		if (send) {
			if (client != null)
				client.send(Message.PROJECTILE, player, index, weapon, x, y, seed);
		} else {
			Unit unit = getPlayer(player).getUnit(index);
			unit.setCurrentWeapon(weapon);
			unit.createAttackPath(x, y, seed);
			if (!unit.isShooting())
				unit.createAttackPath(x, y, seed);
		}
	}

	public void moveUnit(int player, int index, int x, int y, boolean send) {
		if (send) {
			if (client != null)
				client.send(Message.MOVE, player, index, x, y);
		} else
			getPlayer(player).getUnit(index).move(x, y);
	}

	public void joinUnits(int player, int index1, int index2, boolean send) {
		if (send) {
			if (client != null)
				client.send(Message.JOIN, player, index1, index2);
		} else {
			Unit unit1 = getPlayer(player).getUnit(index1);
			Unit unit2 = getPlayer(player).getUnit(index2);
			getPlayer(player).joinUnits(unit1, unit2, send);
		}
	}

	public void setCash(int player, int cash, boolean send) {
		if (send) {
			if (client != null)
				client.send(Message.CASH, player, cash);
		} else {
			getPlayer(player).setCash(cash);
		}
	}

	public void addUnit(int player, String unit, int x, int y, boolean send) {
		if (send) {
			if (client != null)
				client.send(Message.UNIT, player, unit, x, y);
		} else
			getPlayer(player).addUnit(unit, x, y);
	}

	public void setStats(int player, float level, int cashEarned, int cashSpent, int unitsHired, int unitsLost,
			int unitsKilled, boolean send) {
		if (send) {
			if (client != null)
				client.send(Message.STATS, player, level, cashEarned, cashSpent, unitsHired, unitsLost, unitsKilled);
		} else
			getPlayer(player).setStats(level, cashEarned, cashSpent, unitsHired, unitsLost, unitsKilled);
	}

	public void damageUnits(int x, int y, Unit source, long seed, WeaponFile weapon) {
		if (weapon.createsCrater())
			createExplosion(x, y, weapon.getRadius(), true);

		RandomLehmer rng = new RandomLehmer(seed);
		rng.iterate(16);

		damageUnit(source, x, y, rng.nextFloat(0f, 1f));
		float radius = weapon.getRadius();
		if (radius > 0f) {
			int range = (int) (1f + radius);
			int ix, iy;
			float dist;
			for (iy = y - range; iy <= y + range; iy++)
				for (ix = x - range; ix <= x + range; ix++) {
					if (ix == x && iy == y || ix < 0 || ix >= SIZE || iy < 0 || iy >= SIZE)
						continue;
					dist = Maths.dist(x, y, ix, iy);
					if (dist <= radius)
						damageUnit(source, ix, iy, rng.nextFloat(0f, 1f));
				}
		}
	}

	public void damageUnit(Unit source, int x, int y, float chance) {
		Unit target = getUnit(x, y);
		if (target == null)
			return;
		if (chance < 0f)
			chance = RNG.randomFloat(1f);
		target.damage(source, chance);
	}

	public void newTurn(int player, boolean send) {
		if (client != null && send)
			client.send(Message.TURN, player);
		State.PROGRESS = 0f;
		players.get(player).onNewTurn();
		turnPlayer++;
		if (turnPlayer >= players.size()) {
			turnPlayer = 0;
			turn++;
			for (int i = 0; i < 2; i++)
				players.get(i).addIncomeCash();
		}
	}

	public ArrayList<Village> getVillages() {
		return villages;
	}

	public Village getVillage(int index) {
		return villages.get(index);
	}

	public GUIColorText addAction() {
		return actionFrame.addAction(this);
	}

	public boolean isObstacle(int x, int y) {
		if (x < 0 || x >= SIZE || y < 0 || y >= SIZE)
			return true;
		return vegetationMap[x][y] != null || getUnit(x, y) != null;
	}

	public boolean isVisible(Player player, int x, int y) {
		if (x < 0 || x >= SIZE || y < 0 || y >= SIZE)
			return false;
		return player.isVisible(x, y);
	}

	public float getPenalty(int x, int y) {
		if (x < 0 || x >= SIZE || y < 0 || y >= SIZE)
			return 0f;
		return terrainMap[x][y].getPenalty();
	}

	public TerrainTile getTerrain(int x, int y) {
		if (x < 0 || x >= SIZE || y < 0 || y >= SIZE)
			return null;
		return terrainMap[x][y];
	}

	public VegetationTile getVegetation(int x, int y) {
		if (x < 0 || x >= SIZE || y < 0 || y >= SIZE)
			return null;
		return vegetationMap[x][y];
	}

	public float getTemperature(int x, int y) {
		if (x < 0 || x >= SIZE || y < 0 || y >= SIZE)
			return 0.5f;
		return temperatureMap[x][y];
	}

	public int getElevation(int x, int y) {
		if (x < 0 || x >= SIZE || y < 0 || y >= SIZE)
			return 0;
		return elevationMap[x][y];
	}

	public Node[][] getNodeMap() {
		return nodeMap;
	}

	private Node[][] createNodeMap() {
		Node[][] map = new Node[SIZE][SIZE];
		for (int y = 0; y < SIZE; y++)
			for (int x = 0; x < SIZE; x++)
				map[x][y] = new Node(x, y, getPenalty(x, y), getElevation(x, y));
		return map;
	}

	public boolean isPlayerTurn() {
		return turnPlayer == currentPlayer;
	}

	public int getTurnPlayer() {
		return turnPlayer;
	}

	public Player getPlayer(int player) {
		return players.get(player);
	}

	public Player getPlayer() {
		return getPlayer(currentPlayer);
	}

	public Client getClient() {
		return client;
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

	public long getInputSeed() {
		return inputSeed;
	}

	public int getInputSeason() {
		return inputSeason;
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

	public boolean isGameOver() {
		return gameOverFrame != null;
	}

}
