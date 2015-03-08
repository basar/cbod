package net.bsrc.cbod;

import net.bsrc.cbod.core.util.CBODUtil;
import net.bsrc.cbod.opencv.OpenCV;
import org.junit.Test;
import org.opencv.core.Point;
import org.opencv.core.Rect;

import java.util.ArrayList;
import java.util.List;

/**
 * User: bsr Date: 06/10/14 Time: 19:00
 */
public class UtilTest {


    @Test
    public void testUtil(){


        Rect r1 = OpenCV.createRect(16,4,10,4);
        Rect r2 = OpenCV.createRect(4,5,8,6);

        System.out.println(OpenCV.getMinimumDistance(r2,r1));


    }


	public void testListConcat() {

		List<Double> a1 = new ArrayList<Double>();

		for (int i = 0; i < 10; i++) {
			a1.add((double) i);
		}

		List<Double> a2 = new ArrayList<Double>();
		for (int i = 10; i > 0; i--) {
			a2.add((double) i);
		}

		List<List<Double>> aa = new ArrayList<List<Double>>();
		aa.add(a1);
		aa.add(a2);

		List<Double> b1 = new ArrayList<Double>();

		for (int i = 10; i < 15; i++) {
			b1.add((double) i);
		}

		List<Double> b2 = new ArrayList<Double>();

		for (int i = 20; i > 15; i--) {
			b2.add((double) i);
		}

		List<List<Double>> bb = new ArrayList<List<Double>>();
		bb.add(b1);
		bb.add(b2);

		System.out.println(aa.toString());
		System.out.println(bb.toString());

		List<List<Double>> concated = CBODUtil.concatDataLists(aa, bb);

		System.out.println(concated.toString());

	}

}
