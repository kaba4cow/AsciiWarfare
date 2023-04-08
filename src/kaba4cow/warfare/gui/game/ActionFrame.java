package kaba4cow.warfare.gui.game;

import kaba4cow.ascii.core.Display;
import kaba4cow.ascii.drawing.gui.GUIColorText;
import kaba4cow.ascii.drawing.gui.GUIFrame;
import kaba4cow.warfare.game.World;
import kaba4cow.warfare.gui.GUI;

public class ActionFrame extends GUIFrame {

	public ActionFrame() {
		super(GUI.COLOR, false, false);
		setTitle("Actions");
	}

	public GUIColorText addAction(World world) {
		GUIColorText gui = new GUIColorText(this).addText("[" + (world.getWorldWeek() + 1) + ":"
				+ (world.getWorldDay() + 1) + ":" + (world.getWorldHour() + 1) + "] ", getColor());
		resetScrollMax();
		return gui;
	}

	@Override
	public void render() {
		super.render(Display.getWidth() / 4, 0, Display.getWidth() - Display.getWidth() / 4, Display.getHeight() / 5,
				false);
	}

}
