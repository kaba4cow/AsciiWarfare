package kaba4cow.warfare;

import kaba4cow.ascii.MainProgram;
import kaba4cow.ascii.core.Display;
import kaba4cow.ascii.core.Engine;
import kaba4cow.ascii.drawing.drawers.Drawer;
import kaba4cow.ascii.input.Keyboard;
import kaba4cow.warfare.files.GameFiles;
import kaba4cow.warfare.gui.GUI;
import kaba4cow.warfare.gui.MessageFrame;
import kaba4cow.warfare.states.MenuState;
import kaba4cow.warfare.states.AbstractState;

public class Game implements MainProgram {

	private static final int DEFAULT_WIDTH = 70;
	private static final int DEFAULT_HEIGHT = 40;

	public static boolean testMode = false;

	private boolean showFPS;

	private static AbstractState state;

	private static MessageFrame message;

	public Game() {

	}

	@Override
	public void init() {
		message = new MessageFrame();
		showFPS = false;
		new Thread("Initialization") {
			@Override
			public void run() {
				Display.setScreenshotLocation("user/");
				GameFiles.init();
				AbstractState.init();
				switchState(MenuState.getInstance());
			}
		}.start();
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

		if (Keyboard.isKeyDown(Keyboard.KEY_V))
			testMode = !testMode;

		if (Controls.FPS.isKeyDown())
			showFPS = !showFPS;

		if (Controls.SCREENSHOT.isKeyDown())
			Display.takeScreenshot();

		if (message.isActive())
			message.update();
		else if (state != null && !AbstractState.isWaiting())
			state.update(dt);
	}

	@Override
	public void render() {
		if (state == null) {
			Drawer.drawString(0, Display.getHeight() - 1, false, "Loading...", GUI.COLOR);
			return;
		}

		Display.setDrawCursor(true);
		Display.setCursorWaiting(false);

		state.render();
		if (AbstractState.isWaiting())
			AbstractState.renderProgressBar();

		if (message.isActive())
			message.render();

		if (showFPS)
			Drawer.drawString(0, 0, false, "FPS: " + Engine.getCurrentFramerate(), GUI.COLOR);
	}

	public static void switchState(AbstractState state) {
		Game.state = state;
	}

	public static void message(String text) {
		Game.message.setText(text);
	}

	public static void main(String[] args) {
		Settings.init();
		Engine.init("Ascii Warfare", 60);
		if (Settings.isFullscreen())
			Display.createFullscreen();
		else
			Display.createWindowed(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		Display.setCursorWaiting(true);
		Engine.start(new Game());
	}

}
