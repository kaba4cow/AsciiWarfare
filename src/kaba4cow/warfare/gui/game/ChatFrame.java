package kaba4cow.warfare.gui.game;

import kaba4cow.ascii.core.Window;
import kaba4cow.ascii.gui.GUIFrame;
import kaba4cow.ascii.gui.GUITextField;
import kaba4cow.warfare.gui.GUI;

public class ChatFrame extends GUIFrame {

	private final GUITextField text;

	public ChatFrame() {
		super(GUI.COLOR, false, false);
		setTransparent(true);

		text = new GUITextField(this, -1, "");
		text.setActive();
	}

	@Override
	public void render() {
		super.render(Window.getWidth() / 4 - 1, Window.getHeight() / 5 - 1,
				Window.getWidth() - Window.getWidth() / 4 + 2, Window.getHeight() - Window.getHeight() / 5 + 2,
				false);
	}

	public String getText() {
		return text.getText();
	}

}
