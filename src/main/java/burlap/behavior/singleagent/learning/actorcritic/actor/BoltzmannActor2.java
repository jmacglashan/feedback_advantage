package burlap.behavior.singleagent.learning.actorcritic.actor;

import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;

/**
 * @author James MacGlashan
 */
public class BoltzmannActor2 extends BoltzmannActor {

    public BoltzmannActor2(SADomain domain, HashableStateFactory hashingFactory, double learningRate) {
        super(domain, hashingFactory, learningRate);
    }

    @Override
    public void update(EnvironmentOutcome eo, double critique) {
        HashableState sh = this.hashingFactory.hashState(eo.o);
        PolicyNode node = this.getNode(sh);

        double learningRate = this.learningRate.pollLearningRate(this.totalNumberOfSteps, sh.s(), eo.a);

        ActionPreference pref = this.getMatchingPreference(sh, eo.a, node);
        pref.preference += learningRate * critique * (1. - this.actionProb(sh.s(), eo.a));

        this.totalNumberOfSteps++;
    }
}
