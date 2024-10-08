package kaba4cow.warfare.states;

import kaba4cow.ascii.core.Input;
import kaba4cow.ascii.gui.GUIButton;
import kaba4cow.ascii.gui.GUIFrame;
import kaba4cow.warfare.Game;
import kaba4cow.warfare.gui.GUI;

public class SingleplayerPauseState extends AbstractState {

	private static final SingleplayerPauseState instance = new SingleplayerPauseState();

	private final GUIFrame frame;

	public SingleplayerPauseState() {
		frame = new GUIFrame(GUI.COLOR, false, false).setTitle("Pause");

		new GUIButton(frame, -1, "Continue", f -> {
			Game.switchState(SingleplayerState.getInstance());
		});

		new GUIButton(frame, -1, "Load Game", f -> {
			SingleplayerState.getInstance().loadWorld();
		});

		new GUIButton(frame, -1, "Save Game", f -> {
			SingleplayerState.getInstance().saveWorld();
		});

		new GUIButton(frame, -1, "Quit", f -> {
			Game.switchState(MenuState.getInstance());
		});
	}

	public static SingleplayerPauseState getInstance() {
		return instance;
	}

	@Override
	public void update(float dt) {
		if (Input.isKeyDown(Input.KEY_ESCAPE))
			Game.switchState(SingleplayerState.getInstance());

		frame.update();
	}

	@Override
	public void render() {
		renderTitle();
		renderFrame(frame);
	}

}
