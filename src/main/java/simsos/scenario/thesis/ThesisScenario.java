package simsos.scenario.thesis;

import simsos.scenario.thesis.entity.Ambulance;
import simsos.scenario.thesis.entity.ControlTower;
import simsos.scenario.thesis.entity.FireFighter;
import simsos.scenario.thesis.entity.Hospital;
import simsos.simulation.component.Scenario;

public class ThesisScenario extends Scenario {
    public enum SoSType {Directed, Acknowledged, Collaborative, Virtual}

    public ThesisScenario(SoSType type) {
        this.world = new ThesisWorld();

        this.world.addAgent(new ControlTower(this.world, "ControlTower"));
        this.world.addAgent(new FireFighter(this.world, "FireFighter1"));
        this.world.addAgent(new Ambulance(this.world, "Ambulance1"));
        this.world.addAgent(new Hospital(this.world, "Hospital1"));

        this.checker = null;
    }
}
