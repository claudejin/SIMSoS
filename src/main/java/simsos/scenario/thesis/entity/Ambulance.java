package simsos.scenario.thesis.entity;

import simsos.scenario.thesis.ThesisScenario.SoSType;
import simsos.scenario.thesis.ThesisWorld;
import simsos.scenario.thesis.util.*;
import simsos.simulation.component.Action;
import simsos.simulation.component.World;

import java.util.*;

public class Ambulance extends RationalEconomicCS {

    Maptrix<TimedValue> awaitBeliefMap = new Maptrix<TimedValue>(TimedValue.class, ThesisWorld.MAP_SIZE.getLeft(), ThesisWorld.MAP_SIZE.getRight());

    LinkedHashMap<String, Location> hospitalLocations = new LinkedHashMap<String, Location>();
    LinkedHashMap<String, TimedValue<Integer>> hospitalCapacities = new LinkedHashMap<String, TimedValue<Integer>>();

    private Location initialLocation;
    private Location location;

    private enum Direction {NONE, LEFT, RIGHT, UP, DOWN}
    private Direction lastDirection;

    private enum Status {EMPTY, OCCUPIED}
    private Status status;

    private Patient targetPatient = null;
    private Location headingLocation = null;

    public Ambulance(World world, String name, Location location) {
        super(world);

        this.name = name;
        this.initialLocation = location;
        this.reset();
    }

    public void setHospitalInformation(HashMap<String, Location> hospitalLocations, HashMap<String, Integer> hospitalCapacities) {
        this.hospitalLocations.clear();
        this.hospitalCapacities.clear();

        this.hospitalLocations.putAll(hospitalLocations);
        for (Map.Entry<String, Integer> entry : hospitalCapacities.entrySet())
            this.hospitalCapacities.put(entry.getKey(), new TimedValue<Integer>(0, entry.getValue()));
    }

    @Override
    protected void observeEnvironment() {
        // FireFighter observe current location and update already pulled out patients
        TimedValue<Integer> localAwaitBelief = this.awaitBeliefMap.getValue(this.location);

        Set<Patient> pulledoutPatients = ((ThesisWorld) this.world).getPulledoutPatients(this.location);
        localAwaitBelief.updateValue(new TimedValue<Integer>(this.world.getTime(), pulledoutPatients.size()));
    }

    @Override
    protected void consumeInformation() {
        for (Message message : this.incomingInformation) {
            // Capacity response from hospitals at this location
            if (message.sender.startsWith("Hospital") && message.purpose == Message.Purpose.Response && message.data.containsKey("Capacity")) {
                this.hospitalCapacities.get(message.sender).updateValue((TimedValue) message.data.get("Capacity"));

            // Hospitalize response from the hospital
            } else if (message.sender.startsWith("Hospital") && message.purpose == Message.Purpose.Response && message.data.containsKey("Hospitalized")) {
                boolean isHospitalized = (boolean) message.data.get("Hospitalized");
                if (isHospitalized) {
                    this.targetPatient = null;
                    this.headingLocation = null;

                    this.status = Status.EMPTY;
                    this.lastDirection = Direction.NONE;
                } else {

                }

            // Capacity belief and Await belief, shared from other ambulances at this location
            } else if (message.sender.startsWith("Ambulance") && message.purpose == Message.Purpose.Delivery && message.data.containsKey("HospitalCapacityBelief")) {
                LinkedHashMap<String, TimedValue<Integer>> othersCapacityBelief = (LinkedHashMap<String, TimedValue<Integer>>) message.data.get("HospitalCapacityBelief");
                Maptrix<TimedValue<Integer>> othersAwaitBeliefMap = (Maptrix<TimedValue<Integer>>) message.data.get("AwaitBeliefMap");

                for (Map.Entry<String, TimedValue<Integer>> entry : othersCapacityBelief.entrySet())
                    this.hospitalCapacities.get(entry.getKey()).updateValue(entry.getValue());

                for (int x = 0; x < ThesisWorld.MAP_SIZE.getLeft(); x++)
                    for (int y = 0; y < ThesisWorld.MAP_SIZE.getRight(); y++)
                        this.awaitBeliefMap.getValue(x, y).updateValue(othersAwaitBeliefMap.getValue(x, y));

            // Patient set request from ControlTower
            } else if (message.sender.equals("ControlTower") && message.purpose == Message.Purpose.ReqAction && message.data.containsKey("Patient")) {
//                this.immediateActionList.add(new ABCItem(new SetTargetPatient(some patient), 0, 0));
            // Patient set order from ControlTower
            } else if (message.sender.equals("ControlTower") && message.purpose == Message.Purpose.Order && message.data.containsKey("Patient")) {
//                this.immediateActionList.add(new ABCItem(new SetTargetPatient(some patient), 11, 0));
            }
        }
    }

    @Override
    protected void generateActiveImmediateActions() {
        // Request hospital capacity
        switch ((SoSType) this.world.getResources().get("Type")) {
            default:
                Message capacityRequest = new Message();
                capacityRequest.name = "Request hospital capacity";
                capacityRequest.sender = this.getName();
                capacityRequest.receiver = "Hospital";
                capacityRequest.location = this.location;
                capacityRequest.purpose = Message.Purpose.ReqInfo;
                capacityRequest.data.put("Capacity", null);

                this.immediateActionList.add(new ABCItem(new SendMessage(capacityRequest), 10, 1));
        }

        // Share hospital capacity
        switch ((SoSType) this.world.getResources().get("Type")) {
            case Acknowledged:
            case Collaborative:
                Message beliefShare = new Message();
                beliefShare.name = "Share hospital capacity belief";
                beliefShare.sender = this.getName();
                beliefShare.receiver = "Ambulance";
                beliefShare.location = this.location;
                beliefShare.purpose = Message.Purpose.Delivery;
                beliefShare.data.put("HospitalCapacityBelief", this.hospitalCapacities);
                beliefShare.data.put("AwaitBeliefMap", this.awaitBeliefMap);

                this.immediateActionList.add(new ABCItem(new SendMessage(beliefShare), 9, 1));
                break;
        }

        // Set a target patient at this location
        if (this.targetPatient == null) {
            switch ((SoSType) this.world.getResources().get("Type")) {
                case Acknowledged:
                case Collaborative:
                case Virtual:
                    this.immediateActionList.add(new ABCItem(new SetTargetPatient(null), 5, 0));
                    this.immediateActionList.add(new ABCItem(this.pickUpAndStartTransport, 4, 0));
            }

        // Try to pick up a patient and start transporting (when it meets the patient
        } else if (this.headingLocation.equals(this.location) && this.status == Status.EMPTY) {
            switch ((SoSType) this.world.getResources().get("Type")) {
                default:
                    this.immediateActionList.add(new ABCItem(this.pickUpAndStartTransport, 4, 0));
                    break;
            }

        // Try to release the patient to the hospital
        } else if (this.headingLocation.equals(this.location) && this.status == Status.OCCUPIED) {
            switch ((SoSType) this.world.getResources().get("Type")) {
                default:
                    Message releasePatientToHospital = new Message();
                    releasePatientToHospital.name = "Request hospitalizing this patient";
                    releasePatientToHospital.sender = this.getName();
                    releasePatientToHospital.receiver = "Hospital";
                    releasePatientToHospital.location = this.location;
                    releasePatientToHospital.purpose = Message.Purpose.ReqAction;
                    releasePatientToHospital.data.put("Patient", this.targetPatient);

                    this.immediateActionList.add(new ABCItem(new SendMessage(releasePatientToHospital), 3, 1));
                    break;
            }
        }
    }

    @Override
    protected void generatePassiveImmediateActions() {

    }

    @Override
    protected void generateNormalActions() {
        // N-Directed Moves
        if (this.world.getResources().get("Type") == SoSType.Directed) {

        }

        // N-Autonomous Moves
        if (this.world.getResources().get("Type") != SoSType.Directed) {
            // Search for patient to transport
            if (this.targetPatient == null) {
                // Random search
                if (Ambulance.this.location.getX() > 0 && lastDirection != Direction.RIGHT)
                    normalActionList.add(new ABCItem(new Move(Direction.LEFT), 0, calculateMoveCost(Direction.LEFT)));
                if (Ambulance.this.location.getX() < ThesisWorld.MAP_SIZE.getLeft() - 1 && lastDirection != Direction.LEFT)
                    normalActionList.add(new ABCItem(new Move(Direction.RIGHT), 0, calculateMoveCost(Direction.RIGHT)));
                if (Ambulance.this.location.getY() > 0 && lastDirection != Direction.DOWN)
                    normalActionList.add(new ABCItem(new Move(Direction.UP), 0, calculateMoveCost(Direction.UP)));
                if (Ambulance.this.location.getY() < ThesisWorld.MAP_SIZE.getRight() - 1 && lastDirection != Direction.UP)
                    normalActionList.add(new ABCItem(new Move(Direction.DOWN), 0, calculateMoveCost(Direction.DOWN)));

            // Heading to the patient to transport
            // OR Heading to the hospital to deliever
            } else {
                if (Ambulance.this.location.getX() > 0)
                    normalActionList.add(new ABCItem(new Move(Direction.LEFT), 0, calculateMoveCost(Direction.LEFT)));
                if (Ambulance.this.location.getX() < ThesisWorld.MAP_SIZE.getLeft() - 1)
                    normalActionList.add(new ABCItem(new Move(Direction.RIGHT), 0, calculateMoveCost(Direction.RIGHT)));
                if (Ambulance.this.location.getY() > 0)
                    normalActionList.add(new ABCItem(new Move(Direction.UP), 0, calculateMoveCost(Direction.UP)));
                if (Ambulance.this.location.getY() < ThesisWorld.MAP_SIZE.getRight() - 1)
                    normalActionList.add(new ABCItem(new Move(Direction.DOWN), 0, calculateMoveCost(Direction.DOWN)));
            }
        }
    }

    public int calculateMoveCost(Direction direction) {
        // Uncertainty
        int totalCost = this.world.random.nextInt(2);

        int deltaX, deltaY;
        if (direction == Direction.LEFT) {
            deltaX = -1; deltaY = 0;
        } else if (direction == Direction.RIGHT) {
            deltaX = 1; deltaY = 0;
        } else if (direction == Direction.UP) {
            deltaX = 0; deltaY = -1;
        } else {
            deltaX = 0; deltaY = 1;
        }

        if (this.headingLocation == null)
            // Belief cost
            totalCost -= (Integer) this.awaitBeliefMap.getValue(this.location.add(deltaX, deltaY)).getValue(); // Awaiting
        else
            totalCost += this.headingLocation.distanceTo(this.location.add(deltaX, deltaY));

        return totalCost;
    }

    @Override
    public void reset() {
        for (int x = 0; x < ThesisWorld.MAP_SIZE.getLeft(); x++)
            for (int y = 0; y < ThesisWorld.MAP_SIZE.getRight(); y++)
                this.awaitBeliefMap.setValue(x, y, new TimedValue<Integer>(0, 0));

        this.lastDirection = Direction.NONE;
        this.location = new Location(this.initialLocation);
        this.status = Status.EMPTY;
        this.targetPatient = null;
        this.headingLocation = null;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getSymbol() {
        return this.name.replace("Ambulance", "A");
    }

    @Override
    public HashMap<String, Object> getProperties() {
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put("Location", this.location);
        properties.put("AwaitBeliefMap", this.awaitBeliefMap);
        return properties;
    }

    protected class SetTargetPatient extends Action {
        Patient targetPatient;

        public SetTargetPatient(Patient patient) {
            super(0);

            this.targetPatient = targetPatient;
        }

        @Override
        public void execute() {
            if (Ambulance.this.targetPatient != null)
                return;

            if (this.targetPatient != null)
                Ambulance.this.targetPatient = this.targetPatient;
            else {
                Set<Patient> pulledoutPatients = ((ThesisWorld) Ambulance.this.world).getPulledoutPatients(Ambulance.this.location);

                if (!pulledoutPatients.isEmpty()) {
                    this.targetPatient = ((ThesisWorld) Ambulance.this.world).getPulledoutPatients(Ambulance.this.location).iterator().next();
                    Ambulance.this.targetPatient = this.targetPatient;
                    Ambulance.this.headingLocation = new Location(this.targetPatient.getLocation());
                }
            }
        }

        @Override
        public String getName() {
            return null;
        }
    }

    private final Action pickUpAndStartTransport = new Action(0) {

        @Override
        public void execute() {
            if (Ambulance.this.targetPatient == null)
                return;

            // Pick up and start transporting
            if (Ambulance.this.targetPatient.getStatus() == Patient.Status.Pulledout) {
                Ambulance.this.targetPatient.setStatus(Patient.Status.OnTransport);
                Ambulance.this.targetPatient.setLocation(Ambulance.this.location);
                Ambulance.this.headingLocation = Ambulance.this.getBestHospitalLocation();

                Ambulance.this.status = Status.OCCUPIED;

            // Fail to pick up and start transporting
            } else {
                Ambulance.this.targetPatient = null;
                Ambulance.this.headingLocation = null;

                Ambulance.this.status = Status.EMPTY;
            }
        }

        @Override
        public String getName() {
            return Ambulance.this.getName() + ": Pick up a patient and Start transporting";
        }
    };

    private Location getBestHospitalLocation() {
        if (this.hospitalLocations.isEmpty())
            return null;

        List<Map.Entry<String, Location>> list = new ArrayList();
        list.addAll(this.hospitalLocations.entrySet());
        Collections.shuffle(list, this.world.random);
        list.sort(new Comparator<Map.Entry<String, Location>>() {

            @Override
            public int compare(Map.Entry<String, Location> o1, Map.Entry<String, Location> o2) {
                int v1 = o1.getValue().distanceTo(Ambulance.this.location);
                int v2 = o2.getValue().distanceTo(Ambulance.this.location);

                return v1 - v2;
            }
        });

        return list.get(0).getValue();
    }

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
                    Ambulance.this.location.moveX(-1);
                    break;
                case RIGHT:
                    Ambulance.this.location.moveX(1);
                    break;
                case UP:
                    Ambulance.this.location.moveY(-1);
                    break;
                case DOWN:
                    Ambulance.this.location.moveY(1);
                    break;
                default:
                    System.out.println("FireFighter: Move Error"); // Error
            }

            Ambulance.this.lastDirection = this.direction;
        }

        @Override
        public String getName() {
            return null;
        }
    }
}