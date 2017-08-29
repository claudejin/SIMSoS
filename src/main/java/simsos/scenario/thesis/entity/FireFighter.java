package simsos.scenario.thesis.entity;

import simsos.scenario.thesis.ThesisScenario.SoSType;
import simsos.scenario.thesis.ThesisWorld;
import simsos.scenario.thesis.util.*;
import simsos.simulation.component.Action;
import simsos.simulation.component.World;

import java.util.*;

public class FireFighter extends RationalEconomicCS {

    Maptrix<Integer> expectedPatientsMap = (Maptrix<Integer>) this.world.getResources().get("ExpectedPatientsMap");
    Maptrix<HashSet> beliefMap = new Maptrix<HashSet>(HashSet.class, ThesisWorld.MAP_SIZE.getLeft(), ThesisWorld.MAP_SIZE.getRight());

    private Location location = new Location(ThesisWorld.MAP_SIZE.getLeft() / 2, ThesisWorld.MAP_SIZE.getRight() / 2);;

    private enum Direction {NONE, LEFT, RIGHT, UP, DOWN}
    private Direction lastDirection;

    private Patient discoveredPatient = null;

    public FireFighter(World world, String name) {
        super(world);

        this.name = name;
        this.reset();
    }

    @Override
    protected void observeEnvironment() {
        // FireFighter observe current location and update already discovered patients
        Set<Patient> localBelief = this.beliefMap.getValue(this.location);
        Set<Patient> discoveredPatients = ((ThesisWorld) this.world).getDiscoveredPatients(this.location);
        localBelief.addAll(discoveredPatients);

//        // FireFighter observe global location and update already discovered patients
//        Set<Patient> localBelief = null;
//        Set<Patient> discoveredPatients = null;
//        for (int x = 0; x < ThesisWorld.MAP_SIZE.getLeft(); x++)
//            for (int y = 0; y < ThesisWorld.MAP_SIZE.getRight(); y++) {
//                localBelief = this.beliefMap.getValue(x, y);
//                discoveredPatients = ((ThesisWorld) this.world).getDiscoveredPatients(new Location(x, y));
//                localBelief.addAll(discoveredPatients);
//            }
    }

    @Override
    protected void consumeInformation() {
        for (Message message : this.incomingInformation) {
            // Discovery belief share from FireFighters
            if (message.sender.startsWith("FireFighter") && message.purpose == Message.Purpose.Delivery && message.data.containsKey("DiscoveryBelief")) {
                Maptrix<HashSet> othersBeliefMap = (Maptrix<HashSet>) message.data.get("DiscoveryBelief");

                for (int x = 0; x < ThesisWorld.MAP_SIZE.getLeft(); x++)
                    for (int y = 0; y < ThesisWorld.MAP_SIZE.getRight(); y++)
                        this.beliefMap.getValue(x, y).addAll(othersBeliefMap.getValue(x, y));
            }
        }
    }

    @Override
    protected void generateActiveImmediateActions() {
        // I-Discover
        switch ((SoSType) this.world.getResources().get("Type")) {
            default:
            this.immediateActionList.add(new ABCItem(this.discoverPatient, 10, 1));
        }

        // I-Share
        switch ((SoSType) this.world.getResources().get("Type")) {
            case Acknowledged:
            case Collaborative:
                Message beliefShare = new Message();
                beliefShare.name = "Share discovery belief";
                beliefShare.sender = this.getName();
                beliefShare.receiver = "FireFighter";
                beliefShare.location = this.location;
                beliefShare.purpose = Message.Purpose.Delivery;
                beliefShare.data.put("DiscoveryBelief", this.beliefMap);

                this.immediateActionList.add(new ABCItem(new SendMessage(beliefShare), 0, 0));
                break;
        }
    }

    @Override
    protected void generatePassiveImmediateActions() {
        for (Message message : this.incomingRequests) {
            // I-ReportLocation
            if (message.sender.equals("ControlTower") && message.purpose == Message.Purpose.ReqInfo && message.data.containsKey("Location")) {
                switch ((SoSType) this.world.getResources().get("Type")) {
                    case Directed:
                    case Acknowledged:
                        Message locationReport = new Message();
                        locationReport.name = "Respond location report";
                        locationReport.sender = this.getName();
                        locationReport.receiver = message.sender;
                        locationReport.purpose = Message.Purpose.Response;
                        locationReport.data.put("Location", this.location);

                        this.immediateActionList.add(new ABCItem(new SendMessage(locationReport), 0, 0));
                        break;
                }
            }

            // I-ReportDiscovery
            if (message.sender.equals("ControlTower") && message.purpose == Message.Purpose.ReqInfo && message.data.containsKey("Discovered")) {
                switch ((SoSType) this.world.getResources().get("Type")) {
                    case Directed:
                    case Acknowledged:
                        Message discoveryReport = new Message();
                        discoveryReport.name = "Respond discovery report";
                        discoveryReport.sender = this.getName();
                        discoveryReport.receiver = message.sender;
                        discoveryReport.purpose = Message.Purpose.Response;
                        discoveryReport.data.put("Discovered", this.discoveredPatient);
                        discoveryReport.data.put("Location", this.discoveredPatient != null ? new Location(this.discoveredPatient.getLocation()) : null);

                        this.immediateActionList.add(new ABCItem(new SendMessage(discoveryReport), 0, 0));
                        break;
                }
            }
        }
    }

    @Override
    protected void generateNormalActions() {
        // N-Directed Moves
        if (this.world.getResources().get("Type") == SoSType.Directed) {

        }

        // N-Autonomous Moves
        if (this.world.getResources().get("Type") != SoSType.Directed) {
            if (FireFighter.this.location.getX() > 0 && lastDirection != Direction.RIGHT)
                normalActionList.add(new ABCItem(new Move(Direction.LEFT), 0, calculateMoveCost(-1, 0)));
            if (FireFighter.this.location.getX() < ThesisWorld.MAP_SIZE.getLeft() - 1 && lastDirection != Direction.LEFT)
                normalActionList.add(new ABCItem(new Move(Direction.RIGHT), 0, calculateMoveCost(1, 0)));
            if (FireFighter.this.location.getY() > 0 && lastDirection != Direction.DOWN)
                normalActionList.add(new ABCItem(new Move(Direction.UP), 0, calculateMoveCost(0, -1)));
            if (FireFighter.this.location.getY() < ThesisWorld.MAP_SIZE.getRight() - 1 && lastDirection != Direction.UP)
                normalActionList.add(new ABCItem(new Move(Direction.DOWN), 0, calculateMoveCost(0, 1)));
        }

    }

    public int calculateMoveCost(int deltaX, int deltaY) {
        Location nextLocation = this.location.add(deltaX, deltaY);
        // Uncertainty
        int totalCost = this.world.random.nextInt(2);

        // Belief cost
        totalCost += this.beliefMap.getValue(nextLocation.getX(), nextLocation.getY()).size();
        totalCost -= this.expectedPatientsMap.getValue(nextLocation.getX(), nextLocation.getY());

        return totalCost;
    }

    @Override
    public void reset() {
        this.beliefMap.reset();

        this.location.setLocation(ThesisWorld.MAP_SIZE.getLeft() / 2, ThesisWorld.MAP_SIZE.getRight() / 2);

        this.lastDirection = Direction.NONE;
        this.discoveredPatient = null;
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
        properties.put("Location", this.location);
        properties.put("BeliefMap", this.beliefMap);
        return properties;
    }

    private final Action discoverPatient = new Action(0) {

        @Override
        public void execute() {
            FireFighter.this.discoveredPatient = ((ThesisWorld) FireFighter.this.world).getUndiscoveredPatient(FireFighter.this.location);

            if (FireFighter.this.discoveredPatient != null) {
                FireFighter.this.beliefMap.getValue(FireFighter.this.location).add(FireFighter.this.discoveredPatient);
            } else {
                // Fail to discover a patient
            }
        }

        @Override
        public String getName() {
            return FireFighter.this.getName() + ": Discover a patient";
        }
    };

    protected class Move extends Action {
        Direction direction = Direction.NONE;

        public Move(Direction direction) {
            super(1);

            this.direction = direction;
        }

        @Override
        public void execute() {
            switch (this.direction) {
                case LEFT:
                    FireFighter.this.location.moveX(-1);
                    break;
                case RIGHT:
                    FireFighter.this.location.moveX(1);
                    break;
                case UP:
                    FireFighter.this.location.moveY(-1);
                    break;
                case DOWN:
                    FireFighter.this.location.moveY(1);
                    break;
                default:
                    System.out.println("FireFighter: Move Error"); // Error
            }

            FireFighter.this.lastDirection = this.direction;
        }

        @Override
        public String getName() {
            return null;
        }
    }
}
