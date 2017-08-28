package simsos.scenario.robot;

import simsos.simulation.component.Action;
import simsos.simulation.component.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by mgjin on 2017-06-27.
 */
public class RobotWorld extends World {
    public RobotWorld() {
        super(new Random().nextLong());
    }
    @Override
    public HashMap<String, Object> getResources() {
        return new HashMap<String, Object>();
    }

    @Override
    public ArrayList<Action> generateExogenousActions() {
        return new ArrayList<Action>();
    }
}
