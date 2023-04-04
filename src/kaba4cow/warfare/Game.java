package kaba4cow.warfare;

import kaba4cow.ascii.MainProgram;
import kaba4cow.ascii.core.Display;
import kaba4cow.ascii.core.Engine;
import kaba4cow.ascii.drawing.drawers.Drawer;
import kaba4cow.ascii.input.Keyboard;
import kaba4cow.warfare.files.GameFiles;
import kaba4cow.warfare.states.MenuState;
import kaba4cow.warfare.states.State;

public class Game implements MainProgram {

	private static final int DEFAULT_WIDTH = 65;
	private static final int DEFAULT_HEIGHT = 40;

	public static final int GUI_COLOR = 0x0009FC;

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
		if (Keyboard.isKeyDown(Keyboard.KEY_F) && Keyboard.isKey(Keyboard.KEY_CONTROL_LEFT)) {
			if (Display.isFullscreen())
				Display.createWindowed(DEFAULT_WIDTH, DEFAULT_HEIGHT);
			else
				Display.createFullscreen();
			Settings.setFullscreen(Display.isFullscreen());
		}

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

	public static void main(String[] args) throws Exception {
		Settings.init();
		GameFiles.init();
		Engine.init("Ascii Warfare", 60);
		State.init();
		if (Settings.isFullscreen())
			Display.createFullscreen();
		else
			Display.createWindowed(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		Engine.start(new Game());
	}

}
