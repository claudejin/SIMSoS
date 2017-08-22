package simsos.scenario.thesis;

import simsos.scenario.thesis.util.Location;
import simsos.scenario.thesis.util.Pair;
import simsos.scenario.thesis.util.Patient;
import simsos.simulation.analysis.PropertyValue;
import simsos.simulation.analysis.Snapshot;
import simsos.simulation.component.*;

import java.util.*;

// Map, Comm. Channel, The wounded

// - Maintain CSs’ current locations (for simulation log only)
// - Maintain reported wounded persons (for simulation log only)

public class ThesisWorld extends World {
    public static final Pair<Integer, Integer> MAP_SIZE = new Pair<Integer, Integer>(19, 19);

    public ArrayList<Patient> patients = new ArrayList<Patient>();

    public ThesisWorld(int nPatient) {
        for (int i = 0; i < nPatient; i++)
            patients.add(new Patient("Patient" + (i+1)));

        this.reset();
    }

    @Override
    public void reset() {
        super.reset();

        for (Patient patient : this.patients)
            patient.reset();

        // Adjust severity of patients

        // Adjust geographical distribution of patients
        int offset = 0;
        for (Patient patient : this.patients) {
            Random rd = new Random();
            //patient.setLocation(new Location(rd.nextInt(MAP_SIZE.getLeft()), rd.nextInt(MAP_SIZE.getRight())));
            offset++;
            patient.setLocation(new Location(9 + offset, 9 + offset));
        }
    }

    @Override
    public HashMap<String, Object> getResources() {
        HashMap<String, Object> resources = new HashMap<String, Object>();
        resources.put("Agents", this.agents);
        resources.put("Patients", this.patients);

        return resources;
    }

    @Override
    public ArrayList<Action> generateExogenousActions() {
        ArrayList<Action> exo = new ArrayList<Action>();
        exo.add(new Action(0) {

            @Override
            public void execute() {
                for (Patient patient : ThesisWorld.this.patients)
                    patient.bleed();
            }

            @Override
            public String getName() {
                return "World: Patients bleed";
            }
        });

        return exo;
    }

    @Override
    public Snapshot getCurrentSnapshot() {
        Snapshot snapshot = super.getCurrentSnapshot();

        LinkedHashMap<String, Object> worldProperties = new LinkedHashMap<String, Object>();

        worldProperties.put("Time", this.time);
        snapshot.addProperties(null, worldProperties);

        for (Patient patient : this.patients)
            if (patient.getStatus() == Patient.Status.Discovered)
                snapshot.addProperty(patient, "location", patient.getLocation());

        printCurrentMap(snapshot);
        return snapshot;
    }

    private void printCurrentMap(Snapshot snapshot) {
        ArrayList<PropertyValue> prop = snapshot.getProperties();
        System.out.println("Time: " + this.time);

        String [][] map = new String[MAP_SIZE.getLeft()][MAP_SIZE.getRight()];
        int maximalLength = 0;
        for (PropertyValue pv : prop) {
           if (pv.propertyName.equals("location")) {
               System.out.println(pv.subjectName + ": " + pv.value.toString());
               Location location = (Location) pv.value;
               if (map[location.getX()][location.getY()] == null)
                   map[location.getX()][location.getY()] = "";
               map[location.getX()][location.getY()] += pv.subject.getSymbol();
               maximalLength = Math.max(maximalLength, map[location.getX()][location.getY()].length());
           }
        }

        maximalLength = (maximalLength + 1) / 2; // roundup for division by 2

        String horizontal = String.join("", Collections.nCopies(maximalLength, "─"));

        System.out.println("┌" + String.join("┬", Collections.nCopies(MAP_SIZE.getLeft(), horizontal)) + "┐");

        for (int y = 0; y < MAP_SIZE.getRight(); y++) {
            System.out.print("│");
            for (int x = 0; x < MAP_SIZE.getLeft(); x++) {
                if (map[x][y] == null)
                    System.out.printf("%" + (2*maximalLength) + "s", "");
                else
                    System.out.printf("%" + (2*maximalLength) + "s", map[x][y]);
                System.out.print("│");
            }
            System.out.println("");
            if (y < MAP_SIZE.getRight() - 1)
                System.out.println("├" + String.join("┼", Collections.nCopies(MAP_SIZE.getLeft(), horizontal)) + "┤");
        }

        System.out.println("└" + String.join("┴", Collections.nCopies(MAP_SIZE.getLeft(), horizontal)) + "┘");
    }
}
