package burlap.behavior.singleagent.learning.humanfeedback.coach.traceselector;

import burlap.mdp.singleagent.environment.EnvironmentOutcome;

/**
 * @author James MacGlashan.
 */
public interface TraceSelector {

	int useTrace(EnvironmentOutcome eo);

}
