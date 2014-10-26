package net.bsrc.cbod.core;

import java.util.List;

/**
 * User: bsr
 * Date: 26/10/14
 * Time: 13:59
 */
public class NullNormalization implements INormalization {

    @Override
    public void applyNormalization(List<Double> datas) {
        //do nothing
    }

    @Override
    public void applyNormalizations(List<List<Double>> datas) {
        //do nothing
    }
}
