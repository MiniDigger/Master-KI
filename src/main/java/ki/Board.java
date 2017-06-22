package ki;

import java.awt.Point;

import mazeclient.generated.CardType;

public class Board {
	Feld[][] board;

	public Board() {
		board = new Feld[7][7];
	}

	public Feld getFeld(int x, int y) {
		return board[y][x];
	}

	public void setFeld(int x, int y, Feld feld) {
		board[y][x] = feld;
	}

	public Board(Board b) {
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[i].length; j++) {
				board[i][j] = b.board[i][j];
			}
		}
	}

	public void placeShiftCard(CardType card, Point pos, boolean vertical, boolean rightTop) {
		if (vertical) {
			int col = pos.x;
			for (int i = 0; i < 6; i++) {
				if (rightTop) {
					board[6 - i][col] = board[6 - i - 1][col];
				} else {
					board[i][col] = board[i + 1][col];
				}
			}
			board[pos.y][pos.x] = new Feld(card);
		} else {
			int row = pos.y;
			for (int i = 0; i < 6; i++) {
				if (rightTop) {
					board[row][i] = board[row][i + 1];
				} else {
					board[row][6 - i] = board[row][6 - i - 1];
				}
			}
			board[pos.y][pos.x] = new Feld(card);
		}
	}
}
