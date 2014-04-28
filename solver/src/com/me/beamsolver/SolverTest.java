package com.me.beamsolver;

import model.Piece;
import controller.GameEngine.Color;

public class SolverTest {
	private Arrangement get4() {
		Piece[][] pieces4 = new Piece[5][7];
		pieces4[0][0] = new Piece(0, 0, Color.GREEN);
		pieces4[0][2] = new Piece(0, 2, Color.BLUE);
		pieces4[0][4] = new Piece(0, 4, Color.RED);

		pieces4[1][1] = new Piece(1, 1, Color.BLUE);
		pieces4[1][5] = new Piece(1, 5, Color.GREEN);

		pieces4[2][2] = new Piece(2, 2, Color.BLUE);
		pieces4[2][3] = new Piece(2, 3, Color.GREEN);
		pieces4[2][4] = new Piece(2, 4, Color.RED);

		pieces4[3][1] = new Piece(3, 1, Color.RED);

		pieces4[4][0] = new Piece(4, 0, Color.GREEN);
		pieces4[4][6] = new Piece(4, 6, Color.GREEN);
		return new Arrangement(pieces4);
	}

	private Arrangement get5() {
		Piece[][] pieces5 = new Piece[5][7];
		pieces5[0][0] = new Piece(0, 0, Color.GREEN);
		pieces5[0][2] = new Piece(0, 2, Color.BLUE);
		pieces5[0][4] = new Piece(0, 4, Color.RED);

		pieces5[1][1] = new Piece(1, 1, Color.BLUE);
		pieces5[1][5] = new Piece(1, 5, Color.GREEN);

		pieces5[2][2] = new Piece(2, 2, Color.BLUE);
		pieces5[2][3] = new Piece(2, 3, Color.GREEN);
		pieces5[2][4] = new Piece(2, 4, Color.RED);

		pieces5[3][1] = new Piece(3, 1, Color.RED);
		pieces5[3][5] = new Piece(3, 5, Color.GREEN);

		pieces5[4][0] = new Piece(4, 0, Color.GREEN);
		return new Arrangement(pieces5);
	}

	private Arrangement get6() {
		Piece[][] pieces6 = new Piece[5][7];
		pieces6[0][0] = new Piece(0, 0, Color.GREEN);
		pieces6[0][2] = new Piece(0, 2, Color.BLUE);
		pieces6[0][4] = new Piece(0, 4, Color.RED);
		pieces6[0][6] = new Piece(0, 6, Color.RED);

		pieces6[1][1] = new Piece(1, 1, Color.BLUE);
		pieces6[1][5] = new Piece(1, 5, Color.GREEN);

		pieces6[2][2] = new Piece(2, 2, Color.BLUE);
		pieces6[2][3] = new Piece(2, 3, Color.GREEN);
		pieces6[2][4] = new Piece(2, 4, Color.RED);

		pieces6[3][5] = new Piece(3, 5, Color.GREEN);

		pieces6[4][0] = new Piece(4, 0, Color.GREEN);
		return new Arrangement(pieces6);
	}
}