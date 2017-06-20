package ki;

import java.awt.Point;

public abstract class KI {
	
	private KiData data;
	
	Point getPositionOfTreasure() {
		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 7; j++) {
				if (data.board.getFeld(j, i).getTreasure() == data.treasure) {
					return new Point(j, i);
				}
			}
		}
		return null;
	}
	
	abstract void move();
	
}
