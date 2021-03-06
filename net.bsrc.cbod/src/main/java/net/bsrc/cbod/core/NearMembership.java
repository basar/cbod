package net.bsrc.cbod.core;

import net.bsrc.cbod.core.model.CandidateComponent;
import net.bsrc.cbod.opencv.OpenCV;
import org.apache.commons.lang.Validate;
import org.opencv.core.Rect;

/**
 * User: bsr
 * Date: 14/03/15
 * Time: 15:32
 */
public class NearMembership extends BaseMembership {

    private static NearMembership instance = null;


    /**
     * To prevent object creation
     */
    private NearMembership() {

    }

    public static NearMembership getInstance() {

        if (instance == null) {
            synchronized (NearMembership.class) {
                if (instance == null) {
                    instance = new NearMembership();
                }
            }
        }

        return instance;
    }


    @Override
    public double calculateValue(Rect r1, Rect r2) {

        Validate.notNull(r1);
        Validate.notNull(r2);

        double d = OpenCV.getMinimumDistance(r1, r2);

        double largestDimension1 = Math.max(r1.width, r2.height);
        double largestDimension2 = Math.max(r2.width, r2.height);

        double maxDim = Math.max(largestDimension1, largestDimension2);
        double minDim = Math.min(largestDimension1, largestDimension2);

        if (d <= minDim) {
            return 1.0;
        }

        if (d >= (largestDimension1 + largestDimension2)) {
            return 0.0;
        }

        return 1 - ((d - minDim) / maxDim);
    }

}
