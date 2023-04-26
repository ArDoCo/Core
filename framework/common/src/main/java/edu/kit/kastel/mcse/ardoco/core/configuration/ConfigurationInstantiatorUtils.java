/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.configuration;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;

public final class ConfigurationInstantiatorUtils {
    private ConfigurationInstantiatorUtils() {
        throw new IllegalAccessError();
    }

    /**
     * Create an AbstractConfigurable by Reflection.
     * 
     * @param clazz the class of the AbstractConfigurable
     * @return the abstract configurable
     * @throws InvocationTargetException if constructor execution does not work
     * @throws InstantiationException    if constructor execution does not work
     * @throws IllegalAccessException    if constructor execution does not work
     */
    public static AbstractConfigurable createObject(Class<? extends AbstractConfigurable> clazz) throws InvocationTargetException, InstantiationException,
            IllegalAccessException {
        var constructors = Arrays.asList(clazz.getDeclaredConstructors());
        AbstractConfigurable result = null;

        result = findAndCreate(constructors, c -> c.getParameterCount() == 0, new Object[0]);
        if (result != null)
            return result;

        result = findAndCreate(constructors, c -> c.getParameterCount() == 1 && c.getParameterTypes()[0] == Map.class, new Object[] { Map.of() });
        if (result != null)
            return result;

        result = findAndCreate(constructors, c -> c.getParameterCount() == 1 && c.getParameterTypes()[0] == DataRepository.class, new Object[] {
                new DataRepository() });
        if (result != null)
            return result;

        result = findAndCreate(constructors, c -> c.getParameterCount() == 2 && c.getParameterTypes()[0] == String.class && c
                .getParameterTypes()[1] == DataRepository.class, new Object[] { null, null });
        if (result != null)
            return result;

        result = findAndCreate(constructors, c -> c.getParameterCount() == 2 && c.getParameterTypes()[0] == DataRepository.class && c
                .getParameterTypes()[1] == List.class, new Object[] { null, List.of() });
        if (result != null)
            return result;

        throw new IllegalStateException("Not reachable code reached for class " + clazz.getName());

    }

    @SuppressWarnings("java:S3011")
    private static AbstractConfigurable findAndCreate(Collection<Constructor<?>> constructors, Predicate<Constructor<?>> selector, Object[] parameters)
            throws InvocationTargetException, InstantiationException, IllegalAccessException {
        if (constructors.stream().noneMatch(selector)) {
            return null;
        }
        var constructor = constructors.stream().filter(selector).findFirst().orElseThrow();
        constructor.setAccessible(true);
        return (AbstractConfigurable) constructor.newInstance(parameters);
    }
}
