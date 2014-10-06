package net.bsrc.cbod;


import net.bsrc.cbod.core.util.CBODUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * User: bsr
 * Date: 06/10/14
 * Time: 19:00
 */
public class UtilTest {



    @Test
    public void testListConcat(){

        List<Double> a1 = new ArrayList<Double>();

        for(int i=0;i<10;i++){
            a1.add((double)i);
        }

        List<Double> a2=new ArrayList<Double>();
        for(int i=10;i>0;i--){
           a2.add((double)i);
        }

        List<List<Double>> aa = new ArrayList<List<Double>>();
        aa.add(a1);
        aa.add(a2);

        List<Double> b1 = new ArrayList<Double>();

        for(int i=10;i<15;i++){
            b1.add((double)i);
        }

        List<Double> b2 = new ArrayList<Double>();

        for(int i=20;i>15;i--){
            b2.add((double)i);
        }



        List<List<Double>> bb = new ArrayList<List<Double>>();
        bb.add(b1);
        bb.add(b2);

        System.out.println(aa.toString());
        System.out.println(bb.toString());

        List<List<Double>> concated=CBODUtil.concatDataLists(aa, bb);

        System.out.println(concated.toString());


    }

}
