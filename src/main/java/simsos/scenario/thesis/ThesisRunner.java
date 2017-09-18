package simsos.scenario.thesis;

import simsos.scenario.thesis.util.Patient;
import simsos.simulation.Simulator;
import simsos.simulation.analysis.PropertyValue;
import simsos.simulation.analysis.Snapshot;
import simsos.simulation.component.Scenario;
import simsos.simulation.component.World;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class ThesisRunner {
    public static void main(String[] args) {
        int endTick = 8000;

        int nPatient = 100;
        int nFireFighter = 3;

        int cntFailure;
        int minTrial = 1;
        int maxTrial = 5000;

        long startTime;

        try {
            File file = new File(String.format("traces/%dF_simulation_lengths.csv", nFireFighter));
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));

            // For all SoS types
            for (ThesisScenario.SoSType sostype : ThesisScenario.SoSType.values()) {
                System.out.println(sostype);
                Scenario scenario = new ThesisScenario(sostype, nPatient, nFireFighter, 0, 0);
                World world = scenario.getWorld();

                Date nowDate = new Date();
                SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String pre = transFormat.format(nowDate);
                System.out.println(pre);

//            cntFailure = 0;
                for (int i = minTrial; i <= maxTrial; i++) {
                    world.setSeed(new Random().nextLong());
                    ((ThesisWorld) world).setSoSType(sostype);

                    startTime = System.currentTimeMillis();

                    ArrayList<Snapshot> trace = Simulator.execute(world, endTick);

                    bw.write(sostype.toString() + "," + (System.currentTimeMillis() - startTime));
                    bw.newLine();
                    bw.flush();

                    writeTrace(trace, String.format("traces/%dF/%s_%dF_%04d.txt", nFireFighter, sostype, nFireFighter, i));
//                if ((int) world.getCurrentSnapshot().getProperties().get(0).value != nPatient) {
//                    cntFailure++;
//                    System.out.println("Failure" + world.getCurrentSnapshot().getProperties().get(0).value);
//                }
                    if (i % 250 == 0)
                        System.out.println(i);
//                ArrayList<Patient> patients = (ArrayList<Patient>) world.getResources().get("Patients");
//                int numPulledout = 0;
//
//                for (Patient patient : patients)
//                    if (patient.getStatus() == Patient.Status.Pulledout)
//                        numPulledout++;

//                System.out.println(Math.round((float) numPulledout / patients.size() * 100));
//                if (numPulledout == patients.size())
//                    cntComplete++;
                }
                System.out.println("Done!");
//            System.out.println("Completeness: " + (maxTrial - cntFailure) + "/" + maxTrial);
            }

            bw.close();
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
