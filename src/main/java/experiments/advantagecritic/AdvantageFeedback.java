package experiments.advantagecritic;

import burlap.behavior.policy.EnumerablePolicy;
import burlap.behavior.singleagent.humanfeedback.FeedbackReceiver;
import burlap.behavior.singleagent.planning.stochastic.policyiteration.PolicyEvaluation;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.environment.extensions.EnvironmentObserver;
import burlap.statehashing.HashableStateFactory;

/**
 * @author James MacGlashan
 */
public class AdvantageFeedback implements EnvironmentObserver {

    protected FeedbackReceiver receiver;

    protected EnumerablePolicy actor;
    protected SADomain domain;
    protected HashableStateFactory hashingFactory;
    protected double gamma;
    protected double viDelta;
    protected int maxIterations;
    protected PolicyEvaluation pe = null;


    public AdvantageFeedback(FeedbackReceiver receiver, EnumerablePolicy actor, SADomain domain, HashableStateFactory hashingFactory, double gamma, double viDelta, int maxIterations) {
        this.receiver = receiver;
        this.actor = actor;
        this.domain = domain;
        this.hashingFactory = hashingFactory;
        this.gamma = gamma;
        this.viDelta = viDelta;
        this.maxIterations = maxIterations;
    }

    @Override
    public void observeEnvironmentActionInitiation(State o, Action action) {
        if(pe == null){
            pe = new PolicyEvaluation(domain, gamma, hashingFactory, viDelta, maxIterations);
            pe.toggleDebugPrinting(false);
        }
        pe.evaluatePolicy(actor, o);

        double advantage = pe.qValue(o, action) - pe.value(o);

        receiver.receiveHumanFeedback(advantage);
    }

    @Override
    public void observeEnvironmentInteraction(EnvironmentOutcome eo) {

    }

    @Override
    public void observeEnvironmentReset(Environment resetEnvironment) {

    }
}
