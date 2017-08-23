package simsos.scenario.thesis.entity;

import simsos.scenario.thesis.ThesisScenario.SoSType;
import simsos.scenario.thesis.ThesisWorld;
import simsos.scenario.thesis.util.ABCItem;
import simsos.scenario.thesis.util.Location;
import simsos.scenario.thesis.util.Maptrix;
import simsos.scenario.thesis.util.Patient;
import simsos.simulation.component.Action;
import simsos.simulation.component.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

// Rescue the wounded (report their location public)

// Directed
// FF-DirectedMove: Move to the location that Manager directed
// FF-Location: Report the current location
// FF-Report: Report the location of a wounded person to Manager

// Acknowledged
// FF-DirectedMove
// FF-AutonMove: Move to the most likely location to find
// FF-Location
// FF-Share: Share the last wounded map (including updated time & found number of each map point) to other FFs
// FF-Report

// Collaborative
// FF-AutonMove
// FF-Share

// Virtual
// FF-AutonMove

public class FireFighter extends RationalEconomicCS {

    Maptrix<HashSet> beliefMap = new Maptrix<HashSet>(HashSet.class, ThesisWorld.MAP_SIZE.getLeft(), ThesisWorld.MAP_SIZE.getRight());

    private Location headingLocation = ((ThesisWorld) world).getRandomPatientLocation();

    private enum Direction {NONE, LEFT, RIGHT, UP, DOWN}
    private final Action moveLeft = new Action(1) {

        @Override
        public void execute() {
            FireFighter.this.lastDirection = FireFighter.Direction.LEFT;
            FireFighter.this.location.moveX(-1);
            FireFighter.this.discoverPatient(); // Succeed or fail
        }

        @Override
        public String getName() {
            return FireFighter.this.getName() + ": Move left";
        }
    };
    private final Action moveRight = new Action(1) {

        @Override
        public void execute() {
            FireFighter.this.lastDirection = FireFighter.Direction.RIGHT;
            FireFighter.this.location.moveX(1);
            FireFighter.this.discoverPatient(); // Succeed or fail
        }

        @Override
        public String getName() {
            return FireFighter.this.getName() + ": Move right";
        }
    };
    private final Action moveUp = new Action(1) {

        @Override
        public void execute() {
            FireFighter.this.lastDirection = FireFighter.Direction.UP;
            FireFighter.this.location.moveY(-1);
            FireFighter.this.discoverPatient(); // Succeed or fail
        }

        @Override
        public String getName() {
            return FireFighter.this.getName() + ": Move up";
        }
    };
    private final Action moveDown = new Action(1) {

        @Override
        public void execute() {
            FireFighter.this.lastDirection = FireFighter.Direction.DOWN;
            FireFighter.this.location.moveY(1);
            FireFighter.this.discoverPatient(); // Succeed or fail
        }

        @Override
        public String getName() {
            return FireFighter.this.getName() + ": Move down";
        }
    };

    private Location location;
    private Direction lastDirection;

    public FireFighter(World world, String name) {
        super(world);

        this.name = name;
        this.reset();
    }

    @Override
    protected void updateBelief() {
        // Observe environment around me
        Set<Patient> currentLocation = this.beliefMap.getValue(this.location.getX(), this.location.getY());

        Maptrix<ArrayList<Patient>> patientsMap = (Maptrix<ArrayList<Patient>>) this.world.getResources().get("PatientsMap");
        ArrayList<Patient> patients = patientsMap.getValue(this.location.getX(), this.location.getY());

        for (Patient patient : patients)
            if (patient.getStatus() == Patient.Status.Discovered)
                currentLocation.add(patient);
    }

    @Override
    protected void generateActionList() {
        this.immediateActionList.clear();
        this.normalActionList.clear();

        // Location
        switch ((SoSType) this.world.getResources().get("Type")) {
            case Directed:
            case Acknowledged:

                break;
        }

        // Share
        switch ((SoSType) this.world.getResources().get("Type")) {
            case Acknowledged:
            case Collaborative:

                break;
        }

        // Report
        switch ((SoSType) this.world.getResources().get("Type")) {
            case Directed:
            case Acknowledged:

                break;
        }

        if (this.immediateActionList.size() > 0)
            return;

        // Directed Moves
        if (this.world.getResources().get("Type") == SoSType.Directed) {

        }

        // Autonomous Moves
        if (this.world.getResources().get("Type") != SoSType.Directed) {
            if (this.location.equals(this.headingLocation))
                updateHeadingLocation();

            if (FireFighter.this.location.getX() > 0 && lastDirection != Direction.RIGHT)
                normalActionList.add(new ABCItem(this.moveLeft, 0,
                        this.location.add(-1, 0).distanceTo(headingLocation) +
                                this.location.add(-1, 0).xDistanceTo(headingLocation)
                ));
            if (FireFighter.this.location.getX() < ThesisWorld.MAP_SIZE.getLeft() - 1 && lastDirection != Direction.LEFT)
                normalActionList.add(new ABCItem(this.moveRight, 0,
                        this.location.add(1, 0).distanceTo(headingLocation) +
                                this.location.add(1, 0).xDistanceTo(headingLocation)
                ));
            if (FireFighter.this.location.getY() > 0 && lastDirection != Direction.DOWN)
                normalActionList.add(new ABCItem(this.moveUp, 0,
                        this.location.add(0, -1).distanceTo(headingLocation) +
                                this.location.add(0, -1).yDistanceTo(headingLocation)
                ));
            if (FireFighter.this.location.getY() < ThesisWorld.MAP_SIZE.getRight() - 1 && lastDirection != Direction.UP)
                normalActionList.add(new ABCItem(this.moveDown, 0,
                        this.location.add(0, 1).distanceTo(headingLocation) +
                                this.location.add(0, 1).yDistanceTo(headingLocation)
                ));
        }
    }

    private void updateHeadingLocation() {
        while (this.location.equals(this.headingLocation))
            this.headingLocation = ((ThesisWorld) world).getRandomPatientLocation();
    }

    public boolean discoverPatient() {
        Maptrix<ArrayList<Patient>> patientsMap = (Maptrix<ArrayList<Patient>>) this.world.getResources().get("PatientsMap");
        ArrayList<Patient> patients = patientsMap.getValue(this.location.getX(), this.location.getY());

        for (Patient patient : patients) {
            if (patient.getStatus() == Patient.Status.Initial) {
                patient.setStatus(Patient.Status.Discovered);
                updateBelief();

                updateHeadingLocation();
                return true;
            }
        }

        return false;
    }

    @Override
    public void reset() {
        this.beliefMap.reset();

        this.location = new Location(ThesisWorld.MAP_SIZE.getLeft() / 2, ThesisWorld.MAP_SIZE.getRight() / 2);
        updateHeadingLocation();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getSymbol() {
        return this.name.replace("FireFighter", "F");
    }

    @Override
    public HashMap<String, Object> getProperties() {
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put("location", this.location);
        properties.put("BeliefMap", this.beliefMap);
        return properties;
    }
}
