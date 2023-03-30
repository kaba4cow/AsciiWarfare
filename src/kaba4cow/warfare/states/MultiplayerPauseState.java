package kaba4cow.warfare.states;

import kaba4cow.ascii.drawing.gui.GUIButton;
import kaba4cow.ascii.drawing.gui.GUIFrame;
import kaba4cow.ascii.drawing.gui.GUISeparator;
import kaba4cow.ascii.input.Keyboard;
import kaba4cow.warfare.Game;

public class MultiplayerPauseState extends State {

	private static final MultiplayerPauseState instance = new MultiplayerPauseState();

	private GUIFrame frame;

	public MultiplayerPauseState() {
		frame = new GUIFrame(Game.GUI_COLOR, false, false).setTitle("Pause");

		new GUISeparator(frame, -1, true);

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
