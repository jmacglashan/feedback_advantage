package experiments.turtlebot.vfa;

import burlap.behavior.functionapproximation.DifferentiableStateActionValue;
import burlap.behavior.functionapproximation.FunctionGradient;
import burlap.behavior.functionapproximation.ParametricFunction;
import burlap.debugtools.RandomFactory;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import experiments.turtlebot.datastructures.CameraChannels;
import experiments.turtlebot.datastructures.CameraScaleFeatures;
import experiments.turtlebot.states.TurtleChannelsState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author James MacGlashan.
 */
public class TileCodedVision implements DifferentiableStateActionValue {

	/**
	 * A feature index offset for each action when using Q-value function approximation.
	 */
	protected Map<Action, Integer> actionOffset = new HashMap<Action, Integer>();

	protected Map<Integer, Double> parameters = new HashMap<Integer, Double>(100);

	protected int parametersPerAction = -1;

	protected int receptiveWidth = 3;


	protected double lastOutput;
	protected State lastState = null;
	protected List<Integer> activeFeatures;
	protected double initialBiasTermRange = 0.03;

	public TileCodedVision(int receptiveWidth) {
		this.receptiveWidth = receptiveWidth;
	}


	@Override
	public double evaluate(State s, Action a) {

		List<Integer> stateFeatures;
		if(s == this.lastState){
			stateFeatures = this.activeFeatures;
		}
		else{
			stateFeatures = this.getTilesForChannels(((TurtleChannelsState)s).channels, this.receptiveWidth);
		}
		int actionOffset = this.getActionOffset(a);


		if(this.parametersPerAction == -1){
			int pixelDim = 8;
			int nCols = (int)Math.ceil((double)pixelDim / (double)receptiveWidth) + 1;
			int colsBySets = nCols * receptiveWidth;
			this.parametersPerAction = colsBySets * ((TurtleChannelsState)s).channels.channels[0].nScales() * ((TurtleChannelsState)s).channels.nChannels() + 1; //+1 because of bias term
		}


		//StringBuilder buf = new StringBuilder();
		List<Integer> actionFeatures = this.getActionFeatures(stateFeatures, actionOffset);
		double sum = 0.;
		for(int af : actionFeatures){
			double v = 1. * this.getParameter(af);
			sum += v;
			//buf.append(af).append(" ");
		}
		//System.out.println("features: " + buf.toString());

		//now add bias term
		int biasParamInd = (actionOffset * this.parametersPerAction) + parametersPerAction - 1;
		double biasParm = this.getParameter(biasParamInd);
		double bias = Math.tanh(biasParm);
		sum += bias;

		this.lastOutput = sum;
		this.lastState = s;
		this.activeFeatures = stateFeatures;

		return sum;
	}

	@Override
	public FunctionGradient gradient(State s, Action a) {

		List<Integer> stateFeatures;
		if(this.lastState == s){
			stateFeatures = this.activeFeatures;
		}
		else{
			stateFeatures = this.getTilesForChannels(((TurtleChannelsState)s).channels, this.receptiveWidth);
		}
		int actionOffset = this.getActionOffset(a);


		List<Integer> actionFeatures = this.getActionFeatures(stateFeatures, actionOffset);
		FunctionGradient gradient = new FunctionGradient.SparseGradient(stateFeatures.size()+1);
		for(Integer af : actionFeatures){
			gradient.put(af, 1);
		}

		int biasParamInd = (actionOffset * this.parametersPerAction) + parametersPerAction - 1;
		double biasParm = this.getParameter(biasParamInd);
		double tanh = Math.tanh(biasParm);
		double bpd = 1. - (tanh*tanh);
		gradient.put(biasParamInd, bpd);
		this.lastState = s;
		this.activeFeatures = stateFeatures;

		return gradient;
	}


	@Override
	public int numParameters() {
		if(this.parametersPerAction == -1){
			return 0;
		}
		return this.parametersPerAction * this.actionOffset.size();
	}

	@Override
	public double getParameter(int i) {
		Double val = this.parameters.get(i);
		if(val == null){
			val = 0.;
			if(this.isBiasTerm(i)){
				val = (RandomFactory.getMapped(0).nextDouble() * this.initialBiasTermRange * 2.) - this.initialBiasTermRange;
			}
			this.parameters.put(i, val);
		}
		return val;
	}

	@Override
	public void setParameter(int i, double p) {
		this.parameters.put(i, p);
	}

	@Override
	public void resetParameters() {
		this.parameters.clear();
	}

	@Override
	public TileCodedVision copy() {

		TileCodedVision alt = new TileCodedVision(this.receptiveWidth);
		alt.parametersPerAction = this.parametersPerAction;
		alt.parameters = new HashMap<Integer, Double>(this.parameters);
		alt.actionOffset = new HashMap<Action, Integer>(this.actionOffset);

		return alt;
	}


	public int getActionOffset(Action a){
		Integer offset = this.actionOffset.get(a);
		if(offset == null){
			offset = this.actionOffset.size();
			this.actionOffset.put(a, offset);
		}
		return offset;
	}

	public Map<Action, Integer> getActionOffsets() {
		return actionOffset;
	}

	public void setActionOffset(Map<Action, Integer> actionOffset) {
		this.actionOffset = actionOffset;
	}

	public void setActionOffset(Action a, int offset){
		this.actionOffset.put(a, offset);
	}

	protected List<Integer> getActionFeatures(List<Integer> stateFeatures, int actionOffset){

		List<Integer> actionFeatures = new ArrayList<Integer>(stateFeatures.size());
		for(int sf : stateFeatures){
			int af = sf + (actionOffset * this.parametersPerAction);
			actionFeatures.add(af);
		}

		return actionFeatures;

	}

	protected boolean isBiasTerm(int i){
		int sfIndex = i % this.parametersPerAction;
		return sfIndex == this.parametersPerAction - 1;
	}

	protected List<Integer> getTilesForChannels(CameraChannels channels, int receptiveWidth){

		int pixelDim = 8;
		int nCols = (int)Math.ceil((double)pixelDim / (double)receptiveWidth) + 1;
		int colsBySets = nCols * receptiveWidth;
		int colsByScales = colsBySets * channels.channels[0].nScales();

		List<Integer> tilesPerChannel = new ArrayList<Integer>();
		int offset = 0;
		for(int i = 0; i < channels.nChannels(); i++){
			List<Integer> tilesForScales = this.getTilesForScales(channels.channels[i], receptiveWidth);
			for(int t : tilesForScales){
				tilesPerChannel.add(t+offset);
			}

			offset += colsByScales;
		}

		return tilesPerChannel;

	}


	protected List<Integer> getTilesForScales(CameraScaleFeatures scales, int receptiveWidth){


		int pixelDim = 8;
		int nCols = (int)Math.ceil((double)pixelDim / (double)receptiveWidth) + 1;
		int colsBySets = nCols * receptiveWidth;

		List<Integer> tilesPerScale = new ArrayList<Integer>();

		int offset = 0;
		for(int i = 0; i < scales.nScales(); i++){

			if(activationInPixels(scales.scales[i].features)){
				List<Integer> scaleTiles = this.getTiles(scales.scales[i].features, receptiveWidth);
				for(Integer t : scaleTiles){
					tilesPerScale.add(t+offset);
				}
				return tilesPerScale;
			}

			offset += colsBySets;

		}

		return tilesPerScale;

	}


	protected List<Integer> getTiles(double [] pixels, int receptiveWidth){

		int pixelDim = 8;
		int nCols = (int)Math.ceil((double)pixelDim / (double)receptiveWidth) + 1;

		int idShift = 0;
		List<Integer> tiles = new ArrayList<Integer>();
		for(int i = 0; i < receptiveWidth; i++){
			int offset = -receptiveWidth + i + 1;
			int tile = this.getTile(pixels, offset, receptiveWidth);
			int tileId = tile + idShift;
			tiles.add(tileId);
			idShift += nCols;
		}

		return tiles;

	}


	/**
	 * Returns the tile with the highest activation or -1 if no tiles are active
	 * @param pixels
	 * @param offset
	 * @param receptiveWidth
	 * @return
	 */
	protected int getTile(double [] pixels, int offset, int receptiveWidth){
		double [] cols = this.getNormalizedColumns(pixels, offset, receptiveWidth);
		double maxVal = Double.NEGATIVE_INFINITY;
		int maxCol = -1;
		for(int i = 0; i < cols.length; i++){
			if(cols[i] > maxVal){
				maxVal = cols[i];
				maxCol = i;
			}
		}

		if(maxVal == 0.){
			return -1;
		}

		return maxCol;

	}

	protected double [] getNormalizedColumns(double [] pixels, int offset, int receptiveWidth){

		int pixelDim = 8;
		int nCols = (int)Math.ceil((double)pixelDim / (double)receptiveWidth) + 1;
		double [] cols = new double[nCols];

		for(int i = 0; i < pixelDim; i++){

			int r = i*pixelDim;
			for(int j = 0; j < pixelDim; j++){

				int col = (j - offset) / receptiveWidth;
				cols[col] += pixels[r + j];

			}

		}

		double sum = cols[0] + cols[1] + cols[2];
		cols[0] /= sum;
		cols[1] /= sum;
		cols[2] /= sum;

		return cols;
	}


	protected boolean activationInPixels(double [] pixels){

		int height = 8;
		for(int i = 0; i < height; i++){
			int r = i*height;
			for(int j = 0; j < height; j++){
				double p = pixels[r + j];
				if(p > 0.){
					return true;
				}
			}
		}

		return false;

	}
}
