package ki;

import java.awt.Point;

public class PointWeightPair implements Comparable<PointWeightPair> {
	public Point point;
	public int weight;

	public PointWeightPair(Point point, int weight) {
		this.point = point;
		this.weight = weight;
	}

	@Override
	public int compareTo(PointWeightPair o) {
		return Integer.compare(weight, o.weight);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((point == null) ? 0 : point.hashCode());
		result = prime * result + weight;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PointWeightPair other = (PointWeightPair) obj;
		if (point == null) {
			if (other.point != null)
				return false;
		} else if (!point.equals(other.point))
			return false;
		return true;
	}

	public String toString() {
		return point.x + "/" + point.y + ":" + weight + "\n";
	}
}
