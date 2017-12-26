package simvasos.scenario.mciresponse;

import simvasos.scenario.mciresponse.entity.ControlTower;
import simvasos.scenario.mciresponse.entity.FireFighter;
import simvasos.simulation.component.Scenario;
import simvasos.simulation.util.Pair;

public class MCIResponseScenario extends Scenario {
    public enum PatientDistribution {Normal, Uniform}

    public MCIResponseScenario(Pair<Integer, Integer> mapSize, PatientDistribution distPatient, int numPatient, int numFirefighter, int[] sosTypeOfFirefighter, int numStartingPoint, int neighborhoodRange) {
        // Initializing the world
        this.world = new MCIResponseWorld(mapSize, distPatient, numPatient, numStartingPoint, neighborhoodRange);

        if (sosTypeOfFirefighter[2] + sosTypeOfFirefighter[3] > 0)
            this.world.addAgent(new ControlTower(this.world, "ControlTower"));

        // Initializing the CSs
        int initializedCnt = 0;
        for (int i = 0; i < numFirefighter; i++) {
            FireFighter ff = new FireFighter(this.world, "FireFighter" + (i + 1));
            if (sosTypeOfFirefighter[0] > 0) {
                sosTypeOfFirefighter[0] -= 1;
                initializedCnt++;
                ff.setSosType(SoSType.Virtual);
            } else if (sosTypeOfFirefighter[1] > 0) {
                sosTypeOfFirefighter[1] -= 1;
                initializedCnt++;
                ff.setSosType(SoSType.Collaborative);
            } else if (sosTypeOfFirefighter[2] > 0) {
                sosTypeOfFirefighter[2] -= 1;
                initializedCnt++;
                ff.setSosType(SoSType.Acknowledged);
            } else if (sosTypeOfFirefighter[3] > 0) {
                sosTypeOfFirefighter[3] -= 1;
                initializedCnt++;
                ff.setSosType(SoSType.Directed);
            } else {
                System.exit(1); // Error: Not matched
            }

            this.world.addAgent(ff);
        }

        if (initializedCnt != numFirefighter)
            System.exit(1); // Error: Not matched

        this.checker = new PulloutChecker(numPatient);
    }
}
