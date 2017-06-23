package ki;

import java.awt.Point;
import java.util.Comparator;

public class PointWeightPair implements Comparator<PointWeightPair> {
	public Point point;
	public int weight;

	public PointWeightPair(Point point, int weight) {
		this.point = point;
		this.weight = weight;
	}

	@Override
	public int compare(PointWeightPair o1, PointWeightPair o2) {
		return Integer.compare(o1.weight, o2.weight);
	}

	public boolean equals(Object o) {
		return point.equals(((PointWeightPair) o).point);
	}
}
