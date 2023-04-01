package kaba4cow.warfare.gui.game;

import kaba4cow.ascii.core.Display;
import kaba4cow.ascii.drawing.gui.GUIFrame;
import kaba4cow.ascii.drawing.gui.GUISeparator;
import kaba4cow.ascii.drawing.gui.GUIText;
import kaba4cow.ascii.toolbox.maths.Maths;
import kaba4cow.warfare.Game;
import kaba4cow.warfare.game.Unit;

public class SelectedUnitFrame extends GUIFrame {

	private final GUIText name;
	private final GUIText type;

	private final GUIText distance;

	private final GUIText amount;

	private final GUIText health;
	private final GUIText armor;

	private final GUIText visibility;

	public SelectedUnitFrame() {
		super(Game.GUI_COLOR, false, false);
		setTitle("Selected Unit");

		name = new GUIText(this, -1, "");
		type = new GUIText(this, -1, "");
		new GUISeparator(this, -1, false);

		distance = new GUIText(this, -1, "");
		new GUISeparator(this, -1, false);

		amount = new GUIText(this, -1, "");
		new GUISeparator(this, -1, false);

		health = new GUIText(this, -1, "");
		armor = new GUIText(this, -1, "");
		new GUISeparator(this, -1, false);

		visibility = new GUIText(this, -1, "");
	}

	public void render(Unit currentUnit, Unit selectedUnit) {
		name.setText("Name: " + selectedUnit.getUnitFile().getName());
		type.setText("Type: " + selectedUnit.getUnitFile().getTypeName());

		distance.setText("Distance: "
				+ (int) Maths.dist(currentUnit.getX(), currentUnit.getY(), selectedUnit.getX(), selectedUnit.getY()));

		amount.setText("Amount: " + selectedUnit.getUnits() + " / " + selectedUnit.getMaxUnits());

		health.setText("Health: " + (int) selectedUnit.getHealth() + " / " + (int) selectedUnit.getMaxHealth());
		armor.setText("Armor: " + (int) selectedUnit.getArmor());

		visibility.setText("Visibility Range: " + (int) selectedUnit.getVisibilityRadius());

		super.render(0, Display.getHeight() / 2, Display.getWidth() / 4, Display.getHeight() / 2, false);
	}

}
