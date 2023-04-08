package kaba4cow.warfare.gui.info;

import java.util.ArrayList;

import kaba4cow.ascii.core.Display;
import kaba4cow.ascii.drawing.gui.GUIFrame;
import kaba4cow.ascii.drawing.gui.GUISeparator;
import kaba4cow.ascii.drawing.gui.GUIText;
import kaba4cow.warfare.game.Player;
import kaba4cow.warfare.game.Unit;
import kaba4cow.warfare.gui.GUI;

public class UnitFrame extends GUIFrame {

	public UnitFrame(Player player) {
		super(GUI.COLOR, false, false);
		setTitle("Units");

		ArrayList<Unit> units = player.getUnits();
		for (int i = 0; i < units.size(); i++) {
			Unit unit = units.get(i);

			new GUIText(this, -1, "Unit: " + unit.getUnitFile().getName());
			new GUIText(this, -1, "Type: " + unit.getUnitFile().getTypeName());
			new GUISeparator(this, -1, true);

			new GUIText(this, -1, "Amount: " + unit.getUnits());
			new GUIText(this, -1, "Health: " + GUI.slash(unit.getHealth(), unit.getMaxHealth()));
			new GUIText(this, -1, "Moves: " + GUI.slash(unit.getMoves(), unit.getMaxMoves()));
			new GUISeparator(this, -1, false);
		}
	}

	@Override
	public void render() {
		super.render(Display.getWidth() / 4, Display.getHeight() / 5, Display.getWidth() - Display.getWidth() / 4,
				Display.getHeight() - Display.getHeight() / 5, false);
	}

}
