package burlap.mdp.core.action.continuous;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;

import java.util.List;

/**
 * @author James MacGlashan.
 */
public class SimpleContinuousActionType implements ContinuousActionType {

	public String typeName;
	public int numParams;
	public ParameterDomain parameterDomain;

	public SimpleContinuousActionType() {
	}

	public SimpleContinuousActionType(String typeName, int numParams, ParameterDomain parameterDomain) {
		this.typeName = typeName;
		this.numParams = numParams;
		this.parameterDomain = parameterDomain;
	}

	public int numParameters() {
		return this.numParams;
	}

	public ParameterDomain parameterDomain() {
		return parameterDomain;
	}

	public Action generate(double... params) {
		if(params.length != this.numParams){
			throw new RuntimeException("Cannot create a continuous action. Expected prameters with dimension " + this.numParams + " but received " + params.length);
		}
		return new ContinuousAction(typeName, params.clone());
	}

	public String typeName() {
		return this.typeName;
	}

	public Action associatedAction(String strRep) {
		String [] comps = strRep.split(" ");
		double [] params = new double[comps.length];
		for(int i = 0; i < comps.length; i++){
			params[i] = Double.parseDouble(comps[i]);
		}
		return generate(params);
	}

	public List<Action> allApplicableActions(State s) {
		throw new RuntimeException("Cannot return all applicable actions for continuous actions.");
	}
}
