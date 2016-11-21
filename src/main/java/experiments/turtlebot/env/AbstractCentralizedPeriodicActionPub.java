package experiments.turtlebot.env;

import burlap.mdp.core.action.Action;
import burlap.ros.actionpub.ActionPublisher;
import ros.tools.PeriodicPublisher;

/**
 * @author James MacGlashan.
 */
public abstract class AbstractCentralizedPeriodicActionPub implements ActionPublisher{

	/**
	 * The {@link ros.tools.PeriodicPublisher} whose message will be altered by this {@link burlap.ros.actionpub.ActionPublisher}.
	 */
	protected PeriodicPublisher ppub;

	/**
	 * The time delay returned by the {@link #publishAction(Action)} method.
	 */
	protected int timeDelay;

	/**
	 * Initializes
	 * @param ppub the {@link ros.tools.PeriodicPublisher} whose message will be altered by this {@link burlap.ros.actionpub.ActionPublisher}.
	 * @param timeDelay the time delay returned by the {@link #publishAction(Action)} method.
	 */
	public AbstractCentralizedPeriodicActionPub(PeriodicPublisher ppub, int timeDelay) {
		this.ppub = ppub;
		this.timeDelay = timeDelay;
	}


	/**
	 * Returns the {@link ros.tools.PeriodicPublisher} that has its message altered by this object.
	 * @return the {@link ros.tools.PeriodicPublisher} that has its message altered by this object.
	 */
	public PeriodicPublisher getPpub() {
		return ppub;
	}

	/**
	 * Sets the {@link ros.tools.PeriodicPublisher} that has its message altered by this object.
	 * @param ppub the {@link ros.tools.PeriodicPublisher} that has its message altered by this object.
	 */
	public void setPpub(PeriodicPublisher ppub) {
		this.ppub = ppub;
	}


	/**
	 * Returns the time delay in milliseconds returned by the {@link #publishAction(Action)} method
	 * @return the time delay in milliseconds returned by the {@link #publishAction(Action)} method
	 */
	public int getTimeDelay() {
		return timeDelay;
	}

	/**
	 * Sets the time delay in milliseconds returned by the {@link #publishAction(Action)} method
	 * @param timeDelay the time delay in milliseconds returned by the {@link #publishAction(Action)} method
	 */
	public void setTimeDelay(int timeDelay) {
		this.timeDelay = timeDelay;
	}

	@Override
	public int publishAction(Action a) {
		this.ppub.setMsg(this.msg(a));
		return this.timeDelay;
	}

	abstract public Object msg(Action a);
}
