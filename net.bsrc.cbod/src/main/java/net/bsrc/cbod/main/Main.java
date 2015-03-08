package net.bsrc.cbod.main;

import com.googlecode.javacv.cpp.opencv_core.CvMat;
import libsvm.*;
import net.bsrc.cbod.core.*;
import net.bsrc.cbod.core.model.*;
import net.bsrc.cbod.core.persistence.DB4O;
import net.bsrc.cbod.core.persistence.ImageModelService;
import net.bsrc.cbod.core.util.CBODUtil;
import net.bsrc.cbod.jseg.JSEG;
import net.bsrc.cbod.jseg.JSEGParameter;
import net.bsrc.cbod.jseg.JSEGParameterFactory;
import net.bsrc.cbod.mpeg.bil.BilMpeg7Fex;
import net.bsrc.cbod.opencv.OpenCV;
import net.bsrc.cbod.svm.libsvm.LibSvm;
import net.bsrc.cbod.svm.libsvm.LibSvmUtil;
import net.bsrc.cbod.svm.libsvm.SvmModelPair;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Main class for application
 */
public class Main {


    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }


    /**
     * Main entry point for program
     *
     * @param args
     */
    public static void main(String[] args) {



        MultiClassSVM instance = MultiClassSVM.getInstance();


        String imageName = "IMG_" + 31 + ".jpg";
        SVMPredictionResult result = instance.doPredictionWithMultiClassSVMs(imageName, 0.0, new ZScoreNormalization());

        CandidateComponent max=instance.findComponentWithMaximumDecisionFusionResult(result.getCandidateComponents());


        for(CandidateComponent comp1:result.getCandidateComponents()){

            System.out.println(comp1.getObjectType().getName() + " - " + OpenCV.getMinimumDistance(max.getRect(),comp1.getRect()));

        }


        CBODUtil.drawComponentsToImage(result.getCandidateComponents(), result.getInputImageModel(), CBODConstants.MC_OUT_SUFFIX);


        DB4O.getInstance().close();
    }



    public List<CandidateComponent> doGeometricConfigurationProcess(List<CandidateComponent> list){

        List<CandidateComponent> resultList = new ArrayList<CandidateComponent>();

        if(list.size()==0)
            return resultList;

        if(list.size()==1){
            resultList.add(list.get(0));
            return resultList;
        }




        return resultList;
    }




}