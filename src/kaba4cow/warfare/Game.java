package kaba4cow.warfare;

import java.io.File;
import java.io.IOException;

import kaba4cow.ascii.MainProgram;
import kaba4cow.ascii.core.Display;
import kaba4cow.ascii.core.Engine;
import kaba4cow.ascii.drawing.drawers.BoxDrawer;
import kaba4cow.ascii.drawing.drawers.Drawer;
import kaba4cow.ascii.drawing.glyphs.Glyphs;
import kaba4cow.ascii.drawing.gui.GUIButton;
import kaba4cow.ascii.drawing.gui.GUIFrame;
import kaba4cow.ascii.drawing.gui.GUIProgressBar;
import kaba4cow.ascii.drawing.gui.GUIRadioButton;
import kaba4cow.ascii.drawing.gui.GUIRadioPanel;
import kaba4cow.ascii.drawing.gui.GUISeparator;
import kaba4cow.ascii.drawing.gui.GUISlider;
import kaba4cow.ascii.drawing.gui.GUIText;
import kaba4cow.ascii.drawing.gui.GUITextField;
import kaba4cow.ascii.input.Keyboard;
import kaba4cow.ascii.toolbox.files.TableFile;
import kaba4cow.ascii.toolbox.rng.RNG;
import kaba4cow.warfare.files.BiomeFile;
import kaba4cow.warfare.files.BuildingFile;
import kaba4cow.warfare.files.TerrainFile;
import kaba4cow.warfare.files.UnitFile;
import kaba4cow.warfare.files.UnitTypeFile;
import kaba4cow.warfare.files.VegetationFile;
import kaba4cow.warfare.files.WeaponFile;
import kaba4cow.warfare.files.WeaponTypeFile;
import kaba4cow.warfare.game.MenuWorld;
import kaba4cow.warfare.game.World;
import kaba4cow.warfare.network.tcp.Client;

public class Game implements MainProgram {

	public static final int GUI_COLOR = 0x0009FC;
	public static final int MIN_WORLD_SIZE = 20;
	public static final int MAX_WORLD_SIZE = 40;

	private static final int STATE_MENU = 0;
	private static final int STATE_NEWGAME = 1;
	private static final int STATE_MULTIPLAYER = 2;

	private int state;
	private boolean game;
	private boolean waiting;
	private boolean showFPS;

	private GUIFrame[] frames;
	private GUISlider sizeSlider;
	private GUIRadioPanel seasonPanel;
	private GUITextField ipTextField;
	private GUITextField portTextField;

	private GUIFrame progressFrame;

	private MenuWorld menuWorld;
	private World gameWorld;

	private Client client;

	public Game() {

	}

	@Override
	public void init() {
		state = STATE_MENU;
		game = false;
		waiting = false;
		showFPS = false;

		menuWorld = new MenuWorld();

		frames = new GUIFrame[5];

		// MENU
		frames[STATE_MENU] = new GUIFrame(GUI_COLOR, false, false).setTitle("Menu");
		new GUISeparator(frames[STATE_MENU], -1, true);
		new GUIButton(frames[STATE_MENU], -1, "Start New Game", f -> {
			state = STATE_NEWGAME;
		});
		new GUIButton(frames[STATE_MENU], -1, "Continue Game", f -> {
			if (gameWorld == null) {
				if (new File("SAVE").exists())
					loadWorld();
			} else
				game = true;
		});
		new GUIButton(frames[STATE_MENU], -1, "Save Game", f -> {
			if (gameWorld != null)
				saveWorld();
		});
		new GUIButton(frames[STATE_MENU], -1, "Multiplayer", f -> {
			if (client != null) {
				client.close();
				client = null;
			}
			state = STATE_MULTIPLAYER;
		});
		new GUIButton(frames[STATE_MENU], -1, "Quit", f -> {
			Engine.requestClose();
		});

		// NEW GAME
		frames[STATE_NEWGAME] = new GUIFrame(GUI_COLOR, false, false).setTitle("New Game");
		new GUISeparator(frames[STATE_NEWGAME], -1, true);
		new GUIText(frames[STATE_NEWGAME], -1, "Map Size");
		sizeSlider = new GUISlider(frames[STATE_NEWGAME], -1, 0.25f);
		new GUISeparator(frames[STATE_NEWGAME], -1, true);
		seasonPanel = new GUIRadioPanel(frames[STATE_NEWGAME], -1, "Season:");
		new GUIRadioButton(seasonPanel, -1, "Winter");
		new GUIRadioButton(seasonPanel, -1, "Autumn");
		new GUIRadioButton(seasonPanel, -1, "Spring");
		new GUIRadioButton(seasonPanel, -1, "Summer");
		new GUISeparator(frames[STATE_NEWGAME], -1, true);
		new GUIButton(frames[STATE_NEWGAME], -1, "Start", f -> {
			generateWorld();
		});
		new GUIButton(frames[STATE_NEWGAME], -1, "Return", f -> {
			state = STATE_MENU;
		});

		// MULTIPLAYER
		frames[STATE_MULTIPLAYER] = new GUIFrame(GUI_COLOR, false, false).setTitle("Multiplayer");
		new GUISeparator(frames[STATE_MULTIPLAYER], -1, true);
		new GUIText(frames[STATE_MULTIPLAYER], -1, "IP");
		ipTextField = new GUITextField(frames[STATE_MULTIPLAYER], -1, "");
		ipTextField.setMaxCharacters(15);
		new GUIText(frames[STATE_MULTIPLAYER], -1, "Port");
		portTextField = new GUITextField(frames[STATE_MULTIPLAYER], -1, "");
		portTextField.setMaxCharacters(5);
		new GUIButton(frames[STATE_MULTIPLAYER], -1, "Connect", f -> {
			connect();
		});
		new GUIButton(frames[STATE_MULTIPLAYER], -1, "Return", f -> {
			state = STATE_MENU;
		});

		// PROGRESS BAR
		progressFrame = new GUIFrame(GUI_COLOR, false, false);
		new GUIProgressBar(progressFrame, -1, (Void) -> {
			return World.PROGRESS;
		});
	}

	@Override
	public void update(float dt) {
		if (Keyboard.isKeyDown(Keyboard.KEY_F) && Keyboard.isKey(Keyboard.KEY_CONTROL_LEFT))
			if (Display.isFullscreen())
				Display.createWindowed(70, 40);
			else
				Display.createFullscreen();

		if (Keyboard.isKeyDown(Keyboard.KEY_F3))
			showFPS = !showFPS;

		if (!waiting) {
			if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
				if (game && gameWorld.isPlayerTurn()) {
					state = STATE_MENU;
					game = false;
				} else if (gameWorld != null)
					game = true;
			}

			if (game)
				gameWorld.update(dt);
			else
				frames[state].update();
		}
	}

	@Override
	public void render() {
		Display.setDrawCursor(true);
		Display.setCursorWaiting(false);

		if (game)
			gameWorld.render();
		else {
			menuWorld.render();
			renderTitle();
			frames[state].render(0, Display.getGlyphSize(), 22, Display.getHeight() - Display.getGlyphSize(), false);
		}

		if (waiting) {
			Display.setCursorWaiting(true);
			BoxDrawer.disableCollision();
			progressFrame.render(Display.getWidth() / 2, Display.getHeight() / 2, Display.getWidth() / 2, 3, true);
			BoxDrawer.enableCollision();
		}

		if (showFPS)
			Drawer.drawString(0, 0, false, "FPS: " + Engine.getCurrentFramerate(), GUI_COLOR);
	}

	private void renderTitle() {
		int width = Drawer.totalWidth(Display.getTitle());
		int pos = (int) (8f * Engine.getElapsedTime()) % (Display.getWidth() + width);
		Drawer.drawBigString(Display.getWidth() - pos, 0, false, Display.getTitle(), Glyphs.LIGHT_SHADE,
				Game.GUI_COLOR);
	}

	private void generateWorld() {
		new Thread("Generating") {
			@Override
			public void run() {
				progressFrame.setTitle(getName());
				waiting = true;
				gameWorld = new World(sizeSlider.getPosition(), seasonPanel.getIndex());
				gameWorld.setCurrentPlayer(RNG.randomInt(0, 2), true);
				game = true;
				waiting = false;
			}
		}.start();
	}

	public void generateWorld(float size, int season, long seed, int currentPlayer) {
		new Thread("Generating") {
			@Override
			public void run() {
				progressFrame.setTitle(getName());
				waiting = true;
				gameWorld = new World(size, season, seed);
				gameWorld.setCurrentPlayer(currentPlayer, false);
				client.setWorld(gameWorld);
				gameWorld.setClient(client);
				game = true;
				waiting = false;
			}
		}.start();
	}

	private void loadWorld() {
		new Thread("Loading") {
			@Override
			public void run() {
				progressFrame.setTitle(getName());
				waiting = true;
				gameWorld = new World();
				game = true;
				waiting = false;
			}
		}.start();
	}

	private void saveWorld() {
		new Thread("Saving") {
			@Override
			public void run() {
				progressFrame.setTitle(getName());
				waiting = true;
				gameWorld.save(true);
				game = true;
				waiting = false;
			}
		}.start();
	}

	private void connect() {
		if (client != null) {
			client.close();
			gameWorld = null;
			return;
		}

		String ip = ipTextField.getText();
		int port;
		try {
			port = Integer.parseInt(portTextField.getText());
		} catch (NumberFormatException e) {
			return;
		}

		new Thread("Connecting") {
			@Override
			public void run() {
				World.PROGRESS = 0f;
				progressFrame.setTitle(getName());
				waiting = true;
				try {
					new Client(Game.this, ip, port);
				} catch (IOException e) {
					setClient(null);
				}
				waiting = false;
			}
		}.start();
	}

	public void setClient(Client client) {
		this.client = client;
	}

	public static void main(String[] args) {
		TableFile data = TableFile.read(new File("DATA"));

		TerrainFile.loadFiles(data.getTable("Terrain"));
		VegetationFile.loadFiles(data.getTable("Vegetation"));
		BiomeFile.loadFiles(data.getTable("Biomes"));
		WeaponTypeFile.loadFiles(data.getTable("Weapon Types"));
		WeaponFile.loadFiles(data.getTable("Weapons"));
		UnitTypeFile.loadFiles(data.getTable("Unit Types"));
		UnitFile.loadFiles(data.getTable("Units"));
		BuildingFile.loadFiles(data.getTable("Buildings"));

		Engine.init("Ascii Warfare", 60);
		Display.createFullscreen();
		Engine.start(new Game());
	}

}
