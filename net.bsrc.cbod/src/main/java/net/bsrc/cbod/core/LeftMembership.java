package net.bsrc.cbod.core;

import net.bsrc.cbod.opencv.OpenCV;
import org.apache.commons.lang.Validate;
import org.opencv.core.Rect;

/**
 * User: bsr
 * Date: 14/03/15
 * Time: 15:50
 */
public class LeftMembership extends BaseMembership {

    private static LeftMembership instance = null;


    /**
     * To prevent object creation
     */
    private LeftMembership() {

    }

    public static LeftMembership getInstance() {

        if (instance == null) {
            synchronized (LeftMembership.class) {
                if (instance == null) {
                    instance = new LeftMembership();
                }
            }
        }

        return instance;
    }


    @Override
    public double calculateValue(Rect r1, Rect r2) {

        Validate.notNull(r1);
        Validate.notNull(r2);

        double teta = OpenCV.getAngle(r1, r2);

        if (90 < teta && teta < 270)
            return Math.sin(Math.toRadians(teta - 90));

        return 0.0;
    }
}
