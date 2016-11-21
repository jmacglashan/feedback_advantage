package experiments.turtlebot;

import experiments.turtlebot.datastructures.SoundRequest;
import ros.Publisher;
import ros.RosBridge;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;

/**
 * @author James MacGlashan.
 */
public class TurtlebotSoundInterface implements KeyListener {

	Publisher pub;
	SoundRequest rewardSound;
	SoundRequest punishSound;
	Set<Integer> rewardKeys = new HashSet<>();
	Set<Integer> punishKeys = new HashSet<>();

	public TurtlebotSoundInterface(RosBridge ros, String pathToRewardSound, String pathToPunishSound){
		this.pub = new Publisher("/robotsound", "sound_play/SoundRequest", ros, true);
		this.rewardSound = new SoundRequest(pathToRewardSound);
		this.punishSound = new SoundRequest(pathToPunishSound);
	}

	public TurtlebotSoundInterface addRewardKeys(int ... keyCodes){
		for(int k : keyCodes){
			rewardKeys.add(k);
		}
		return this;
	}

	public TurtlebotSoundInterface addPunishKeys(int ... keyCodes){
		for(int k : keyCodes){
			punishKeys.add(k);
		}
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
		int keycode = e.getKeyCode();
		if(rewardKeys.contains(keycode)){
			this.pub.publish(rewardSound);
		}
		else if(punishKeys.contains(keycode)){
			this.pub.publish(punishSound);
		}
	}

}
