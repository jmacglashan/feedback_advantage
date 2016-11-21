package burlap.behavior.functionapproximation;

import burlap.behavior.functionapproximation.sparse.SparseStateFeatures;
import burlap.behavior.functionapproximation.sparse.StateFeature;
import burlap.behavior.singleagent.auxiliary.StateEnumerator;

import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.statehashing.HashableStateFactory;

import java.util.Arrays;
import java.util.List;

/**
 * @author James MacGlashan
 */
public class TabularFeatures implements SparseStateFeatures {

    protected StateEnumerator enumerator;
    protected HashableStateFactory hashingFactory;

    public TabularFeatures(SADomain domain, HashableStateFactory hashingFactory) {
        this.hashingFactory = hashingFactory;
        this.enumerator = new StateEnumerator(domain, hashingFactory);
    }

    @Override
    public List<StateFeature> features(State s) {
        return Arrays.asList(new StateFeature(enumerator.getEnumeratedID(s), 1.0));
    }

    @Override
    public SparseStateFeatures copy() {
        return this;
    }

    @Override
    public int numFeatures() {
        return enumerator.numStatesEnumerated();
    }
}
