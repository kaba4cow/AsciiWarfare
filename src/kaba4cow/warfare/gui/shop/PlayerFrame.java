package kaba4cow.warfare.gui.shop;

import kaba4cow.ascii.core.Window;
import kaba4cow.ascii.drawing.gui.GUIFrame;
import kaba4cow.ascii.drawing.gui.GUIText;
import kaba4cow.warfare.game.Player;
import kaba4cow.warfare.gui.GUI;

public class PlayerFrame extends GUIFrame {

	private final GUIText cash;

	private final Player player;

	public PlayerFrame(Player player) {
		super(GUI.COLOR, false, false);
		setTitle("Player");

		this.player = player;

		new GUIText(this, -1, "Level: " + GUI.level(player.getLevel()));
		cash = new GUIText(this, -1, "");
		new GUIText(this, -1, "Income: " + player.getIncome());
	}

	public void render(boolean full) {
		cash.setText("Cash: " + player.getCash());
		if (full)
			super.render(0, 0, Window.getWidth(), Window.getHeight() / 5, false);
		else
			super.render(Window.getWidth() / 4, 0, Window.getWidth() - Window.getWidth() / 4,
					Window.getHeight() / 5, false);
	}

}
