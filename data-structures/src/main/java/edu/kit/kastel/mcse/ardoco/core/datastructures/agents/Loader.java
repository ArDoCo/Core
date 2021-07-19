package edu.kit.kastel.mcse.ardoco.core.datastructures.agents;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.reflections.Reflections;

/**
 * This helper class can load {@link ILoadable ILoadables}.
 */
public final class Loader {
    // Just for local debugging, as this method finds all necessary classes
    private static final boolean USE_REFLECTION = true;

    private static final Map<Class<?>, ImmutableList<Class<?>>> CACHE = new HashMap<>();

    private static final Logger logger = LogManager.getLogger(Loader.class);

    private Loader() {
        throw new IllegalAccessError();
    }

    /**
     * Load loadable of a specific class (and subclasses).
     *
     * @param <A>   the generic type of the class
     * @param clazz the class to load
     * @return the loaded classes (id - instance)
     */
    public static <A extends ILoadable> Map<String, A> loadLoadable(Class<A> clazz) {
        return USE_REFLECTION ? loadLoadableViaReflect(clazz) : loadLoadableViaServiceLoader(clazz);
    }

    /**
     * Load loadable via service loader.
     *
     * @param <A>   the class type to load
     * @param clazz the class to load
     * @return a map from name (see {@link ILoadable#getId()}) to instance
     */
    public static <A extends ILoadable> Map<String, A> loadLoadableViaServiceLoader(Class<A> clazz) {
        ServiceLoader<A> loader = ServiceLoader.load(clazz);
        Map<String, A> loads = new HashMap<>();

        for (A a : loader) {
            loads.put(a.getId(), a);
        }

        return loads;
    }

    /**
     * Load loadable via reflection. <b>Classes have to be located in/below {@link edu.kit.kastel.mcse.ardoco}</b>
     *
     * @param <A>   the class type to load
     * @param clazz the class to load
     * @return a map from name (see {@link ILoadable#getId()}) to instance
     *
     */
    public static <A extends ILoadable> Map<String, A> loadLoadableViaReflect(Class<A> clazz) {
        Map<String, A> loads = new HashMap<>();

        synchronized (CACHE) {
            if (CACHE.containsKey(clazz)) {
                Set<A> loaded = load(CACHE.get(clazz));
                for (A a : loaded) {
                    loads.put(a.getId(), a);
                }
                return loads;
            }
        }

        var reflect = new Reflections("edu.kit.kastel.mcse.ardoco");
        Set<Class<?>> subtypes = new HashSet<>(reflect.getSubTypesOf(clazz));
        subtypes.removeIf(Loader::hasNoPublicConstructor);

        synchronized (CACHE) {
            CACHE.put(clazz, Lists.immutable.withAll(subtypes));
        }

        Set<A> loaded = load(subtypes);
        for (A a : loaded) {
            loads.put(a.getId(), a);
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

        logger.warn("No Default Constructor found for {}", clazz.getSimpleName());
        return true;

    }

    private static <A extends ILoadable> Set<A> load(Iterable<Class<?>> clazzes) {
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
