package burlap.behavior.singleagent.learning.traces.tabular;

import burlap.mdp.core.action.Action;
import burlap.statehashing.HashableState;

/**
 * @author James MacGlashan.
 */
class SAKey {
	public HashableState s;
	public Action a;

	public SAKey(HashableState s, Action a) {
		this.s = s;
		this.a = a;
	}

	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;

		SAKey saKey = (SAKey) o;

		if(!s.equals(saKey.s)) return false;
		return a.equals(saKey.a);

	}

	@Override
	public int hashCode() {
		int result = s.hashCode();
		result = 31 * result + a.hashCode();
		return result;
	}
}
