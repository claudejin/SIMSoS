package simsos.scenario.thesis.entity;

import com.sun.org.apache.xpath.internal.operations.Bool;
import simsos.scenario.thesis.ThesisScenario.SoSType;
import simsos.scenario.thesis.ThesisWorld;
import simsos.scenario.thesis.util.*;
import simsos.simulation.component.Action;
import simsos.simulation.component.World;
import sun.plugin2.message.ShowStatusMessage;

import java.util.*;

public class FireFighter extends RationalEconomicCS {

    Maptrix<Integer> expectedPatientsMap = (Maptrix<Integer>) this.world.getResources().get("ExpectedPatientsMap");
    Maptrix<Boolean> beliefMap = new Maptrix<Boolean>(Boolean.TYPE, ThesisWorld.MAP_SIZE.getLeft(), ThesisWorld.MAP_SIZE.getRight());

    private Location location = new Location(ThesisWorld.MAP_SIZE.getLeft() / 2, ThesisWorld.MAP_SIZE.getRight() / 2);;
    private Location headingLocation = null;
    private int headingBenefit = 0;

    public enum Direction {NONE, LEFT, RIGHT, UP, DOWN}
    private Direction lastDirection;

    private enum Status {Pullout, Complete}
    private Status status;

    private Patient pulledoutPatient = null;

    public FireFighter(World world, String name) {
        super(world);

        this.name = name;
        this.reset();
    }

    @Override
    protected void observeEnvironment() {
        // FireFighter observe current location and update already pulled-out patients
    }

    @Override
    protected void consumeInformation() {
        for (Message message : this.incomingInformation) {
            // PullOut belief share from FireFighters
            if (message.sender.startsWith("ControlTower") && message.purpose == Message.Purpose.Delivery && message.data.containsKey("PulloutBelief")) {
                Maptrix<Boolean> othersBeliefMap = (Maptrix<Boolean>) message.data.get("PulloutBelief");

                boolean localBelief = false;
                for (int x = 0; x < ThesisWorld.MAP_SIZE.getLeft(); x++)
                    for (int y = 0; y < ThesisWorld.MAP_SIZE.getRight(); y++) {
                        localBelief = this.beliefMap.getValue(x, y);
                        localBelief = localBelief || othersBeliefMap.getValue(x, y);
                        this.beliefMap.setValue(x, y, localBelief);
                    }
            }
        }
    }

    @Override
    protected void generateActiveImmediateActions() {
        // Pullout patients at this location
        switch ((SoSType) this.world.getResources().get("Type")) {
            default:
                // Do pullout patients at this location
                this.immediateActionList.add(new ABCItem(this.pulloutPatient, 10, 1));

                // Report pullout belief to others
                Message beliefShare = new Message();
                beliefShare.name = "Report Pullout belief";
                beliefShare.sender = this.getName();
                beliefShare.receiver = "ControlTower";
                beliefShare.purpose = Message.Purpose.Delivery;
                beliefShare.data.put("PulloutLocation", this.location);

                this.immediateActionList.add(new ABCItem(new SendMessage(beliefShare), 9, 1));
        }

//        // I-Share to other FireFighters
//        if ((SoSType) this.world.getResources().get("Type") !=  SoSType.Virtual) {
//                Message beliefShare = new Message();
//                beliefShare.name = "Share Pullout belief";
//                beliefShare.sender = this.getName();
//                beliefShare.receiver = "FireFighter";
////                beliefShare.location = this.location;
//                beliefShare.purpose = Message.Purpose.Delivery;
//                beliefShare.data.put("PulloutBelief", this.beliefMap);
//
//                this.immediateActionList.add(new ABCItem(new SendMessage(beliefShare), 5, 1));
//        }
//
//        // Report to ControlTower
//        switch ((SoSType) this.world.getResources().get("Type")) {
//            case Directed:
//            case Acknowledged:
//
//                break;
//        }
    }

    @Override
    protected void generatePassiveImmediateActions() {
        for (Message message : this.incomingRequests) {
            // N-Directed Moves
            if (message.sender.equals("ControlTower") && message.purpose == Message.Purpose.Order) {
                boolean accepted = false;
                Location newLocation = (Location) message.data.get("HeadingLocation");

                if (this.headingLocation == null && this.status == Status.Complete) {
                    this.headingLocation = newLocation;
                    accepted = true;
                }

                Message beliefShare = new Message();
                beliefShare.name = "Direction acception";
                beliefShare.sender = this.getName();
                beliefShare.receiver = message.sender;
                beliefShare.purpose = Message.Purpose.Response;
                beliefShare.data.put("Accepted", accepted);
                beliefShare.data.put("HeadingLocation", newLocation);

                this.immediateActionList.add(new ABCItem(new SendMessage(beliefShare), 0, 0));

            // N-Acked Moves
            } else if (message.sender.equals("ControlTower") && message.purpose == Message.Purpose.ReqAction) {
                boolean accepted = false;
                Location newLocation = (Location) message.data.get("HeadingLocation");

                if (this.headingLocation == null && this.status == Status.Complete) {
                    if (this.world.random.nextInt(4) < 2) {
                        this.headingLocation = newLocation;
                        this.headingBenefit = (Integer) message.data.get("AdditionalBenefit");
                        accepted = true;
                    }
                }

                Message beliefShare = new Message();
                beliefShare.name = "Direction acception";
                beliefShare.sender = this.getName();
                beliefShare.receiver = message.sender;
                beliefShare.purpose = Message.Purpose.Response;
                beliefShare.data.put("Accepted", accepted);
                beliefShare.data.put("HeadingLocation", newLocation);

                this.immediateActionList.add(new ABCItem(new SendMessage(beliefShare), 0, 0));
            }
        }
    }

    @Override
    protected void generateNormalActions() {
        // N-Autonomous Moves
        if (this.status == Status.Complete) {
            switch ((SoSType) this.world.getResources().get("Type")) {
                case Directed:
                    if (this.location.getX() > 0)
                        this.directedNormalActionList.add(new ABCItem(new Move(Direction.LEFT), 0, calculateMoveCost(Direction.LEFT, true)));
                    if (this.location.getX() < ThesisWorld.MAP_SIZE.getLeft() - 1)
                        this.directedNormalActionList.add(new ABCItem(new Move(Direction.RIGHT), 0, calculateMoveCost(Direction.RIGHT, true)));
                    if (this.location.getY() > 0)
                        this.directedNormalActionList.add(new ABCItem(new Move(Direction.UP), 0, calculateMoveCost(Direction.UP, true)));
                    if (this.location.getY() < ThesisWorld.MAP_SIZE.getRight() - 1)
                        this.directedNormalActionList.add(new ABCItem(new Move(Direction.DOWN), 0, calculateMoveCost(Direction.DOWN, true)));
                    break;
                case Acknowledged:
                    if (FireFighter.this.location.getX() > 0 && lastDirection != Direction.RIGHT)
                        this.normalActionList.add(new ABCItem(new Move(Direction.LEFT), headingBenefit, calculateMoveCost(Direction.LEFT, true)));
                    if (FireFighter.this.location.getX() < ThesisWorld.MAP_SIZE.getLeft() - 1 && lastDirection != Direction.LEFT)
                        this.normalActionList.add(new ABCItem(new Move(Direction.RIGHT), headingBenefit, calculateMoveCost(Direction.RIGHT, true)));
                    if (FireFighter.this.location.getY() > 0 && lastDirection != Direction.DOWN)
                        this.normalActionList.add(new ABCItem(new Move(Direction.UP), headingBenefit, calculateMoveCost(Direction.UP, true)));
                    if (FireFighter.this.location.getY() < ThesisWorld.MAP_SIZE.getRight() - 1 && lastDirection != Direction.UP)
                        this.normalActionList.add(new ABCItem(new Move(Direction.DOWN), headingBenefit, calculateMoveCost(Direction.DOWN, true)));
                case Collaborative:
                case Virtual:
                    if (FireFighter.this.location.getX() > 0 && lastDirection != Direction.RIGHT)
                        this.normalActionList.add(new ABCItem(new Move(Direction.LEFT), 0, calculateMoveCost(Direction.LEFT, false)));
                    if (FireFighter.this.location.getX() < ThesisWorld.MAP_SIZE.getLeft() - 1 && lastDirection != Direction.LEFT)
                        this.normalActionList.add(new ABCItem(new Move(Direction.RIGHT), 0, calculateMoveCost(Direction.RIGHT, false)));
                    if (FireFighter.this.location.getY() > 0 && lastDirection != Direction.DOWN)
                        this.normalActionList.add(new ABCItem(new Move(Direction.UP), 0, calculateMoveCost(Direction.UP, false)));
                    if (FireFighter.this.location.getY() < ThesisWorld.MAP_SIZE.getRight() - 1 && lastDirection != Direction.UP)
                        this.normalActionList.add(new ABCItem(new Move(Direction.DOWN), 0, calculateMoveCost(Direction.DOWN, false)));
            }
        }
    }

    public int calculateMoveCost(Direction direction, boolean headingTo) {
        if (direction == Direction.LEFT)
            return calculateMoveCost(-1, 0, headingTo);
        else if (direction == Direction.RIGHT)
            return calculateMoveCost(1, 0, headingTo);
        else if (direction == Direction.UP)
            return calculateMoveCost(0, -1, headingTo);
        else
            return calculateMoveCost(0, 1, headingTo);
    }

    public int calculateMoveCost(int deltaX, int deltaY, boolean headingTo) {
        Location nextLocation = this.location.add(deltaX, deltaY);
        return calculateMoveCost(nextLocation, headingTo);
    }

    public int calculateMoveCost(Location nextLocation, boolean headingTo) {
        int totalCost = 0;

        if (headingTo && this.headingLocation != null) {
            // Headindg cost
            totalCost += nextLocation.distanceTo(this.headingLocation);
        } else {
            // Uncertainty
            totalCost += this.world.random.nextInt(8);

            // Belief cost
            totalCost += this.beliefMap.getValue(nextLocation.getX(), nextLocation.getY()) ? 4 : 0;
            totalCost -= this.expectedPatientsMap.getValue(nextLocation.getX(), nextLocation.getY()) * 4;
        }

        return totalCost;
    }

    @Override
    public void reset() {
        this.beliefMap.reset();
        for (int x = 0; x < ThesisWorld.MAP_SIZE.getLeft(); x++)
            for (int y = 0; y < ThesisWorld.MAP_SIZE.getRight(); y++)
                this.beliefMap.setValue(x, y, false);

        this.status = Status.Pullout;
        this.location.setLocation(ThesisWorld.MAP_SIZE.getLeft() / 2, ThesisWorld.MAP_SIZE.getRight() / 2);

        this.lastDirection = Direction.NONE;
        this.pulledoutPatient = null;
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
//        properties.put("PulloutBeliefMap", this.beliefMap);
        return properties;
    }

    private final Action pulloutPatient = new Action(0) {

        @Override
        public void execute() {
            ArrayList<Patient> trappedPatients = ((ThesisWorld) FireFighter.this.world).getTrappedPatients(FireFighter.this.location);

            if (trappedPatients.size() > 1)
                FireFighter.this.status = Status.Pullout;
            else
                FireFighter.this.status = Status.Complete;

            if (trappedPatients.size() > 0) {
                FireFighter.this.pulledoutPatient = trappedPatients.get(0);
                pulledoutPatient.setStatus(Patient.Status.Pulledout);
            } else {
                FireFighter.this.pulledoutPatient = null;
            }
        }

        @Override
        public String getName() {
            return FireFighter.this.getName() + ": Pull out a patient";
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

            if (FireFighter.this.headingLocation != null)
                if (FireFighter.this.headingLocation.equals(FireFighter.this.location))
                    FireFighter.this.headingLocation = null;

            FireFighter.this.lastDirection = this.direction;
        }

        @Override
        public String getName() {
            return null;
        }
    }
}
