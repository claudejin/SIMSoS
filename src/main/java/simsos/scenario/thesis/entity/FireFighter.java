package simsos.scenario.thesis.entity;

import simsos.scenario.thesis.ThesisWorld;
import simsos.scenario.thesis.util.Location;
import simsos.scenario.thesis.util.Patient;
import simsos.simulation.component.Action;
import simsos.simulation.component.Agent;
import simsos.simulation.component.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.function.Supplier;

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

public class FireFighter extends Agent {

    private enum Direction {NONE, LEFT, RIGHT, UP, DOWN}

    private Location location;
    private Direction lastDirection;

    public FireFighter(World world, String name) {
        super(world);

        this.name = name;
        this.reset();
    }

    @Override
    public Action step() {
        // return Action.getNullAction(1, "null");
        return new Action(1) {

            @Override
            public void execute() {
                ArrayList<Supplier<Integer>> functions = new ArrayList<Supplier<Integer>>();

                if (FireFighter.this.location.getX() > 0 && lastDirection != Direction.RIGHT)
                    functions.add(FireFighter.this::moveLeft);
                if (FireFighter.this.location.getX() < ThesisWorld.MAP_SIZE.getLeft() - 1 && lastDirection != Direction.LEFT)
                    functions.add(FireFighter.this::moveRight);
                if (FireFighter.this.location.getY() > 0 && lastDirection != Direction.DOWN)
                    functions.add(FireFighter.this::moveUp);
                if (FireFighter.this.location.getY() < ThesisWorld.MAP_SIZE.getRight() - 1 && lastDirection != Direction.UP)
                    functions.add(FireFighter.this::moveDown);

                functions.get(new Random().nextInt(functions.size())).get();
                FireFighter.this.discoverPatient(); // Succeed or fail
            }

            @Override
            public String getName() {
                return FireFighter.this.getName() + ": Move randomly";
            }
        };
    }

    public int moveLeft() {
        this.lastDirection = Direction.LEFT;
        return this.location.moveX(-1);
    }

    public int moveRight() {
        this.lastDirection = Direction.RIGHT;
        return this.location.moveX(1);
    }

    public int moveUp() {
        this.lastDirection = Direction.UP;
        return this.location.moveY(-1);
    }

    public int moveDown() {
        this.lastDirection = Direction.DOWN;
        return this.location.moveY(1);
    }

    public boolean discoverPatient() {
        ArrayList<Patient> patients = (ArrayList<Patient>) this.world.getResources().get("Patients");
        for (Patient patient : patients) {
            if (patient.getLocation().equals(FireFighter.this.location) &&
                    patient.getStatus() == Patient.Status.Initial) {
                patient.setStatus(Patient.Status.Discovered);
                return true;
            }
        }

        return false;
    }

    @Override
    public void reset() {
        this.location = new Location(ThesisWorld.MAP_SIZE.getLeft() / 2, ThesisWorld.MAP_SIZE.getRight() / 2);
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
        return properties;
    }
}
