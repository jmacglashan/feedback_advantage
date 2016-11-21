package burlap.mdp.core.action.continuous;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;

/**
 * @author James MacGlashan.
 */
public interface ContinuousActionType extends ActionType {

	int numParameters();
	ParameterDomain parameterDomain();
	Action generate(double...params);


	class ParameterDomain{

		public double [] lower;
		public double [] upper;

		public ParameterDomain() {
		}

		public ParameterDomain(double[] lower, double[] upper) {
			this.lower = lower;
			this.upper = upper;
		}
	}

}
