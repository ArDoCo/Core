package edu.kit.kastel.mcse.ardoco.core.datastructures.agents;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;

public final class Loader {
	// Just for local debugging, as this method finds all necessary classes
	public static final boolean USE_REFLECTION = true;

	private static final Map<Class<?>, List<Class<?>>> CACHE = new HashMap<>();

	private static final Logger logger = LogManager.getLogger(Loader.class);

	private Loader() {
		throw new IllegalAccessError();
	}

	public static <A extends ILoadable> Map<String, A> loadLoadable(Class<A> classA) {
		return USE_REFLECTION ? loadLoadableViaReflect(classA) : loadLoadableViaServiceLoader(classA);
	}

	public static <A extends ILoadable> Map<String, A> loadLoadableViaServiceLoader(Class<A> classA) {
		ServiceLoader<A> loader = ServiceLoader.load(classA);
		Map<String, A> loads = new HashMap<>();

		for (A a : loader) {
			loads.put(a.getName(), a);
		}

		return loads;
	}

	public static <A extends ILoadable> Map<String, A> loadLoadableViaReflect(Class<A> classA) {
		Map<String, A> loads = new HashMap<>();

		synchronized (CACHE) {
			if (CACHE.containsKey(classA)) {
				Set<A> loaded = load(CACHE.get(classA));
				for (A a : loaded) {
					loads.put(a.getName(), a);
				}
				return loads;
			}
		}

		Reflections reflect = new Reflections("edu.kit.kastel.mcse.ardoco");
		Set<Class<?>> subtypes = new HashSet<>(reflect.getSubTypesOf(classA));
		subtypes.removeIf(Loader::hasNoPublicConstructor);

		synchronized (CACHE) {
			CACHE.put(classA, new ArrayList<>(subtypes));
		}

		Set<A> loaded = load(subtypes);
		for (A a : loaded) {
			loads.put(a.getName(), a);
		}
		return loads;
	}

	private static boolean hasNoPublicConstructor(Class<?> clazz) {
		try {
			for (Constructor<?> c : clazz.getDeclaredConstructors()) {
				if (Modifier.isPublic(c.getModifiers()) && c.getParameterCount() == 0) {
					return false;
				}
			}
		} catch (SecurityException e) {
			logger.error(e.getMessage(), e);
		}

		logger.warn("No Default Constructor found for " + clazz.getSimpleName());
		return true;

	}

	private static <A extends ILoadable> Set<A> load(Collection<Class<?>> clazzes) {
		Set<A> instances = new HashSet<>();
		for (Class<?> clazz : clazzes) {
			@SuppressWarnings("unchecked")
			A a = create((Class<? extends A>) clazz);
			if (a != null) {
				instances.add(a);
			}
		}
		return instances;
	}

	private static <A extends ILoadable> A create(Class<? extends A> clazz) {

		try {
			return clazz.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

}
