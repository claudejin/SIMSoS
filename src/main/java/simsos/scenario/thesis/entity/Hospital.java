package simsos.scenario.thesis.entity;

import simsos.scenario.thesis.ThesisWorld;
import simsos.scenario.thesis.util.Location;
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

public class Hospital extends RationalEconomicCS {

    private Location location;

    public Hospital(World world, String name) {
        super(world);

        this.name = name;
        this.reset();
    }

    @Override
    protected void observeEnvironment() {

    }

    @Override
    protected void consumeInformation() {

    }

    @Override
    protected void generateActiveImmediateActions() {

    }

    @Override
    protected void generatePassiveImmediateActions() {

    }

    @Override
    protected void generateNormalActions() {

    }

    @Override
    public void reset() {
        this.location = new Location(ThesisWorld.MAP_SIZE.getLeft() / 2, ThesisWorld.MAP_SIZE.getRight() / 2);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getSymbol() {
        return this.name.replace("Hospital", "H");
    }

    @Override
    public HashMap<String, Object> getProperties() {
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put("Location", this.location);
        return properties;
    }
}
