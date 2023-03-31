package kaba4cow.warfare.gui.shop;

import kaba4cow.ascii.core.Display;
import kaba4cow.ascii.drawing.gui.GUIFrame;
import kaba4cow.ascii.drawing.gui.GUIText;
import kaba4cow.warfare.Game;
import kaba4cow.warfare.game.Player;

public class PlayerFrame extends GUIFrame {

	private final GUIText cash;

	private final Player player;

	public PlayerFrame(Player player) {
		super(Game.GUI_COLOR, false, false);
		setTitle("Player");

		this.player = player;

		cash = new GUIText(this, -1, "");
		new GUIText(this, -1, "Income: " + player.getIncome());
	}

	public void render(boolean full) {
		cash.setText("Cash: " + player.getCash());
		if (full)
			super.render(0, 0, Display.getWidth(), Display.getHeight() / 5, false);
		else
			super.render(Display.getWidth() / 4, 0, Display.getWidth() - Display.getWidth() / 4,
					Display.getHeight() / 5, false);
	}

}
