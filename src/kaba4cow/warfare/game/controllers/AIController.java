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
	private ArrayList<Unit> units;
	private ArrayList<Unit> defenseUnits;
	private ArrayList<Unit> attackUnits;

	private HashMap<Unit, Village> unitVillages;

	private int currentUnit;

	private int prevUnits;
	private int targetPrice;

	public AIController() {
		super(0xF54);
		this.villages = null;
		this.units = null;
		this.defenseUnits = null;
		this.attackUnits = null;
		this.unitVillages = new HashMap<>();
		this.currentUnit = -1;
		this.prevUnits = 0;
		resetTargetPrice();
	}

	@Override
	public void update(float dt) {
		if (villages == null)
			sortVillages();

		if (currentUnit == -1) {
			units = player.getUnits();

			if (defenseUnits == null) {
				defenseUnits = new ArrayList<>();
				attackUnits = new ArrayList<>();
				for (Unit unit : units) {
					if (RNG.randomBoolean())
						defenseUnits.add(unit);
					else
						attackUnits.add(unit);
				}
			}
			for (int i = defenseUnits.size() - 1; i >= 0; i--) {
				Unit unit = defenseUnits.get(i);
				if (unit.isDestroyed())
					defenseUnits.remove(i);
			}
			for (int i = attackUnits.size() - 1; i >= 0; i--) {
				Unit unit = attackUnits.get(i);
				if (unit.isDestroyed())
					attackUnits.remove(i);
			}

			LinkedList<Village> freeVillages = new LinkedList<>();
			for (int i = 0; i < villages.size(); i++)
				if (villages.get(i).getOccupier(world) == null)
					freeVillages.add(villages.get(i));
			currentUnit = 0;
			if (prevUnits != defenseUnits.size()) {
				int ratio = defenseUnits.size() / villages.size();
				int unitIndex = 0;
				LinkedList<Unit> freeUnits = new LinkedList<>(defenseUnits);
				while (!freeUnits.isEmpty()) {
					Unit unit = freeUnits.removeFirst();
					Village village;
					if (unitIndex % 3 != 0 && !freeVillages.isEmpty())
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
			prevUnits = defenseUnits.size();
		} else if (player.getCurrentUnit().isMoving() || player.getCurrentUnit().isShooting())
			return;
		player.setCurrentUnit(currentUnit);

		Unit unit = player.getCurrentUnit();
		if (defenseUnits.contains(unit)) {
			Village village = unitVillages.get(unit);
			Vector2i pos = getVillagePoint(unit, village);
			processUnitAttack(unit);
			processUnitMove(unit, pos);
		} else if (attackUnits.contains(unit)) {
			Unit target = getClosestUnit(unit);
			Vector2i unitPos = getUnitPoint(unit, target);
			processUnitAttack(unit);
			processUnitMove(unit, unitPos);
		}

		currentUnit++;
		if (currentUnit >= player.getUnits().size()) {
			processHire();
			currentUnit = -1;
			world.newTurn(player.getIndex(), false);
		}
		State.PROGRESS = (float) currentUnit / (float) units.size();
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

		Unit newUnit = player.addUnit(unit.getID(), -1, -1);
		if (RNG.chance(0.8f))
			attackUnits.add(newUnit);
		else
			defenseUnits.add(newUnit);
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
				Vector2i unitPos = getUnitAttackPoint(unit, weapons[i], target);
				if (unitPos != null) {
					unit.createPath(unitPos.x, unitPos.y);
					if (!unit.isMoving())
						unit.createPath(unitPos.x, unitPos.y);
				} else {
					unit.createAttackPath(target.getX(), target.getY());
					if (!unit.isShooting())
						unit.createAttackPath(target.getX(), target.getY());
					if (!unit.isShooting())
						unit.createAttackPath(target.getX(), target.getY());
				}
			}
		}
	}

	private void processUnitMove(Unit unit, Vector2i pos) {
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

	private Unit getClosestUnit(Unit source) {
		ArrayList<Unit> list = world.getPlayer().getUnits();
		Unit target = null;
		float minDist = Float.POSITIVE_INFINITY;
		for (int i = 0; i < list.size(); i++) {
			Unit current = list.get(i);
			float dist = Maths.dist(source.getX(), source.getY(), current.getX(), current.getY());
			if (dist < minDist) {
				minDist = dist;
				target = current;
			}
		}
		return target;
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
				minValue = value;
				target = unit;
			}
		}

		return target;
	}

	private Vector2i getVillagePoint(Unit unit, Village village) {
		if (village == null)
			return null;

		int x, y;
		int range = village.radius;
		float maxDistSq = range * range;
		float distSq;

		distSq = Maths.distSq(unit.getX(), unit.getY(), village.x, village.y);
		if (distSq < maxDistSq)
			return new Vector2i(unit.getX(), unit.getY());

		int attempts = 0;
		final int maxAttempts = 16;
		while (true) {
			x = village.x + RNG.randomInt(-range, range);
			y = village.y + RNG.randomInt(-range, range);

			if (!world.isObstacle(x, y)) {
				distSq = Maths.distSq(x, y, village.x, village.y);
				if (distSq < maxDistSq)
					return new Vector2i(x, y);
			}

			attempts++;
			if (attempts >= maxAttempts)
				return null;
		}
	}

	private Vector2i getUnitPoint(Unit unit, Unit target) {
		if (target == null)
			return null;
		int x, y;
		int range = 0;
		WeaponFile[] weapons = unit.getUnitFile().getWeapons();
		for (int i = 0; i < weapons.length; i++)
			range = Maths.max(range, (int) weapons[i].getRange());
		float maxDistSq = range * range;
		float distSq;

		distSq = Maths.distSq(unit.getX(), unit.getY(), target.getX(), target.getY());
		if (distSq < maxDistSq)
			return null;

		for (int i = 0; i < 16; i++) {
			x = target.getX() + RNG.randomInt(-range, range);
			y = target.getY() + RNG.randomInt(-range, range);

			if (!world.isObstacle(x, y)) {
				distSq = Maths.distSq(x, y, target.getX(), target.getY());
				if (distSq < maxDistSq)
					return new Vector2i(x, y);
			}
		}

		return null;
	}

	private Vector2i getUnitAttackPoint(Unit unit, WeaponFile weapon, Unit target) {
		int x, y;
		int range = (int) weapon.getRange();
		float maxDistSq = range * range;
		float distSq;

		distSq = Maths.distSq(unit.getX(), unit.getY(), target.getX(), target.getY());
		if (distSq < maxDistSq)
			return null;

		if (Pathfinder.getAttackPath(world, player, weapon, unit.getX(), unit.getY(), target.getX(),
				target.getY()) != null)
			return null;

		for (int i = 0; i < 16; i++) {
			x = target.getX() + RNG.randomInt(-range, range);
			y = target.getY() + RNG.randomInt(-range, range);

			if (!world.isObstacle(x, y)
					&& Pathfinder.getAttackPath(world, player, weapon, x, y, target.getX(), target.getY()) != null) {
				distSq = Maths.distSq(x, y, target.getX(), target.getY());
				if (distSq < maxDistSq)
					return new Vector2i(x, y);
			}
		}

		return null;
	}

	@Override
	public void render(int offX, int offY) {

	}

}
