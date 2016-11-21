package burlap.behavior.singleagent.humanfeedback.tamer.rewardmodel;


import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;

/**
 * @author James MacGlashan.
 */
public interface HumanRewardModel extends RewardFunction {

	void updateModel(State s, Action a, double reward);
	HumanRewardModel copy();
	void resetModel();


}
