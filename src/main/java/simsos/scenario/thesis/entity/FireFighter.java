package simsos.scenario.thesis.entity;

import simsos.simulation.component.Action;
import simsos.simulation.component.Agent;
import simsos.simulation.component.World;

import java.util.HashMap;

// Rescue the wounded (report their location public)

// Directed
// FF-DirectedMove: Move to the location that Manager directed
// FF-Location: Report the current location
// FF-Report: Report the location of a wounded person to Manager

// Acknowledged
// FF-DirectedMove
// FF-AutonMove: Move to the most likely location to find
// FF-Location
// FF-Share: Share the last wounded map (including updated time & found number of each map point) to other FFs
// FF-Report

// Collaborative
// FF-AutonMove
// FF-Share

// Virtual
// FF-AutonMove

public class FireFighter extends Agent {

    public FireFighter(World world, String name) {
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
