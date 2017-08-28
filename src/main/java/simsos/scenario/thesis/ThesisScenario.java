package simsos.scenario.thesis;

import simsos.scenario.thesis.entity.Ambulance;
import simsos.scenario.thesis.entity.ControlTower;
import simsos.scenario.thesis.entity.FireFighter;
import simsos.scenario.thesis.entity.Hospital;
import simsos.simulation.component.Scenario;

public class ThesisScenario extends Scenario {
    public enum SoSType {Directed, Acknowledged, Collaborative, Virtual}

    public ThesisScenario(SoSType type, int nPatient, int nFireFighter, int nAmbulance, int nHospital) {
        this.world = new ThesisWorld(type, nPatient);

//        this.world.addAgent(new ControlTower(this.world, "ControlTower"));

        for (int i = 1; i <= nFireFighter; i++)
            this.world.addAgent(new FireFighter(this.world, "FireFighter" + i));
        for (int i = 1; i <= nAmbulance; i++)
            this.world.addAgent(new Ambulance(this.world, "Ambulance" + i));
        for (int i = 1; i <= nHospital; i++)
            this.world.addAgent(new Hospital(this.world, "Hospital" + i));

        this.checker = null;
    }
}
