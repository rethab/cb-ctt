package ch.rethab.cbctt.moea;

import org.moeaframework.core.Initialization;

import java.io.Serializable;

public interface InitializationFactory extends Serializable {

    Initialization create(int populationSize);

}