package edu.kit.ipd.consistency_analyzer.agents;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public abstract class Loader {

	private Loader() {
		throw new IllegalAccessError();
	}

	public static <A extends ILoadable> Map<String, A> loadLoadable(Class<A> classA) {

		ServiceLoader<A> loader = ServiceLoader.load(classA);

		Map<String, A> loads = new HashMap<>();

		for (A a : loader) {
			loads.put(a.getName(), a);
		}

		return loads;

	}

}
