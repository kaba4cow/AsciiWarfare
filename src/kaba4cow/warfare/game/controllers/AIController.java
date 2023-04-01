package kaba4cow.warfare.game.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import kaba4cow.ascii.toolbox.maths.Maths;
import kaba4cow.ascii.toolbox.maths.vectors.Vector2i;
import kaba4cow.ascii.toolbox.rng.RNG;
import kaba4cow.warfare.files.UnitFile;
import kaba4cow.warfare.files.WeaponFile;
import kaba4cow.warfare.game.Unit;
import kaba4cow.warfare.game.Village;
import kaba4cow.warfare.pathfinding.Pathfinder;
import kaba4cow.warfare.states.State;

public class AIController extends Controller {

	private ArrayList<Village> villages;

	private HashMap<Unit, Village> unitVillages;

	private int currentUnit;

	private int prevUnits;
	private int targetPrice;

	public AIController() {
		super(0xF54);
		this.villages = null;
		this.unitVillages = new HashMap<>();
		this.currentUnit = -1;
		this.prevUnits = 0;
		resetTargetPrice();
	}

	@Override
	public void update(float dt) {
		if (villages == null)
			sortVillages();

		ArrayList<Unit> units = player.getUnits();
		State.PROGRESS = (float) currentUnit / (float) units.size();

		if (currentUnit == -1) {
			LinkedList<Village> freeVillages = new LinkedList<>();
			for (int i = 0; i < villages.size(); i++)
				if (villages.get(i).getOccupier(world) != player)
					freeVillages.add(villages.get(i));
			currentUnit = 0;
			if (prevUnits != units.size()) {
				int ratio = units.size() / villages.size();
				int unitIndex = 0;
				LinkedList<Unit> freeUnits = new LinkedList<>(units);
				while (!freeUnits.isEmpty()) {
					Unit unit = freeUnits.removeFirst();
					Village village;
					if (unitIndex % 4 != 0 && !freeVillages.isEmpty())
						village = freeVillages.removeFirst();
					else {
						int index = RNG.randomInt(0, ratio + 3);
						if (index >= villages.size())
							index = villages.size() - 1;
						village = villages.get(index);
					}
					unitVillages.put(unit, village);
					unitIndex++;
				}
			}
			prevUnits = units.size();
		} else if (player.getCurrentUnit().isMoving())
			return;
		if (currentUnit >= player.getUnits().size()) {
			processHire();
			currentUnit = -1;
			world.newTurn(player.getIndex(), false);
			return;
		}
		player.setCurrentUnit(currentUnit);

		Unit unit = player.getCurrentUnit();
		if (unit.isMoving())
			return;

		if (RNG.randomBoolean())
			processUnitMove(unit);
		processUnitAttack(unit);

		currentUnit++;
	}

	private void resetTargetPrice() {
		targetPrice = RNG.randomInt(UnitFile.getMinPrice(), UnitFile.getMaxPrice() + 1);
	}

	private void processHire() {
		if (player.getCash() < targetPrice || world.getVillage(player.getVillage()).getOccupier(world) != player)
			return;

		float minPriceDist = Float.POSITIVE_INFINITY;
		UnitFile unit = null;
		HashMap<String, UnitFile> units = UnitFile.getFiles();
		for (String id : units.keySet()) {
			int price = units.get(id).getPrice();
			float priceDist = Maths.dist(targetPrice, price);
			if (priceDist < minPriceDist) {
				minPriceDist = priceDist;
				unit = units.get(id);
			}
		}

		if (unit == null)
			return;

		player.addUnit(unit.getID(), -1, -1);
		player.removeCash(unit.getPrice());
		resetTargetPrice();
	}

	private void processUnitAttack(Unit unit) {
		WeaponFile[] weapons = unit.getUnitFile().getWeapons();
		for (int i = 0; i < weapons.length; i++) {
			for (int j = 0; j < weapons[i].getAttacks(); j++) {
				Unit target = getUnitTarget(unit, weapons[i]);
				if (target == null)
					continue;
				unit.createAttackPath(target.getX(), target.getY());
				if (!unit.isShooting())
					unit.createAttackPath(target.getX(), target.getY());
			}
		}
	}

	private void processUnitMove(Unit unit) {
		Village village = unitVillages.get(unit);
		Vector2i pos = getVillagePoint(unit, village);
		if (pos == null)
			return;
		player.setIgnoreVisibility(true);
		unit.createPath(pos.x, pos.y);
		if (!unit.isMoving())
			unit.createPath(pos.x, pos.y);
		if (!unit.isMoving())
			unit.createPath(pos.x, pos.y);
		player.setIgnoreVisibility(false);
	}

	private void sortVillages() {
		villages = new ArrayList<>(world.getVillages());
		Village village = villages.get(player.getVillage());

		Village village1, village2;
		int i, j;
		float dist1, dist2;
		int n = villages.size() - 1;
		for (i = 0; i < n; i++) {
			for (j = 0; j < n; j++) {
				village1 = villages.get(j);
				village2 = villages.get(j + 1);
				dist1 = Maths.dist(village.x, village.y, village1.x, village1.y) / (float) village1.getIncome();
				dist2 = Maths.dist(village.x, village.y, village2.x, village2.y) / (float) village2.getIncome();
				if (dist1 > dist2)
					Collections.swap(villages, j, j + 1);
			}
		}
	}

	private Unit getUnitTarget(Unit source, WeaponFile weapon) {
		Unit target = null;

		float maxArmor = weapon.getPiercing();
		float maxDist = weapon.getRange();

		float minValue = Float.POSITIVE_INFINITY;
		float value, dist, health;
		ArrayList<Unit> units = world.getPlayer().getUnits();
		for (int i = 0; i < units.size(); i++) {
			Unit unit = units.get(i);
			if (unit.getArmor() > maxArmor)
				continue;
			dist = Maths.dist(source.getX(), source.getY(), unit.getX(), unit.getY());
			if (dist > maxDist)
				continue;
			health = unit.getUnits() * unit.getMaxHealth() + unit.getHealth();
			value = health + dist;

			if (value < minValue) {
				if (Pathfinder.getAttackPath(world, player, weapon, source.getX(), source.getY(), unit.getX(),
						unit.getY()) == null)
					continue;
				minValue = value;
				target = unit;
			}
		}

		return target;
	}

	private Vector2i getVillagePoint(Unit unit, Village village) {
		int x, y;
		int range = village.radius;
		float maxDistSq = range * range;
		float distSq;

		distSq = Maths.distSq(unit.getX(), unit.getY(), village.x, village.y);
		if (distSq < maxDistSq)
			return null;

		while (true) {
			x = village.x + RNG.randomInt(-range, range);
			y = village.y + RNG.randomInt(-range, range);

			if (world.isObstacle(x, y))
				continue;
			distSq = Maths.distSq(x, y, village.x, village.y);
			if (distSq < maxDistSq)
				break;
		}

		return new Vector2i(x, y);
	}

	@Override
	public void render(int offX, int offY) {

	}

}
