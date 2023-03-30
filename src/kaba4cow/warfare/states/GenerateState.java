package kaba4cow.warfare.states;

import kaba4cow.ascii.drawing.gui.GUIButton;
import kaba4cow.ascii.drawing.gui.GUIFrame;
import kaba4cow.ascii.drawing.gui.GUIRadioButton;
import kaba4cow.ascii.drawing.gui.GUIRadioPanel;
import kaba4cow.ascii.drawing.gui.GUISeparator;
import kaba4cow.ascii.drawing.gui.GUISlider;
import kaba4cow.ascii.drawing.gui.GUIText;
import kaba4cow.warfare.Game;

public class GenerateState extends State {

	private static final GenerateState instance = new GenerateState();

	private GUIFrame frame;
	private GUISlider sizeSlider;
	private GUIRadioPanel seasonPanel;

	public GenerateState() {
		frame = new GUIFrame(Game.GUI_COLOR, false, false).setTitle("New Game");

		new GUISeparator(frame, -1, true);

		new GUIText(frame, -1, "Map Size");
		sizeSlider = new GUISlider(frame, -1, 0f);

		new GUISeparator(frame, -1, true);

		seasonPanel = new GUIRadioPanel(frame, -1, "Season:");
		new GUIRadioButton(seasonPanel, -1, "Winter");
		new GUIRadioButton(seasonPanel, -1, "Autumn");
		new GUIRadioButton(seasonPanel, -1, "Spring");
		new GUIRadioButton(seasonPanel, -1, "Summer");

		new GUISeparator(frame, -1, true);

		new GUIButton(frame, -1, "Start", f -> {
			SingleplayerState.getInstance().generateWorld(sizeSlider.getPosition(), seasonPanel.getIndex());
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
		frame.update();
	}

	@Override
	public void render() {
		renderTitle();
		renderFrame(frame);
	}

}
