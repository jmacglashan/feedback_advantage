package burlap.mdp.core.action.continuous;

import burlap.mdp.core.action.SimpleAction;

import java.util.Arrays;

/**
 * @author James MacGlashan.
 */
public class ContinuousAction extends SimpleAction {
	public double [] parameters;

	public ContinuousAction() {
	}

	public ContinuousAction(String name, double[] parameters) {
		super(name);
		this.parameters = parameters;
	}

	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;
		if(!super.equals(o)) return false;

		ContinuousAction that = (ContinuousAction) o;

		return Arrays.equals(parameters, that.parameters);

	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + Arrays.hashCode(parameters);
		return result;
	}

	@Override
	public String toString() {
		return this.actionName() + Arrays.toString(this.parameters);
	}
}
