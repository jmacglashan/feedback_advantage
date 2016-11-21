package burlap.behavior.singleagent.learning.actorcritic.actor;

import burlap.behavior.functionapproximation.DifferentiablePolicy;
import burlap.behavior.functionapproximation.FunctionGradient;
import burlap.behavior.functionapproximation.ParametricFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionUtils;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;

import java.util.List;

/**
 * @author James MacGlashan
 */
public class BoltzmannActor3 extends BoltzmannActor {

    public BoltzmannActor3(SADomain domain, HashableStateFactory hashingFactory, double learningRate) {
        super(domain, hashingFactory, learningRate);
    }

    @Override
    public void update(EnvironmentOutcome eo, double critique) {
        HashableState sh = this.hashingFactory.hashState(eo.o);
        PolicyNode node = this.getNode(sh);

        double learningRate = this.learningRate.pollLearningRate(this.totalNumberOfSteps, sh.s(), eo.a);

        ActionPreference pref = this.getMatchingPreference(sh, eo.a, node);
        pref.preference += learningRate * critique * (1. - this.actionProb(sh.s(), eo.a));

        //now do off action selection updates
        List<Action> actions = ActionUtils.allApplicableActionsForTypes(((SADomain)domain).getActionTypes(), eo.o);
        for(Action a : actions){
            if(!a.equals(eo.a)){
                ActionPreference opref = this.getMatchingPreference(sh, a, node);
                opref.preference += learningRate * critique * -this.actionProb(eo.o, a);
            }
        }

        this.totalNumberOfSteps++;
    }
}


