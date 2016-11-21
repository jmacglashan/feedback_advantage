package experiments.caction;

import burlap.behavior.functionapproximation.dense.DenseStateFeatures;
import burlap.behavior.singleagent.learning.actorcritic.actor.LinearGaussianActor;
import burlap.behavior.singleagent.learning.traces.approximation.CumulativeParametricControlTrace;
import burlap.mdp.core.action.continuous.ContinuousActionType;
import burlap.mdp.core.action.continuous.SimpleContinuousActionType;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.NullState;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.shell.BurlapShell;
import burlap.shell.command.ShellCommand;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.PrintStream;
import java.util.Scanner;

/**
 * @author James MacGlashan.
 */
public class InteractiveCActionTrain {

	public static void main(String[] args) {
		SADomain domain = new SADomain();
//		SimpleContinuousActionType type = new SimpleContinuousActionType("a", 1,
//				new ContinuousActionType.ParameterDomain(new double[]{-1}, new double[]{1}));


		SimpleContinuousActionType type = new SimpleContinuousActionType("a", 2,
				new ContinuousActionType.ParameterDomain(new double[]{-.2, -.9}, new double[]{.2, .9}));

		domain.addActionType(type);

		DenseStateFeatures cf = new DenseStateFeatures() {
			public double[] features(State s) {
				return new double[]{1.};
			}

			public DenseStateFeatures copy() {
				return this;
			}
		};

		LinearGaussianActor actor = new LinearGaussianActor(cf, cf, 1, 1, type, 0.1, new CumulativeParametricControlTrace(0.));
		actor.setMaxGradientMag(2.);

		BurlapShell shell = new BurlapShell(domain, System.in, System.out);
		shell.addCommand(new CTrainCommand(actor, null));

		shell.start();

	}


	static class CTrainCommand implements ShellCommand{


		protected OptionParser parser = new OptionParser("sc:ph*");

		protected LinearGaussianActor actor;
		protected Action lastAction;


		public CTrainCommand(LinearGaussianActor actor, Action lastAction) {
			this.actor = actor;
			this.lastAction = lastAction;
		}

		public String commandName() {
			return "tr";
		}

		public int call(BurlapShell shell, String argString, Scanner is, PrintStream os) {

			String [] comps = argString.split(" ");
			OptionSet oset = this.parser.parse(argString.split(" "));

			if(oset.has("s")){
				lastAction = actor.action(NullState.instance);
				os.println(lastAction.toString());
			}
			else if(oset.has("c")){
				String val = (String)oset.valueOf("c");
				double critique = Double.parseDouble(val);
				actor.update(new EnvironmentOutcome(NullState.instance, lastAction, NullState.instance, 0., false), critique);
			}
			else if(oset.has("p")){
				os.println(actor.toString());
			}






			return 0;
		}
	}


}
