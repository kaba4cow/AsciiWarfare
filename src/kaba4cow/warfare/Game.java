package kaba4cow.warfare;

import kaba4cow.ascii.MainProgram;
import kaba4cow.ascii.core.Display;
import kaba4cow.ascii.core.Engine;
import kaba4cow.ascii.drawing.drawers.Drawer;
import kaba4cow.warfare.files.GameFiles;
import kaba4cow.warfare.gui.GUI;
import kaba4cow.warfare.states.MenuState;
import kaba4cow.warfare.states.State;

public class Game implements MainProgram {

	private static final int DEFAULT_WIDTH = 70;
	private static final int DEFAULT_HEIGHT = 40;

	private boolean showFPS;

	private static State state;

	public Game() {

	}

	@Override
	public void init() {
		showFPS = false;
		Display.setScreenshotLocation("USER/");
		Settings.init();
		GameFiles.init();
		State.init();
		if (Settings.isFullscreen())
			Display.createFullscreen();
		else
			Display.createWindowed(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		switchState(MenuState.getInstance());
	}

	@Override
	public void update(float dt) {
		if (Controls.FULLSCREEN.isKeyDown()) {
			if (Display.isFullscreen())
				Display.createWindowed(DEFAULT_WIDTH, DEFAULT_HEIGHT);
			else
				Display.createFullscreen();
			Settings.setFullscreen(Display.isFullscreen());
		}

		if (Controls.FPS.isKeyDown())
			showFPS = !showFPS;

		if (Controls.SCREENSHOT.isKeyDown())
			Display.takeScreenshot();

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
			Drawer.drawString(0, 0, false, "FPS: " + Engine.getCurrentFramerate(), GUI.COLOR);
	}

	public static void switchState(State state) {
		Game.state = state;
	}

	public static void main(String[] args) throws Exception {
		Engine.init("Ascii Warfare", 60);
		Display.createWindowed(20, 5);
		Display.setDrawCursor(false);
		Drawer.drawString(0, Display.getHeight() - 1, false, "Loading...", GUI.COLOR);
		Display.repaint();
		Engine.start(new Game());
	}

}
