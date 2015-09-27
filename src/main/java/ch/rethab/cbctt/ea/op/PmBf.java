package ch.rethab.cbctt.ea.op;

import ch.rethab.cbctt.meta.ParametrizationPhenotype;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variable;
import org.moeaframework.core.Variation;
import org.moeaframework.core.variable.BinaryVariable;
import org.moeaframework.core.variable.RealVariable;

/**
 *
 * Combination of pm and bit flip for mixed type mutation.
 *
 * @author Most methods of this class were copied from
 * the MOEA Framework ((c) 2009 - 2015 David Hadka).
 *
 * @author Reto Habluetzel, 2015
 */
public class PmBf implements Variation {

    /**
     * The pmProbability this operator is applied to each decision variable.
     */
    private final double pmProbability;

    /**
     * The distribution index controlling the shape of the polynomial mutation.
     */
    private final double pmDistributionIndex;

    /**
     * The bfProbability of flipping a bit.
     */
    private final double bfProbability;

    public PmBf(double pmProbability, double pmDistributionIndex, double bfProbability) {
        this.pmProbability = pmProbability;
        this.pmDistributionIndex = pmDistributionIndex;
        this.bfProbability = bfProbability;
    }

    @Override
    public int getArity() {
        return 1;
    }

    @Override
    public Solution[] evolve(Solution[] parents) {
        Solution result = parents[0].copy();

        // if the population size changes, we reset the upper bound for the offspring size
        int popSize = -1;
        for (int i = 0; i < result.getNumberOfVariables(); i++) {
            Variable variable = result.getVariable(i);

            if (variable instanceof RealVariable) {

                RealVariable rv = (RealVariable) variable;
                if (i == ParametrizationPhenotype.ARCHIVE_SIZE_IDX) {
                    if (popSize == -1) throw new IllegalStateException("Population size should have been set before");
                    double newVal = Math.min(rv.getValue(), popSize); // must be reduce if population size is reduced
                    rv = new RealVariable(newVal, rv.getLowerBound(), popSize);
                    result.setVariable(i, rv);
                }

                if (PRNG.nextDouble() <= pmProbability) {
                    evolveReal(rv, pmDistributionIndex);
                }

                if (i == ParametrizationPhenotype.POPULATION_SIZE_IDX) {
                    popSize = (int) rv.getValue();
                }

            } else if (variable instanceof BinaryVariable) {

                evolve(i, (BinaryVariable)variable, bfProbability);
            }
        }

        return new Solution[] { result };
    }

    /**
     * Mutates the specified variable using bit flip mutation.
     *
     * @param variable the variable to be mutated
     */
    public static void evolve(int idx, BinaryVariable variable, double probability) {
        for (int i = 0; i < variable.getNumberOfBits(); i++) {
            if (PRNG.nextDouble() <= probability) {
                variable.set(i, !variable.get(i));

                // make sure there is always one variator by resetting
                if (idx == ParametrizationPhenotype.VARIATOR_IDX) {
                    if (allZeroes(variable)) {
                        variable.set(i, !variable.get(i));
                    }
                }
            }
        }
    }

    private static boolean allZeroes(BinaryVariable v) {
        return v.getBitSet().nextSetBit(0) == -1;
    }

	/*
	 * The following source code is modified from the DTLZ variator module for
	 * PISA. This implementation was chosen over Kalyanmoy Deb's original PM
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
     * Mutates the specified variable using polynomial mutation.
     *
     * @param v the variable to be mutated
     * @param distributionIndex the distribution index controlling the shape of
     *        the polynomial mutation
     */
    public static void evolveReal(RealVariable v, double distributionIndex) {
        double u = PRNG.nextDouble();
        double x = v.getValue();
        double lb = v.getLowerBound();
        double ub = v.getUpperBound();
        double dx = ub - lb;
        double delta;

        if (lb == ub) {
            v.setValue(lb);
            return;
        }

        if (u < 0.5) {
            double bu = (x - lb) / dx;
            double b = 2 * u + (1 - 2 * u)
                    * (Math.pow(1 - bu, (distributionIndex + 1)));
            delta = Math.pow(b, (1.0 / (distributionIndex + 1))) - 1.0;
        } else {
            double bu = (ub - x) / dx;
            double b = 2 * (1 - u) + 2 * (u - 0.5)
                    * (Math.pow(1 - bu, (distributionIndex + 1)));
            delta = 1.0 - Math.pow(b, (1.0 / (distributionIndex + 1)));
        }

        x = x + delta * dx;

        if (x < lb) {
            x = lb;
        } else if (x > ub) {
            x = ub;
        }

        v.setValue(x);
    }
}
