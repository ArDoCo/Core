package edu.kit.ipd.consistency_analyzer.modelproviders;

import java.util.List;

import edu.kit.ipd.consistency_analyzer.datastructures.IInstance;
import edu.kit.ipd.consistency_analyzer.datastructures.IRelation;
import edu.kit.ipd.consistency_analyzer.modelproviders.exception.InconsistentModelException;

public interface IModelConnector {

	// TODO: Model as Input?

	public List<IInstance> getInstances();

	public List<IRelation> getRelations(List<IInstance> instances) throws InconsistentModelException;

}
