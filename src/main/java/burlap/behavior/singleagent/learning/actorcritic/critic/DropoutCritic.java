package burlap.behavior.singleagent.learning.actorcritic.critic;

import burlap.behavior.singleagent.learning.actorcritic.Critic;
import burlap.debugtools.RandomFactory;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;

/**
 * @author James MacGlashan.
 */
public class DropoutCritic implements Critic {

	protected Critic delegate;
	protected double dropProb;

	public DropoutCritic(Critic delegate, double dropProb) {
		this.delegate = delegate;
		this.dropProb = dropProb;
	}

	public void startEpisode(State s) {
		this.delegate.startEpisode(s);
	}

	public void endEpisode() {
		this.delegate.endEpisode();
	}

	public double critique(EnvironmentOutcome eo) {

		double roll = RandomFactory.getMapped(0).nextDouble();
		if(roll < this.dropProb){
			return 0.;
		}

		return this.delegate.critique(eo);
	}

	public void reset() {
		this.delegate.reset();
	}
}
