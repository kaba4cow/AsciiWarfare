package kaba4cow.warfare.gui;

import kaba4cow.ascii.toolbox.utils.StringUtils;

public final class GUI {

	public static final int COLOR = 0x0009FC;

	private GUI() {

	}

	public static String slash(int value1, int value2) {
		return value1 + " / " + value2;
	}

	public static String slash(float value1, float value2) {
		return format(value1) + " / " + format(value2);
	}

	public static String percent(float value) {
		return StringUtils.percent(value);
	}

	public static String level(float value) {
		return ((int) value) + " (" + percent(value % 1f) + ")";
	}

	public static String format(float value) {
		return StringUtils.format1(value);
	}

}
