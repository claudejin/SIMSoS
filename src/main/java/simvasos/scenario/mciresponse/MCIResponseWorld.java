package simvasos.scenario.mciresponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.distribution.NormalDistribution;
import simvasos.modelparsing.modeling.ABCPlus.ABCPlusCS;
import simvasos.scenario.mciresponse.entity.FireFighter;
import simvasos.simulation.analysis.PropertyValue;
import simvasos.simulation.analysis.Snapshot;
import simvasos.simulation.component.Action;
import simvasos.simulation.component.Agent;
import simvasos.simulation.component.Message;
import simvasos.simulation.component.World;
import simvasos.simulation.util.Location;
import simvasos.simulation.util.Maptrix;
import simvasos.simulation.util.Pair;
import simvasos.simulation.util.TimedValue;

import java.util.*;

// Map, Comm. Channel, The wounded

// - Maintain CSs’ current locations (for simulation log only)
// - Maintain reported wounded persons (for simulation log only)

public class MCIResponseWorld extends World {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public MCIResponseScenario.PatientDistribution disPatient = MCIResponseScenario.PatientDistribution.Uniform;
    public final int nPatient;
    public final int numStartingPoint;
    public final int neighborhoodRange;
    public int messageCnt;

    public Pair<Integer, Integer> MAP_SIZE = null;
    public ArrayList<Location> startingPoints = null;
    public Maptrix<Integer> expectedPatientsMap = null;
    public Maptrix<ArrayList> patientsMap = null;

    public ArrayList<Patient> patients = new ArrayList<Patient>();

    private static int stringFactor = 2;

    public MCIResponseWorld(Pair<Integer, Integer> mapSize, MCIResponseScenario.PatientDistribution distPatient, int numPatient, int numStartingPoint, int neighborhoodRange) {
        super(1);

        MAP_SIZE = new Pair<Integer, Integer>(mapSize);

        this.disPatient = distPatient;
        this.nPatient = numPatient;
        this.numStartingPoint = numStartingPoint;
        this.neighborhoodRange = neighborhoodRange;

        for (int i = 0; i < nPatient; i++)
            patients.add(new Patient(this.random, "Patient" + (i+1)));

        this.reset();
    }

    @Override
    public void reset() {
        super.reset();

        for (Patient patient : this.patients)
            patient.reset();

        this.expectedPatientsMap = new Maptrix<Integer>(Integer.TYPE, MAP_SIZE.getLeft(), MAP_SIZE.getRight());
        this.patientsMap = new Maptrix<ArrayList>(ArrayList.class, MAP_SIZE.getLeft(), MAP_SIZE.getRight());

        // Adjust geographical distribution of patients
        patientsMap.reset();
        int pindex = 0;
        for (int x = 0; x < MAP_SIZE.getLeft(); x++)
            for (int y = 0; y < MAP_SIZE.getRight(); y++) {
                this.patients.get(pindex).setLocation(new Location(x, y));
                this.patientsMap.getValue(x, y).add(this.patients.get(pindex));
                pindex++;
            }

        this.startingPoints = new ArrayList<Location>();
        this.startingPoints.add(new Location(0, 0)); // Left-top
        this.startingPoints.add(new Location(MAP_SIZE.getLeft() - 1, 0)); // Right-top
        this.startingPoints.add(new Location(0, MAP_SIZE.getRight() - 1)); // Left-bottom
        this.startingPoints.add(new Location(MAP_SIZE.getLeft() - 1, MAP_SIZE.getRight() - 1)); // Right-bottom

        Collections.shuffle(this.startingPoints, this.random);
        int i = 0;
        for (Agent agent : this.agents)
            if (agent instanceof FireFighter) {
                ((FireFighter) agent).setStartingLocation(startingPoints.get(i++ % numStartingPoint));
                agent.reset();
            }
//        for (Patient patient : this.patients) {
//            patient.setLocation(getRandomPatientLocation());
//            patientsMap.getValue(patient.getLocation()).add(patient);
//        }
//        generateExpectedPatientsMap();

        this.messageCnt = 0;
    }

    @Override
    public void progress(int time) {
        super.progress(time);

        for (Agent agent : this.agents)
            if (agent instanceof ABCPlusCS)
                ((ABCPlusCS) agent).progress();
    }

    public boolean checkValidLocation(Location location) {
        return checkValidLocation(location.getX(), location.getY());
    }

    public boolean checkValidLocation(int x, int y) {
        if (x < 0 || x > MAP_SIZE.getLeft() - 1)
            return false;
        if (y < 0 || y > MAP_SIZE.getRight() - 1)
            return false;

        return true;
    }

    private boolean checkValidPatientLocation(Location location) {
//        for (Agent agent : this.agents) {
//            if (agent instanceof Hospital) {
//                Location hospitalLocation = (Location) agent.getProperties().get("Location");
//                if (hospitalLocation.equals(location))
//                    return false;
//            }
//        }

        return true;
    }

    private void generateExpectedPatientsMap() {
        NormalDistribution xND = new NormalDistribution(MAP_SIZE.getLeft() / 2, MAP_SIZE.getLeft() / 4);
        NormalDistribution yND = new NormalDistribution(MAP_SIZE.getRight() / 2, MAP_SIZE.getRight() / 4);

        for (int x = 0; x < MAP_SIZE.getLeft(); x++)
            for (int y = 0; y < MAP_SIZE.getRight(); y++) {
                double xProb = xND.cumulativeProbability(x) - xND.cumulativeProbability(x - 1);
                double nX = xProb * MAP_SIZE.getLeft() * MAP_SIZE.getRight();

                double yProb = xND.cumulativeProbability(y) - xND.cumulativeProbability(y - 1);
                int nXY = (int) Math.round(yProb * nX);

                this.expectedPatientsMap.setValue(x, y, nXY);
            }
    }

    private Location getRandomPatientLocation() {
        int x = -1, y = -1;
        Location location = null;

        boolean stoppingCondition = false;
        while (!stoppingCondition) {
            x = (int) Math.round(this.random.nextGaussian() * (MAP_SIZE.getLeft() / 4) + MAP_SIZE.getLeft() / 2);
            y = (int) Math.round(this.random.nextGaussian() * (MAP_SIZE.getRight() / 4) + MAP_SIZE.getRight() / 2);
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
        resources.put("Patients", this.patients);
        resources.put("ExpectedPatientsMap", this.expectedPatientsMap);

        return resources;
    }

    public void sendMessage(Message message) {
        // Send the message to the receiver(s)
        for (Agent agent : this.agents)
            if (agent instanceof ABCPlusCS) {
                if (agent.getName().equals(message.sender))
                    continue;

                if (agent.getName().startsWith(message.receiver)) {
                    if (message.location == null) {
                        ((ABCPlusCS) agent).receiveMessage(message);
//                        System.out.println("[Message]: FROM " + message.sender + " TO " + agent.getName() + " - " + message.getName());
                        this.messageCnt++;
                    } else {
                        Map<String, Object> props = agent.getProperties();
                        if (props.containsKey("Location"))
                            if (message.location.distanceTo((Location) props.get("Location")) <= this.neighborhoodRange) {
                                ((ABCPlusCS) agent).receiveMessage(message);
//                                System.out.println("[Message]: FROM " + message.sender + " TO " + agent.getName() + " - " + message.getName());
                                this.messageCnt++;
                            }
                    }
                }
            }
    }

    @Override
    public ArrayList<Action> generateExogenousActions() {
        ArrayList<Action> exo = new ArrayList<Action>();
        exo.add(new Action(0) {

            @Override
            public void execute() {
                for (Patient patient : MCIResponseWorld.this.patients)
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
//        Snapshot snapshot = new Snapshot();

        for (Patient patient : this.patients)
            snapshot.addProperty(patient, "Location", patient.getLocation());

        LinkedHashMap<String, Object> worldProperties = new LinkedHashMap<String, Object>();
        worldProperties.put("Pulledout", getPulledoutPatients().size());
        worldProperties.put("MessageCnt", this.messageCnt);
        snapshot.addProperties(null, worldProperties);

//        System.out.println("Time: " + this.time);
//        printExpectedPatientsMap();
//        printCurrentMap(snapshot);
//        printBeliefMap(snapshot);
        return snapshot;
    }

    private void printExpectedPatientsMap() {
        System.out.println("Expected Patients Map");

        String [][] map = new String[MAP_SIZE.getLeft()][MAP_SIZE.getRight()];
        int maximalLength = 0;

        for (int x = 0; x < MAP_SIZE.getLeft(); x++)
            for (int y = 0; y < MAP_SIZE.getRight(); y++) {
                map[x][y] = "" + expectedPatientsMap.getValue(x, y);
                maximalLength = Math.max(maximalLength, map[x][y].length());
            }

        maximalLength = (maximalLength + 1) / stringFactor; // roundup for division by 2

        String horizontal = String.join("", Collections.nCopies(maximalLength, "─"));

        System.out.println("┌" + String.join("┬", Collections.nCopies(MAP_SIZE.getLeft(), horizontal)) + "┐");

        for (int y = 0; y < MAP_SIZE.getRight(); y++) {
            System.out.print("│");
            for (int x = 0; x < MAP_SIZE.getLeft(); x++) {
                if (map[x][y] == null)
                    System.out.print(StringUtils.repeat(" ", stringFactor * maximalLength));
                else
                    System.out.print(map[x][y] + StringUtils.repeat(" ", stringFactor * maximalLength - map[x][y].length()));
                System.out.print("│");
            }
            System.out.println("");
            if (y < MAP_SIZE.getRight() - 1)
                System.out.println("├" + String.join("┼", Collections.nCopies(MAP_SIZE.getLeft(), horizontal)) + "┤");
        }

        System.out.println("└" + String.join("┴", Collections.nCopies(MAP_SIZE.getLeft(), horizontal)) + "┘");
    }

    private void printBeliefMap(Snapshot snapshot) {
        ArrayList<PropertyValue> prop = snapshot.getProperties();

        for (PropertyValue pv : prop) {
            if (pv.propertyName.endsWith("BeliefMap")) {
                System.out.println(pv.subjectName + ": " + pv.propertyName);

                String [][] map = new String[MAP_SIZE.getLeft()][MAP_SIZE.getRight()];
                int maximalLength = 0;

                Maptrix beliefMap = (Maptrix) pv.value;

                for (int x = 0; x < MAP_SIZE.getLeft(); x++)
                    for (int y = 0; y < MAP_SIZE.getRight(); y++) {
                        if (pv.propertyName.equals("PatientsBeliefMap"))
                            map[x][y] = "" + ((TimedValue) beliefMap.getValue(x, y)).toString();
                        else if (pv.propertyName.equals("PulloutBeliefMap"))
                            map[x][y] = "" + ((boolean) beliefMap.getValue(x, y) ? ANSI_GREEN + "C" + ANSI_RESET : ANSI_RED + "P" + ANSI_RESET);
                        else {
                            System.out.println("Undefined belief");
                            System.exit(1);
                        }
                        maximalLength = Math.max(maximalLength, map[x][y].replaceAll("\u001B\\[[;\\d]*m", "").length());
                    }

                maximalLength = (maximalLength + 1) / stringFactor; // roundup for division by 2

                String horizontal = String.join("", Collections.nCopies(maximalLength, "─"));

                System.out.println("┌" + String.join("┬", Collections.nCopies(MAP_SIZE.getLeft(), horizontal)) + "┐");

                for (int y = 0; y < MAP_SIZE.getRight(); y++) {
                    System.out.print("│");
                    for (int x = 0; x < MAP_SIZE.getLeft(); x++) {
                        if (map[x][y] == null)
                            System.out.print(StringUtils.repeat(" ", stringFactor * maximalLength));
                        else
                            System.out.print(map[x][y] + StringUtils.repeat(" ", maximalLength * stringFactor - map[x][y].replaceAll("\u001B\\[[;\\d]*m", "").length()));
                        System.out.print("│");
                    }
                    System.out.println("");
                    if (y < MAP_SIZE.getRight() - 1)
                        System.out.println("├" + String.join("┼", Collections.nCopies(MAP_SIZE.getLeft(), horizontal)) + "┤");
                }

                System.out.println("└" + String.join("┴", Collections.nCopies(MAP_SIZE.getLeft(), horizontal)) + "┘");
            }
        }
    }
    private void printCurrentMap(Snapshot snapshot) {
        ArrayList<PropertyValue> prop = snapshot.getProperties();
        String [][] map = new String[MAP_SIZE.getLeft()][MAP_SIZE.getRight()];
        int[] maximalLength = new int[Math.max(MAP_SIZE.getLeft(), MAP_SIZE.getRight())];

        for (PropertyValue pv : prop) {
           if (pv.propertyName.equals("Location")) {
//               System.out.println(pv.subjectName + ": " + pv.value.toString());
               Location location = (Location) pv.value;
               if (map[location.getX()][location.getY()] == null)
                   map[location.getX()][location.getY()] = "";
               if (pv.subject instanceof Patient)
                   if (((Patient) pv.subject).getStatus() == Patient.Status.Pulledout)
                       map[location.getX()][location.getY()] += ANSI_RED + pv.subject.getSymbol() + ANSI_RESET;
                   else if (((Patient) pv.subject).getStatus() == Patient.Status.OnTransport)
                       map[location.getX()][location.getY()] += ANSI_BLUE + pv.subject.getSymbol() + ANSI_RESET;
                   else if (((Patient) pv.subject).getStatus() == Patient.Status.Hospitalized)
                       map[location.getX()][location.getY()] += ANSI_GREEN + pv.subject.getSymbol() + ANSI_RESET;
                   else
                       map[location.getX()][location.getY()] += pv.subject.getSymbol();
               else
                   map[location.getX()][location.getY()] += pv.subject.getSymbol();
               maximalLength[location.getX()] = Math.max(maximalLength[location.getX()], map[location.getX()][location.getY()].replaceAll("\u001B\\[[;\\d]*m", "").length());
           }
        }

//        maximalLength = (maximalLength + 1) / stringFactor; // roundup for division by 2

        System.out.print("┌");
        for (int x = 0; x < MAP_SIZE.getLeft() - 1; x++)
            System.out.print(String.join("", Collections.nCopies((maximalLength[x] + 1) / stringFactor, "─")) + "┬");
        System.out.println(String.join("", Collections.nCopies((maximalLength[MAP_SIZE.getLeft() - 1] + 1) / stringFactor, "─")) + "┐");

        for (int y = 0; y < MAP_SIZE.getRight(); y++) {
            System.out.print("│");
            for (int x = 0; x < MAP_SIZE.getLeft(); x++) {
                if (map[x][y] == null)
                    System.out.print(StringUtils.repeat(" ", (maximalLength[x] + 1) / stringFactor * stringFactor));
                else
                    System.out.print(map[x][y] + StringUtils.repeat(" ", (maximalLength[x] + 1) / stringFactor * stringFactor - map[x][y].replaceAll("\u001B\\[[;\\d]*m", "").length()));
                System.out.print("│");
            }
            System.out.println("");

            if (y < MAP_SIZE.getRight() - 1) {
                System.out.print("├");
                for (int x = 0; x < MAP_SIZE.getLeft() - 1; x++)
                    System.out.print(String.join("", Collections.nCopies((maximalLength[x] + 1) / stringFactor, "─")) + "┼");
                System.out.println(String.join("", Collections.nCopies((maximalLength[MAP_SIZE.getLeft() - 1] + 1) / stringFactor, "─")) + "┤");
            }
        }

        System.out.print("└");
        for (int x = 0; x < MAP_SIZE.getLeft() - 1; x++)
            System.out.print(String.join("", Collections.nCopies((maximalLength[x] + 1) / stringFactor, "─")) + "┴");
        System.out.println(String.join("", Collections.nCopies((maximalLength[MAP_SIZE.getLeft() - 1] + 1) / stringFactor, "─")) + "┘");
    }

    public Set<Patient> getPulledoutPatients() {
        Set<Patient> pulledoutPatients = new HashSet<Patient>();

        for (Patient patient : this.patients) {
            if (patient.getStatus() == Patient.Status.Pulledout) {
                pulledoutPatients.add(patient);
            }
        }

        return pulledoutPatients;
    }

    public Set<Patient> getPulledoutPatients(Location location) {
        Set<Patient> pulledoutPatients = new HashSet<Patient>();

        ArrayList<Patient> patients = this.patientsMap.getValue(location);
        for (Patient patient : patients) {
            if (patient.getStatus() == Patient.Status.Pulledout) {
                pulledoutPatients.add(patient);
            }
        }

        return pulledoutPatients;
    }

    public ArrayList<Patient> getTrappedPatients(Location location) {
        ArrayList<Patient> patients = this.patientsMap.getValue(location);
        ArrayList<Patient> trappedPatients = new ArrayList<Patient>();

        for (Patient patient : patients)
            if (patient.getStatus() == Patient.Status.Trapped)
                trappedPatients.add(patient);

        return trappedPatients;
    }

    public int getNumberOfTrappedPatient(Location location) {
        ArrayList<Patient> patients = this.patientsMap.getValue(location);

        int numTrapped = 0;
        for (Patient patient : patients)
            if (patient.getStatus() == Patient.Status.Trapped)
                numTrapped++;

        return numTrapped;
    }
}
