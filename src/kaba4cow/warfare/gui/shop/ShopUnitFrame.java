package kaba4cow.warfare.gui.shop;

import kaba4cow.ascii.core.Window;
import kaba4cow.ascii.gui.GUIButton;
import kaba4cow.ascii.gui.GUIFrame;
import kaba4cow.ascii.gui.GUISeparator;
import kaba4cow.ascii.gui.GUIText;
import kaba4cow.warfare.files.UnitFile;
import kaba4cow.warfare.files.WeaponFile;
import kaba4cow.warfare.game.Player;
import kaba4cow.warfare.gui.GUI;

public class ShopUnitFrame extends GUIFrame {

	public ShopUnitFrame(Player player, UnitFile unit) {
		super(GUI.COLOR, false, false);
		setTitle("Unit");

		new GUIButton(this, -1, "Hire (" + unit.getPrice() + ")", f -> {
			int price = unit.getPrice();
			int cash = player.getCash();
			if (cash >= price) {
				player.addUnit(unit.getID(), -1, -1);
				player.removeCash(price);
			}
		});
		new GUISeparator(this, -1, false);

		new GUIText(this, -1, "Name: " + unit.getName());
		new GUIText(this, -1, "Type: " + unit.getTypeName());
		new GUISeparator(this, -1, true);

		new GUIText(this, -1, "Moves: " + GUI.format(unit.getMoves()));
		new GUIText(this, -1, "Health: " + GUI.format(unit.getHealth()));
		new GUIText(this, -1, "Armor: " + GUI.format(unit.getArmor()));
		new GUISeparator(this, -1, true);

		new GUIText(this, -1, "Visibility: " + GUI.format(unit.getVisibility()));
		new GUISeparator(this, -1, false);

		new GUIText(this, -1, "Weapons:");
		new GUISeparator(this, -1, true);

		WeaponFile[] weapons = unit.getWeapons();
		for (int i = 0; i < weapons.length; i++) {
			new GUISeparator(this, -1, false);
			new GUIText(this, -1, weapons[i].getName() + " " + weapons[i].getType());
			new GUISeparator(this, -1, true);
			new GUIText(this, -1, "Damage: " + GUI.format(weapons[i].getDamage()));
			new GUIText(this, -1, "Piercing: " + GUI.format(weapons[i].getPiercing()));
			new GUIText(this, -1, "Range: " + GUI.format(weapons[i].getRange()));
			new GUIText(this, -1, "Accuracy: " + GUI.percent(weapons[i].getAccuracy()));
			new GUISeparator(this, -1, true);
		}

	}

	@Override
	public void render() {
		super.render(0, 0, Window.getWidth() / 4, Window.getHeight(), false);
	}

}
