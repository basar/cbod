package net.bsrc.cbod.core;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * User: bsr
 * Date: 11/10/14
 * Time: 22:26
 */
public class PCAReduction implements IDimensionReduction {


    private static final Logger logger = LoggerFactory.getLogger(PCAReduction.class);


    private int maxComponents = 0;

    public PCAReduction(int maxComponents) {
        this.maxComponents = maxComponents;
    }

    public PCAReduction() {

    }

    @Override
    public List<List<Double>> doTransformations(List<List<Double>> list) {

        List<List<Double>> result = new ArrayList<List<Double>>();

        int maxFeatureSize = 0;

        for (List<Double> temp : list) {
            if (maxFeatureSize == 0) {
                maxFeatureSize = temp.size();
                continue;
            }
            if (maxFeatureSize < temp.size()) {
                maxFeatureSize = temp.size();
            }
        }

        Mat dataMat = new Mat(maxFeatureSize, list.size(), CvType.CV_32F);


        for (int i = 0; i < list.size(); i++) {
            List<Double> temp = list.get(i);
            for (int j = 0; j < maxFeatureSize; j++) {
                try {
                    dataMat.put(j, i, temp.get(j));
                } catch (IndexOutOfBoundsException ex) {
                    dataMat.put(j, i, 0);
                }
            }
        }

        Mat mean = new Mat();
        Mat vectors = new Mat();


        if (this.maxComponents != 0)
            Core.PCACompute(dataMat, mean, vectors, maxComponents);
        else
            Core.PCACompute(dataMat, mean, vectors);


        for(int i=0;i<vectors.cols();i++){
            List<Double> temp = new ArrayList<Double>();
            for(int j=0;j<vectors.rows();j++){
                double val=vectors.get(j,i)[0];
                temp.add(val);
            }
            result.add(temp);
        }


        return result;

    }

    @Override
    public List<Double> doTransformation(List<Double> list) {


        int maxFeatureSize = 0;


        return null;
    }
}
