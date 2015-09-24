package ch.rethab.cbctt.ea.op;

import org.moeaframework.core.Variation;

/**
 * @author Reto Habluetzel, 2015
 */
public interface CbcttVariation extends Variation {

    /** Shows some pretty name of the variation plus parameters used */
    String name();

}
