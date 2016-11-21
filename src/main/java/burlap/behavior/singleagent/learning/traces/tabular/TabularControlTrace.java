package burlap.behavior.singleagent.learning.traces.tabular;

import burlap.datastructures.HashedAggregator;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author James MacGlashan.
 */
public abstract class TabularControlTrace {


	protected HashedAggregator <SAKey> trace = new HashedAggregator<SAKey>();
	protected HashableStateFactory hashingFactory;
	protected double lambda;
	protected double minVal = 1e-6;

	public TabularControlTrace(HashableStateFactory hashingFactory, double lambda) {
		this.hashingFactory = hashingFactory;
		this.lambda = lambda;
	}

	public TabularControlTrace(HashableStateFactory hashingFactory, double lambda, double minVal) {
		this.hashingFactory = hashingFactory;
		this.lambda = lambda;
		this.minVal = minVal;
	}

	public void begin(){
		this.trace.clear();
	}

	public Iterator<ControlTraceVal> pollTraceWithUpdate(State s, Action a){
		final SAKey lastStep = new SAKey(this.hashState(s), a);

		if(!this.trace.containsKey(lastStep)){
			this.trace.set(lastStep, 0.);
		}

		final Iterator<Map.Entry<SAKey, Double>> hashIter = this.trace.getHashMap().entrySet().iterator();
		final LinkedList <SAKey> toRemove = new LinkedList<SAKey>();
		Iterator<ControlTraceVal> iter = new Iterator<ControlTraceVal>() {
			public boolean hasNext() {
				return hashIter.hasNext();
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}

			public ControlTraceVal next() {
				Map.Entry<SAKey, Double> me = hashIter.next();
				TracePoll tp = traceVal(me.getKey(), me.getValue(), lastStep);
				ControlTraceVal traceVal = new ControlTraceVal(me.getKey().s, me.getKey().a, tp.traceVal);
				if(tp.nextTraceVal < minVal){
					toRemove.add(me.getKey());
				}
				else{
					trace.set(me.getKey(), tp.nextTraceVal);
				}

				//handle final cleanup
				if(!hashIter.hasNext()){
					for(SAKey key : toRemove){
						trace.remove(key);
					}
				}

				return traceVal;
			}
		};

		return iter;
	}

	protected abstract TracePoll traceVal(SAKey key, double curVal, SAKey lastStep);


	protected HashableState hashState(State s){
		return this.hashingFactory.hashState(s);
	}


}
