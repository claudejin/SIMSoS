package simsos.scenario.thesis;

import simsos.simulation.component.Action;
import simsos.simulation.component.World;

import java.util.ArrayList;

// Map, Comm. Channel, The wounded

// - Maintain CSs’ current locations (for simulation log only)
// - Maintain reported wounded persons (for simulation log only)

public class ThesisWorld extends World {
    @Override
    public ArrayList<Action> generateExogenousActions() {
        return new ArrayList<Action>();
    }
}
