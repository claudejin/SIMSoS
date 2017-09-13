package simsos;

import mci.Main;
import simsos.scenario.thesis.ThesisScenario.SoSType;
import simsos.scenario.thesis.ThesisScenario;
import simsos.scenario.thesis.ThesisWorld;
import simsos.scenario.thesis.util.Patient;
import simsos.simulation.Simulator;
import simsos.simulation.component.Scenario;
import simsos.simulation.component.World;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by mgjin on 2017-06-12.
 */
public class SIMSoS {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("old")) {
            String[] passedArgs = Arrays.copyOfRange(args, 1, args.length);
            try {
                Main.experimentMain(passedArgs);
            } catch (IOException e) {
                System.out.println("Error: Old version is not runnable");
                e.printStackTrace();
            } finally {
                System.exit(0);
            }
        }

        SoSType sostype = SoSType.Acknowledged;

        Scenario scenario = new ThesisScenario(sostype, 100, 3, 0, 1);
        World world = scenario.getWorld();

        for (int i = 0; i < 5000; i++) {
//            world.setSeed(1);
            world.setSeed(new Random().nextLong());
            ((ThesisWorld) world).setSoSType(sostype);
            Simulator.execute(world, 50);

            ArrayList<Patient> patients = (ArrayList<Patient>) world.getResources().get("Patients");
            int numPulledout = 0;
            int numOnTransport = 0;
            int numHospitalized = 0;
            for (Patient patient : patients) {
                if (patient.getStatus() == Patient.Status.Pulledout)
                    numPulledout++;
                else if (patient.getStatus() == Patient.Status.OnTransport)
                    numOnTransport++;
                else if (patient.getStatus() == Patient.Status.Hospitalized)
                    numHospitalized++;
            }
//            System.out.println("Pullout Rate: " + Math.round((float) numPulledout / patients.size() * 100) + "% (" + numPulledout + "/" + patients.size() + ")");
            System.out.println(Math.round((float) numPulledout / patients.size() * 100));
//            System.out.println("OnTransport Rate: " + Math.round((float) numOnTransport / patients.size() * 100) + "% (" + numOnTransport + "/" + patients.size() + ")");
//            System.out.println("Hospitalized Rate: " + Math.round((float) numHospitalized / patients.size() * 100) + "% (" + numHospitalized + "/" + patients.size() + ")");
//            System.out.println(Math.round((float) (numPulledout+numOnTransport+numHospitalized) / patients.size() * 100) + "," + Math.round((float) numHospitalized / patients.size() * 100));
        }

//        PropertyChecker checker = scenario.getChecker();
//
//        SPRT sprt = new SPRT();
//
//        for (int i = 1; i < 100; i++) {
//            sprt.reset(0.05, 0.05, 0.01, 0.01 * i);
//
//            while (sprt.isSampleNeeded()) {
//                ArrayList<Snapshot> simulationLog = Simulator.execute(world, 11);
//                sprt.addSample(checker.isSatisfied(simulationLog));
//            }
//
//            System.out.println("Theta: " + (0.01 * i) + ", Sample Size: " + sprt.getSampleSize() + ", Decision: " + sprt.getDecision());
//        }
    }
}
