package experiments.advantagecritic;

import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.performance.LearningAlgorithmExperimenter;
import burlap.behavior.singleagent.auxiliary.performance.PerformanceMetric;
import burlap.behavior.singleagent.auxiliary.performance.TrialMode;
import burlap.behavior.singleagent.learning.actorcritic.actor.BoltzmannActor2;
import burlap.behavior.singleagent.learning.actorcritic.actor.BoltzmannActor3;
import burlap.behavior.singleagent.learning.actorcritic.critic.*;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.LearningAgentFactory;
import burlap.behavior.singleagent.learning.actorcritic.ActorCritic;
import burlap.behavior.singleagent.learning.actorcritic.actor.BoltzmannActor;
import burlap.behavior.singleagent.learning.actorcritic.actor.BoltzmannTraceActor;
import burlap.behavior.singleagent.learning.actorcritic.critics.TDLambda;
import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.domain.singleagent.gridworld.GridWorldTerminalFunction;
import burlap.domain.singleagent.gridworld.state.GridWorldState;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.common.GoalBasedRF;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import cern.colt.Arrays;

/**
 * @author James MacGlashan.
 */
public class TestAdvantage {

	public static void runEpisodeCompare(){

		GridWorldDomain gwd = new GridWorldDomain(11, 11);
		gwd.setMapToFourRooms();
		gwd.setTf(new GridWorldTerminalFunction(10, 10));
		gwd.setRf(new GoalBasedRF(gwd.getTf(), 1., 0.));

		final SADomain domain = gwd.generateDomain();
		final double gamma = 0.99;
		final HashableStateFactory hashingFactory = new SimpleHashableStateFactory();
		final SimulatedEnvironment env = new SimulatedEnvironment(domain, new GridWorldState(0, 0));

		LearningAlgorithmExperimenter exp = new LearningAlgorithmExperimenter(env, 2, 100,
				//new TDTracesOnlyFactory(domain, hashingFactory, gamma, 0.3, 0.1, 0.6),
				//new TDTracesOnlyFactory(domain, hashingFactory, gamma, 0.3, 0.1, 0.0),
				//new ActorTDTracesFactory(domain, hashingFactory, gamma, 0.3, 0.1, 0.6),
				//new ActorTDTracesFactory(domain, hashingFactory, gamma, 1.0, 0.1, 0.6),
				//new ActorTDTracesFactory(domain, hashingFactory, gamma, 1.0, 0.1, 1.0),
				//new AdvantageTracesFactory(domain, hashingFactory, gamma, 1.0, 1.0),
                //new QCriticFactory(domain, hashingFactory, gamma, 0.05),
                //new PolicyTargetCriticFactory(domain, hashingFactory, gamma, 1.0),
                //new PolicyTargetCriticFactory(domain, hashingFactory, gamma, 1.0, -1.0));
                //new QStarCriticFactory(domain, hashingFactory, gamma, 0.01));
				new AdvantageFactory(domain, hashingFactory, gamma, 1.0));


		exp.setUpPlottingConfiguration(400, 300, 2, 800, TrialMode.MOST_RECENT_AND_AVERAGE, PerformanceMetric.STEPS_PER_EPISODE);

		exp.startExperiment();

	}


	public static void printStats(double [][] stats){

		double sums[] = new double[stats.length];
		for(int i = 0; i < stats.length; i++){
			for(int j = 0; j < stats[i].length; j++){
				if(j > 0){
					System.out.print(", ");
				}
				System.out.print(stats[i][j]);
				sums[i] += stats[i][j];
			}
			System.out.println();
			sums[i] /= stats[i].length;
		}

		System.out.println();
		System.out.println(Arrays.toString(sums));
	}


	public static double [][] vsNumStepsToReach(Environment env, int nTrials, int targetSteps, int nConsecutive, LearningAgentFactory...agents){

		double [][] results = new double[agents.length][nTrials];

		for(int i = 0; i < agents.length; i++){
			for(int j = 0; j < nTrials; j++){
				System.out.println("Starting " + agents[i].getAgentName() + " trial " + j);
				results[i][j] = numStepsToReach(agents[i].generateAgent(), env, targetSteps, nConsecutive);
			}

		}

		return results;

	}


	public static int numStepsToReach(LearningAgent agent, Environment env, int targetSteps, int nConsecutive){

		int totalSteps = 0;
		int consecutive = 0;
		int consecutiveSteps = 0;

		while(consecutive < nConsecutive){

			Episode e = agent.runLearningEpisode(env);
			int steps = e.numActions();
			totalSteps += steps;
			if(steps <= targetSteps){
				consecutiveSteps += steps;
				consecutive++;
			}
			else{
				consecutive = 0;
				consecutiveSteps = 0;
			}

			env.resetEnvironment();

		}

		int timeToHitMark = totalSteps - consecutiveSteps;
		return timeToHitMark;

	}



	public static class TDTracesOnlyFactory implements LearningAgentFactory{

		protected SADomain domain;
		protected HashableStateFactory hashingFactory;
		protected double gamma;
		protected double actorLR;
		protected double TDLR;
		protected double lambda;

		public TDTracesOnlyFactory(SADomain domain, HashableStateFactory hashingFactory, double gamma, double actorLR, double TDLR, double lambda) {
			this.domain = domain;
			this.hashingFactory = hashingFactory;
			this.gamma = gamma;
			this.actorLR = actorLR;
			this.TDLR = TDLR;
			this.lambda = lambda;
		}

		public String getAgentName() {
			return "TD Traces " + this.actorLR + " " + this.TDLR + " " + this.lambda;
		}

		public LearningAgent generateAgent() {
			ActorCritic ac = new ActorCritic(
					new BoltzmannActor(domain, hashingFactory, this.actorLR),
					new TDLambda(gamma, hashingFactory, this.TDLR, 0., this.lambda));

			return ac;
		}
	}

	public static class ActorTDTracesFactory implements LearningAgentFactory{

		protected SADomain domain;
		protected HashableStateFactory hashingFactory;
		protected double gamma;
		protected double actorLR;
		protected double TDLR;
		protected double lambda;

		public ActorTDTracesFactory(SADomain domain, HashableStateFactory hashingFactory, double gamma, double actorLR, double TDLR, double lambda) {
			this.domain = domain;
			this.hashingFactory = hashingFactory;
			this.gamma = gamma;
			this.actorLR = actorLR;
			this.TDLR = TDLR;
			this.lambda = lambda;
		}

		public String getAgentName() {
			return "Actor-TD Traces " + this.actorLR + " " + this.TDLR + " " + this.lambda;
		}

		public LearningAgent generateAgent() {
			ActorCritic ac = new ActorCritic(
					new BoltzmannTraceActor(domain, hashingFactory, actorLR, this.lambda*this.gamma),
					new TDLambda(gamma, hashingFactory, this.TDLR, 0., this.lambda));

			return ac;
		}
	}

	public static class AdvantageFactory implements LearningAgentFactory{

		protected SADomain domain;
		protected HashableStateFactory hashingFactory;
		protected double gamma;
		protected double actorLR;

		public AdvantageFactory(SADomain domain, HashableStateFactory hashingFactory, double gamma, double actorLR) {
			this.domain = domain;
			this.hashingFactory = hashingFactory;
			this.gamma = gamma;
			this.actorLR = actorLR;
		}

		public String getAgentName() {
			return "Advantage " + this.actorLR;
		}

		public LearningAgent generateAgent() {
			BoltzmannActor3 actor = new BoltzmannActor3(domain, hashingFactory, actorLR);
			ActorCritic ac = new ActorCritic(
					actor,
					new AdvantageCritic(actor, domain, hashingFactory, gamma, 0.01, 30));
			return ac;
		}
	}

    public static class QCriticFactory implements LearningAgentFactory{

        protected SADomain domain;
        protected HashableStateFactory hashingFactory;
        protected double gamma;
        protected double actorLR;

        public QCriticFactory(SADomain domain, HashableStateFactory hashingFactory, double gamma, double actorLR) {
            this.domain = domain;
            this.hashingFactory = hashingFactory;
            this.gamma = gamma;
            this.actorLR = actorLR;
        }

        public String getAgentName() {
            return "Q^pi " + this.actorLR;
        }

        public LearningAgent generateAgent() {
            BoltzmannActor3 actor = new BoltzmannActor3(domain, hashingFactory, actorLR);
            ActorCritic ac = new ActorCritic(
                    actor,
                    new QCritic(actor, domain, hashingFactory, gamma, 0.01, 30));
            return ac;
        }
    }

    public static class QStarCriticFactory implements LearningAgentFactory{

        protected SADomain domain;
        protected HashableStateFactory hashingFactory;
        protected double gamma;
        protected double actorLR;

        public QStarCriticFactory(SADomain domain, HashableStateFactory hashingFactory, double gamma, double actorLR) {
            this.domain = domain;
            this.hashingFactory = hashingFactory;
            this.gamma = gamma;
            this.actorLR = actorLR;
        }

        public String getAgentName() {
            return "Q^pi " + this.actorLR;
        }

        public LearningAgent generateAgent() {
            BoltzmannActor3 actor = new BoltzmannActor3(domain, hashingFactory, actorLR);
            ActorCritic ac = new ActorCritic(
                    actor,
                    new QStarCritic(actor, domain, hashingFactory, gamma, 0.01, 30));
            return ac;
        }
    }

    public static class PolicyTargetCriticFactory implements LearningAgentFactory{

        protected SADomain domain;
        protected HashableStateFactory hashingFactory;
        protected double gamma;
        protected double actorLR;
        protected double wrongVal = 0.;

        public PolicyTargetCriticFactory(SADomain domain, HashableStateFactory hashingFactory, double gamma, double actorLR) {
            this.domain = domain;
            this.hashingFactory = hashingFactory;
            this.gamma = gamma;
            this.actorLR = actorLR;
        }

        public PolicyTargetCriticFactory(SADomain domain, HashableStateFactory hashingFactory, double gamma, double actorLR, double wrongVal) {
            this.domain = domain;
            this.hashingFactory = hashingFactory;
            this.gamma = gamma;
            this.actorLR = actorLR;
            this.wrongVal = wrongVal;
        }

        public String getAgentName() {
            return "policy " + this.actorLR + " " + this.wrongVal;
        }

        public LearningAgent generateAgent() {
            BoltzmannActor3 actor = new BoltzmannActor3(domain, hashingFactory, actorLR);
            ActorCritic ac = new ActorCritic(
                    actor,
                    new PolicyTargetCritic(actor, domain, hashingFactory, gamma, 0.01, 30, wrongVal));
            return ac;
        }
    }


	public static class AdvantageTracesFactory implements LearningAgentFactory{

		protected SADomain domain;
		protected HashableStateFactory hashingFactory;
		protected double gamma;
		protected double actorLR;
		protected double lambda;

		public AdvantageTracesFactory(SADomain domain, HashableStateFactory hashingFactory, double gamma, double actorLR, double lambda) {
			this.domain = domain;
			this.hashingFactory = hashingFactory;
			this.gamma = gamma;
			this.actorLR = actorLR;
			this.lambda = lambda;
		}

		public String getAgentName() {
			return "Advantage Traces " + this.actorLR;
		}

		public LearningAgent generateAgent() {
			BoltzmannActor actor = new BoltzmannTraceActor(domain, hashingFactory, actorLR, this.lambda*this.gamma);
			ActorCritic ac = new ActorCritic(
					actor,
					new AdvantageCritic(actor, domain, hashingFactory, gamma, 0.01, 30));
			return ac;
		}
	}

	public static class AdvantageDropoutFactory implements LearningAgentFactory{

		protected SADomain domain;
		protected HashableStateFactory hashingFactory;
		protected double gamma;
		protected double actorLR;
		protected double lambda;
		protected double dropout;


		public AdvantageDropoutFactory(SADomain domain, HashableStateFactory hashingFactory, double gamma, double actorLR, double lambda, double dropout) {
			this.domain = domain;
			this.hashingFactory = hashingFactory;
			this.gamma = gamma;
			this.actorLR = actorLR;
			this.lambda = lambda;
			this.dropout = dropout;
		}

		public String getAgentName() {
			return "Advantages " + lambda + " " + dropout;
		}

		public LearningAgent generateAgent() {
			BoltzmannActor actor = new BoltzmannTraceActor(domain, hashingFactory, actorLR, this.lambda);
			ActorCritic ac = new ActorCritic(
					actor,
					new SignedCritic(new DropoutCritic(new AdvantageCritic(actor, domain, hashingFactory, gamma, 0.01, 30), dropout)));
			return ac;
		}
	}




	public static void main(String[] args) {

		GridWorldDomain gwd = new GridWorldDomain(11, 11);
		gwd.setMapToFourRooms();
		gwd.setTf(new GridWorldTerminalFunction(10, 10));
		gwd.setRf(new GoalBasedRF(gwd.getTf(), 1., 0.));

		final SADomain domain = gwd.generateDomain();
		final double gamma = 0.99;
		final HashableStateFactory hashingFactory = new SimpleHashableStateFactory();
		final SimulatedEnvironment env = new SimulatedEnvironment(domain, new GridWorldState(0, 0));

        runEpisodeCompare();

//		double dropOut = 0.9;
//		while(dropOut < 0.91){
//			double [][] stats = vsNumStepsToReach(env, 20, 50, 5,
//					//new AdvantageDropoutFactory(domain, hashingFactory, gamma, 1., 0., dropOut),
//					//new AdvantageDropoutFactory(domain, hashingFactory, gamma, 1., 0.0, 0.));
//					//new AdvantageFactory(domain, hashingFactory, gamma, 0.1));
//
//
//			System.out.println("Dropout: " + dropOut);
//			printStats(stats);
//
//			System.out.println("---------------------------------------------------");
//
//			dropOut += 0.1;
//		}




	}

}
