package ch.rethab.cbctt.ea.op;

import org.moeaframework.core.Variation;

import java.io.Serializable;

/**
 * @author Reto Habluetzel, 2015
 */
public interface CbcttVariation extends Variation, Serializable {

    /** Shows some pretty name of the variation plus parameters used */
    String name();

}
