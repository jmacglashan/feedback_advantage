package burlap.behavior.singleagent.learning.actorcritic.actor;

import burlap.behavior.functionapproximation.DifferentiablePolicy;
import burlap.behavior.functionapproximation.DifferentiableStateActionValue;
import burlap.behavior.functionapproximation.FunctionGradient;
import burlap.behavior.functionapproximation.ParametricFunction;
import burlap.behavior.functionapproximation.sparse.LinearVFA;
import burlap.behavior.policy.EnumerablePolicy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.policy.support.ActionProb;
import burlap.behavior.singleagent.learning.SparseGradientUtils;
import burlap.datastructures.BoltzmannDistribution;
import burlap.datastructures.HashedAggregator;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.action.ActionUtils;
import burlap.mdp.core.state.State;

import java.util.ArrayList;
import java.util.List;

public class DiffBoltzmann3 implements DifferentiablePolicy, EnumerablePolicy{

    protected DifferentiableStateActionValue vfa;
    List<ActionType> actionTypes;

    public DiffBoltzmann3(LinearVFA vfa, List<ActionType> actionTypes) {
        this.vfa = vfa;
        this.actionTypes = actionTypes;
    }

    @Override
    public FunctionGradient gradient(State s, Action a) {
        List<Action> actions = ActionUtils.allApplicableActionsForTypes(this.actionTypes, s);
        double [] prefs = new double[actions.size()];
        List<FunctionGradient> prefGrds = new ArrayList<>();
        int ti = 0;
        for(int i = 0; i < actions.size(); i++){
            Action queryAction = actions.get(i);
            if(a.equals(queryAction)){
                ti = i;
            }
            prefs[i] = this.vfa.evaluate(s, queryAction);
            prefGrds.add(this.vfa.gradient(s, queryAction));
        }
        BoltzmannDistribution bd = new BoltzmannDistribution(prefs);
        double [] probs = bd.getProbabilities();

        //now compute gradient sum
        HashedAggregator<Integer> sumGrads = new HashedAggregator<>();
        for(int i = 0; i < probs.length; i++){
            double scalar = -probs[i];
            if(i == ti){
                scalar += 1;
            }
            FunctionGradient prefGrad = prefGrds.get(i);
            SparseGradientUtils.scalarMultSumInto(prefGrad, scalar, sumGrads);
        }

        FunctionGradient policyGrad = SparseGradientUtils.toGradient(sumGrads);

        return policyGrad;
    }

    @Override
    public double evaluate(State s, Action a) {
        List<Action> actions = ActionUtils.allApplicableActionsForTypes(this.actionTypes, s);
        double [] prefs = new double[actions.size()];
        int ti = 0;
        for(int i = 0; i < actions.size(); i++){
            if(a.equals(actions.get(i))){
                ti = i;
            }
            prefs[i] = this.vfa.evaluate(s, actions.get(i));
        }
        BoltzmannDistribution bd = new BoltzmannDistribution(prefs);
        double [] probs = bd.getProbabilities();
        return probs[ti];
    }

    @Override
    public int numParameters() {
        return this.vfa.numParameters();
    }

    @Override
    public double getParameter(int i) {
        return this.vfa.getParameter(i);
    }

    @Override
    public void setParameter(int i, double p) {
        this.vfa.setParameter(i, p);
    }

    @Override
    public void resetParameters() {
        this.vfa.resetParameters();
    }

    @Override
    public ParametricFunction copy() {
        return this;
    }

    @Override
    public Action action(State s) {
        return PolicyUtils.sampleFromActionDistribution(this, s);
    }

    @Override
    public List<ActionProb> policyDistribution(State s) {
        List<Action> actions = ActionUtils.allApplicableActionsForTypes(this.actionTypes, s);
        double [] prefs = new double[actions.size()];
        for(int i = 0; i < actions.size(); i++){
            prefs[i] = this.vfa.evaluate(s, actions.get(i));
        }
        BoltzmannDistribution bd = new BoltzmannDistribution(prefs);
        double [] probs = bd.getProbabilities();
        List<ActionProb> aps = new ArrayList<>();
        for(int i = 0; i < actions.size(); i++){
            aps.add(new ActionProb(actions.get(i), probs[i]));
        }
        return aps;
    }

    @Override
    public double actionProb(State s, Action a) {
        return this.evaluate(s, a);
    }

    @Override
    public boolean definedFor(State s) {
        return true;
    }
}
