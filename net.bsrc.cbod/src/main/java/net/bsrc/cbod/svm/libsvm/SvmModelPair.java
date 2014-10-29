package net.bsrc.cbod.svm.libsvm;

import java.util.List;

/**
 * User: bsr
 * Date: 28/10/14
 * Time: 22:19
 */
public class SvmModelPair {


    private double label;

    private List<List<Double>> datas;

    public SvmModelPair(double label, List<List<Double>> datas) {
        this.label = label;
        this.datas = datas;
    }

    public double getLabel() {
        return label;
    }

    public void setLabel(double label) {
        this.label = label;
    }

    public List<List<Double>> getDatas() {
        return datas;
    }

    public void setDatas(List<List<Double>> datas) {
        this.datas = datas;
    }
}
