package burlap.behavior.singleagent.learning;

import burlap.behavior.functionapproximation.FunctionGradient;
import burlap.datastructures.HashedAggregator;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author James MacGlashan
 */
public class SparseGradientUtils {


    public static FunctionGradient toGradient(HashedAggregator<Integer> summedParams){
        FunctionGradient fg = new FunctionGradient.SparseGradient(summedParams.size());
        for(Map.Entry<Integer, Double> e : summedParams.entrySet()){
            fg.put(e.getKey(), e.getValue());
        }
        return fg;
    }

    public static void scalarMult(FunctionGradient fg, double scalar){
        for(FunctionGradient.PartialDerivative pd : fg.getNonZeroPartialDerivatives()){
            double scaled = pd.value * scalar;
            fg.put(pd.parameterId, scaled);
        }
    }

    public static FunctionGradient scalarMultCopy(FunctionGradient fg, double scalar){
        FunctionGradient cfg = new FunctionGradient.SparseGradient(fg.numNonZeroPDs());
        for(FunctionGradient.PartialDerivative pd : fg.getNonZeroPartialDerivatives()){
            double scaled = pd.value * scalar;
            cfg.put(pd.parameterId, scaled);
        }
        return cfg;
    }



    public static void sumInto(FunctionGradient fg, HashedAggregator<Integer> sum){
        for(FunctionGradient.PartialDerivative pd : fg.getNonZeroPartialDerivatives()){
            sum.add(pd.parameterId, pd.value);
        }
    }

    public static void scalarMultSumInto(FunctionGradient fg, double mult, HashedAggregator<Integer> sum){
        for(FunctionGradient.PartialDerivative pd : fg.getNonZeroPartialDerivatives()){
            sum.add(pd.parameterId, mult*pd.value);
        }
    }

    public static FunctionGradient diffGrad(FunctionGradient a, FunctionGradient b){
        Set<Integer> pIds = pdIdSet(a, b);

        //now compute
        FunctionGradient fg = new FunctionGradient.SparseGradient(pIds.size());
        for(int pid : pIds){
            double v = a.getPartialDerivative(pid) - b.getPartialDerivative(pid);
            fg.put(pid, v);
        }

        return fg;

    }

    public static FunctionGradient addGrad(FunctionGradient a, FunctionGradient b){

        Set<Integer> pIds = pdIdSet(a, b);

        //now compute
        FunctionGradient fg = new FunctionGradient.SparseGradient(pIds.size());
        for(int pid : pIds){
            double v = a.getPartialDerivative(pid) + b.getPartialDerivative(pid);
            fg.put(pid, v);
        }

        return fg;

    }

    public static Set<Integer> pdIdSet(FunctionGradient a, FunctionGradient b){
        Set<FunctionGradient.PartialDerivative> aSet = a.getNonZeroPartialDerivatives();
        Set<FunctionGradient.PartialDerivative> bSet = b.getNonZeroPartialDerivatives();
        Set<Integer> pIds = new HashSet<>(aSet.size()+bSet.size());
        for(FunctionGradient.PartialDerivative pd : aSet){
            pIds.add(pd.parameterId);
        }
        for(FunctionGradient.PartialDerivative pd : bSet){
            pIds.add(pd.parameterId);
        }

        return pIds;
    }

}
