package burlap.behavior.singleagent.learning.actorcritic.critic;

import burlap.behavior.singleagent.learning.actorcritic.Critic;
import burlap.debugtools.RandomFactory;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;

/**
 * @author James MacGlashan.
 */
public class AggregateDropoutCritic extends DropoutCritic {

	protected double sum = 0.;
	protected int num = 0;

	public AggregateDropoutCritic(Critic delegate, double dropProb) {
		super(delegate, dropProb);
	}

	@Override
	public void startEpisode(State s) {
		this.sum = 0.;
		this.num++;
	}

	@Override
	public double critique(EnvironmentOutcome eo) {

		double mostRecent = this.delegate.critique(eo);
		this.sum += mostRecent;
		this.num++;

		double roll = RandomFactory.getMapped(0).nextDouble();
		if(roll < this.dropProb){
			return 0.;
		}

		double ret = sum / this.num;
		this.sum = 0.;
		this.num = 0;
		return ret;
	}
}
