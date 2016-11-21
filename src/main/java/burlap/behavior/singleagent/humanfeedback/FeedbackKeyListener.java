package burlap.behavior.singleagent.humanfeedback;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.*;

/**
 * @author James MacGlashan.
 */
public class FeedbackKeyListener implements KeyListener {

	protected Map<Integer, Double> feedbackMap = new HashMap<>();
	protected List<FeedbackReceiver> receivers = new ArrayList<>();

	public FeedbackKeyListener(FeedbackReceiver...receivers) {
		this.receivers.addAll(Arrays.asList(receivers));
	}

	public FeedbackKeyListener assignFeedback(int keycode, double feedbackVal){
		this.feedbackMap.put(keycode, feedbackVal);
		return this;
	}

	public FeedbackKeyListener clearFeedbackAssignment(int keycode){
		this.feedbackMap.remove(keycode);
		return this;
	}

	public FeedbackKeyListener clearAllFeedbackAssignments(){
		this.feedbackMap.clear();
		return this;
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
		Double f = feedbackMap.get(key);
		if(f != null){
		    for(FeedbackReceiver r : receivers){
                r.receiveHumanFeedback(f);
            }

		}
	}
}
