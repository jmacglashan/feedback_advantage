package burlap.behavior.singleagent.learning.actorcritic.critic;

import burlap.behavior.policy.EnumerablePolicy;
import burlap.behavior.singleagent.learning.actorcritic.Critic;
import burlap.behavior.singleagent.planning.stochastic.policyiteration.PolicyEvaluation;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.statehashing.HashableStateFactory;

/**
 * @author James MacGlashan.
 */
public class AdvantageCritic implements Critic {

	protected EnumerablePolicy actor;
	protected SADomain domain;
	protected HashableStateFactory hashingFactory;
	protected double gamma;
	protected double viDelta;
	protected int maxIterations;
	protected PolicyEvaluation pe;

	public AdvantageCritic(EnumerablePolicy actor, SADomain domain, HashableStateFactory hashingFactory, double gamma, double viDelta, int maxIterations) {
		this.actor = actor;
		this.domain = domain;
		this.hashingFactory = hashingFactory;
		this.gamma = gamma;
		this.viDelta = viDelta;
		this.maxIterations = maxIterations;
	}

	public void startEpisode(State s) {

	}

	public void endEpisode() {

	}

	public double critique(EnvironmentOutcome eo) {

		if(pe == null){
			pe = new PolicyEvaluation(domain, gamma, hashingFactory, viDelta, maxIterations);
			pe.toggleDebugPrinting(false);
		}
		pe.evaluatePolicy(actor, eo.o);

		double advantage = pe.qValue(eo.o, eo.a) - pe.value(eo.o);

		return advantage;
	}

	public void reset() {

	}
}
