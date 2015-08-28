package org.moeaframework.util.distributed;

import org.moeaframework.core.Solution;

/**
 * Making FutureSolution accessible.
 *
 * @author Reto Habluetzel, 2015
 */
public class PublicFutureSolution extends FutureSolution {

    public PublicFutureSolution(Solution solution) {
        super(solution);
    }
}
