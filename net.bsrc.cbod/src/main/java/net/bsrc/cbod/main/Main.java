package net.bsrc.cbod.main;

import com.fuzzylite.Engine;
import com.fuzzylite.imex.FclImporter;
import com.fuzzylite.imex.FllImporter;
import net.bsrc.cbod.core.*;
import net.bsrc.cbod.core.model.CandidateComponent;
import net.bsrc.cbod.core.model.EObjectType;
import net.bsrc.cbod.core.model.SVMPredictionResult;
import net.bsrc.cbod.core.persistence.DB4O;
import net.bsrc.cbod.core.util.CBODUtil;
import net.bsrc.cbod.opencv.OpenCV;
import org.apache.commons.io.FileUtils;
import org.opencv.core.Core;
import org.opencv.core.Rect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
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

        doPrediction("test.jpg", false);



        DB4O.getInstance().close();
    }


    private static void doPrediction(String imageName, boolean writeToFile) {

        MultiClassSVM instance = MultiClassSVM.getInstance();


        SVMPredictionResult result = instance.doPredictionWithMultiClassSVMs(imageName, 0.0, new ZScoreNormalization());
        List<CandidateComponent> nonFilteredList = result.getCandidateComponents();
        List<CandidateComponent> filteredList = null;

        double fuzzyResult = 0.0;
        CandidateComponent max = instance.findComponentWithMaximumDecisionFusionResult(result.getCandidateComponents());
        if (max != null) {

            if (writeToFile) {
                writeMaxAndOtherComponentRelationsToFile(imageName, max, result.getCandidateComponents());
            }

            filteredList = doFalseComponentElimination(result.getCandidateComponents());
            fuzzyResult = doFuzzyOperation(filteredList);

        }
        OpenCV.drawComponentsToImage(nonFilteredList, max, result.getInputImageModel(), CBODConstants.MC_OUT_SUFFIX);
        OpenCV.drawComponentsToImage(filteredList, max, result.getInputImageModel(), CBODConstants.F_OUT_SUFFIX,fuzzyResult);

    }

    private static double doFuzzyOperation(List<CandidateComponent> components) {


        FclImporter importer = new FclImporter();

        String s = CBODUtil.getFileDataAsString("/Users/bsr/Documents/Phd/cbod/fuzzy/cbod.fcl");

        Engine engine = importer.fromString(s);

        CandidateComponent t1 = null;
        CandidateComponent t2 = null;
        CandidateComponent lp = null;
        CandidateComponent w1 = null;
        CandidateComponent w2 = null;

        for (CandidateComponent component : components) {
            if (t1 == null && component.getObjectType() == EObjectType.TAIL_LIGHT) {
                t1 = component;
                continue;
            }
            if (t2 == null && component.getObjectType() == EObjectType.TAIL_LIGHT) {
                t2 = component;
                continue;
            }
            if (component.getObjectType() == EObjectType.LICENSE_PLATE) {
                lp = component;
                continue;
            }
            if (w1 == null && component.getObjectType() == EObjectType.WHEEL) {
                w1 = component;
                continue;
            }
            if (w2 == null && component.getObjectType() == EObjectType.WHEEL) {
                w2 = component;
            }
        }

        double t1Ex = t1 != null ? t1.getDecisionFusionResult() : 0.0;
        double t2Ex = t2 != null ? t2.getDecisionFusionResult() : 0.0;
        double lpEx = lp != null ? lp.getDecisionFusionResult() : 0.0;
        double w1Ex = w1 != null ? w1.getDecisionFusionResult() : 0.0;
        double w2Ex = w2 != null ? w2.getDecisionFusionResult() : 0.0;
        double t1lpNr = (t1 != null && lp != null) ? NearMembership.getInstance().calculateValue(t1, lp) : 0.0;
        double t2lpNr = (t2 != null && lp != null) ? NearMembership.getInstance().calculateValue(t2, lp) : 0.0;
        double w1w2Far = (w1 != null && w2 != null) ? FarMembership.getInstance().calculateValue(w1, w2) : 0.0;
        double w1w2Al = (w1!=null && w2!=null) ? Math.max(BelowMembership.getInstance().calculateValue(w1, w2),
                AboveMembership.getInstance().calculateValue(w1, w2)) : 0.0;
        double t1t2Al = (t1!=null && t2!=null) ? Math.max(BelowMembership.getInstance().calculateValue(t1, t2),
                AboveMembership.getInstance().calculateValue(t1, t2)) : 0.0;

        //kuralin tetiklenmesi icin
        if(w1!=null && w2!=null && w1w2Al==0.0){
            w1w2Al = 0.01;
        }
        //kuralin tetiklenmesi icin
        if(t1!=null && t2!=null && t1t2Al==0.0){
            t1t2Al = 0.01;
        }
        if(t1lpNr==1.0)
            t1lpNr = 0.9999;
        if(t2lpNr == 1.0){
            t2lpNr = 0.9999;
        }


        engine.getInputVariable(CBODConstants.T1_EX).setInputValue(t1Ex);
        engine.getInputVariable(CBODConstants.T2_EX).setInputValue(t2Ex);
        engine.getInputVariable(CBODConstants.LP_EX).setInputValue(lpEx);
        engine.getInputVariable(CBODConstants.W1_EX).setInputValue(w1Ex);
        engine.getInputVariable(CBODConstants.W2_EX).setInputValue(w2Ex);
        engine.getInputVariable(CBODConstants.T1LP_NR).setInputValue(t1lpNr);
        engine.getInputVariable(CBODConstants.T2LP_NR).setInputValue(t2lpNr);
        engine.getInputVariable(CBODConstants.W1W2_FAR).setInputValue(w1w2Far);
        engine.getInputVariable(CBODConstants.W1w2_AL).setInputValue(w1w2Al);
        engine.getInputVariable(CBODConstants.T1T2_AL).setInputValue(t1t2Al);



        StringBuilder status = new StringBuilder();
        if (!engine.isReady(status))
            throw new RuntimeException("Engine not ready. " +
                    "The following errors were encountered:\n" + status.toString());

        engine.process();

        return engine.getOutputVariable(CBODConstants.CAR_EX).defuzzify();


    }


    private static List<CandidateComponent> doFalseComponentElimination(List<CandidateComponent> list) {

        List<CandidateComponent> resultList = new ArrayList<CandidateComponent>();
        resultList.addAll(list);

        if (list.size() == 0)
            return resultList;

        if (list.size() == 1) {
            return resultList;
        }

        MultiClassSVM instance = MultiClassSVM.getInstance();
        //Ilk olarak tum parcalar arasinda en yuksek decision degere sahip olan bulunacak
        CandidateComponent max = instance.findComponentWithMaximumDecisionFusionResult(list);
        //En yuksek dereceli parca ile kesisen aynı sınıfa ait diger objeleri cikar
        list = removeCandidatesIfintersectAndSameObjectType(max, list);

        double wheelAlignThreshold = 0.40;
        double tailLightAlignThreshold = 0.30;

        //Eger en yuksek dereceli obje teker ise
        if (max.getObjectType() == EObjectType.WHEEL) {
            //Kesisen farlar cikarilacak
            list = removeCandidatesIfintersectAny(max, EObjectType.TAIL_LIGHT, list);
            //Yakin olan plakalar cikarilacak
            list = removeCandidates(max, EObjectType.LICENSE_PLATE, NearMembership.getInstance(), 1.0, Operator.EQUAL, list);
            //Yakın olan tekerler cikarilacak
            list = removeCandidates(max, EObjectType.WHEEL, NearMembership.getInstance(), 1.0, Operator.EQUAL, list);
            //Altda olan farlar cikarilacak
            list = removeCandidates(max, EObjectType.TAIL_LIGHT, BelowMembership.getInstance(), 0.0, Operator.BIGGER, list);
            //Altda olan plakalar cikarilacak
            list = removeCandidates(max, EObjectType.LICENSE_PLATE, BelowMembership.getInstance(), 0.0, Operator.BIGGER, list);
            //Max component ile en hizali teker haric digerlerini cikar
            list = removeCandidatesExceptMostAlign(max, EObjectType.WHEEL, wheelAlignThreshold, list);

        }

        if (max.getObjectType() == EObjectType.TAIL_LIGHT) {
            //Kesisen tekerler cikarilacak
            list = removeCandidatesIfintersectAny(max, EObjectType.WHEEL, list);
            //Uzak olan plakalari cikar
            list = removeCandidates(max, EObjectType.LICENSE_PLATE, FarMembership.getInstance(), 1.0, Operator.EQUAL, list);
            //Yukarda olan tekerleri cikar (threshold gerekebilir)
            list = removeCandidates(max, EObjectType.WHEEL, AboveMembership.getInstance(), 0.20, Operator.BIGGER, list);
            //Max component ile en hizali far haric digerlerini cikar
            list = removeCandidatesExceptMostAlign(max, EObjectType.TAIL_LIGHT, tailLightAlignThreshold, list);


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
            list = removeCandidatesExceptMostAlign(maxTaillight, EObjectType.TAIL_LIGHT, tailLightAlignThreshold, list);
        }

        //Eger ikiden fazla teker bulunmus ise
        if (getCountObjectType(EObjectType.WHEEL, list) > 2) {
            //Aralarindan en yuksek decision value'ya sahip olanini bul
            CandidateComponent maxWheel = instance.findComponentWithMaximumDecisionFusionResult(EObjectType.WHEEL, list);
            //Max taillight component ile en hizali far haric digerlerini cikar
            list = removeCandidatesExceptMostAlign(maxWheel, EObjectType.WHEEL, wheelAlignThreshold, list);
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

        return removeCandidatesIfintersectAny(source, source.getObjectType(), targetList);
    }

    private static List<CandidateComponent> removeCandidatesIfintersectAny(
            CandidateComponent source, EObjectType objectType, List<CandidateComponent> targetList) {

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
            if (target.getObjectType() != objectType) {
                resultList.add(target);
            }
        }


        return resultList;
    }


    private static void writeMaxAndOtherComponentRelationsToFile(String imageName, CandidateComponent max, List<CandidateComponent> components) {


        Rect r1 = max.getRect();

        File outputFile = FileUtils.getFile(CBODUtil.getCbodTempDirectory().concat("/").concat("log.txt"));

        StringBuilder sb = new StringBuilder();
        sb.append("\nImage Name: ").append(imageName);
        sb.append("\nMax component type: ").append(max.getObjectType().getName());
        sb.append("\nDecision Value: ").append(max.getDecisionFusionResult());
        sb.append("\n--------------------------------------------------------\n");


        for (CandidateComponent comp1 : components) {

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


    }
}