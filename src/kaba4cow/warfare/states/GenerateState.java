package kaba4cow.warfare.states;

import kaba4cow.ascii.drawing.gui.GUIButton;
import kaba4cow.ascii.drawing.gui.GUIFrame;
import kaba4cow.ascii.drawing.gui.GUIRadioButton;
import kaba4cow.ascii.drawing.gui.GUIRadioPanel;
import kaba4cow.ascii.drawing.gui.GUISeparator;
import kaba4cow.ascii.input.Keyboard;
import kaba4cow.warfare.Game;
import kaba4cow.warfare.Settings;
import kaba4cow.warfare.gui.GUI;

public class GenerateState extends State {

	private static final GenerateState instance = new GenerateState();

	private final GUIFrame frame;
	private final GUIRadioPanel sizePanel;
	private final GUIRadioPanel seasonPanel;

	public GenerateState() {
		frame = new GUIFrame(GUI.COLOR, false, false).setTitle("New Game");

		sizePanel = new GUIRadioPanel(frame, -1, "Map Size:");
		new GUIRadioButton(sizePanel, -1, "Small");
		new GUIRadioButton(sizePanel, -1, "Medium");
		new GUIRadioButton(sizePanel, -1, "Large");
		sizePanel.setIndex(Settings.getWorldSize());

		new GUISeparator(frame, -1, true);

		seasonPanel = new GUIRadioPanel(frame, -1, "Season:");
		new GUIRadioButton(seasonPanel, -1, "Winter");
		new GUIRadioButton(seasonPanel, -1, "Autumn");
		new GUIRadioButton(seasonPanel, -1, "Spring");
		new GUIRadioButton(seasonPanel, -1, "Summer");
		seasonPanel.setIndex(Settings.getWorldSeason());

		new GUISeparator(frame, -1, true);

		new GUIButton(frame, -1, "Start", f -> {
			Settings.setWorldInfo(seasonPanel.getIndex());
			SingleplayerState.getInstance().generateWorld(seasonPanel.getIndex());
		});

		new GUIButton(frame, -1, "Return", f -> {
			Game.switchState(MenuState.getInstance());
		});
	}

	public static GenerateState getInstance() {
		return instance;
	}

	@Override
	public void update(float dt) {
		if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
			Game.switchState(MenuState.getInstance());
		
		frame.update();
	}

	@Override
	public void render() {
		renderTitle();
		renderFrame(frame);
	}

}
