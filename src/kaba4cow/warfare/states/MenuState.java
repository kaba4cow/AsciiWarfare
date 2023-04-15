package kaba4cow.warfare.states;

import kaba4cow.ascii.core.Engine;
import kaba4cow.ascii.gui.GUIButton;
import kaba4cow.ascii.gui.GUIFrame;
import kaba4cow.warfare.Game;
import kaba4cow.warfare.gui.GUI;

public class MenuState extends AbstractState {

	private static final MenuState instance = new MenuState();

	private final GUIFrame frame;

	public MenuState() {
		frame = new GUIFrame(GUI.COLOR, false, false).setTitle("Menu");

		new GUIButton(frame, -1, "Start New Game", f -> {
			SingleplayerState.getInstance().generateWorld();
		});

		new GUIButton(frame, -1, "Load Game", f -> {
			SingleplayerState.getInstance().loadWorld();
		});

		new GUIButton(frame, -1, "Multiplayer", f -> {
			Game.switchState(ConnectState.getInstance());
		});

		new GUIButton(frame, -1, "Settings", f -> {
			Game.switchState(SettingsState.getInstance());
		});

		new GUIButton(frame, -1, "Quit", f -> {
			Engine.requestClose();
		});
	}

	public static MenuState getInstance() {
		return instance;
	}

	@Override
	public void update(float dt) {
		frame.update();
	}

	@Override
	public void render() {
		renderTitle();
		renderFrame(frame);
	}

}
