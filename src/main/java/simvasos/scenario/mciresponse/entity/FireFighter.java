package simvasos.scenario.mciresponse.entity;

import simvasos.modelparsing.modeling.ABCPlus.ABCItem;
import simvasos.modelparsing.modeling.ABCPlus.ABCPlusCS;
import simvasos.scenario.mciresponse.MCIResponseWorld;
import simvasos.scenario.mciresponse.Patient;
import simvasos.simulation.component.Action;
import simvasos.simulation.component.Message;
import simvasos.simulation.component.Scenario;
import simvasos.simulation.component.World;
import simvasos.simulation.util.Location;
import simvasos.simulation.util.Maptrix;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

public class FireFighter extends ABCPlusCS {
    Maptrix<Boolean> beliefMap = new Maptrix<Boolean>(Boolean.TYPE, ((MCIResponseWorld) this.world).MAP_SIZE.getLeft(), ((MCIResponseWorld) this.world).MAP_SIZE.getRight());

    private Location startingLocation = new Location(0, 0);
    private Location location = new Location(0, 0); // new Location(((MCIResponseWorld) this.world).MAP_SIZE.getLeft() / 2, ((MCIResponseWorld) this.world).MAP_SIZE.getRight() / 2);
    private Location headingLocation = null;
    private int headingBenefit = 0;

    public enum Direction {NONE, LEFT, RIGHT, UP, DOWN}
    private Direction lastDirection;

    private enum Status {Pullout, Complete}
//    private int idleTime;
    private Status status;

    private Patient pulledoutPatient = null;

    public FireFighter(World world) {
        super(world);

        this.name = "Firefighter Unkown";
        this.reset();
    }

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
        if (this.sosType == Scenario.SoSType.Virtual)
            return;

        for (Message message : this.incomingInformation) {
            // PullOut belief share from FireFighters
            if (message.purpose == Message.Purpose.Delivery && message.data.containsKey("PulloutBelief")) {
                Maptrix<Boolean> othersBeliefMap = (Maptrix<Boolean>) message.data.get("PulloutBelief");

                boolean localBelief;
                for (int x = 0; x < ((MCIResponseWorld) this.world).MAP_SIZE.getLeft(); x++)
                    for (int y = 0; y < ((MCIResponseWorld) this.world).MAP_SIZE.getRight(); y++) {
                        Location location = new Location(x, y);
                        localBelief = this.beliefMap.getValue(location);
                        localBelief = localBelief || othersBeliefMap.getValue(location);
                        this.beliefMap.setValue(location, localBelief);
                    }
            }
        }
    }

    @Override
    protected void generateActiveImmediateActions() {
        // Do pullout patients at this location
        this.immediateActionList.add(new ABCItem(this.pulloutPatient, 10, 1));

        if (this.sosType == Scenario.SoSType.Acknowledged
                || this.sosType == Scenario.SoSType.Directed) {
            // Report pullout belief to others
            Message beliefShare = new Message();
            beliefShare.name = "Report Pullout belief";
            beliefShare.sender = this.getName();
            beliefShare.receiver = "ControlTower";
            beliefShare.purpose = Message.Purpose.Delivery;
            beliefShare.data.put("PulloutLocation", this.location);

            this.immediateActionList.add(new ABCItem(new SendMessage(beliefShare), 9, 1));
        }

        if (this.sosType != Scenario.SoSType.Virtual) {
            // Report pullout belief to others
            Message beliefShare = new Message();
            beliefShare.name = "Share Pullout belief";
            beliefShare.sender = this.getName();
            beliefShare.receiver = "FireFighter";
            beliefShare.purpose = Message.Purpose.Delivery;
            beliefShare.location = this.location;
            beliefShare.data.put("PulloutBelief", this.beliefMap);

            this.immediateActionList.add(new ABCItem(new SendMessage(beliefShare), 9, 1));
        }

//        // Set heading location to incomplete area
//        if (this.sosType != Scenario.SoSType.Directed) {
//            if (this.idleTime >= 4)
//                this.immediateActionList.add(new ABCItem(this.setHeadingLocation, 5, 0));
//        }
    }

    @Override
    protected void generatePassiveImmediateActions() {
        if (this.sosType == Scenario.SoSType.Virtual)
            return;

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
//                    if (this.idleTime >= 4) {
                        this.headingLocation = newLocation;
                        this.headingBenefit = (Integer) message.data.get("AdditionalBenefit");
                        accepted = true;
//                    }
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
            switch (this.sosType) {
                case Directed:
                    addFourDirectionMoves(this.directedNormalActionList, 0, true);
                    break;
                case Acknowledged:
                    addFourDirectionMoves(this.normalActionList, headingBenefit, true);
                case Collaborative:
                case Virtual:
                    addFourDirectionMoves(this.normalActionList, 0, false);
            }
        }
    }

    public void addFourDirectionMoves(ArrayList<ABCItem> actionList, int additionalBenefit, boolean directMove) {
        int x = this.location.getX();
        int y = this.location.getY();

        int leftSum = 0, rightSum = 0, upSum = 0, downSum = 0;
//        for (int dx = 0; dx < ((MCIResponseWorld) this.world).MAP_SIZE.getLeft(); dx++) {
//            for (int dy = 0; dy < ((MCIResponseWorld) this.world).MAP_SIZE.getRight(); dy++) {
//                if (this.beliefMap.getValue(dx, dy)) {
//                    if (dx < x)
//                        leftSum++;
//                    else if (dx > x)
//                        rightSum++;
//
//                    if (dy < y)
//                        upSum++;
//                    else if (dy > y)
//                        downSum++;
//                }
//            }
//        }

        if (x > 0 && this.beliefMap.getValue(x - 1, y))
            leftSum++;
        if (x < ((MCIResponseWorld) this.world).MAP_SIZE.getLeft() - 1 && this.beliefMap.getValue(x + 1, y))
            rightSum++;
        if (y > 0 && this.beliefMap.getValue(x, y - 1))
            upSum++;
        if (y < ((MCIResponseWorld) this.world).MAP_SIZE.getRight() - 1 && this.beliefMap.getValue(x, y + 1))
            downSum++;

//        int startingPointPenalty = this.location.distanceTo(this.startingLocation);

        if (x > 0)
            actionList.add(new ABCItem(new Move(Direction.LEFT), additionalBenefit, leftSum) {
                @Override
                public int secondUtility() {
                    return (0 - FireFighter.this.location.add(-1, 0).distanceTo(FireFighter.this.startingLocation));
                }
            });
        if (x < ((MCIResponseWorld) this.world).MAP_SIZE.getLeft() - 1)
            actionList.add(new ABCItem(new Move(Direction.RIGHT), additionalBenefit, rightSum) {
                @Override
                public int secondUtility() {
                    return (0 - FireFighter.this.location.add(1, 0).distanceTo(FireFighter.this.startingLocation));
                }
            });
        if (y > 0)
            actionList.add(new ABCItem(new Move(Direction.UP), additionalBenefit, upSum) {
                @Override
                public int secondUtility() {
                    return (0 - FireFighter.this.location.add(0, -1).distanceTo(FireFighter.this.startingLocation));
                }
            });
        if (y < ((MCIResponseWorld) this.world).MAP_SIZE.getRight() - 1)
            actionList.add(new ABCItem(new Move(Direction.DOWN), additionalBenefit, downSum) {
                @Override
                public int secondUtility() {
                    return (0 - FireFighter.this.location.add(0, 1).distanceTo(FireFighter.this.startingLocation));
                }
            });
    }

    @Override
    public void reset() {
        this.beliefMap.reset();
        for (int x = 0; x < ((MCIResponseWorld) this.world).MAP_SIZE.getLeft(); x++)
            for (int y = 0; y < ((MCIResponseWorld) this.world).MAP_SIZE.getRight(); y++)
                this.beliefMap.setValue(x, y, false);

        this.status = Status.Pullout;
//        this.idleTime = 0;
        this.location.setLocation(new Location(this.startingLocation));

        this.lastDirection = Direction.NONE;
        this.pulledoutPatient = null;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String setName(String name) {
        return this.name = name;
    }

    @Override
    public String getSymbol() {
        return this.name.replace("FireFighter", "F");
    }

    public void setStartingLocation(Location location) {
        this.startingLocation = new Location(location);
    }

    public Location getStartingLocation() {
        return this.startingLocation;
    }

    @Override
    public HashMap<String, Object> getProperties() {
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put("Location", new Location(this.location));
        properties.put("PulloutBeliefMap", this.beliefMap);
        return properties;
    }

    private final Action pulloutPatient = new Action(0) {

        @Override
        public void execute() {
            ArrayList<Patient> trappedPatients = ((MCIResponseWorld) FireFighter.this.world).getTrappedPatients(FireFighter.this.location);

            if (trappedPatients.size() > 1)
                FireFighter.this.status = Status.Pullout;
            else {
                FireFighter.this.status = Status.Complete;
                FireFighter.this.beliefMap.setValue(FireFighter.this.location, true);
            }

            if (trappedPatients.size() > 0) {
                FireFighter.this.pulledoutPatient = trappedPatients.get(0);
                pulledoutPatient.setStatus(Patient.Status.Pulledout);
//                FireFighter.this.idleTime = 0;
//                System.out.println(FireFighter.this.getName() + ": idle time reset");
            } else {
                FireFighter.this.beliefMap.setValue(FireFighter.this.location, true);
                FireFighter.this.pulledoutPatient = null;
//                FireFighter.this.idleTime++;
//                System.out.println(FireFighter.this.getName() + ": idle " + FireFighter.this.idleTime + " times");
            }
        }

        @Override
        public String getName() {
            return FireFighter.this.getName() + ": Pull out a patient";
        }
    };

    private final Action setHeadingLocation = new Action(0) {

        @Override
        public void execute() {
            if (FireFighter.this.headingLocation == null) {
                int mapX = ((MCIResponseWorld) FireFighter.this.world).MAP_SIZE.getLeft();
                int mapY = ((MCIResponseWorld) FireFighter.this.world).MAP_SIZE.getRight();

                PriorityQueue<Location> targetLocations = new PriorityQueue<Location>(mapX * mapY, new Comparator<Location>() {

                    @Override
                    public int compare(Location o1, Location o2) {
                        int v1 = FireFighter.this.location.distanceTo(o1);
                        int v2 = FireFighter.this.location.distanceTo(o2);

                        return v1 - v2;
                    }
                });

                for (int x = 0; x < mapX; x++)
                    for (int y = 0; y < mapY; y++)
                        if (!FireFighter.this.beliefMap.getValue(x, y))
                            targetLocations.offer(new Location(x, y));

                if (targetLocations.size() == 0)
                    return;

                FireFighter.this.headingLocation = targetLocations.poll();
            }
        }

        @Override
        public String getName() {
            return FireFighter.this.getName() + ": Set heading location to incomplete area";
        }
    };

    private class Move extends Action {
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
