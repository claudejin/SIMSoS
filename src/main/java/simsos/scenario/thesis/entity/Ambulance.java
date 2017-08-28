package simsos.scenario.thesis.entity;

import simsos.scenario.thesis.ThesisWorld;
import simsos.scenario.thesis.util.Location;
import simsos.simulation.component.Action;
import simsos.simulation.component.Agent;
import simsos.simulation.component.World;

import java.util.HashMap;

// Transport the wounded to hospital

// Directed
// AM-DirectedMove: Move to the wounded person that Manager directed, and transport him
// AM-Location: Report the current location & status
// AM-Report: Report the transport of the wounded person to Manager

// Acknowledged
// AM-DirectedMove
// AM-AutonMove: Move to the location likely to find a wounded person, and transport him to hospital
// AM-Location
// AM-Share: Share the last wounded map (including updated time & found number of each map point) to other AMs
// AM-Report

// Collaborative
// AM-AutonMove
// AM-Share

// Virtual
// AM-AutonMove

public class Ambulance extends Agent {

    private Location location;

    public Ambulance(World world, String name) {
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
        this.location = new Location(ThesisWorld.MAP_SIZE.getLeft() / 2, ThesisWorld.MAP_SIZE.getRight() / 2);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getSymbol() {
        return this.name.replace("Ambulance", "A");
    }

    @Override
    public HashMap<String, Object> getProperties() {
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put("Location", this.location);
        return properties;
    }
}
