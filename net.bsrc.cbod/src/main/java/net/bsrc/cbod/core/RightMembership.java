package net.bsrc.cbod.core;

import net.bsrc.cbod.opencv.OpenCV;
import org.apache.commons.lang.Validate;
import org.opencv.core.Rect;

/**
 * User: bsr
 * Date: 14/03/15
 * Time: 15:51
 */
public class RightMembership extends BaseMembership {


    private static RightMembership instance = null;


    /**
     * To prevent object creation
     */
    private RightMembership() {

    }

    public static RightMembership getInstance() {

        if (instance == null) {
            synchronized (RightMembership.class) {
                if (instance == null) {
                    instance = new RightMembership();
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

        if ((0 <= teta && teta < 90) || (270 < teta && teta <= 360))
            return Math.sin(Math.toRadians(teta + 90));

        return 0.0;


    }
}
