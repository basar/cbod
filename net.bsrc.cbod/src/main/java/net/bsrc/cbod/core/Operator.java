package net.bsrc.cbod.core;

/**
 * User: bsr
 * Date: 14/03/15
 * Time: 16:37
 */
public enum Operator {

    EQUAL {
        @Override
        public boolean apply(double x1, double x2) {
            return x1 == x2;
        }
    },
    BIGGER_EQUAL {
        @Override
        public boolean apply(double x1, double x2) {
            return x1 >= x2;
        }
    },
    BIGGER {
        @Override
        public boolean apply(double x1, double x2) {
            return x1 > x2;
        }
    },
    SMALLER_EQUAL {
        @Override
        public boolean apply(double x1, double x2) {
            return x1 <= x2;
        }
    },
    SMALLER {
        @Override
        public boolean apply(double x1, double x2) {
            return x1 < x2;
        }
    };


    public abstract boolean apply(double x1, double x2);
}
