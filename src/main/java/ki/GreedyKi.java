package ki;

import java.awt.Point;

public class GreedyKi extends KI {

	@Override
	void move() {
		Point coord = getPositionOfTreasure();
		// Karte reinschieben
		int[][] posToShiftCard = new int[][] { { 1, 0 }, { 3, 0 }, { 5, 0 }, { 6, 1 }, { 6, 3 }, { 6, 5 }, { 5, 6 },
				{ 3, 6 }, { 1, 6 }, { 0, 5 }, { 0, 3 }, { 0, 1 } };
	}
}
