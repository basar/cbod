package net.bsrc.cbod.core;

import net.bsrc.cbod.core.model.CandidateComponent;
import org.opencv.core.Rect;

/**
 * User: bsr
 * Date: 14/03/15
 * Time: 15:30
 */
public interface IMembershipFunction {

    /**
     * calculate membership degree of two rectangle
     * @param r1 source
     * @param r2 target
     * @return
     */
    double calculateValue(Rect r1,Rect r2);


    /**
     * calculate membership degree of two component
     * @param c1 source
     * @param c2 target
     * @return
     */
    double calculateValue(CandidateComponent c1,CandidateComponent c2);


}
