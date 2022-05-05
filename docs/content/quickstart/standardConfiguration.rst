Standard Configuration
=========================

.. code-block::

    ArticleTypeNameExtractor::enabled=false
    ArticleTypeNameExtractor::probability=1.000000
    ConnectionGenerator::enabledAgents=InitialConnectionAgent,ReferenceAgent,InstanceConnectionAgent
    CorefAgent::doMerging=false
    CorefAgent::enabled=false
    ExtractionDependentOccurrenceExtractor::probability=1.000000
    InDepArcsExtractor::nameOrTypeWeight=0.500000
    InDepArcsExtractor::probability=1.000000
    InconsistencyChecker::enabledAgents=InitialInconsistencyAgent,MissingModelElementInconsistencyAgent,MissingTextForModelElementInconsistencyAgent
    InitialConnectionAgent::enabledExtractors=NameTypeConnectionExtractor,ExtractionDependentOccurrenceExtractor
    InitialInconsistencyAgent::enabledFilters=RecommendedInstanceProbabilityFilter,OccasionFilter
    InitialRecommendationAgent::enabledExtractors=NameTypeExtractor
    InitialTextAgent::enabledExtractors=NounExtractor,InDepArcsExtractor,OutDepArcsExtractor,ArticleTypeNameExtractor,SeparatedNamesExtractor
    InstanceConnectionAgent::probability=1.000000
    InstanceConnectionAgent::probabilityWithoutType=0.800000
    MissingModelElementInconsistencyAgent::minSupport=1.000000
    MissingTextForModelElementInconsistencyAgent::types=BasicComponent,CompositeComponent
    MissingTextForModelElementInconsistencyAgent::whitelist=DummyRecommender,Cache
    ModelExtractionState::minTypeParts=2
    NameTypeConnectionExtractor::probability=1.000000
    NameTypeExtractor::probability=1.000000
    NounExtractor::nameOrTypeWeight=0.500000
    NounExtractor::probability=0.200000
    OccasionFilter::expectedAppearances=2
    OutDepArcsExtractor::nameOrTypeWeight=0.500000
    OutDepArcsExtractor::probability=0.800000
    PhraseAgent::phraseConfidence=0.600000
    PhraseAgent::specialNamedEntityConfidence=0.600000
    PhraseRecommendationAgent::confidence=0.800000
    RecommendationGenerator::enabledAgents=InitialRecommendationAgent,PhraseRecommendationAgent,InstanceRelationAgent
    RecommendedInstanceProbabilityFilter::dynamicThreshold=true
    RecommendedInstanceProbabilityFilter::dynamicThresholdFactor=0.700000
    RecommendedInstanceProbabilityFilter::threshold=0.600000
    RecommendedInstanceProbabilityFilter::thresholdNameAndTypeProbability=0.300000
    RecommendedInstanceProbabilityFilter::thresholdNameOrTypeProbability=0.800000
    ReferenceAgent::probability=0.750000
    SeparatedNamesExtractor::probability=0.800000
    TextExtraction::enabledAgents=InitialTextAgent,PhraseAgent,CorefAgent





