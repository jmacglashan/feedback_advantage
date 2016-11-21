package burlap.behavior.singleagent.learning.humanfeedback.coach;

import burlap.behavior.singleagent.learning.actorcritic.Critic;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;

/**
 * @author James MacGlashan.
 */
public class CoachCritic implements Critic {

	@Override
	public void startEpisode(State s) {
		//do nothing
	}

	@Override
	public void endEpisode() {
		//do nothing
	}

	@Override
	public double critique(EnvironmentOutcome eo) {
		return eo.r;
	}

	@Override
	public void reset() {

	}
}
