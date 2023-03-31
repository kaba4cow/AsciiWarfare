package kaba4cow.warfare;

import java.io.File;

import kaba4cow.ascii.MainProgram;
import kaba4cow.ascii.core.Display;
import kaba4cow.ascii.core.Engine;
import kaba4cow.ascii.drawing.drawers.Drawer;
import kaba4cow.ascii.input.Keyboard;
import kaba4cow.ascii.toolbox.files.TableFile;
import kaba4cow.warfare.files.BiomeFile;
import kaba4cow.warfare.files.BuildingFile;
import kaba4cow.warfare.files.TerrainFile;
import kaba4cow.warfare.files.UnitFile;
import kaba4cow.warfare.files.UnitTypeFile;
import kaba4cow.warfare.files.VegetationFile;
import kaba4cow.warfare.files.WeaponFile;
import kaba4cow.warfare.files.WeaponTypeFile;
import kaba4cow.warfare.states.MenuState;
import kaba4cow.warfare.states.State;

public class Game implements MainProgram {

	public static final int GUI_COLOR = 0x0009FC;
	public static final int WORLD_SIZES = 3;
	public static final int MIN_WORLD_SIZE = 20;
	public static final int MAX_WORLD_SIZE = 32;

	private boolean showFPS;

	private static State state;

	public Game() {

	}

	@Override
	public void init() {
		switchState(MenuState.getInstance());
		showFPS = false;
	}

	@Override
	public void update(float dt) {
		if (Keyboard.isKeyDown(Keyboard.KEY_F) && Keyboard.isKey(Keyboard.KEY_CONTROL_LEFT))
			if (Display.isFullscreen())
				Display.createWindowed(60, 40);
			else
				Display.createFullscreen();

		if (Keyboard.isKeyDown(Keyboard.KEY_F3))
			showFPS = !showFPS;

		if (!State.isWaiting())
			state.update(dt);
	}

	@Override
	public void render() {
		Display.setDrawCursor(true);
		Display.setCursorWaiting(false);

		state.render();
		if (State.isWaiting())
			State.renderProgressBar();

		if (showFPS)
			Drawer.drawString(0, 0, false, "FPS: " + Engine.getCurrentFramerate(), GUI_COLOR);
	}

	public static void switchState(State state) {
		Game.state = state;
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

	public static void main(String[] args) throws Exception {
		loadData();
		Engine.init("Ascii Warfare", 60);
		State.init();
//		Display.createFullscreen();
		Display.createWindowed(60, 40);
		Engine.start(new Game());
	}

}
