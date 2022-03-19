/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common;

import edu.kit.kastel.mcse.ardoco.core.api.agent.AbstractAgent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class AbstractConfigurable implements IConfigurable {
	protected static final String LIST_SEPARATOR = ",";

	protected final Logger logger = LogManager.getLogger(this.getClass());

	protected final <E> List<E> findByClassName(List<String> selected, List<E> instances) {
		MutableList<E> target = Lists.mutable.empty();
		for (var clazz : selected) {
			var elem = instances.stream().filter(e -> e.getClass().getSimpleName().equals(clazz)).findFirst().orElseThrow(() -> new IllegalArgumentException("Could not find " + clazz));
			target.add(elem);
		}
		return target;
	}

	@Override
	public final void applyConfiguration(Map<String, String> additionalConfiguration) {
		applyConfiguration(additionalConfiguration, this.getClass());
		delegateApplyConfigurationToInternalObjects(additionalConfiguration);
	}

	protected abstract void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration);

	private void applyConfiguration(Map<String, String> additionalConfiguration, Class<?> currentClass) {
		if (currentClass == Object.class || currentClass == AbstractAgent.class)
			return;

		var fields = currentClass.getDeclaredFields();
		for (Field field : fields) {
			if (!field.isAnnotationPresent(Configurable.class)) {
				continue;
			}
			Configurable c = field.getAnnotation(Configurable.class);
			String key = c.key().isBlank() ? (currentClass.getSimpleName() + "::" + field.getName()) : c.key();
			if (additionalConfiguration.containsKey(key)) {
				setValue(field, additionalConfiguration.get(key));
			}
		}

		applyConfiguration(additionalConfiguration, currentClass.getSuperclass());
	}

	private void setValue(Field field, String value) {
		var clazz = field.getType();
		var parsedValue = parse(clazz, value);
		if (parsedValue == null)
			return;

		try {
			field.setAccessible(true);
			field.set(this, value);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private Object parse(Class<?> clazz, String value) {
		if (clazz == Integer.class || clazz == Integer.TYPE) {
			return Integer.parseInt(value);
		}
		if (clazz == Double.class || clazz == Double.TYPE) {
			return Double.parseDouble(value);
		}
		if (List.class.isAssignableFrom(clazz) && clazz.getComponentType() == String.class) {
			return Arrays.stream(value.split(LIST_SEPARATOR)).toList();
		}

		throw new IllegalArgumentException("Could not find a parse method for fields of type: " + clazz);
	}
}
