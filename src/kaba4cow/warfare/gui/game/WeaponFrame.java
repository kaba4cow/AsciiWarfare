package kaba4cow.warfare.gui.game;

import kaba4cow.ascii.core.Display;
import kaba4cow.ascii.drawing.gui.GUIButton;
import kaba4cow.ascii.drawing.gui.GUIFrame;
import kaba4cow.ascii.drawing.gui.GUISeparator;
import kaba4cow.ascii.drawing.gui.GUIText;
import kaba4cow.warfare.Game;
import kaba4cow.warfare.files.WeaponFile;
import kaba4cow.warfare.game.Unit;

public class WeaponFrame extends GUIFrame {

	private final Unit unit;

	private final GUIText[] attacks;
	private final GUIButton[] buttons;

	public WeaponFrame(Unit unit) {
		super(Game.GUI_COLOR, false, false);
		setTitle("Weapons");
		this.unit = unit;

		WeaponFile[] weapons = unit.getUnitFile().getWeapons();
		attacks = new GUIText[weapons.length];
		buttons = new GUIButton[weapons.length];
		for (int i = 0; i < weapons.length; i++) {
			Integer index = i;

			new GUIText(this, -1, weapons[i].getName() + " " + weapons[i].getType());
			new GUISeparator(this, -1, true);
			new GUIText(this, -1, "Damage: " + weapons[i].getDamage());
			new GUIText(this, -1, "Piercing: " + weapons[i].getPiercing());
			new GUIText(this, -1, "Range: " + weapons[i].getRange());
			new GUIText(this, -1, "Accuracy: " + weapons[i].getAccuracy());
			new GUISeparator(this, -1, true);
			attacks[i] = new GUIText(this, -1, "");

			buttons[i] = new GUIButton(this, -1, "", f -> {
				unit.setCurrentWeapon(index);
			});
			new GUISeparator(this, -1, false);
		}
	}

	@Override
	public void render() {
		for (int i = 0; i < attacks.length; i++) {
			attacks[i].setText(String.format("Rounds: %d / %d", unit.getAttacks(i),
					unit.getUnitFile().getWeapons()[i].getAttacks()));
			if (i == unit.getCurrentWeaponIndex()) {
				buttons[i].setText("Selected");
				buttons[i].setColor(0x000888 & getColor());
			} else {
				buttons[i].setText("Select");
				buttons[i].setColor(getColor());
			}
		}

		super.render(0, 0, Display.getWidth() / 4, Display.getHeight() / 2, false);
	}

}
