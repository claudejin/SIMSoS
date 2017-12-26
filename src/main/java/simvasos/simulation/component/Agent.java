package simvasos.simulation.component;

import simvasos.simulation.analysis.HasName;

import java.util.HashMap;

/**
 * Created by mgjin on 2017-06-21.
 */
public abstract class Agent implements HasName {
    protected World world = null;
    protected String name = null;
    protected Scenario.SoSType sosType = Scenario.SoSType.Virtual;

    public Agent(World world) {
        this.world = world;
    }

    public abstract Action step();
    public abstract void reset();
    public abstract String getName();
    public abstract String setName(String name);
    public Scenario.SoSType getSoSType() {
        return this.sosType;
    }
    public void setSosType(Scenario.SoSType sosType) {
        this.sosType = sosType;
    }

    public abstract HashMap<String, Object> getProperties();
}
