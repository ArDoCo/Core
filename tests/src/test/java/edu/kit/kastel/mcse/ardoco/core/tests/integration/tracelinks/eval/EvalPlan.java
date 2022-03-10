package edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval;

import java.util.Properties;

public class EvalPlan {

    private final String id;
    private final String group;
    private final String x;
    private final Properties properties;

    public EvalPlan(String group, String x) {
        this.id = group + "_" + x;
        this.group = group;
        this.x = x;
        this.properties = new Properties();
    }

    public String getId() {
        return id;
    }

    public String getGroup() { return group; }

    public String getX() { return x; }

    public Properties getProperties() {
        return properties;
    }

    public EvalPlan with(String key, String value) {
        this.properties.setProperty(key, value);
        return this;
    }

    public EvalPlan with(EvalPlan otherPlan) {
        for (Object otherKey : otherPlan.properties.keySet()) {
            this.properties.setProperty(otherKey.toString(), otherPlan.properties.getProperty(otherKey.toString()));
        }

        return this;
    }

}
