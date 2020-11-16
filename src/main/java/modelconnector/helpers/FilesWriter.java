package modelconnector.helpers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;
import modelconnector.connectionGenerator.state.ConnectionState;
import modelconnector.connectionGenerator.state.InstanceLink;
import modelconnector.connectionGenerator.state.RelationLink;
import modelconnector.modelExtractor.state.Instance;
import modelconnector.modelExtractor.state.ModelExtractionState;
import modelconnector.modelExtractor.state.Relation;
import modelconnector.recommendationGenerator.state.RecommendationState;
import modelconnector.recommendationGenerator.state.RecommendedInstance;
import modelconnector.recommendationGenerator.state.RecommendedRelation;
import modelconnector.textExtractor.state.NounMapping;
import modelconnector.textExtractor.state.TextExtractionState;

/**
 * Class encapsulating all writing/ logging methods, like writing the results,
 * or the read in sentences in a file
 *
 * @author Sophie
 *
 */
public final class FilesWriter {

	private FilesWriter() {
		throw new IllegalAccessError();
	}

	/**
	 * Writes the sentences as they are stored in the PARSE graph into a file.
	 *
	 * @param graph the graph to read from.
	 */
	public static void writeSentencesInFile(IGraph graph) {

		File debugGraphSentences = new File(ModelConnectorConfiguration.fileForInput_Path);
		try {
			if (debugGraphSentences.createNewFile()) {
				System.out.println("File created: " + debugGraphSentences.getName());

			} else {
				System.out.println("File already exists.");
			}

			FileWriter myWriter = new FileWriter(debugGraphSentences);

			int minSentenceNumber = 0;
			for (INode node : graph.getNodes()) {
				if (Integer.valueOf(node.getAttributeValue("sentenceNumber").toString()) + 1 > minSentenceNumber) {
					myWriter.append("\n" + (Integer.valueOf(node.getAttributeValue("sentenceNumber").toString()) + 1) + ": ");
					minSentenceNumber++;
				}
				myWriter.append(" " + GraphUtils.getNodeValue(node));
			}

			myWriter.close();
			System.out.println("Successfully wrote to the file.");

		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}

	}

	public static void writeEval1ToFile(IGraph graph, TextExtractionState textExtractionState, double min) {

		File debugGraphSentences = new File("evaluations/EvalA1.txt");
		try {
			if (debugGraphSentences.createNewFile()) {
				System.out.println("File created: " + debugGraphSentences.getName());

			} else {
				System.out.println("File already exists.");
			}

			FileWriter myWriter = new FileWriter(debugGraphSentences);

			int minSentenceNumber = 0;
			String value = " | ";

			for (INode node : graph.getNodes()) {
				int sentenceNo = Integer.valueOf(node.getAttributeValue("sentenceNumber").toString());

				if (sentenceNo + 1 > minSentenceNumber) {
					myWriter.append("\n" + value + "\n");
					myWriter.append("\n" + (sentenceNo + 1) + " | ");
					value = " | ";
					minSentenceNumber++;
				}

				String nodeValue = GraphUtils.getNodeValue(node);
				if (nodeValue.length() == 1 && !Character.isLetter(nodeValue.charAt(0))) {
					continue;
				}
				myWriter.append(nodeValue + " | ");
				if (textExtractionState.getNounMappingsByNode(node).isEmpty()) {
					value += "0" + " | ";
				} else {
					value += "1" + " | ";
				}
			}
			myWriter.append("\n" + value + "\n");

			myWriter.append("\n \n Processing Time in minutes: " + min);

			myWriter.close();
			System.out.println("Successfully wrote to the file.");

		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}

	}

	public static void wirteRecommendedRelationToFile(RecommendationState recommendationState) {
		File debugGraphSentences = new File("evaluations/EvalRecommendationRelations.txt");
		try {
			if (debugGraphSentences.createNewFile()) {
				System.out.println("File created: " + debugGraphSentences.getName());

			} else {
				System.out.println("File already exists.");
			}

			FileWriter myWriter = new FileWriter(debugGraphSentences);

			List<RecommendedRelation> recommendedRelations = recommendationState.getRecommendedRelations();

			Comparator<RecommendedRelation> comRecommendedRelationByName = //
					(ri1, ri2) -> ri1.getRelationInstances().get(0).getName().compareTo(ri2.getRelationInstances().get(0).getName());

			Collections.sort(recommendedRelations, comRecommendedRelationByName);

			myWriter.append("Recommended Instances | Sentences | Probability \n\n");
			for (RecommendedRelation ri : recommendedRelations) {

				String recommendationString = "";

				List<RecommendedInstance> recommendedInstances = ri.getRelationInstances();

				for (RecommendedInstance rins : recommendedInstances) {
					recommendationString += rins.getName() + " : " + rins.getType() + " , ";
				}
				recommendationString += " | ";

				List<INode> positions = ri.getNodes();
				for (INode n : positions) {
					recommendationString += Integer.valueOf(n.getAttributeValue("sentenceNumber").toString()) + 1 + ", ";
				}

				recommendationString += " | ";
				double probability = ri.getProbability();
				recommendationString += probability + "\n";

				myWriter.append(recommendationString);

			}
			myWriter.close();
			System.out.println("Successfully wrote to the file.");

		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}

	public static void writeConnectionRelationsToFile(ConnectionState connectionState) {
		File debugGraphSentences = new File("evaluations/EvalRelationLinks.txt");
		try {
			if (debugGraphSentences.createNewFile()) {
				System.out.println("File created: " + debugGraphSentences.getName());

			} else {
				System.out.println("File already exists.");
			}

			FileWriter myWriter = new FileWriter(debugGraphSentences);

			List<RelationLink> relationLinks = connectionState.getRelationLinks();

			myWriter.append("UID (Ansatz) | Model Instances | Textual Instances| Probability\n\n");

			for (RelationLink rl : relationLinks) {
				Relation modelRelation = rl.getModelRelation();
				List<Instance> modelInstances = modelRelation.getInstances();
				String modelInstanceNames = "";
				for (Instance i : modelInstances) {
					modelInstanceNames += i.getLongestName() + ":" + i.getLongestType() + ", ";
				}

				String textualInstanceNames = "";
				for (RecommendedInstance ri : rl.getTextualRelation().getRelationInstances()) {
					textualInstanceNames += ri.getName() + ":" + ri.getType() + ", ";
				}

				String relationLinkString = //
						modelRelation.getUid() + " | " + //
								modelInstanceNames + " | " + //
								textualInstanceNames + " | " + //
								rl.getProbability() + "\n";
				myWriter.append(relationLinkString);
			}

			myWriter.close();
			System.out.println("Successfully wrote to the file.");

		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}

	public static void writeConnectionsToFile(ConnectionState connectionState, double min) {
		File debugGraphSentences = new File("evaluations/EvalInstanceLinks.txt");
		try {
			if (debugGraphSentences.createNewFile()) {
				System.out.println("File created: " + debugGraphSentences.getName());

			} else {
				System.out.println("File already exists.");
			}

			FileWriter myWriter = new FileWriter(debugGraphSentences);

			List<InstanceLink> instanceLinks = connectionState.getInstanceLinks();

			myWriter.append("UID (Ansatz) | Model Name | ModelType | Textual Name| Names|Textual Type| Types |Probability \n\n");

			for (InstanceLink il : instanceLinks) {

				Set<String> names = new HashSet<>();
				List<Integer> namePositions = new ArrayList<>();
				Set<String> types = new HashSet<>();
				List<Integer> typePositions = new ArrayList<>();
				RecommendedInstance textualInstance = il.getTextualInstance();
				Instance modelInstance = il.getModelInstance();

				for (NounMapping nameMapping : textualInstance.getNameMappings()) {
					names.addAll(nameMapping.getOccurrences());
					namePositions.addAll(nameMapping.getMappingSentenceNo());
				}
				for (NounMapping typeMapping : textualInstance.getTypeMappings()) {
					types.addAll(typeMapping.getOccurrences());
					typePositions.addAll(typeMapping.getMappingSentenceNo());
				}

				String instanceLinkString = modelInstance.getUid() + " | " + modelInstance.getLongestName() + //
						" | " + String.join(", ", modelInstance.getLongestType()) + " | " + textualInstance.getName() + //
						" | " + names.toString() + " | " + textualInstance.getType() + " | " + types.toString() + " | " + //
						il.getProbability() + "\n";

				myWriter.append(instanceLinkString);
			}

			myWriter.append("\n \n Processing Time in minutes: " + min);

			myWriter.close();
			System.out.println("Successfully wrote to the file.");

		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}

	public static void writeRecommendationsToFile(RecommendationState recommendationState, double min) {

		File debugGraphSentences = new File("evaluations/EvalRecommendations.txt");
		try {
			if (debugGraphSentences.createNewFile()) {
				System.out.println("File created: " + debugGraphSentences.getName());

			} else {
				System.out.println("File already exists.");
			}

			FileWriter myWriter = new FileWriter(debugGraphSentences);

			List<RecommendedInstance> recommendedInstances = recommendationState.getRecommendedInstances();

			Comparator<RecommendedInstance> comRecommendedInstanceByName = //
					(ri1, ri2) -> ri1.getName().compareTo(ri2.getName());
			Collections.sort(recommendedInstances, comRecommendedInstanceByName);

			myWriter.append("Name | Type | Probability | nameMappings| | | | | typeMappings| | | |\n");
			myWriter.append("| | | reference | kind| occurrences | position| probability| reference | kind | occurrences | position| probability\n\n");

			for (RecommendedInstance ri : recommendedInstances) {

				String name = ri.getName();
				String type = ri.getType();
				double probability = ri.getProbability();

				List<NounMapping> nameMappings = ri.getNameMappings();
				List<NounMapping> typeMappings = ri.getTypeMappings();

				List<String> typeMappingStrings = new ArrayList<>();
				List<String> nameMappingStrings = new ArrayList<>();

				for (NounMapping nameMapping : nameMappings) {

					String nameString = "" + //
							nameMapping.getReference() + " | " + //
							nameMapping.getKind() + " | " + //
							String.join(", ", nameMapping.getOccurrences()) + " | " + //
							nameMapping.getMappingSentenceNo() + //
							" | " + nameMapping.getProbability() + "|";
					nameMappingStrings.add(nameString);
				}

				for (NounMapping typeMapping : typeMappings) {

					String typeString = "" + //
							typeMapping.getReference() + " | " + //
							typeMapping.getKind() + " | " + //
							String.join(", ", typeMapping.getOccurrences()) + " | " + //
							typeMapping.getMappingSentenceNo() + //
							" | " + typeMapping.getProbability() + "\n";
					typeMappingStrings.add(typeString);
				}

				String recommendationString = "" + name + " | " + type + " | " + probability + " |";

				if (nameMappingStrings.size() >= 1) {
					recommendationString += nameMappingStrings.get(0);

					nameMappingStrings.remove(0);
				} else {
					recommendationString += "| | | | |";
				}

				if (typeMappingStrings.size() >= 1) {
					recommendationString += typeMappingStrings.get(0);

					typeMappingStrings.remove(0);
				} else {
					recommendationString += "\n";
				}

				for (int i = 0; i < nameMappingStrings.size() && i < typeMappingStrings.size(); i++) {

					recommendationString += "| | |" + nameMappingStrings.get(i) + typeMappingStrings.get(i);
				}

				int nameAmount = nameMappingStrings.size();
				int typeAmount = typeMappingStrings.size();

				if (nameAmount > 0 && typeAmount > nameAmount) {
					for (int i = nameAmount - 1; i < typeAmount; i++) {
						recommendationString += "| | | | | | | |" + typeMappingStrings.get(i);
					}
				} else if (typeAmount > 0 && nameAmount > typeAmount) {
					for (int i = typeAmount - 1; i < nameAmount; i++) {
						recommendationString += "| | | " + nameMappingStrings.get(i) + "\n";
					}
				}

				myWriter.append(recommendationString);

			}

			myWriter.append("\n \n Processing Time in minutes: " + min);

			myWriter.close();
			System.out.println("Successfully wrote to the file.");

		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}

	}

	/***
	 * Writes the states into a file
	 *
	 * @param extractionState     the extraction state, containing the extracted
	 *                            elements of the model
	 * @param ntrState            the name type relation state, containing the
	 *                            mappings found in the text, sorted in name, type
	 *                            or name_or_type
	 * @param recommendationState the supposing state, containing the supposing
	 *                            mappings for instances, as well as relations
	 * @param connectionState     containing all instances and relations, matched by
	 *                            supposed mappings
	 * @param durationInMinutes   past time in minutes the approach neeeded to
	 *                            calculate the results
	 */
	public static void writeStatesToFile(ModelExtractionState extractionState, TextExtractionState ntrState, //
			RecommendationState recommendationState, ConnectionState connectionState, double durationInMinutes) {

		File resultFile = new File(ModelConnectorConfiguration.fileForResults_Path);

		try {
			if (resultFile.createNewFile()) {
				System.out.println("File created: " + resultFile.getName());

			} else {
				System.out.println("File already exists.");
			}

			FileWriter myWriter = new FileWriter(resultFile);

			myWriter.write("Results of ModelConnector: \n");
			myWriter.write("---------------------------------------------------------------------------------------------------------------------------------------------\n\n");

			myWriter.write("ExtractorState: \n");
			myWriter.write(extractionState.toString());
			myWriter.write("---------------------------------------------------------------------------------------------------------------------------------------------\n\n");

			myWriter.write("FoundNames as Set: \n");
			List<String> nameList = ntrState.getNameList();
			Collections.sort(nameList);
			myWriter.write(nameList.toString() + "\n");
			myWriter.write("---------------------------------------------------------------------------------------------------------------------------------------------\n\n");

			myWriter.write("FoundNameTerms as Set: \n");
			List<String> nameTermList = ntrState.getNameTermList();
			Collections.sort(nameTermList);
			myWriter.write(nameTermList.toString() + "\n");
			myWriter.write("---------------------------------------------------------------------------------------------------------------------------------------------\n\n");

			myWriter.write("FoundNORTs as Set: \n");
			List<String> nortList = ntrState.getNortList();
			Collections.sort(nortList);
			myWriter.write(nortList.toString() + "\n");
			myWriter.write("---------------------------------------------------------------------------------------------------------------------------------------------\n\n");

			myWriter.write("FoundTypes as Set: \n");
			List<String> typeList = ntrState.getTypeList();
			Collections.sort(typeList);
			myWriter.write(typeList.toString() + "\n");
			myWriter.write("---------------------------------------------------------------------------------------------------------------------------------------------\n\n");

			myWriter.write("FoundTypeTerms as Set: \n");
			List<String> typeTermList = ntrState.getTypeTermList();
			Collections.sort(typeTermList);
			myWriter.write(typeTermList.toString() + "\n");
			myWriter.write("---------------------------------------------------------------------------------------------------------------------------------------------\n\n");

			myWriter.write("Instances of the Recommendation State: \n");

			List<RecommendedInstance> recommendedInstances = recommendationState.getRecommendedInstances();
			Comparator<RecommendedInstance> comRecommendedInstanceByName = //
					(ri1, ri2) -> ri1.getName().compareTo(ri2.getName());
			Collections.sort(recommendedInstances, comRecommendedInstanceByName);

			for (RecommendedInstance ri : recommendedInstances) {
				myWriter.write(ri.toString() + "\n");
			}
			myWriter.write("---------------------------------------------------------------------------------------------------------------------------------------------\n\n");

			myWriter.write("Instances of the Connection State: \n");

			Comparator<InstanceLink> compInstByUID = (i1, i2) -> Integer.compare(i1.getModelInstance().getUid(), (i2.getModelInstance().getUid()));
			List<InstanceLink> instanceMappings = new ArrayList<>(connectionState.getInstanceLinks());
			Collections.sort(instanceMappings, compInstByUID);

			for (InstanceLink imap : instanceMappings) {

				myWriter.write(imap.toString() + "\n");
			}

			myWriter.write("---------------------------------------------------------------------------------------------------------------------------------------------\n\n");

			myWriter.write("Relations of the Recommendation State: \n");

			List<RecommendedRelation> rels = recommendationState.getRecommendedRelations();
			Comparator<RecommendedRelation> compRRelationsByFirstInstanceName = //
					(rl1, rl2) -> rl1.getRelationInstances().get(0).getName().compareTo(rl2.getRelationInstances().get(0).getName());
			Collections.sort(rels, compRRelationsByFirstInstanceName);

			for (RecommendedRelation si : rels) {
				myWriter.write(si.toString() + "\n");
			}

			myWriter.write("---------------------------------------------------------------------------------------------------------------------------------------------\n\n");

			myWriter.write("Relations of the Connection State: \n");

			Comparator<RelationLink> compRelByUID = (i1, i2) -> Integer.compare(i1.getModelRelation().getUid(), (i2.getModelRelation().getUid()));
			List<RelationLink> relationLinks = new ArrayList<>(connectionState.getRelationLinks());
			Collections.sort(relationLinks, compRelByUID);

			for (RelationLink rlink : relationLinks) {
				myWriter.write(rlink.toString() + "\n");
			}

			myWriter.write("---------------------------------------------------------------------------------------------------------------------------------------------\n\n");
			myWriter.write("---------------------------------------------------------------------------------------------------------------------------------------------\n\n");
			myWriter.write("ExecutionTime in minutes: " + durationInMinutes);
			myWriter.write("---------------------------------------------------------------------------------------------------------------------------------------------\n\n");
			myWriter.write("---------------------------------------------------------------------------------------------------------------------------------------------\n\n");

			myWriter.close();
			System.out.println("Successfully wrote to the file.");

		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}

	}

}
