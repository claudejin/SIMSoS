package simsos.scenario.thesis;

import org.apache.commons.lang3.StringUtils;
import simsos.scenario.thesis.ThesisScenario.SoSType;
import simsos.scenario.thesis.entity.Hospital;
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
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public SoSType type = null;
    public static final Pair<Integer, Integer> MAP_SIZE = new Pair<Integer, Integer>(9, 9);

    public ArrayList<Patient> patients = new ArrayList<Patient>();

    public ThesisWorld(SoSType type, int nPatient) {
        this.type = type;

        for (int i = 0; i < nPatient; i++)
            patients.add(new Patient("Patient" + (i+1)));

        this.reset();
    }

    public void reset(SoSType type) {
        this.type = type;

        this.reset();
    }

    @Override
    public void reset() {
        super.reset();

        for (Patient patient : this.patients)
            patient.reset();

        // Adjust severity of patients

        // Adjust geographical distribution of patients
//        Random rd = new Random();
        for (Patient patient : this.patients) {
            patient.setLocation(getRandomPatientLocation());
//            int x = -1, y = -1;
//            do {
//                x = (int) Math.round(rd.nextGaussian() * 1.5 + MAP_SIZE.getLeft() / 2);
//                y = (int) Math.round(rd.nextGaussian() * 1.5 + MAP_SIZE.getLeft() / 2);
//
//                patient.setLocation(new Location(x, y));
//            } while (!checkValidPatientLocation(x, y));
        }
    }

    public boolean checkValidLocation(int x, int y) {
        if (x < 0 || x > MAP_SIZE.getLeft() - 1)
            return false;
        if (y < 0 || y > MAP_SIZE.getRight() - 1)
            return false;

        return true;
    }

    public boolean checkValidLocation(Location location) {
        return checkValidLocation(location.getX(), location.getY());
    }

    public boolean checkValidPatientLocation(Location location) {
        for (Agent agent : this.agents) {
            if (agent instanceof Hospital) {
                Location hospitalLocation = (Location) agent.getProperties().get("location");
                if (hospitalLocation.equals(location))
                    return false;
            }
        }

        return true;
    }

    public Location getRandomPatientLocation() {
        Random rd = new Random();
        int x = -1, y = -1;
        Location location = null;

        boolean stoppingCondition = false;
        while (!stoppingCondition) {
            x = (int) Math.round(rd.nextGaussian() * 1.5 + MAP_SIZE.getLeft() / 2);
            y = (int) Math.round(rd.nextGaussian() * 1.5 + MAP_SIZE.getLeft() / 2);
            location = new Location(x, y);

            if (!checkValidLocation(location))
                continue;

            if (!checkValidPatientLocation(location))
                continue;

            stoppingCondition = true;
        }

        return location;
    }

    @Override
    public HashMap<String, Object> getResources() {
        HashMap<String, Object> resources = new HashMap<String, Object>();
        resources.put("Time", this.time);
        resources.put("Type", this.type);
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
                snapshot.addProperty(patient, "location", patient.getLocation());

//        printCurrentMap(snapshot);
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
               if (pv.subject instanceof Patient)
                   if (((Patient) pv.subject).getStatus() == Patient.Status.Discovered)
                       map[location.getX()][location.getY()] += ANSI_RED + pv.subject.getSymbol() + ANSI_RESET;
                   else
                       map[location.getX()][location.getY()] += pv.subject.getSymbol();
               else
                   map[location.getX()][location.getY()] += pv.subject.getSymbol();
               maximalLength = Math.max(maximalLength, map[location.getX()][location.getY()].replaceAll("\u001B\\[[;\\d]*m", "").length());
           }
        }

        maximalLength = (maximalLength + 1) / 2; // roundup for division by 2

        String horizontal = String.join("", Collections.nCopies(maximalLength, "─"));

        System.out.println("┌" + String.join("┬", Collections.nCopies(MAP_SIZE.getLeft(), horizontal)) + "┐");

        for (int y = 0; y < MAP_SIZE.getRight(); y++) {
            System.out.print("│");
            for (int x = 0; x < MAP_SIZE.getLeft(); x++) {
                if (map[x][y] == null)
                    System.out.print(StringUtils.repeat(" ", 2 * maximalLength));
                else
                    System.out.print(map[x][y] + StringUtils.repeat(" ", 2 * maximalLength - map[x][y].replaceAll("\u001B\\[[;\\d]*m", "").length()));
                System.out.print("│");
            }
            System.out.println("");
            if (y < MAP_SIZE.getRight() - 1)
                System.out.println("├" + String.join("┼", Collections.nCopies(MAP_SIZE.getLeft(), horizontal)) + "┤");
        }

        System.out.println("└" + String.join("┴", Collections.nCopies(MAP_SIZE.getLeft(), horizontal)) + "┘");
    }
}
