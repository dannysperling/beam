package com.me.beamsolver;

import java.util.Map;

import model.Board;
import model.Piece;
import controller.LevelLoader;
import controller.LevelOrderer;
import controller.GameEngine.Color;

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
	
	private Arrangement get4() {
		Piece[][] pieces4 = new Piece[5][7];
		pieces4[0][0] = new Piece(0, 0, Color.PURPLE);
		pieces4[0][2] = new Piece(0, 2, Color.BLUE);
		pieces4[0][4] = new Piece(0, 4, Color.ORANGE);

		pieces4[1][1] = new Piece(1, 1, Color.BLUE);
		pieces4[1][5] = new Piece(1, 5, Color.PURPLE);

		pieces4[2][2] = new Piece(2, 2, Color.BLUE);
		pieces4[2][3] = new Piece(2, 3, Color.PURPLE);
		pieces4[2][4] = new Piece(2, 4, Color.ORANGE);

		pieces4[3][1] = new Piece(3, 1, Color.ORANGE);

		pieces4[4][0] = new Piece(4, 0, Color.PURPLE);
		pieces4[4][6] = new Piece(4, 6, Color.PURPLE);
		return new Arrangement(pieces4);
	}

	private Arrangement get5() {
		Piece[][] pieces5 = new Piece[5][7];
		pieces5[0][0] = new Piece(0, 0, Color.PURPLE);
		pieces5[0][2] = new Piece(0, 2, Color.BLUE);
		pieces5[0][4] = new Piece(0, 4, Color.ORANGE);

		pieces5[1][1] = new Piece(1, 1, Color.BLUE);
		pieces5[1][5] = new Piece(1, 5, Color.PURPLE);

		pieces5[2][2] = new Piece(2, 2, Color.BLUE);
		pieces5[2][3] = new Piece(2, 3, Color.PURPLE);
		pieces5[2][4] = new Piece(2, 4, Color.ORANGE);

		pieces5[3][1] = new Piece(3, 1, Color.ORANGE);
		pieces5[3][5] = new Piece(3, 5, Color.PURPLE);

		pieces5[4][0] = new Piece(4, 0, Color.PURPLE);
		return new Arrangement(pieces5);
	}

	private Arrangement get6() {
		Piece[][] pieces6 = new Piece[5][7];
		pieces6[0][0] = new Piece(0, 0, Color.PURPLE);
		pieces6[0][2] = new Piece(0, 2, Color.BLUE);
		pieces6[0][4] = new Piece(0, 4, Color.ORANGE);
		pieces6[0][6] = new Piece(0, 6, Color.ORANGE);

		pieces6[1][1] = new Piece(1, 1, Color.BLUE);
		pieces6[1][5] = new Piece(1, 5, Color.PURPLE);

		pieces6[2][2] = new Piece(2, 2, Color.BLUE);
		pieces6[2][3] = new Piece(2, 3, Color.PURPLE);
		pieces6[2][4] = new Piece(2, 4, Color.ORANGE);

		pieces6[3][5] = new Piece(3, 5, Color.PURPLE);

		pieces6[4][0] = new Piece(4, 0, Color.PURPLE);
		return new Arrangement(pieces6);
	}
	
	private void checkSolution(Map<String, Integer> table) {
		System.out.println("0 : " + table.get("G5G1BB2BGR2RR1G5G"));
		System.out.println("1 : " + table.get("G7BBG1BGR2RR1G5G"));
		System.out.println("2 : " + table.get("G1B5B1G1BGR2RR1G5G"));
		System.out.println("3 : " + table.get("G1B5BG2BGR2RR1G5G"));
		System.out.println("4 : " + table.get("G1B1R3BG2BGR2R2G5G"));
		System.out.println("5 : " + table.get("G1B1R3BG2BGR2RG1G"));
		System.out.println("6 : " + table.get("G1B1R1R1BG2BGR3G1G"));
	}
}
