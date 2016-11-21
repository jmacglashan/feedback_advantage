package experiments.turtlebot;

import burlap.mdp.core.state.State;
import burlap.visualizer.StatePainter;
import burlap.visualizer.StateRenderLayer;
import burlap.visualizer.Visualizer;
import experiments.turtlebot.datastructures.CameraScaleFeatures;
import experiments.turtlebot.states.TurtleChannelsState;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;

/**
 * @author James MacGlashan.
 */
public class CameraFeaturesVisualizer {


	public static Visualizer getChannelsVisualizer(int factor){
		final Visualizer v = new Visualizer(getChannelsStateRenderLayer(factor));

		v.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {

			}

			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyChar()=='1'){
					((CameraFeaturesVisualizer.CameraChannelsPainter)v.getStateRenderLayer().getStatePainters().get(0)).setChannelToPaint(0);
				}
				else if(e.getKeyChar() == '2'){
					((CameraFeaturesVisualizer.CameraChannelsPainter)v.getStateRenderLayer().getStatePainters().get(0)).setChannelToPaint(1);
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {

			}
		});

		return v;

	}

	public static StateRenderLayer getChannelsStateRenderLayer(int factor){
		StateRenderLayer srl = new StateRenderLayer();
		srl.addStatePainter(new CameraChannelsPainter(factor));
		return srl;
	}

	public static class CameraChannelsPainter implements StatePainter{

		protected int factor;
		protected int channelToPaint = 0;

		public CameraChannelsPainter(int factor) {
			this.factor = factor;
		}

		public int getChannelToPaint() {
			return channelToPaint;
		}

		public void setChannelToPaint(int channelToPaint) {
			this.channelToPaint = channelToPaint;
		}

		@Override
		public void paint(Graphics2D g2, State s, float cWidth, float cHeight) {

			TurtleChannelsState tcs = (TurtleChannelsState)s;

			CameraScaleFeatures cf = tcs.channels.getChannel(this.channelToPaint);

			float rWidth = cWidth / factor;
			float rHeight = cHeight / factor;


			for(int r = 0; r < factor; r++){
				for(int c = 0; c < factor; c++){

					float ry = rHeight*r;
					float rx = rWidth*c;

					double nVal = CameraScaleFeatures.CameraScaleFeaturesHelper.getFeatures(cf, 0, r, c, factor);
					double mVal = CameraScaleFeatures.CameraScaleFeaturesHelper.getFeatures(cf, 1, r, c, factor);
					double fVal = CameraScaleFeatures.CameraScaleFeaturesHelper.getFeatures(cf, 2, r, c, factor);


					//do near
					Color col = new Color((int)nVal, (int)nVal, (int)nVal);
					g2.setColor(col);
					Rectangle2D.Float rect = new Rectangle2D.Float(rx, ry, rWidth, rHeight);
					g2.fill(rect);

					//do mid
					col = new Color((int)mVal, (int)mVal, (int)mVal);
					g2.setColor(col);
					rect = new Rectangle2D.Float(rx + (rWidth/4), ry + (rHeight/4), rWidth/2, rHeight/2);
					g2.fill(rect);

					//do far
					col = new Color((int)fVal, (int)fVal, (int)fVal);
					g2.setColor(col);
					rect = new Rectangle2D.Float(rx + (3*rWidth/8), ry + (3*rHeight/8), rWidth/4, rHeight/4);
					g2.fill(rect);

					if(fVal > 0. || mVal > 0. || nVal > 0.){
						g2.setColor(Color.red);
						g2.setStroke(new BasicStroke(2f));
						g2.draw(new Rectangle2D.Float(rx, ry, rWidth, rHeight));
					}

				}
			}


		}
	}

}
