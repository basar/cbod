package net.bsrc.cbod.core;

import net.bsrc.cbod.core.model.CandidateComponent;
import org.apache.commons.lang.Validate;

/**
 * User: bsr
 * Date: 14/03/15
 * Time: 15:37
 */
public abstract class BaseMembership implements IMembershipFunction{




    @Override
    public final double calculateValue(CandidateComponent c1, CandidateComponent c2) {
        Validate.notNull(c1);
        Validate.notNull(c2);
        return calculateValue(c1.getRect(),c2.getRect());
    }

}
