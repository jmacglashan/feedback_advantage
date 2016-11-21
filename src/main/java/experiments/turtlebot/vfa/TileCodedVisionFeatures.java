package experiments.turtlebot.vfa;

import burlap.behavior.functionapproximation.sparse.SparseStateFeatures;
import burlap.behavior.functionapproximation.sparse.StateFeature;
import burlap.mdp.core.state.State;
import experiments.turtlebot.datastructures.CameraChannels;
import experiments.turtlebot.datastructures.CameraScaleFeatures;
import experiments.turtlebot.states.TurtleChannelsState;

import java.util.ArrayList;
import java.util.List;

/**
 * @author James MacGlashan.
 */
public class TileCodedVisionFeatures implements SparseStateFeatures {

	protected int receptiveWidth = 3;
	protected int numFeatures = 0;

	public TileCodedVisionFeatures(int receptiveWidth) {
		this.receptiveWidth = receptiveWidth;
	}

	@Override
	public List<StateFeature> features(State s) {
		List<Integer> ifeatures = this.getTilesForChannels(((TurtleChannelsState)s).channels, this.receptiveWidth);
		ifeatures.add(numFeatures-1); //adding constant bias term

		List<StateFeature> features = new ArrayList<>(ifeatures.size());
		for(Integer i : ifeatures){
			StateFeature sf = new StateFeature(i, 1.);
			features.add(sf);
		}

		return features;
	}

	@Override
	public SparseStateFeatures copy() {
		return new TileCodedVisionFeatures(this.receptiveWidth);
	}

	@Override
	public int numFeatures() {
		return this.numFeatures;
	}

	public int numFeaturesFor(int pixelDim, int nScales, int nChannels){
		int nCols = (int)Math.ceil((double)pixelDim / (double)receptiveWidth) + 1;
		int colsBySets = nCols * receptiveWidth;
		int colsByScales = colsBySets * nScales;
		int num = colsByScales * nChannels;
		num += 1; //1 for bias term

		return num;
	}

	protected List<Integer> getTilesForChannels(CameraChannels channels, int receptiveWidth){

		int pixelDim = 8;
		int nCols = (int)Math.ceil((double)pixelDim / (double)receptiveWidth) + 1;
		int colsBySets = nCols * receptiveWidth;
		int colsByScales = colsBySets * channels.channels[0].nScales();
		this.numFeatures = colsByScales * channels.channels.length + 1; //+1 for bias term

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
