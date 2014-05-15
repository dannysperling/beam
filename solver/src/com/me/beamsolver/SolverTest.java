package com.me.beamsolver;

import model.Board;
import controller.LevelLoader;
import controller.LevelOrderer;

public class SolverTest {
	
	private static LevelOrderer levelOrderer = new LevelOrderer(
			"../beam-android/assets/data/levels/levelOrder.txt", false);
	private static LevelLoader levelLoader = new LevelLoader(
			"../beam-android/assets/data/levels/levels.xml", levelOrderer,
			false);
	
	public static void main(String[] args) {
		checkLevel(1, 1);
		checkLevel(1, 2);
		checkLevel(1, 3);
		checkLevel(1, 4);
		checkLevel(1, 5);
		checkLevel(1, 6);
		System.out.println();
		
		checkLevel(2, 1);
		checkLevel(2, 2);
		checkLevel(2, 3);
		checkLevel(2, 4);
		checkLevel(2, 5);
		System.out.println();
		
		checkLevel(3, 1);
		checkLevel(3, 2);
		checkLevel(3, 3);
		checkLevel(3, 4);
		checkLevel(3, 5);
		checkLevel(3, 6);
		System.out.println();
		
		checkLevel(4, 1);
		checkLevel(4, 2);
		checkLevel(4, 3);
		checkLevel(4, 4);
		checkLevel(4, 5);
		System.out.println();
		
		checkLevel(5, 1);
		checkLevel(5, 2);
		checkLevel(5, 3);
		checkLevel(5, 4);
		System.out.println("Level 5-5 delayed.");
		checkLevel(5, 6);
		checkLevel(5, 7);
		checkLevel(5, 8);
		System.out.println();
		
		checkLevel(6, 1);
		checkLevel(6, 2);
		checkLevel(6, 3);
		checkLevel(6, 4);
		checkLevel(6, 5);
		System.out.println();
		
		checkLevel(7, 1);
		checkLevel(7, 2);
		checkLevel(7, 3);
		checkLevel(7, 4);
		checkLevel(7, 5);
		checkLevel(7, 6);
		checkLevel(7, 7);
		System.out.println();
		
		checkLevel(8, 1);
		System.out.println("Level 8-2 delayed.");
		System.out.println("Level 8-3 delayed.");
		checkLevel(8, 4);
		checkLevel(8, 5);
		System.out.println("Level 8-6 delayed.");
		System.out.println();
		
		System.out.println("Level 9-1 delayed.");
		checkLevel(9, 2);
		checkLevel(9, 3);
		System.out.println("Level 9-4 delayed.");
		checkLevel(9, 5);
		checkLevel(9, 6);
		checkLevel(9, 7);
		checkLevel(9, 8);
		System.out.println();
		
		System.out.println("Solving delayed levels: ");
		checkLevel(5, 5);
		checkLevel(8, 2);
		checkLevel(8, 3);
		checkLevel(8, 6);
		checkLevel(9, 1);
		checkLevel(9, 4);
		System.out.println("Done.");
	}
	
	private static void checkLevel(int world, int ordinalInWorld) {
		Board toSolve = levelLoader.getLevel(world, ordinalInWorld);
		Solver solver = new Solver(toSolve, false);
		solver.solve();
		if (solver.getMovesNeeded() != toSolve.perfect) {
			System.out.println("Level " + world + "-" + ordinalInWorld + " failed!");
		} else {
			System.out.println("Level " + world + "-" + ordinalInWorld + " passed.");
		}
	}
}
