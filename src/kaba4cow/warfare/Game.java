package kaba4cow.warfare;

import kaba4cow.ascii.MainProgram;
import kaba4cow.ascii.core.Engine;
import kaba4cow.ascii.core.Input;
import kaba4cow.ascii.core.Renderer;
import kaba4cow.ascii.core.Window;
import kaba4cow.ascii.drawing.Drawer;
import kaba4cow.warfare.files.GameFiles;
import kaba4cow.warfare.gui.GUI;
import kaba4cow.warfare.gui.MessageFrame;
import kaba4cow.warfare.states.AbstractState;
import kaba4cow.warfare.states.MenuState;

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
				GameFiles.init();
				AbstractState.init();
				switchState(MenuState.getInstance());
			}
		}.start();
	}

	@Override
	public void update(float dt) {
		if (Controls.FULLSCREEN.isKeyDown()) {
			if (Window.isFullscreen())
				Window.createWindowed(DEFAULT_WIDTH, DEFAULT_HEIGHT);
			else
				Window.createFullscreen();
			Settings.setFullscreen(Window.isFullscreen());
		}

		if (Controls.FPS.isKeyDown())
			showFPS = !showFPS;

		if (Input.isKeyDown(Input.KEY_5))
			testMode = !testMode;

		if (message.isActive())
			message.update();
		else if (state != null && !AbstractState.isWaiting())
			state.update(dt);
	}

	@Override
	public void render() {
		if (state == null) {
			Drawer.drawString(Window.getWidth() / 2, Window.getHeight() / 2, true, "Loading...", GUI.COLOR);
			return;
		}

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

	@Override
	public void onLostFocus() {
		if (state != null)
			state.onLostFocus();
	}

	public static void main(String[] args) {
		Settings.init();
		Engine.init("Ascii Warfare", 12, 60);
		if (Settings.isFullscreen())
			Window.createFullscreen();
		else
			Window.createWindowed(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		Renderer.setFont(Settings.getFont());
		Engine.start(new Game());
	}

}
