package simsos.scenario.thesis.entity;

import simsos.simulation.component.Action;
import simsos.simulation.component.Agent;
import simsos.simulation.component.World;

import java.util.HashMap;

// Orchestrate CSs to achieve SoS-level goals

// Directed
// - Maintain CSs’ current locations
// - Maintain the location of reported wounded persons
// - Enforce the decisions which CS to do which action

// Acknowledged
// - Try to maintain CSs’ current locations
// - Try to maintain the location of reported wounded persons
// - Try to enforce a suggestion that which CS to do which action

// Collaborative
// - Nothing

// Virtual
// - Nothing

public class ControlTower extends Agent {

    public ControlTower(World world, String name) {
        super(world);

        this.name = name;
        this.reset();
    }

    @Override
    public Action step() {
        return Action.getNullAction(1, "null");
    }

    @Override
    public void reset() {

    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public HashMap<String, Object> getProperties() {
        return new HashMap<String, Object>();
    }
}
