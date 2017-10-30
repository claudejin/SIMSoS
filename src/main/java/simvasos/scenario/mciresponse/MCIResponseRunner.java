package simvasos.scenario.mciresponse;

import simvasos.simulation.Simulator;
import simvasos.simulation.analysis.Snapshot;
import simvasos.simulation.component.Scenario;
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
        int endTick = 8000;

        int nPatient = 100;
        int nFireFighter = 3;

        int cntFailure;
        int minTrial = 1;
        int maxTrial = 100;

        long startTime;
        long duration;

        try {
            File file = new File(String.format("traces/%dF_simulation_lengths.csv", nFireFighter));
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));

            File file2 = new File(String.format("traces/%dF_simulation_messageCnt.csv", nFireFighter));
            BufferedWriter bw2 = new BufferedWriter(new FileWriter(file2));

            // For all SoS types
            for (MCIResponseScenario.SoSType sostype : MCIResponseScenario.SoSType.values()) {
                if (sostype != MCIResponseScenario.SoSType.Virtual)
                    continue;
                System.out.println(sostype);
                Scenario scenario = new MCIResponseScenario(sostype, nPatient, nFireFighter, 0, 0);
                World world = scenario.getWorld();

                Date nowDate = new Date();
                SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String pre = transFormat.format(nowDate);
                System.out.println(pre);

//                cntFailure = 0;
                long durationsum = 0;
                for (int i = minTrial; i <= maxTrial; i++) {
                    world.setSeed(new Random().nextLong());
                    ((MCIResponseWorld) world).setSoSType(sostype);

                    startTime = System.currentTimeMillis();

                    ArrayList<Snapshot> trace = Simulator.execute(world, endTick);

                    duration = (System.currentTimeMillis() - startTime);
                    durationsum += duration;
                    bw.write(sostype.toString() + "," + duration);
                    bw.newLine();
                    bw.flush();
                    bw2.write(sostype.toString() + "," + world.getCurrentSnapshot().getProperties().get(1).value);
                    bw2.newLine();
                    bw.flush();

                    writeTrace(trace, String.format("traces/%dF/%s_%dF_%04d.txt", nFireFighter, sostype, nFireFighter, i));
//                    if ((int) world.getCurrentSnapshot().getProperties().get(0).value != nPatient) {
//                        cntFailure++;
//                        System.out.println("Failure" + world.getCurrentSnapshot().getProperties().get(0).value);
//                    }
                    if (i % 250 == 0)
                        System.out.println("> Trace collected: " + i);
//                ArrayList<Patient> patients = (ArrayList<Patient>) world.getResources().get("Patients");
//                int numPulledout = 0;
//
//                for (Patient patient : patients)
//                    if (patient.getStatus() == Patient.Status.Pulledout)
//                        numPulledout++;

//                System.out.println(world.getCurrentSnapshot().getProperties().get(0).value + ", duration: " + duration);
//                if (numPulledout == patients.size())
//                    cntComplete++;
                }
                System.out.println("Done!");
//                System.out.println("Completeness: " + (maxTrial - cntFailure) + "/" + maxTrial);
                System.out.println("Average duration: " + durationsum / (maxTrial - minTrial + 1));
            }

            bw.close();
            bw2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Date nowDate = new Date();
        SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String pre = transFormat.format(nowDate);
        System.out.println(pre);
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
