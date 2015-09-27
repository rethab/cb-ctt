# Benchmarks Version A:


### Parameters run-a-00 / run-a-01
|Parameter     |Setting                                      |
|--------------|---------------------------------------------|
|PopulationSize|50                                           |
|OffspringSize |50                                           |
|k             |3                                            |
|Crossover     |CourseBased, Curriculum, Sector(sectorSize=3)|
|Mutation      |CourseBased(prob=1.000)                      |

### Parameters run-a-02
|Parameter     |Setting                                      |
|--------------|---------------------------------------------|
|PopulationSize|50                                           |
|OffspringSize |50                                           |
|k             |3                                            |
|Crossover     |CourseBased, Curriculum, Sector(sectorSize=3)|
|Mutation      |CourseBased(prob=0.100)                      |

### Parameters run-a-03
|Parameter     |Setting                                      |
|--------------|---------------------------------------------|
|PopulationSize|50                                           |
|OffspringSize |50                                           |
|k             |3                                            |
|Crossover     |Sector(sectorSize=3)                         |
|Mutation      |CourseBased(prob=0.100)                      |

### Parameters run-a-04
|Parameter     |Setting                                      |
|--------------|---------------------------------------------|
|PopulationSize|50                                           |
|OffspringSize |50                                           |
|k             |3                                            |
|Crossover     |Sector(sectorSize=10)                        |
|Mutation      |CourseBased(prob=0.100)                      |


### Plot
- X Axis: Number of Generations
- Y Axis: Number of Soft-Constraint Violations

![Run A Plot](run-a.png)
