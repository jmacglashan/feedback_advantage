package burlap.behavior.singleagent.humanfeedback.tamer;

import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.MDPSolver;
import burlap.behavior.singleagent.humanfeedback.FeedbackReceiver;
import burlap.behavior.singleagent.humanfeedback.tamer.rewardmodel.HumanRewardModel;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.valuefunction.QProvider;
import burlap.behavior.valuefunction.QValue;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;

import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of TAMER with infrequent action selection [1] (when the the trainer has enough time to explicitly provide
 * feedback for each state-action pair). When human feedback of zero is given, it is ignored and not used as a training sample.
 *
 * <br/><br/>
 * 1. W. Bradley Knox. Learning from Human-Generated Reward. Dissertation. September 2012.
 * @author James MacGlashan.
 */
public class TamerInfrequent extends MDPSolver implements LearningAgent, FeedbackReceiver, QProvider{

	protected HumanRewardModel hr;
	protected volatile double lastHumanReward;
	protected Policy learningPolicy = new GreedyQPolicy(this);

	public TamerInfrequent(SADomain domain, HumanRewardModel hr) {
		this.solverInit(domain, 0.0, null);
		this.hr = hr;
	}

	public HumanRewardModel getHr() {
		return hr;
	}

	public void setHr(HumanRewardModel hr) {
		this.hr = hr;
	}

	public Policy getLearningPolicy() {
		return learningPolicy;
	}

	public void setLearningPolicy(Policy learningPolicy) {
		this.learningPolicy = learningPolicy;
	}


	@Override
	public void receiveHumanFeedback(double f) {
		lastHumanReward += f;
	}

	@Override
	public Episode runLearningEpisode(Environment env) {
		return this.runLearningEpisode(env, -1);
	}

	@Override
	public Episode runLearningEpisode(Environment env, int maxSteps) {
		this.lastHumanReward = 0.;
		Episode ea = new Episode(env.currentObservation());

		int steps = 0;
		while(!env.isInTerminalState() && (steps < maxSteps || maxSteps == -1) ){

			State curState = env.currentObservation();
			Action ga = learningPolicy.action(curState);
			EnvironmentOutcome eo = env.executeAction(ga);
			double h = this.getAndResetHumanFeedback();
			ea.transition(ga, eo.op, h);
			//if(h != 0.) {
				this.hr.updateModel(curState, ga, h);
			//}

			steps++;

		}

		return ea;
	}

	@Override
	public void resetSolver() {
		this.lastHumanReward = 0.;
		this.hr.resetModel();
	}

    @Override
    public List<QValue> qValues(State s) {
        List<Action> gas = this.applicableActions(s);
        List<QValue> qs = new ArrayList<QValue>(gas.size());
        for(Action ga : gas){
            qs.add(new QValue(s, ga, this.qValue(s, ga)));
        }
        return qs;
    }

    @Override
    public double qValue(State s, Action a) {
        double q = this.hr.reward(s, a, null);
        return q;
    }



	@Override
	public double value(State s) {
		return QProvider.Helper.maxQ(this, s);
	}

	protected double getAndResetHumanFeedback(){
		double val = this.lastHumanReward;
		this.lastHumanReward = 0.;
		return val;
	}

}
