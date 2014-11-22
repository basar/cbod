package net.bsrc.cbod.svm.libsvm;

import libsvm.svm;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;
import net.bsrc.cbod.core.util.CBODUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * User: bsr
 * Date: 26/10/14
 * Time: 21:42
 */
public class LibSvmUtil {


    private static final Logger logger = LoggerFactory.getLogger(LibSvm.class);


    public static svm_parameter createSvmParameter(double c, double gamma, boolean probabilistic) {

        svm_parameter param = new svm_parameter();
        param.svm_type = svm_parameter.C_SVC;
        param.kernel_type = svm_parameter.RBF;
        param.probability = 0;
        if (probabilistic) param.probability = 1;
        param.C = c;
        param.gamma = gamma;
        //default values
        param.coef0 = 0;
        param.nu = 0.5;
        param.p = 0.1;
        param.cache_size = 20000;
        param.eps = 0.001;

        return param;
    }

    public  static svm_parameter createSvmParameter(int c,int gamma,boolean probabilistic){
        return createSvmParameter(Math.pow(2.0,c),Math.pow(2.0,gamma),probabilistic);
    }


    public static svm_node[] createSVMNodeArray(List<Double> list) {

        svm_node[] svmNodeArr = new svm_node[list.size()];

        for (int j = 0; j < list.size(); j++) {
            svm_node node = new svm_node();
            node.index = j + 1;
            node.value = list.get(j);

            svmNodeArr[j] = node;
        }

        return svmNodeArr;
    }


    /**
     * create an svm problem
     *
     * @param svmModelPairs
     * @return
     */
    public static svm_problem createSvmProblem(List<SvmModelPair> svmModelPairs) {

        //total data size
        int l = 0;
        //labels
        double[] y;
        //svmNodes
        svm_node[][] svmNodes;

        for (int i = 0; i < svmModelPairs.size(); i++) {
            SvmModelPair svmModelPair = svmModelPairs.get(i);
            List<List<Double>> datas = svmModelPair.getDatas();
            l = l + datas.size();
        }

        y = new double[l];
        svmNodes = new svm_node[l][];
        //global counter
        int cnt = 0;

        for (int i = 0; i < svmModelPairs.size(); i++) {

            SvmModelPair svmModelPair = svmModelPairs.get(i);
            List<List<Double>> datas = svmModelPair.getDatas();

            for (int j = 0; j < datas.size(); j++) {

                double[] feature = CBODUtil.toArray(datas.get(j));
                svmNodes[cnt] = new svm_node[feature.length];
                y[cnt] = svmModelPair.getLabel();
                for (int k = 0; k < feature.length; k++) {
                    svm_node svmNode = new svm_node();
                    svmNode.index = k + 1; //svm node index must be started 1
                    svmNode.value = feature[k];
                    svmNodes[cnt][k] = svmNode;
                }
                //increase global counter
                cnt++;
            }
        }

        svm_problem svmProblem = new svm_problem();
        svmProblem.l = l;
        svmProblem.y = y;
        svmProblem.x = svmNodes;

        return svmProblem;
    }

    /**
     * @param svmProblem svm problem
     * @param param      svm parameter
     * @param costStep   loop step for C parameter
     * @param gammaStep  loop step for Gamma parameter
     * @param fold       fold for cross validation
     */
    public static void doCrossValidation(svm_problem svmProblem, svm_parameter param, int costStep, int gammaStep, int fold) {

        /**
        String checkParameterResult = svm.svm_check_parameter(svmProblem, param);
        if (checkParameterResult != null) {
            logger.error("Parameters not feasible:{}", checkParameterResult);
            return;
        }
        **/

        List<String> stats = new ArrayList<String>();
        int maxSuccess = 0;
        double maxCost = 0;
        double maxGamma = 0;
        double maxI = 0;
        double maxJ = 0;
        double totalSuccess = 0.0;
        int totalCrossValidationCount = 0;

        for (double i = -5; i <= 15; i = i + costStep) {

            for (double j = -15; j <= 3; j = j + gammaStep) {

                param.C = Math.pow(2.0, i);
                param.gamma = Math.pow(2.0, j);

                double[] target = new double[svmProblem.l];
                //cross validation
                svm.svm_cross_validation(svmProblem, param, fold, target);

                int success = 0;
                for (int k = 0; k < target.length; k++) {
                    if (target[k] == svmProblem.y[k]) {
                        success++;
                    }
                }

                if (success > maxSuccess) {
                    maxSuccess = success;
                    maxCost = param.C;
                    maxGamma = param.gamma;
                    maxI = i;
                    maxJ = j;
                }


                double successPercentage = ((double) success / (double) svmProblem.l) * 100;
                totalSuccess = totalSuccess + successPercentage;
                totalCrossValidationCount = totalCrossValidationCount + 1;

                StringBuilder sb = new StringBuilder();

                sb.append("[");
                sb.append("Cost[" + i + "]:").append(param.C).append(" ");
                sb.append("Gamma[" + j + "]:").append(param.gamma).append(" ");
                sb.append("Success:").append(successPercentage);
                sb.append("]");

                stats.add(sb.toString());

            }
        }

        for (String stat : stats) {
            logger.debug(stat);
        }

        StringBuilder sb = new StringBuilder();

        sb.append("Max:[");
        sb.append("Cost[" + maxI + "]:").append(maxCost).append(" ");
        sb.append("Gamma[" + maxJ + "]:").append(maxGamma).append(" ");
        sb.append("Success:").append(((double) maxSuccess / (double) svmProblem.l) * 100);
        sb.append("]");

        logger.debug(sb.toString());

        sb = new StringBuilder();
        sb.append("[Mean Success:").append((totalSuccess / (double) totalCrossValidationCount));
        sb.append("]");

        // logger.debug(sb.toString());
    }


}