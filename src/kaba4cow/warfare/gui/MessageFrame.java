package kaba4cow.warfare.gui;

import kaba4cow.ascii.core.Window;
import kaba4cow.ascii.drawing.drawers.BoxDrawer;
import kaba4cow.ascii.drawing.gui.GUIButton;
import kaba4cow.ascii.drawing.gui.GUIFrame;
import kaba4cow.ascii.drawing.gui.GUISeparator;
import kaba4cow.ascii.drawing.gui.GUIText;

public class MessageFrame extends GUIFrame {

	private final GUIText text;

	public MessageFrame() {
		super(GUI.COLOR, false, false);

		text = new GUIText(this, -1, "");
		new GUISeparator(this, -1, true);
		new GUIButton(this, -1, "OK", f -> {
			text.setText("");
		});
	}

	@Override
	public void render() {
		BoxDrawer.disableCollision();
		if (isActive())
			super.render(Window.getWidth() / 2, Window.getHeight() / 2, Window.getWidth() / 2,
					Window.getHeight() / 5, true);
		BoxDrawer.enableCollision();
	}

	public void setText(String text) {
		this.text.setText(text);
	}

	public boolean isActive() {
		return !text.getText().isEmpty();
	}

}
