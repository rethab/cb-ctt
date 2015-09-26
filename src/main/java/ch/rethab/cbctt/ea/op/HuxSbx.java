package ch.rethab.cbctt.ea.op;

import ch.rethab.cbctt.meta.ParametrizationPhenotype;
import jmetal.encodings.variable.Real;
import org.moeaframework.core.*;
import org.moeaframework.core.variable.BinaryVariable;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.core.variable.RealVariable;

/**
 *
 * Combination of hux and sbx for mixed type variation.
 *
 * @author Most methods of this class were copied from
 * the MOEA Framework ((c) 2009 - 2015 David Hadka).
 *
 * @author Reto Habluetzel, 2015
 */
public class HuxSbx implements Variation {

    /**
     * The huxProbability of applying this operator.
     */
    private final double huxProbability;

    /**
     * The probability of applying this SBX operator to each variable.
     */
    private final double sbxProbability;

    /**
     * The distribution index of this SBX operator.
     */
    private final double sbxDistributionIndex;

    public HuxSbx(double huxProbability, double sbxProbability, double sbxDistributionIndex) {
        this.huxProbability = huxProbability;
        this.sbxProbability = sbxProbability;
        this.sbxDistributionIndex = sbxDistributionIndex;
    }

    @Override
    public int getArity() {
        return 2;
    }

    @Override
    public Solution[] evolve(Solution[] parents) {
        Solution result1 = parents[0].copy();
        Solution result2 = parents[1].copy();

        // sbx probability is outside the loop
        boolean doSbx = PRNG.nextDouble() <= sbxProbability;

        /* The population size is stored, so we can dynamically adjust
         * the upper bounds of the offspring and the sector size. this
         * is required, as those exceeding the population size makes no
         * sense.
         *
         * We use the minimum population size of the two, in order to
         * make sure the bounds work for both offspring. Note that using
         * the smaller value should not reduce the domain in the long
         * term, because we set this in every mutation based on the
         * current population. In other words: A concern may be that
         * reducing an upper bound by a smaller value would in the
         * long term make the upper bounds smaller and smaller and
         * thereby reduce the range the values could take.
         */
        int popSize = -1;

        for (int i = 0; i < result1.getNumberOfVariables(); i++) {
            Variable variable1 = result1.getVariable(i);
            Variable variable2 = result2.getVariable(i);

            if (variable1 instanceof BinaryVariable && variable2 instanceof BinaryVariable) {
                if (PRNG.nextDouble() <= huxProbability) {
                    evolveHux((BinaryVariable) variable1, (BinaryVariable) variable2);
                }
            } else if (variable1 instanceof RealVariable && variable2 instanceof RealVariable) {
                RealVariable rv1 = (RealVariable) variable1;
                RealVariable rv2 = (RealVariable) variable2;

                if (doSbx && PRNG.nextBoolean()) {
                    Variable[] vars = evolveSbx(i, popSize, rv1, rv2, sbxDistributionIndex);
                    result1.setVariable(i, vars[0]);
                    result2.setVariable(i, vars[1]);
                }

                // save the population upper bound so we can later re-use it for the archive size upper bound
                if (i == ParametrizationPhenotype.POPULATION_SIZE_IDX) {
                    popSize = (int) Math.min(rv1.getValue(), rv2.getValue());
                    System.out.println("Setting popSize="+popSize);
                }
            } else {
                throw new IllegalArgumentException("Unexpected variable type");
            }
        }
        return new Solution[] { result1, result2 };
    }

    /**
     * Evolves the specified variables using the HUX operator.
     *
     * @param v1 the first variable
     * @param v2 the second variable
     */
    public static void evolveHux(BinaryVariable v1, BinaryVariable v2) {
        if (v1.getNumberOfBits() != v2.getNumberOfBits()) {
            throw new FrameworkException("binary variables not same length");
        }

        for (int i = 0; i < v1.getNumberOfBits(); i++) {
            boolean value = v1.get(i);

            if ((value != v2.get(i)) && PRNG.nextBoolean()) {
                v1.set(i, !value);
                v2.set(i, value);
            }
        }
    }

	/*
	 * The following source code is modified from the DTLZ variator module for
	 * PISA. This implementation was chosen over Kalyanmoy Deb's original SBX
	 * implementation due to license incompatibilities with the LGPL. The DTLZ
	 * variator module license is provided below.
	 *
	 * Copyright (c) 2002-2003 Swiss Federal Institute of Technology,
	 * Computer Engineering and Networks Laboratory. All rights reserved.
	 *
	 * PISA - A Platform and Programming Language Independent Interface for
	 * Search Algorithms.
	 *
	 * DTLZ - Scalable Test Functions for MOEAs - A variator module for PISA
	 *
	 * Permission to use, copy, modify, and distribute this software and its
	 * documentation for any purpose, without fee, and without written
	 * agreement is hereby granted, provided that the above copyright notice
	 * and the following two paragraphs appear in all copies of this
	 * software.
	 *
	 * IN NO EVENT SHALL THE SWISS FEDERAL INSTITUTE OF TECHNOLOGY, COMPUTER
	 * ENGINEERING AND NETWORKS LABORATORY BE LIABLE TO ANY PARTY FOR DIRECT,
	 * INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF
	 * THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE SWISS
	 * FEDERAL INSTITUTE OF TECHNOLOGY, COMPUTER ENGINEERING AND NETWORKS
	 * LABORATORY HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
	 *
	 * THE SWISS FEDERAL INSTITUTE OF TECHNOLOGY, COMPUTER ENGINEERING AND
	 * NETWORKS LABORATORY, SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING,
	 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
	 * FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS
	 * ON AN "AS IS" BASIS, AND THE SWISS FEDERAL INSTITUTE OF TECHNOLOGY,
	 * COMPUTER ENGINEERING AND NETWORKS LABORATORY HAS NO OBLIGATION TO
	 * PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
	 */
    /**
     * Evolves the specified variables using the SBX operator.
     *
     * @param v1 the first variable
     * @param v2 the second variable
     * @param distributionIndex the distribution index of this SBX operator
     */
    public static Variable[] evolveSbx(int idx, int popSize, RealVariable v1, RealVariable v2,
                              double distributionIndex) {

        if (idx == ParametrizationPhenotype.ARCHIVE_SIZE_IDX || idx == ParametrizationPhenotype.SECTOR_SIZE_IDX) {
            if (popSize == -1) {
                throw new IllegalStateException("population upper bound should have been initialized before");
            }
            // give them both the higher upper bound to make sure they don't exceed them
            v1 = EncodingUtils.newReal(v1.getLowerBound(), popSize);
            v2 = EncodingUtils.newReal(v2.getLowerBound(), popSize);
        }

        System.out.println("evolveSbx: Idx: " + idx);

        double x0 = v1.getValue();
        double x1 = v2.getValue();

        double dx = Math.abs(x1 - x0);

        if (dx > Settings.EPS) {
            double lb = v1.getLowerBound();
            double ub = v1.getUpperBound();
            double bl;
            double bu;

            if (x0 < x1) {
                bl = 1 + 2 * (x0 - lb) / dx;
                bu = 1 + 2 * (ub - x1) / dx;
            } else {
                bl = 1 + 2 * (x1 - lb) / dx;
                bu = 1 + 2 * (ub - x0) / dx;
            }

            //use symmetric distributions
            if (bl < bu) {
                bu = bl;
            } else {
                bl = bu;
            }

            double p_bl = 1 - 1 / (2 * Math.pow(bl, distributionIndex + 1));
            double p_bu = 1 - 1 / (2 * Math.pow(bu, distributionIndex + 1));
            double u = PRNG.nextDouble();

            //prevent out-of-bounds values if PRNG draws the value 1.0
            if (u == 1.0) {
                u = Math.nextAfter(u, -1.0);
            }

            double u0 = u * p_bl;
            double u1 = u * p_bu;
            double b0;
            double b1;

            if (u0 <= 0.5) {
                b0 = Math.pow(2 * u0, 1 / (distributionIndex + 1));
            } else {
                b0 = Math.pow(0.5 / (1 - u0), 1 / (distributionIndex + 1));
            }

            if (u1 <= 0.5) {
                b1 = Math.pow(2 * u1, 1 / (distributionIndex + 1));
            } else {
                b1 = Math.pow(0.5 / (1 - u1), 1 / (distributionIndex + 1));
            }

            if (x0 < x1) {
                v1.setValue(0.5 * (x0 + x1 + b0 * (x0 - x1)));
                v2.setValue(0.5 * (x0 + x1 + b1 * (x1 - x0)));
            } else {
                v1.setValue(0.5 * (x0 + x1 + b1 * (x0 - x1)));
                v2.setValue(0.5 * (x0 + x1 + b0 * (x1 - x0)));
            }

            //this makes PISA's SBX compatible with other implementations
            //which swap the values
            if (PRNG.nextBoolean()) {
                double temp = v1.getValue();
                v1.setValue(v2.getValue());
                v2.setValue(temp);
            }

            //guard against out-of-bounds values
            if (v1.getValue() < lb) {
                v1.setValue(lb);
            } else if (v1.getValue() > ub) {
                v1.setValue(ub);
            }

            if (v2.getValue() < lb) {
                v2.setValue(lb);
            } else if (v2.getValue() > ub) {
                v2.setValue(ub);
            }
        }
    }
}
