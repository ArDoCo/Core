package edu.kit.ipd.consistency_analyzer.modelproviders;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.kit.ipd.consistency_analyzer.datastructures.IInstance;
import edu.kit.ipd.consistency_analyzer.datastructures.IRelation;
import edu.kit.ipd.consistency_analyzer.modelproviders.HardCodedModelInput.Quadruple;
import edu.kit.ipd.consistency_analyzer.modelproviders.HardCodedModelInput.Triple;
import edu.kit.ipd.consistency_analyzer.modelproviders.exception.InconsistentModelException;
import edu.kit.ipd.constistency_analyzer.datastructures.Instance;
import edu.kit.ipd.constistency_analyzer.datastructures.Relation;

public class HardCodedModelConnector implements IModelConnector {

	HardCodedModelInput hcModel;

	public HardCodedModelConnector(HardCodedModelInput hcModel) {
		this.hcModel = hcModel;
	}

	@Override
	public List<IInstance> getInstances() {
		List<IInstance> instances = new ArrayList<>();
		for (Triple<String, String, String> inst : hcModel.getInstances()) {
			instances.add(new Instance(inst.getFirst(), inst.getSecond(), inst.getThird()));
		}
		return instances;
	}

	@Override
	public List<IRelation> getRelations(List<IInstance> instances) throws InconsistentModelException {
		List<ModeledRelation> modeledRelations = loadRelationsFromModel();

		return matchModeledRelationsWithInstances(instances, modeledRelations);
	}

	private List<ModeledRelation> loadRelationsFromModel() {
		List<ModeledRelation> modeledRelations = new ArrayList<>();
		for (Quadruple<String, String, String, String> rel : hcModel.getRelations()) {
			modeledRelations.add(new ModeledRelation(rel.getFirst(), rel.getSecond(), rel.getThird(), rel.getFourth()));
		}
		return modeledRelations;
	}

	private List<IRelation> matchModeledRelationsWithInstances(List<IInstance> instances, List<ModeledRelation> modeledRelations) throws InconsistentModelException {

		List<IRelation> relations = new ArrayList<>();

		for (ModeledRelation mRel : modeledRelations) {
			List<String> instanceUIDs = mRel.getUIDsOfInstances();

			IRelation rel = new Relation(getInstanceByUID(instances, instanceUIDs.get(0)), getInstanceByUID(instances, instanceUIDs.get(1)), mRel.getType(), mRel.getUID());

			if (instanceUIDs.size() > 2) {
				List<IInstance> additionalInstances = new ArrayList<>();
				for (int i = 2; i < instanceUIDs.size(); i++) {
					additionalInstances.add(getInstanceByUID(instances, instanceUIDs.get(i)));
				}
				rel.addOtherInstances(additionalInstances);
			}
			relations.add(rel);

		}

		return relations;

	}

	private IInstance getInstanceByUID(List<IInstance> instances, String uid) throws InconsistentModelException {
		List<IInstance> matchedInstances = instances.stream().filter(i -> i.getUid().equals(uid)).collect(Collectors.toList());

		if (matchedInstances.size() == 1) {
			return matchedInstances.get(0);
		} else {
			throw new InconsistentModelException("UID has been found " + matchedInstances.size() + " times.");
		}

	}

	private class ModeledRelation {

		private List<String> uidsOfInstances = new ArrayList<>();
		private String relationType;
		private String uid;

		public ModeledRelation(String uidInstance0, String uidInstance1, String relationType, String uid) {
			uidsOfInstances.add(uidInstance0);
			uidsOfInstances.add(uidInstance1);
			this.relationType = relationType;
			this.uid = uid;
		}

		public List<String> getUIDsOfInstances() {
			return uidsOfInstances;
		}

		public String getType() {
			return relationType;
		}

		public String getUID() {
			return uid;
		}
	}

}
