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
		this();
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[i].length; j++) {
				board[i][j] = b.board[i][j];
			}
		}
	}

	public Point[] placeShiftCard(CardType card, Point cardPos, Point playerPos, Point treasurePos, boolean vertical,
			boolean rightTop) {
		// playerPos,treasurePos
		Point[] positions = new Point[] { playerPos, treasurePos };
		if (vertical) {
			int col = cardPos.x;
			if (playerPos.x == col) {
				positions[0].y += rightTop ? 1 : -1;
				if (positions[0].y > 6) {
					positions[0].y = 0;
				}
				if (positions[0].y < 0) {
					positions[0].y = 6;
				}
			}
			if (treasurePos.x == col) {
				positions[1].y += rightTop ? 1 : -1;
			}
			for (int i = 0; i < 6; i++) {
				if (rightTop) {
					board[6 - i][col] = board[6 - i - 1][col];
				} else {
					board[i][col] = board[i + 1][col];
				}
			}
			board[cardPos.y][cardPos.x] = new Feld(card);
		} else {
			int row = cardPos.y;
			if (playerPos.y == row) {
				positions[0].x += rightTop ? -1 : 1;
				if (positions[0].x > 6) {
					positions[0].x = 0;
				}
				if (positions[0].x < 0) {
					positions[0].x = 6;
				}
			}
			if (treasurePos.y == row) {
				positions[1].x += rightTop ? -1 : 1;
			}
			for (int i = 0; i < 6; i++) {
				if (rightTop) {
					board[row][i] = board[row][i + 1];
				} else {
					board[row][6 - i] = board[row][6 - i - 1];
				}
			}
			board[cardPos.y][cardPos.x] = new Feld(card);
		}
		return positions;
	}
}
