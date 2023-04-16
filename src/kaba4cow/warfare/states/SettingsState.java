package kaba4cow.warfare.states;

import kaba4cow.ascii.core.Input;
import kaba4cow.ascii.core.Renderer;
import kaba4cow.ascii.gui.GUIButton;
import kaba4cow.ascii.gui.GUIFrame;
import kaba4cow.ascii.gui.GUIRadioButton;
import kaba4cow.ascii.gui.GUIRadioPanel;
import kaba4cow.warfare.Game;
import kaba4cow.warfare.Settings;
import kaba4cow.warfare.gui.GUI;

public class SettingsState extends AbstractState {

	private static final SettingsState instance = new SettingsState();

	private final GUIFrame frame;

	private final GUIRadioPanel fontPanel;

	public SettingsState() {
		frame = new GUIFrame(GUI.COLOR, false, false).setTitle("Settings");

		fontPanel = new GUIRadioPanel(frame, -1, "Font");
		for (int font = 0; font < Renderer.getFontCount(); font++)
			new GUIRadioButton(fontPanel, -1, Renderer.getFontName(font));
		fontPanel.setIndex(Renderer.getFont());

		new GUIButton(frame, -1, "Quit", f -> {
			Game.switchState(MenuState.getInstance());
		});
	}

	public static SettingsState getInstance() {
		return instance;
	}

	@Override
	public void update(float dt) {
		if (Input.isKeyDown(Input.KEY_ESCAPE))
			Game.switchState(MenuState.getInstance());

		frame.update();
		if (Renderer.setFont(fontPanel.getIndex()))
			Settings.setFont(fontPanel.getIndex());
	}

	@Override
	public void render() {
		renderTitle();
		renderFrame(frame);
	}

}
