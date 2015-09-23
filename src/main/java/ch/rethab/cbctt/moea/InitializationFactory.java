package ch.rethab.cbctt.moea;

import org.moeaframework.core.Initialization;

public interface InitializationFactory {

    Initialization create(int populationSize);

}