package net.bsrc.cbod.core;

import net.bsrc.cbod.opencv.OpenCV;
import org.apache.commons.lang.Validate;
import org.opencv.core.Rect;

/**
 * User: bsr
 * Date: 14/03/15
 * Time: 15:48
 */
public class BelowMembership extends BaseMembership {

    private static BelowMembership instance = null;


    /**
     * To prevent object creation
     */
    private BelowMembership() {

    }

    public static BelowMembership getInstance() {

        if (instance == null) {
            synchronized (BelowMembership.class) {
                if (instance == null) {
                    instance = new BelowMembership();
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

        if (180 < teta && teta <= 360)
            return Math.sin(Math.toRadians(teta - 180));

        return 0.0;
    }
}
