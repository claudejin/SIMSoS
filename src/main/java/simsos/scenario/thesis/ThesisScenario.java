package simsos.scenario.thesis;

import simsos.scenario.thesis.entity.Ambulance;
import simsos.scenario.thesis.entity.ControlTower;
import simsos.scenario.thesis.entity.FireFighter;
import simsos.scenario.thesis.entity.Hospital;
import simsos.scenario.thesis.util.Location;
import simsos.simulation.component.Scenario;

public class ThesisScenario extends Scenario {
    public enum SoSType {Directed, Acknowledged, Collaborative, Virtual}

    public ThesisScenario(SoSType type, int nPatient, int nFireFighter, int nAmbulance, int nHospital) {
        this.world = new ThesisWorld(type, nPatient);

        for (int i = 1; i <= nFireFighter; i++)
            this.world.addAgent(new FireFighter(this.world, "FireFighter" + i));
        for (int i = 1; i <= nAmbulance; i++)
            this.world.addAgent(new Ambulance(this.world, "Ambulance" + i, new Location(ThesisWorld.MAP_SIZE.getLeft() / 2, ThesisWorld.MAP_SIZE.getRight() / 2)));
//        this.world.addAgent(new Ambulance(this.world, "Ambulance1", new Location(ThesisWorld.MAP_SIZE.getLeft() / 2, ThesisWorld.MAP_SIZE.getRight() / 2)));
//        this.world.addAgent(new Ambulance(this.world, "Ambulance1", new Location(ThesisWorld.MAP_SIZE.getLeft() / 2-2, ThesisWorld.MAP_SIZE.getRight() / 2-2)));
//        this.world.addAgent(new Ambulance(this.world, "Ambulance2", new Location(ThesisWorld.MAP_SIZE.getLeft() / 2+1, ThesisWorld.MAP_SIZE.getRight() / 2+1)));
        for (int i = 1; i <= nHospital; i++)
            this.world.addAgent(new Hospital(this.world, "Hospital" + i, new Location(ThesisWorld.MAP_SIZE.getLeft() / 2, ThesisWorld.MAP_SIZE.getRight() / 2), 32));
//        this.world.addAgent(new Hospital(this.world, "Hospital1", new Location(ThesisWorld.MAP_SIZE.getLeft() / 2-2, ThesisWorld.MAP_SIZE.getRight() / 2-2), 16));
//        this.world.addAgent(new Hospital(this.world, "Hospital2", new Location(ThesisWorld.MAP_SIZE.getLeft() / 2+1, ThesisWorld.MAP_SIZE.getRight() / 2+1), 16));

        if (type != SoSType.Virtual)
            this.world.addAgent(new ControlTower(this.world, "ControlTower"));

        this.checker = null;
    }
}
