package simsos.scenario.thesis;

import simsos.scenario.thesis.util.Patient;
import simsos.simulation.Simulator;
import simsos.simulation.component.Scenario;
import simsos.simulation.component.World;

import java.util.ArrayList;
import java.util.Random;

public class ThesisRunner {
    public static void main(String[] args) {
        // For all SoS types
        for (ThesisScenario.SoSType sostype : ThesisScenario.SoSType.values()) {
            Scenario scenario = new ThesisScenario(sostype, 100, 3, 0, 1);
            World world = scenario.getWorld();

            for (int i = 0; i < 10; i++) {
                world.setSeed(new Random().nextLong());
                ((ThesisWorld) world).setSoSType(sostype);
                Simulator.execute(world, 50);

                ArrayList<Patient> patients = (ArrayList<Patient>) world.getResources().get("Patients");
                int numPulledout = 0;

                for (Patient patient : patients)
                    if (patient.getStatus() == Patient.Status.Pulledout)
                        numPulledout++;

                System.out.println(Math.round((float) numPulledout / patients.size() * 100));
            }
        }
    }
}
