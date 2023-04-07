package kaba4cow.warfare.gui.shop;

import kaba4cow.ascii.core.Display;
import kaba4cow.ascii.drawing.gui.GUIButton;
import kaba4cow.ascii.drawing.gui.GUIFrame;
import kaba4cow.ascii.drawing.gui.GUISeparator;
import kaba4cow.ascii.drawing.gui.GUIText;
import kaba4cow.warfare.Game;
import kaba4cow.warfare.files.UnitFile;
import kaba4cow.warfare.files.WeaponFile;
import kaba4cow.warfare.game.Player;

public class ShopUnitFrame extends GUIFrame {

	public ShopUnitFrame(Player player, UnitFile unit) {
		super(Game.GUI_COLOR, false, false);
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

		new GUIText(this, -1, "Moves: " + unit.getMoves());
		new GUIText(this, -1, "Health: " + unit.getHealth());
		new GUIText(this, -1, "Armor: " + unit.getArmor());
		new GUISeparator(this, -1, true);

		new GUIText(this, -1, "Visibility: " + unit.getVisibility());
		new GUISeparator(this, -1, false);

		new GUIText(this, -1, "Weapons:");
		new GUISeparator(this, -1, true);

		WeaponFile[] weapons = unit.getWeapons();
		for (int i = 0; i < weapons.length; i++) {
			new GUISeparator(this, -1, false);
			new GUIText(this, -1, weapons[i].getName() + " " + weapons[i].getType());
			new GUISeparator(this, -1, true);
			new GUIText(this, -1, "Damage: " + weapons[i].getDamage());
			new GUIText(this, -1, "Piercing: " + weapons[i].getPiercing());
			new GUIText(this, -1, "Range: " + weapons[i].getRange());
			new GUIText(this, -1, "Accuracy: " + weapons[i].getAccuracy());
			new GUISeparator(this, -1, true);
			new GUIText(this, -1, "Attacks: " + weapons[i].getAttacks());
		}

	}

	@Override
	public void render() {
		super.render(0, 0, Display.getWidth() / 4, Display.getHeight(), false);
	}

}
