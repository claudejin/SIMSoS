package simsos.scenario.thesis.entity;

import simsos.simulation.component.Action;
import simsos.simulation.component.Agent;
import simsos.simulation.component.World;

import java.util.HashMap;

// Treat patients to recover

// Directed
// HS-Directed:
// HS-Report: Report the number of currently available beds

// Acknowledged
// HS-Directed:
// HS-Auton:
// HS-Share: Share the number of currently available beds to AMs
// HS-Report

// Collaborative
// HS-Auton
// HS-Share

// Virtual
// HS-Auton

public class Hospital extends Agent {

    public Hospital(World world, String name) {
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
