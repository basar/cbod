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
import java.util.HashSet;
import java.util.List;
import java.util.Set;


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


        String imageName = "IMG_" + 70 + ".jpg";
        SVMPredictionResult result = instance.doPredictionWithMultiClassSVMs(imageName, 0.0, new ZScoreNormalization());

        CandidateComponent max = instance.findComponentWithMaximumDecisionFusionResult(result.getCandidateComponents());
        Rect r1 = max.getRect();

        for (CandidateComponent comp1 : result.getCandidateComponents()) {

            if (!comp1.equals(max)) {
                Rect r2 = comp1.getRect();
                logger.debug("Object Type:{}",comp1.getObjectType().getName());
                logger.debug("Left:{}",FuzzyFunctions.leftMembershipFunction(r1,r2));
                logger.debug("Right:{}",FuzzyFunctions.rightMembershipFunction(r1,r2));
                logger.debug("Above:{}",FuzzyFunctions.aboveMembershipFunction(r1,r2));
                logger.debug("Below:{}",FuzzyFunctions.belowMembershipFunction(r1,r2));
                logger.debug("Near:{}",FuzzyFunctions.nearMembershipFunction(r1,r2));
                logger.debug("Far:{}",FuzzyFunctions.farMembershipFunction(r1,r2));
                logger.debug("---------------------------------------------");

            }


        }


        CBODUtil.drawComponentsToImage(result.getCandidateComponents(), result.getInputImageModel(), CBODConstants.MC_OUT_SUFFIX);


        DB4O.getInstance().close();
    }


    public List<CandidateComponent> doFalseComponentElimination(List<CandidateComponent> list) {

        List<CandidateComponent> resultList = new ArrayList<CandidateComponent>();

        if (list.size() == 0)
            return resultList;

        if (list.size() == 1) {
            resultList.add(list.get(0));
            return resultList;
        }

        Set<CandidateComponent> toRemoved = new HashSet<CandidateComponent>();

        MultiClassSVM instance = MultiClassSVM.getInstance();
        //Ilk olarak tum parcalar arasinda en yuksek decision degere sahip olan bulunacak
        CandidateComponent maxCandidate = instance.findComponentWithMaximumDecisionFusionResult(resultList);

        if (maxCandidate.getObjectType() == EObjectType.WHEEL) {


        }

        if (maxCandidate.getObjectType() == EObjectType.TAIL_LIGHT) {

        }

        if (maxCandidate.getObjectType() == EObjectType.LICENSE_PLATE) {

        }


        return resultList;
    }


}