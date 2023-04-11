package kaba4cow.warfare.states;

import kaba4cow.ascii.drawing.gui.GUIButton;
import kaba4cow.ascii.drawing.gui.GUIFrame;
import kaba4cow.ascii.input.Keyboard;
import kaba4cow.warfare.Game;
import kaba4cow.warfare.gui.GUI;

public class MultiplayerPauseState extends AbstractState {

	private static final MultiplayerPauseState instance = new MultiplayerPauseState();

	private final GUIFrame frame;

	public MultiplayerPauseState() {
		frame = new GUIFrame(GUI.COLOR, false, false).setTitle("Pause");

		new GUIButton(frame, -1, "Continue", f -> {
			Game.switchState(MultiplayerState.getInstance());
		});

		new GUIButton(frame, -1, "Disconnect", f -> {
			MultiplayerState.getInstance().disconnect();
		});
	}

	public static MultiplayerPauseState getInstance() {
		return instance;
	}

	@Override
	public void update(float dt) {
		if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
			Game.switchState(MultiplayerState.getInstance());

		frame.update();
	}

	@Override
	public void render() {
		renderTitle();
		renderFrame(frame);
	}

}
