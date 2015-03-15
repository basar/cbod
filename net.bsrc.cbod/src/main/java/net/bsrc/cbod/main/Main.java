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
import org.apache.commons.io.FileUtils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
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


        File outputFile = FileUtils.getFile(CBODUtil.getCbodTempDirectory().concat("/").concat("log.txt"));

        for (int i = 84; i <= 84; i++) {

            String imageName = "IMG_" + i + ".jpg";

            SVMPredictionResult result = instance.doPredictionWithMultiClassSVMs(imageName, 0.0, new ZScoreNormalization());

            CandidateComponent max = instance.findComponentWithMaximumDecisionFusionResult(result.getCandidateComponents());
            Rect r1 = max.getRect();

            StringBuilder sb = new StringBuilder();
            sb.append("\nImage Name: IMG_").append(i).append(".jpg");
            sb.append("\nMax component type: ").append(max.getObjectType().getName());
            sb.append("\nDecision Value: ").append(max.getDecisionFusionResult());
            sb.append("\n--------------------------------------------------------\n");

            List<CandidateComponent> filteredList = doFalseComponentElimination(result.getCandidateComponents());

            for (CandidateComponent comp1 : result.getCandidateComponents()) {

                if (!comp1.equals(max)) {
                    Rect r2 = comp1.getRect();
                    sb.append("Object Type   : ").append(comp1.getObjectType().getName()).append("\n");
                    sb.append("Decision Value: ").append(comp1.getDecisionFusionResult()).append("\n");
                    sb.append("Left          : ").append(LeftMembership.getInstance().calculateValue(r1, r2)).append("\n");
                    sb.append("Right         : ").append(RightMembership.getInstance().calculateValue(r1, r2)).append("\n");
                    sb.append("Above         : ").append(AboveMembership.getInstance().calculateValue(r1, r2)).append("\n");
                    sb.append("Below         : ").append(BelowMembership.getInstance().calculateValue(r1, r2)).append("\n");
                    sb.append("Near          : ").append(NearMembership.getInstance().calculateValue(r1, r2)).append("\n");
                    sb.append("Far           : ").append(FarMembership.getInstance().calculateValue(r1, r2)).append("\n");
                    sb.append("----------------------------------------------------------------------\n");

                }

            }

            sb.append("##################################################################################");
            try {
                FileUtils.writeStringToFile(outputFile, sb.toString(), true);
            } catch (IOException e) {
                logger.error("", e);
            }
            OpenCV.drawComponentsToImage(filteredList, max, result.getInputImageModel(), CBODConstants.MC_OUT_SUFFIX);

        }

        DB4O.getInstance().close();
    }


    private static List<CandidateComponent> doFalseComponentElimination(List<CandidateComponent> list) {

        List<CandidateComponent> resultList = new ArrayList<CandidateComponent>();
        resultList.addAll(list);

        if (list.size() == 0)
            return resultList;

        if (list.size() == 1) {
            resultList.add(list.get(0));
            return resultList;
        }

        MultiClassSVM instance = MultiClassSVM.getInstance();
        //Ilk olarak tum parcalar arasinda en yuksek decision degere sahip olan bulunacak
        CandidateComponent max = instance.findComponentWithMaximumDecisionFusionResult(list);
        //En yuksek dereceli parca ile kesisen aynı sınıfa ait diger objeleri cikar
        list = removeCandidatesIfintersectAndSameObjectType(max, list);

        //Eger en yuksek dereceli obje teker ise
        if (max.getObjectType() == EObjectType.WHEEL) {
            //Kesisen farlar cikarilacak
            list = removeCandidatesIfintersectAny(max,EObjectType.TAIL_LIGHT,list);
            //Yakin olan plakalar cikarilacak
            list = removeCandidates(max, EObjectType.LICENSE_PLATE, NearMembership.getInstance(), 1.0, Operator.EQUAL, list);
            //Yakın olan tekerler cikarilacak
            list = removeCandidates(max, EObjectType.WHEEL, NearMembership.getInstance(), 1.0, Operator.EQUAL, list);
            //Altda olan farlar cikarilacak
            list = removeCandidates(max, EObjectType.TAIL_LIGHT, BelowMembership.getInstance(), 0.0, Operator.BIGGER, list);
            //Altda olan plakalar cikarilacak
            list = removeCandidates(max, EObjectType.LICENSE_PLATE, BelowMembership.getInstance(), 0.0, Operator.BIGGER, list);
            //Max component ile en hizali teker haric digerlerini cikar
            list = removeCandidatesExceptMostAlign(max, EObjectType.WHEEL, 0.30, list);

        }

        if (max.getObjectType() == EObjectType.TAIL_LIGHT) {
            //Kesisen tekerler cikarilacak
            list = removeCandidatesIfintersectAny(max,EObjectType.WHEEL,list);
            //Uzak olan plakalari cikar
            list = removeCandidates(max, EObjectType.LICENSE_PLATE, FarMembership.getInstance(), 1.0, Operator.EQUAL, list);
            //Yukarda olan tekerleri cikar (threshold gerekebilir)
            list = removeCandidates(max, EObjectType.WHEEL, AboveMembership.getInstance(), 0.20, Operator.BIGGER, list);
            //Max component ile en hizali far haric digerlerini cikar
            list = removeCandidatesExceptMostAlign(max, EObjectType.TAIL_LIGHT, 0.30, list);


        }

        if (max.getObjectType() == EObjectType.LICENSE_PLATE) {

            //Diger plakalar cikarilacak
            list = removeCandidatesIfSameObjectType(max, list);
            //Uzak olan farlar cikarilacak
            list = removeCandidates(max, EObjectType.TAIL_LIGHT, FarMembership.getInstance(), 1.0, Operator.EQUAL, list);
            //Yakın olan tekerler cikarilacak
            list = removeCandidates(max, EObjectType.WHEEL, NearMembership.getInstance(), 1.0, Operator.EQUAL, list);
            //Yukarda olan tekerleri cikar (threshold gerekebilir)
            list = removeCandidates(max, EObjectType.WHEEL, AboveMembership.getInstance(), 0.20, Operator.BIGGER, list);


        }

        //Eger ikiden fazla far bulunmus ise
        if (getCountObjectType(EObjectType.TAIL_LIGHT, list) > 2) {
            //Aralarindan en yuksek decision value'ya sahip olanini bul
            CandidateComponent maxTaillight = instance.findComponentWithMaximumDecisionFusionResult(EObjectType.TAIL_LIGHT, list);
            //Max taillight component ile en hizali far haric digerlerini cikar
            list = removeCandidatesExceptMostAlign(maxTaillight, EObjectType.TAIL_LIGHT, 0.30, list);
        }

        //Eger ikiden fazla teker bulunmus ise
        if (getCountObjectType(EObjectType.WHEEL, list) > 2) {
            //Aralarindan en yuksek decision value'ya sahip olanini bul
            CandidateComponent maxWheel = instance.findComponentWithMaximumDecisionFusionResult(EObjectType.WHEEL, list);
            //Max taillight component ile en hizali far haric digerlerini cikar
            list = removeCandidatesExceptMostAlign(maxWheel, EObjectType.WHEEL, 0.30, list);
        }

        //Eger birden fazla license plate varsa decision fusion en yüksek olan alınacak digerleri cikarilacak
        list = removeCandidatesExceptHighestDecisionValue(EObjectType.LICENSE_PLATE, list);


        return list;
    }


    private static int getCountObjectType(EObjectType objectType, List<CandidateComponent> list) {

        int count = 0;
        for (CandidateComponent candidateComponent : list) {
            if (candidateComponent.getObjectType() == objectType)
                count++;
        }
        return count;

    }


    private static List<CandidateComponent> removeCandidatesExceptHighestDecisionValue(EObjectType objectType, List<CandidateComponent> list) {

        CandidateComponent max = null;

        List<CandidateComponent> resultList = new ArrayList<CandidateComponent>();

        for (CandidateComponent target : list) {
            if (target.getObjectType() == objectType) {
                if (max == null) {
                    max = target;
                    continue;
                }
                if (!max.equals(target)) {
                    if (target.getDecisionFusionResult() > max.getDecisionFusionResult()) {
                        max = target;
                    }
                }
            }

        }

        if (max == null) return list;

        for (CandidateComponent target : list) {
            if (target.equals(max) || target.getObjectType() != objectType) {
                resultList.add(target);
            }
        }

        return resultList;
    }


    private static List<CandidateComponent> removeCandidatesIfSameObjectType(
            CandidateComponent source, List<CandidateComponent> targetList) {

        if (source == null)
            return targetList;

        List<CandidateComponent> resultList = new ArrayList<CandidateComponent>();

        for (CandidateComponent target : targetList) {
            if (!source.equals(target)) {
                if (source.getObjectType() == target.getObjectType()) {
                    continue;
                }
            }
            resultList.add(target);
        }


        return resultList;

    }


    private static List<CandidateComponent> removeCandidatesIfintersectAndSameObjectType(
            CandidateComponent source, List<CandidateComponent> targetList) {

       return removeCandidatesIfintersectAny(source,source.getObjectType(),targetList);
    }

    private static List<CandidateComponent> removeCandidatesIfintersectAny(
            CandidateComponent source,EObjectType objectType,List<CandidateComponent> targetList) {

        if (source == null)
            return targetList;

        List<CandidateComponent> resultList = new ArrayList<CandidateComponent>();

        for (CandidateComponent candidateComponent : targetList) {
            if (!source.equals(candidateComponent)) {
                if (objectType == candidateComponent.getObjectType()) {
                    if (OpenCV.intersect(source.getRect(), candidateComponent.getRect())) {
                        continue;
                    }
                }
            }
            resultList.add(candidateComponent);
        }


        return resultList;
    }


    private static List<CandidateComponent> removeCandidates(
            CandidateComponent source, EObjectType type, IMembershipFunction mf, double threshold,
            Operator op, List<CandidateComponent> targetList) {

        if (source == null)
            return targetList;

        List<CandidateComponent> resultList = new ArrayList<CandidateComponent>();

        for (CandidateComponent target : targetList) {

            if (!source.equals(target) && target.getObjectType() == type) {

                double membershipValue = mf.calculateValue(source, target);
                boolean result = op.apply(membershipValue, threshold);
                if (result) {
                    continue;
                }

            }
            resultList.add(target);
        }


        return resultList;
    }


    private static List<CandidateComponent> removeCandidatesExceptMostAlign(
            CandidateComponent source, EObjectType objectType, double threshold,
            List<CandidateComponent> list) {

        if (source == null) return list;

        List<CandidateComponent> resultList = new ArrayList<CandidateComponent>();

        CandidateComponent mostAlign = null;
        double mostAlignValue = 0;


        for (CandidateComponent target : list) {
            if (!target.equals(source)) {
                if (target.getObjectType() == objectType) {
                    if (mostAlign == null) {
                        mostAlign = target;
                        mostAlignValue = Math.max(BelowMembership.getInstance().calculateValue(source, target),
                                AboveMembership.getInstance().calculateValue(source, target));
                        continue;
                    }

                    double alignValue = Math.max(BelowMembership.getInstance().calculateValue(source, target),
                            AboveMembership.getInstance().calculateValue(source, target));
                    if (alignValue < mostAlignValue) {
                        mostAlign = target;
                        mostAlignValue = alignValue;
                    }

                }
            }
        }


        if (mostAlign == null) return list;


        for (CandidateComponent target : list) {
            if (target.equals(source)) {
                resultList.add(target);
                continue;
            }
            if (target.equals(mostAlign) && mostAlignValue < threshold) {
                resultList.add(target);
                continue;
            }
            if(target.getObjectType()!=objectType) {
                resultList.add(target);
            }
        }


        return resultList;
    }
}