package simvasos;

import org.apache.commons.lang3.StringUtils;
import simvasos.scenario.mciresponse.MCIResponseScenario;
import simvasos.simulation.Simulator;
import simvasos.simulation.analysis.PropertyValue;
import simvasos.simulation.analysis.Snapshot;
import simvasos.simulation.component.Scenario;
import simvasos.simulation.component.World;
import simvasos.simulation.util.Pair;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

/**
 * Created by mgjin on 2017-06-12.
 */
public class SIMVASoS {
    public static void main(String[] args) {
        SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        System.out.println("Session starting: " + datetimeFormat.format(new Date()));

        Pair<Integer, Integer> mapSize = new Pair<Integer, Integer>(4, 4);
        MCIResponseScenario.PatientDistribution distPatient = MCIResponseScenario.PatientDistribution.Uniform;
        int numPatient = mapSize.getLeft() * mapSize.getRight();
        int numFirefighter = 2;
        int[] sosTypeOfFirefighter = {0, 0, 2, 0}; // virtual, collaborative, acknowledged, directed
        int numStartingPoint = 2;
        int neighborhoodRange = 1;
        int numSampling = 1000;

        int endTick = (numPatient + numFirefighter - 1) / numFirefighter;

        System.out.println("Map size: " + mapSize.getLeft() + " x " + mapSize.getRight());
        System.out.println("# Trapped people: " + numPatient + " (goals)");
        System.out.println("# Firefighter: " + numFirefighter);
        System.out.println("SoS Types: " + sosTypeOfFirefighter[0] + " virtual, " + sosTypeOfFirefighter[1] + " collaborative, " + sosTypeOfFirefighter[2] + " acknowledged, " + sosTypeOfFirefighter[3] + " directed");
        System.out.println("# Starting points: " + numStartingPoint + " different random location(s)");
        System.out.println("Neighborhood range: " + neighborhoodRange + " cell(s)");
        System.out.println("# Samples: " + numSampling);
        System.out.println("End simulation tick: " + endTick);

        Scenario scenario = new MCIResponseScenario(mapSize, distPatient, numPatient, numFirefighter, sosTypeOfFirefighter, numStartingPoint, neighborhoodRange);
        World world = scenario.getWorld();

        int goalAchievement = 0;
        int goalAchievementSum = 0;
        int perfectAchcievementCnt = 0;
        int[] goalAchievementDist = new int[mapSize.getLeft() * mapSize.getRight() + 1];

        for (int i = 0; i < numSampling; i++) {
            world.setSeed(new Random().nextInt());
            ArrayList<Snapshot> trace = Simulator.execute(world, endTick);
            ArrayList<PropertyValue> properties = trace.get(endTick).getProperties();
            goalAchievement = (int) properties.get(properties.size() - 2).value;
            goalAchievementDist[goalAchievement]++;
            goalAchievementSum += goalAchievement;
            if (goalAchievement >= (numFirefighter * endTick))
                perfectAchcievementCnt++;
        }

        printGoalAchievementDistribution(goalAchievementDist, numSampling);

        System.out.println("Average: " + 1.0 * goalAchievementSum / numSampling);

        System.out.println("\nSession complete: " + datetimeFormat.format(new Date()));
    }

    public static void printGoalAchievementDistribution(int[] goalAchievementDist, int numSampling) {
        System.out.println(StringUtils.repeat('=', 80));
        int max = 0;
        for (int i = 0; i < goalAchievementDist.length; i++) {
            System.out.println(String.format("%2d: ", i)
                    + StringUtils.repeat(
                            '*', goalAchievementDist[i] > 0 ?
                            Math.max(goalAchievementDist[i] * 100 / numSampling, 1)
                            : 0)
                    + String.format(" %.2f%%", goalAchievementDist[i] * 100.0 / numSampling));
        }
        System.out.println(StringUtils.repeat('=', 80));
    }
}
