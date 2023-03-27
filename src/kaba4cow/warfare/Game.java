package kaba4cow.warfare;

import java.io.File;

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
import kaba4cow.ascii.input.Keyboard;
import kaba4cow.ascii.toolbox.MemoryAnalyzer;
import kaba4cow.ascii.toolbox.files.TableFile;
import kaba4cow.ascii.toolbox.maths.Maths;
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

public class Game implements MainProgram {

	public static final int GUI_COLOR = 0x0009FC;
	public static final int MIN_WORLD_SIZE = 20;
	public static final int MAX_WORLD_SIZE = 40;

	private static final int STATE_MENU = 0;
	private static final int STATE_SETTINGS = 1;
	private static final int STATE_NEWGAME = 2;

	private int state;
	private boolean game;
	private boolean waiting;

	private GUIFrame[] frames;
	private GUISlider sizeSlider;
	private GUIRadioPanel seasonPanel;

	private GUIFrame progressFrame;

	private int worldSize;
	private int worldSeason;

	private MenuWorld menuWorld;
	private World gameWorld;

	public Game() {

	}

	@Override
	public void init() {
		state = STATE_MENU;
		game = false;
		waiting = false;

		menuWorld = new MenuWorld();

		frames = new GUIFrame[5];

		// MENU
		frames[STATE_MENU] = new GUIFrame(GUI_COLOR, false, false);
		frames[STATE_MENU].setTitle("Menu");
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
		new GUIButton(frames[STATE_MENU], -1, "Settings", f -> {
			state = STATE_SETTINGS;
		});
		new GUIButton(frames[STATE_MENU], -1, "Quit", f -> {
			Engine.requestClose();
		});

		// SETTINGS
		frames[STATE_SETTINGS] = new GUIFrame(GUI_COLOR, false, false);
		frames[STATE_SETTINGS].setTitle("Settings");
		new GUIButton(frames[STATE_SETTINGS], -1, "Return", f -> {
			state = STATE_MENU;
		});

		// NEW GAME
		frames[STATE_NEWGAME] = new GUIFrame(GUI_COLOR, false, false);
		frames[STATE_NEWGAME].setTitle("New Game");
		new GUIText(frames[STATE_NEWGAME], -1, "Map Size");
		sizeSlider = new GUISlider(frames[STATE_NEWGAME], -1, 0.25f);
		new GUISeparator(frames[STATE_NEWGAME], -1, false);
		seasonPanel = new GUIRadioPanel(frames[STATE_NEWGAME], -1, "Season:");
		new GUIRadioButton(seasonPanel, -1, "Winter");
		new GUIRadioButton(seasonPanel, -1, "Autumn");
		new GUIRadioButton(seasonPanel, -1, "Spring");
		new GUIRadioButton(seasonPanel, -1, "Summer");
		new GUISeparator(frames[STATE_NEWGAME], -1, false);
		new GUIButton(frames[STATE_NEWGAME], -1, "Start", f -> {
			worldSize = 10 * (int) Maths.map(sizeSlider.getPosition(), 0f, 1f, MIN_WORLD_SIZE, MAX_WORLD_SIZE);
			worldSeason = seasonPanel.getIndex();
			generateWorld();
		});
		new GUIButton(frames[STATE_NEWGAME], -1, "Return", f -> {
			state = STATE_MENU;
		});

		progressFrame = new GUIFrame(GUI_COLOR, false, false);
		new GUIProgressBar(progressFrame, -1, (Void) -> {
			return World.PROGRESS;
		});
	}

	@Override
	public void update(float dt) {
		if (Keyboard.isKeyDown(Keyboard.KEY_F) && Keyboard.isKey(Keyboard.KEY_CONTROL_LEFT))
			if (Display.isFullscreen())
				Display.createWindowed(80, 42);
			else
				Display.createFullscreen();

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

		MemoryAnalyzer.update();
		InfoPanel.update(dt);
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
			frames[state].render(Display.getWidth() / 2, (Display.getHeight() + Display.getGlyphSize()) / 2,
					Display.getWidth() / 3, Display.getHeight() / 2, true);
		}

		if (waiting) {
			Display.setCursorWaiting(true);
			BoxDrawer.disableCollision();
			progressFrame.render(Display.getWidth() / 2, Display.getHeight() / 2, Display.getWidth() / 2, 3, true);
			BoxDrawer.enableCollision();
		}

		InfoPanel.render();
	}

	private void renderTitle() {
		int width = Drawer.totalWidth(Display.getTitle());
		int pos = (int) (10f * Engine.getElapsedTime()) % (Display.getWidth() + width);
		Drawer.drawBigString(Display.getWidth() - pos, 0, false, Display.getTitle(), Glyphs.LIGHT_SHADE,
				Game.GUI_COLOR);
	}

	private void generateWorld() {
		new Thread() {
			@Override
			public void run() {
				progressFrame.setTitle("Generating world");
				waiting = true;
				gameWorld = new World(worldSize, worldSeason);
				game = true;
				waiting = false;
			}
		}.start();
	}

	private void loadWorld() {
		new Thread() {
			@Override
			public void run() {
				progressFrame.setTitle("Loading game");
				waiting = true;
				gameWorld = new World();
				game = true;
				waiting = false;
			}
		}.start();
	}

	private void saveWorld() {
		new Thread() {
			@Override
			public void run() {
				progressFrame.setTitle("Saving game");
				waiting = true;
				gameWorld.save(true);
				game = true;
				waiting = false;
			}
		}.start();
	}

	@Override
	public void onClose() {
		MemoryAnalyzer.printFinalInfo();
	}

	public static void loadData() {
		TableFile data = TableFile.read(new File("DATA"));

		TerrainFile.loadFiles(data.getTable("Terrain"));
		VegetationFile.loadFiles(data.getTable("Vegetation"));
		BiomeFile.loadFiles(data.getTable("Biomes"));
		WeaponTypeFile.loadFiles(data.getTable("Weapon Types"));
		WeaponFile.loadFiles(data.getTable("Weapons"));
		UnitTypeFile.loadFiles(data.getTable("Unit Types"));
		UnitFile.loadFiles(data.getTable("Units"));
		BuildingFile.loadFiles(data.getTable("Buildings"));
	}

	public static void main(String[] args) {
		loadData();
		InfoPanel.init();

		Engine.init("Ascii Warfare", 60);
		Display.createFullscreen();
		Engine.start(new Game());
	}

}
