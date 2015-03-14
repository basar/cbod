package net.bsrc.cbod.core;

import net.bsrc.cbod.opencv.OpenCV;
import org.apache.commons.lang.Validate;
import org.opencv.core.Rect;

/**
 * User: bsr
 * Date: 14/03/15
 * Time: 15:41
 */
public class AboveMembership extends BaseMembership{

    private static AboveMembership instance = null;


    /**
     * To prevent object creation
     */
    private AboveMembership() {

    }

    public static AboveMembership getInstance() {

        if (instance == null) {
            synchronized (AboveMembership.class) {
                if (instance == null) {
                    instance = new AboveMembership();
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

        if (0 < teta && teta < 180)
            return Math.sin(Math.toRadians(teta));

        return 0.0;
    }
}
