package burlap.behavior.singleagent.learning.humanfeedback;

import burlap.behavior.singleagent.humanfeedback.environment.HumanFeedbackEnvironment;
import burlap.behavior.singleagent.learning.LearningAgent;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * @author James MacGlashan.
 */
public class LearnerKeyControl implements KeyListener {

	protected HumanFeedbackEnvironment env;
	protected LearningAgent agent;
	protected Thread learningThread = null;

	protected int terminateKey;
	protected int resetKey;
	protected int beginLearningKey;

	protected boolean autoReset = true;

	protected volatile boolean needsFinish = false;


	public LearnerKeyControl(HumanFeedbackEnvironment env, LearningAgent agent, int terminateKey, int beginLearningKey) {
		this.env = env;
		this.agent = agent;
		this.terminateKey = terminateKey;
		this.beginLearningKey = beginLearningKey;
	}

	public LearnerKeyControl(HumanFeedbackEnvironment env, LearningAgent agent, int terminateKey, int resetKey, int beginLearningKey) {
		this.env = env;
		this.agent = agent;
		this.terminateKey = terminateKey;
		this.resetKey = resetKey;
		this.beginLearningKey = beginLearningKey;
	}

	public boolean isAutoReset() {
		return autoReset;
	}

	public void setAutoReset(boolean autoReset) {
		this.autoReset = autoReset;
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {

	}

	@Override
	public void keyReleased(KeyEvent e) {
		int key = e.getKeyCode();

		if(key == terminateKey && needsFinish){
			this.env.setTerminalSignal(true);
			System.out.println("Ending learning");
		}
		else if(key == resetKey && !needsFinish){
			this.env.resetEnvironment();
			System.out.println("Called Reset");
		}
		else if(key == beginLearningKey){
			if(learningThread != null){
				if(env.isInTerminalState() && needsFinish){ //learning running, but will die shortly, so we can wait and restart
					try {
						learningThread.join();
					} catch(InterruptedException e1) {
						e1.printStackTrace();
					}
					if(this.autoReset){
						this.env.resetEnvironment();
					}
					this.startLearningThread();
				}
				else if(!needsFinish){
					if(this.autoReset){
						this.env.resetEnvironment();
					}
					this.startLearningThread();
				}
			}
			else{
				this.startLearningThread();
			}
		}

	}

	protected void startLearningThread(){
		this.learningThread = new Thread(new Runnable() {
			@Override
			public void run() {
				needsFinish = true;
				System.out.println("Starting learning");
				try {
					agent.runLearningEpisode(env);
				} catch(Exception e){
					e.printStackTrace();
				}
				needsFinish = false;
				System.out.println("Learning Stopped");
			}
		});
		this.learningThread.start();
	}
}
