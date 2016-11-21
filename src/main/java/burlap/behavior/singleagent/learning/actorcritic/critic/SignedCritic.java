package burlap.behavior.singleagent.learning.actorcritic.critic;

import burlap.behavior.singleagent.learning.actorcritic.Critic;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;

/**
 * @author James MacGlashan.
 */
public class SignedCritic implements Critic {

	protected Critic delegate;

	public SignedCritic(Critic delegate) {
		this.delegate = delegate;
	}

	public void startEpisode(State s) {
		this.delegate.startEpisode(s);
	}

	public void endEpisode() {
		this.delegate.endEpisode();
	}

	public double critique(EnvironmentOutcome eo) {

		double src = this.delegate.critique(eo);
		return Math.signum(src);
	}

	public void reset() {
		this.delegate.reset();
	}
}
