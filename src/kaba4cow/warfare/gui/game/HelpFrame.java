package kaba4cow.warfare.gui.game;

import kaba4cow.ascii.core.Window;
import kaba4cow.ascii.drawing.gui.GUIColorText;
import kaba4cow.ascii.drawing.gui.GUIFrame;
import kaba4cow.ascii.drawing.gui.GUISeparator;
import kaba4cow.ascii.toolbox.Colors;
import kaba4cow.warfare.Controls;
import kaba4cow.warfare.gui.GUI;

public class HelpFrame extends GUIFrame {

	public HelpFrame() {
		super(GUI.COLOR, false, false);
		setTitle("Controls");

		Controls[] controls = Controls.values();
		for (int i = 0; i < controls.length; i++) {
			new GUISeparator(this, -1, true);
			new GUIColorText(this)//
					.addText(controls[i].getName() + ": ", GUI.COLOR)//
					.addText(" " + controls[i].getCodeName() + " ", Colors.swap(GUI.COLOR));
		}
	}

	@Override
	public void render() {
		super.render(Window.getWidth() / 4, Window.getHeight() / 5, Window.getWidth() - Window.getWidth() / 4,
				Window.getHeight() - Window.getHeight() / 5, false);
	}

}
