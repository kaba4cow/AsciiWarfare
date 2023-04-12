package kaba4cow.warfare.gui.game;

import kaba4cow.ascii.core.Display;
import kaba4cow.ascii.drawing.gui.GUIFrame;
import kaba4cow.ascii.drawing.gui.GUITextField;
import kaba4cow.ascii.input.Keyboard;
import kaba4cow.warfare.gui.GUI;

public class ChatFrame extends GUIFrame {

	private final GUITextField text;

	public ChatFrame() {
		super(GUI.COLOR, false, false);
		setTransparent(true);

		text = new GUITextField(this, -1, "");
		text.setActive();
		Keyboard.resetLastTyped();
	}

	@Override
	public void render() {
		super.render(Display.getWidth() / 4 - 1, Display.getHeight() / 5 - 1,
				Display.getWidth() - Display.getWidth() / 4 + 2, Display.getHeight() - Display.getHeight() / 5 + 2,
				false);
	}

	public String getText() {
		return text.getText();
	}

}
