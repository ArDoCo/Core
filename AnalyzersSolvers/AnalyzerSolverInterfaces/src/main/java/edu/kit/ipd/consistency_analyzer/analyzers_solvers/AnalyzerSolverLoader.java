package edu.kit.ipd.consistency_analyzer.analyzers_solvers;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public class AnalyzerSolverLoader {

	public static <A extends ILoadable> Map<String, A> loadLoadable(Class<A> classA) {

		ServiceLoader<A> loader = ServiceLoader.load(classA);

		Map<String, A> analyzers = new HashMap<>();

		for (A a : loader) {
			analyzers.put(a.getName(), a);
		}

		return analyzers;

	}

}
