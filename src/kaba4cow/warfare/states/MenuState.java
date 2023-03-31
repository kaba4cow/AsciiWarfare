package kaba4cow.warfare.states;

import kaba4cow.ascii.core.Engine;
import kaba4cow.ascii.drawing.gui.GUIButton;
import kaba4cow.ascii.drawing.gui.GUIFrame;
import kaba4cow.warfare.Game;

public class MenuState extends State {

	private static final MenuState instance = new MenuState();

	private final GUIFrame frame;

	public MenuState() {
		frame = new GUIFrame(Game.GUI_COLOR, false, false).setTitle("Menu");

		new GUIButton(frame, -1, "Start New Game", f -> {
			Game.switchState(GenerateState.getInstance());
		});

		new GUIButton(frame, -1, "Load Game", f -> {
			SingleplayerState.getInstance().loadWorld();
		});

		new GUIButton(frame, -1, "Multiplayer", f -> {
			Game.switchState(ConnectState.getInstance());
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
