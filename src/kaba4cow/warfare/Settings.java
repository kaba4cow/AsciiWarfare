package kaba4cow.warfare;

import java.util.prefs.Preferences;

public class Settings {

	private static Preferences preferences;

	private Settings() {

	}

	public static void init() {
		preferences = Preferences.userNodeForPackage(Settings.class);
	}

	public static boolean isFullscreen() {
		return preferences.getBoolean("fullscreen", true);
	}

	public static void setFullscreen(boolean fullscreen) {
		preferences.putBoolean("fullscreen", fullscreen);
	}

	public static String getServerAddress() {
		return preferences.get("ip", "");
	}

	public static String getServerPort() {
		return preferences.get("port", "");
	}

	public static void setServerInfo(String serverAddress, String serverPort) {
		preferences.put("ip", serverAddress);
		preferences.put("port", serverPort);
	}

	public static int getWorldSize() {
		return preferences.getInt("size", 0);
	}

	public static int getWorldSeason() {
		return preferences.getInt("season", 2);
	}

	public static void setWorldInfo(int worldSize, int worldSeason) {
		preferences.putInt("size", worldSize);
		preferences.putInt("season", worldSeason);
	}

}
