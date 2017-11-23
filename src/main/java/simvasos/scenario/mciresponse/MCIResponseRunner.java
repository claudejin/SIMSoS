package simvasos.scenario.mciresponse;

import simvasos.simulation.Simulator;
import simvasos.simulation.analysis.Snapshot;
import simvasos.simulation.component.Scenario;
import simvasos.scenario.mciresponse.MCIResponseScenario.SoSType;
import simvasos.simulation.component.World;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class MCIResponseRunner {
    public static void main(String[] args) {
        SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String testSession = "ABCPlus_Rebuilt";
        int endTick = 7200;//5000 // 8000
        int minTrial = 101;
        int maxTrial = 250;//50

        try {
            File simulationLogFile = new File(String.format("traces/" + testSession + "/" + testSession + "_simulation_logs.csv"));

            BufferedWriter simulationLogWriter = new BufferedWriter(new FileWriter(simulationLogFile, true));

//            simulationLogWriter.write("nPatient,nFireFighter,SoSType,Duration,MessageCount");
//            simulationLogWriter.newLine();

            // nPatient, nFireFighter
            SoSType[] targetTypeArray = {SoSType.Virtual, SoSType.Collaborative, SoSType.Acknowledged, SoSType.Directed};
            int[] nPatientArray = {50, 100, 150, 200, 250}; //{10, 50, 100, 300, 500};
            int[] nFireFighterArray = {2, 5, 10, 25, 50}; //{2, 3, 10, 60, 100};

            ArrayList<Snapshot> trace;
            long startTime;
            long duration;
            long durationSum;
            int messageCnt;
            int messageCntSum;
            int goalSum;

            for (int nPatient : nPatientArray) {
                for (int nFireFighter : nFireFighterArray) {
                    for (SoSType sostype : targetTypeArray) {
                        System.out.println("Patient: " + nPatient + ", Firefighter: " + nFireFighter + ", SoS: " + sostype);
                        System.out.println(datetimeFormat.format(new Date()));

                        Scenario scenario = new MCIResponseScenario(sostype, nPatient, nFireFighter, 0, 0);
                        World world = scenario.getWorld();

                        durationSum = 0;
                        messageCntSum = 0;
                        goalSum = 0;
                        for (int i = minTrial - 1; i <= maxTrial; i++) {
                            world.setSeed(new Random().nextLong());
                            ((MCIResponseWorld) world).setSoSType(sostype);

                            startTime = System.currentTimeMillis();

                            trace = Simulator.execute(world, endTick);

                            if (i == minTrial - 1)
                                continue;

                            duration = (System.currentTimeMillis() - startTime);
                            durationSum += duration;
                            goalSum += (int) world.getCurrentSnapshot().getProperties().get(0).value;
                            messageCnt = (int) world.getCurrentSnapshot().getProperties().get(1).value;
                            messageCntSum += messageCnt;

                            simulationLogWriter.write(nPatient + "," + nFireFighter + "," + sostype.toString() + "," + duration + "," + messageCnt);
                            simulationLogWriter.newLine();

                            writeTrace(trace, String.format("traces/%s/%04d_%03d_%s_%04d.txt", testSession, nPatient, nFireFighter, sostype, i));
                        }

                        simulationLogWriter.flush();
                        System.out.println("Average duration: " + durationSum / (maxTrial - minTrial + 1));
                        System.out.println("Average goalAchievement: " + goalSum * 100.0 / nPatient / (maxTrial - minTrial + 1));
                        System.out.println("Average messageCnt: " + messageCntSum / (maxTrial - minTrial + 1));
                        System.out.println("");
                    }
                }
            }

            simulationLogWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Session complete: " + datetimeFormat.format(new Date()));
    }

    private static void writeTrace(List<Snapshot> trace, String filename) {
        try {
            File file = new File(filename);
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));

            for (Snapshot snapshot : trace) {
                bw.write((snapshot.getProperties().get(0).value).toString());
                bw.newLine();
            }

            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
