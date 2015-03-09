package net.bsrc.cbod.core;

import net.bsrc.cbod.core.model.CandidateComponent;
import net.bsrc.cbod.opencv.OpenCV;
import org.opencv.core.Rect;

/**
 * User: bsr
 * Date: 09/03/15
 * Time: 20:29
 */
public class FuzzyFunctions {


    public static double nearMembershipFunction(Rect r1, Rect r2) {


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


    public static double farMembershipFunction(Rect r1, Rect r2) {


        double d = OpenCV.getMinimumDistance(r1, r2);

        double largestDimension1 = Math.max(r1.width, r2.height);
        double largestDimension2 = Math.max(r2.width, r2.height);

        double maxDim = Math.max(largestDimension1, largestDimension2);
        double minDim = Math.min(largestDimension1, largestDimension2);

        if (d <= minDim) {
            return 0.0;
        }

        if (d >= (largestDimension1 + largestDimension2)) {
            return 1.0;
        }

        return ((d - minDim) / maxDim);

    }


    public static double rightMembershipFunction(Rect r1, Rect r2) {

        double teta = OpenCV.getAngle(r1, r2);

        if ((0 < teta && teta < 90) || (270 < teta && teta < 360))
            return Math.sin(Math.toRadians(teta + 90));

        return 0.0;

    }


    public static double leftMembershipFunction(Rect r1, Rect r2) {

        double teta = OpenCV.getAngle(r1, r2);

        if (0 < teta && teta < 270)
            return Math.sin(Math.toRadians(teta - 90));

        return 0.0;

    }


    public static double aboveMembershipFunction(Rect r1, Rect r2) {

        double teta = OpenCV.getAngle(r1, r2);

        if (0 < teta && teta < 180)
            return Math.sin(Math.toRadians(teta));

        return 0.0;

    }

    public static double belowMembershipFunction(Rect r1, Rect r2) {

        double teta = OpenCV.getAngle(r1, r2);

        if (180 < teta && teta < 360)
            return Math.sin(Math.toRadians(teta - 180));

        return 0.0;

    }

}
