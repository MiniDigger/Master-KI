package ki;

import java.util.Comparator;

public class FieldWeightPair implements Comparator<FieldWeightPair> {
	public Feld field;
	public int weight;

	public FieldWeightPair(Feld field, int weight) {
		this.field = field;
		this.weight = weight;
	}

	@Override
	public int compare(FieldWeightPair o1, FieldWeightPair o2) {
		return Integer.compare(o1.weight, o2.weight);
	}
}
