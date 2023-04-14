package kaba4cow.warfare.gui.game;

import kaba4cow.ascii.core.Window;
import kaba4cow.ascii.drawing.gui.GUIButton;
import kaba4cow.ascii.drawing.gui.GUIFrame;
import kaba4cow.ascii.drawing.gui.GUISeparator;
import kaba4cow.ascii.drawing.gui.GUIText;
import kaba4cow.warfare.files.WeaponFile;
import kaba4cow.warfare.game.Unit;
import kaba4cow.warfare.gui.GUI;

public class WeaponFrame extends GUIFrame {

	private final Unit unit;

	private final GUIText[] attacks;
	private final GUIButton[] buttons;

	public WeaponFrame(Unit unit) {
		super(GUI.COLOR, false, false);
		setTitle("Weapons");
		this.unit = unit;

		WeaponFile[] weapons = unit.getUnitFile().getWeapons();
		attacks = new GUIText[weapons.length];
		buttons = new GUIButton[weapons.length];
		for (int i = 0; i < weapons.length; i++) {
			Integer index = i;

			new GUIText(this, -1, weapons[i].getName() + " " + weapons[i].getType());
			new GUISeparator(this, -1, true);
			new GUIText(this, -1, "Damage: " + GUI.format(weapons[i].getDamage()));
			new GUIText(this, -1, "Piercing: " + GUI.format(weapons[i].getPiercing()));
			new GUIText(this, -1, "Range: " + GUI.format(weapons[i].getRange()));
			new GUIText(this, -1, "Accuracy: " + GUI.percent(weapons[i].getAccuracy()));
			new GUISeparator(this, -1, true);
			attacks[i] = new GUIText(this, -1, "");

			buttons[i] = new GUIButton(this, -1, "", f -> {
				unit.setCurrentWeapon(index);
				unit.resetAttackPath();
			});
			new GUISeparator(this, -1, false);
		}
	}

	@Override
	public void render() {
		for (int i = 0; i < attacks.length; i++) {
			attacks[i].setText("Available: " + (unit.canShoot(i) ? "YES" : "NO"));
			if (i == unit.getCurrentWeaponIndex())
				buttons[i].setText("Selected");
			else
				buttons[i].setText("Select");
		}

		super.render(0, 0, Window.getWidth() / 4, Window.getHeight() / 2, false);
	}

}
