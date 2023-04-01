package kaba4cow.warfare.gui.game;

import kaba4cow.ascii.core.Display;
import kaba4cow.ascii.drawing.gui.GUIFrame;
import kaba4cow.ascii.drawing.gui.GUISeparator;
import kaba4cow.ascii.drawing.gui.GUIText;
import kaba4cow.warfare.Game;
import kaba4cow.warfare.game.Unit;

public class CurrentUnitFrame extends GUIFrame {

	private final GUIText name;
	private final GUIText type;

	private final GUIText amount;
	private final GUIText moves;

	private final GUIText health;
	private final GUIText armor;

	private final GUIText visibility;

	public CurrentUnitFrame() {
		super(Game.GUI_COLOR, false, false);
		setTitle("Current Unit");

		name = new GUIText(this, -1, "");
		type = new GUIText(this, -1, "");
		new GUISeparator(this, -1, false);

		amount = new GUIText(this, -1, "");
		moves = new GUIText(this, -1, "");
		new GUISeparator(this, -1, false);

		health = new GUIText(this, -1, "");
		armor = new GUIText(this, -1, "");
		new GUISeparator(this, -1, false);

		visibility = new GUIText(this, -1, "");
	}

	public void render(Unit unit) {
		if (unit != null) {
			name.setText("Name: " + unit.getUnitFile().getName());
			type.setText("Type: " + unit.getUnitFile().getTypeName());

			amount.setText("Amount: " + unit.getUnits() + " / " + unit.getMaxUnits());
			moves.setText("Moves: " + (int) unit.getMoves() + " / " + (int) unit.getMaxMoves());

			health.setText("Health: " + (int) unit.getHealth() + " / " + (int) unit.getMaxHealth());
			armor.setText("Armor: " + (int) unit.getArmor());

			visibility.setText("Visibility: " + (int) unit.getVisibilityRadius());
		}

		super.render(0, 0, Display.getWidth() / 4, Display.getHeight() / 2, false);
	}

}
