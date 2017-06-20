package ki;

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
}
