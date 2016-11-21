package burlap.behavior.singleagent.learning.actorcritic.critic;

import burlap.behavior.policy.EnumerablePolicy;
import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.policy.Policy;
import burlap.behavior.policy.support.ActionProb;
import burlap.behavior.singleagent.learning.actorcritic.Critic;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;

import java.util.List;

/**
 * @author James MacGlashan
 */
public class PolicyTargetCritic implements Critic {

    protected EnumerablePolicy actor;
    protected SADomain domain;
    protected HashableStateFactory hashingFactory;
    protected double gamma;
    protected double viDelta;
    protected int maxIterations;
    protected ValueIteration vi = null;
    protected Policy targetPolicy = null;
    protected double wrongVal = 0.;

    public PolicyTargetCritic(EnumerablePolicy actor, SADomain domain, HashableStateFactory hashingFactory, double gamma, double viDelta, int maxIterations) {
        this.actor = actor;
        this.domain = domain;
        this.hashingFactory = hashingFactory;
        this.gamma = gamma;
        this.viDelta = viDelta;
        this.maxIterations = maxIterations;
    }

    public PolicyTargetCritic(EnumerablePolicy actor, SADomain domain, HashableStateFactory hashingFactory, double gamma, double viDelta, int maxIterations, double wrongVal) {
        this.actor = actor;
        this.domain = domain;
        this.hashingFactory = hashingFactory;
        this.gamma = gamma;
        this.viDelta = viDelta;
        this.maxIterations = maxIterations;
        this.wrongVal = wrongVal;
    }

    public void startEpisode(State s) {

    }

    public void endEpisode() {

    }

    public double critique(EnvironmentOutcome eo) {

        if(vi == null){
            vi = new ValueIteration(domain, 0.99, new SimpleHashableStateFactory(), viDelta, maxIterations);
            vi.planFromState(eo.o);
            targetPolicy = new GreedyQPolicy(vi);
        }

        if(targetPolicy.actionProb(eo.o, eo.a) > 0.){
            return 1.;
        }
        return this.wrongVal;
    }

    public void reset() {

    }

}
