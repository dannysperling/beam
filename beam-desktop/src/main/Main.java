package main;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import controller.GameEngine;

public class Main {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "beam";
		cfg.useGL20 = true;

		cfg.width = 360;
		cfg.height = 640;

		cfg.foregroundFPS = 60;

		new LwjglApplication(new GameEngine(), cfg);
	}
}
