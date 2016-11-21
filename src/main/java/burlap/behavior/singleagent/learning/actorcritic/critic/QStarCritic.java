package burlap.behavior.singleagent.learning.actorcritic.critic;

import burlap.behavior.policy.EnumerablePolicy;
import burlap.behavior.singleagent.learning.actorcritic.Critic;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;

/**
 * @author James MacGlashan
 */
public class QStarCritic implements Critic {

    protected EnumerablePolicy actor;
    protected SADomain domain;
    protected HashableStateFactory hashingFactory;
    protected double gamma;
    protected double viDelta;
    protected int maxIterations;
    protected ValueIteration vi = null;

    public QStarCritic(EnumerablePolicy actor, SADomain domain, HashableStateFactory hashingFactory, double gamma, double viDelta, int maxIterations) {
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

        if(vi == null){
            vi = new ValueIteration(domain, 0.99, new SimpleHashableStateFactory(), viDelta, maxIterations);
            vi.planFromState(eo.o);
        }

        return vi.qValue(eo.o, eo.a);
    }

    public void reset() {

    }

}
