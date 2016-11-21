package experiments.turtlebot;

import burlap.behavior.functionapproximation.dense.DenseStateFeatures;
import burlap.behavior.learningrate.ConstantLR;
import burlap.behavior.singleagent.humanfeedback.environment.HumanFeedbackEnvironment;
import burlap.behavior.singleagent.learning.actorcritic.ActorCritic;
import burlap.behavior.singleagent.learning.actorcritic.actor.FilteredGaussianActor;
import burlap.behavior.singleagent.learning.actorcritic.actor.LinearGaussianActor;
import burlap.behavior.singleagent.learning.humanfeedback.coach.CoachActor;
import burlap.behavior.singleagent.learning.humanfeedback.coach.CoachCritic;
import burlap.behavior.singleagent.learning.humanfeedback.coach.traceselector.ConstantSelector;
import burlap.behavior.singleagent.learning.traces.approximation.CumulativeParametricControlTrace;
import burlap.behavior.singleagent.learning.traces.approximation.ParametricControlTrace;
import burlap.mdp.core.action.continuous.ContinuousActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.shell.visual.VisualExplorer;
import burlap.visualizer.Visualizer;
import experiments.turtlebot.env.TurtleChannelsEnv;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

/**
 * @author James MacGlashan.
 */
public class TurtlebotContinuousTrain {

	public static void main(String[] args) {

		String rosURI = "ws://138.16.161.195:9090";
		String stateTopic = "/turtle_env/camera_features/channel_features";
		String actionTopic = "/mobile_base/commands/velocity";

		TurtleChannelsEnv rootEnv = TurtleChannelsEnv.continuousActionConstructor(rosURI, stateTopic, actionTopic);
		HumanFeedbackEnvironment henv = new HumanFeedbackEnvironment(rootEnv);

		ContinuousActionType cat = (ContinuousActionType)rootEnv.getDomain().getAction("a");


		//TileCodedVisionFeatures features = new TileCodedVisionFeatures(3);
		//SparseToDenseFeatures dfeatures = new SparseToDenseFeatures(features);
		DenseStateFeatures dfeatures = new DenseStateFeatures() {
			@Override
			public double[] features(State s) {
				return new double[]{1.};
			}

			@Override
			public DenseStateFeatures copy() {
				return this;
			}
		};
		//int nF = features.numFeaturesFor(8, 3, 2);
		int nF = 1;
		LinearGaussianActor policy = new FilteredGaussianActor(dfeatures, dfeatures, nF, nF, cat, 0.0, null);
		policy.setSigmaParam(0, 0.1); //max 6 active features?
		policy.setSigmaParam(1, 0.5); //max 6 active features?
		policy.setMuParams(0.01);

		CoachActor ha = new CoachActor(
				policy,
				Arrays.<ParametricControlTrace>asList(new CumulativeParametricControlTrace(0.)),
				new ConstantSelector(0),
				1,
				new ConstantLR(0.1));
		ha.setMaxGradientMag(0.5);
		CoachCritic critic = new CoachCritic();
		ActorCritic ac = new ActorCritic(ha, critic);



		Visualizer v = CameraFeaturesVisualizer.getChannelsVisualizer(8);
		TurtlebotInterfaceConfigure.configureForSlidePresenter(henv, v, ac);




		VisualExplorer exp = new VisualExplorer(rootEnv.getDomain(), rootEnv, v);
		exp.initGUI();
		exp.startLiveStatePolling(33);

		createPolicyParamsViewer(policy, henv);



	}


	public static void createPolicyParamsViewer(final LinearGaussianActor policy, final Environment env){

		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		dataset.addValue(0., "PolicyDist", "v_mu");
		dataset.addValue(0., "PolicyDist", "v_sig");
		dataset.addValue(0., "PolicyDist", "r_mu");
		dataset.addValue(0., "PolicyDist", "r_sig");

		final JFreeChart chart = ChartFactory.createBarChart("Policy Dist Params", "Predicted Policy Param", "Value", dataset);

		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new Dimension(700, 700));

		JFrame frame = new JFrame();
		//frame.setContentPane(chartPanel);
		frame.setLayout(new BorderLayout());
		frame.add(chartPanel, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);


		Thread valueUpdateThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while(true) {

					double [][] distParams = policy.distributionParametersFor(env.currentObservation());
					dataset.setValue(distParams[0][0], "PolicyDist", "v_mu");
					dataset.setValue(distParams[0][1], "PolicyDist", "v_sig");
					dataset.setValue(distParams[1][0], "PolicyDist", "r_mu");
					dataset.setValue(distParams[1][1], "PolicyDist", "r_sig");

					chart.fireChartChanged();
					try {
						Thread.sleep(300);
					} catch(InterruptedException e) {
						e.printStackTrace();
					}


				}
			}
		});
		valueUpdateThread.start();


	}

}
