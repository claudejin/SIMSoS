package simsos.scenario.thesis.entity;

import simsos.scenario.thesis.ThesisScenario;
import simsos.scenario.thesis.ThesisWorld;
import simsos.scenario.thesis.util.*;
import simsos.simulation.component.Action;
import simsos.simulation.component.Agent;
import simsos.simulation.component.World;

import java.util.*;

public class Ambulance extends RationalEconomicCS {

    Maptrix<Integer> expectedPatientsMap = (Maptrix<Integer>) this.world.getResources().get("ExpectedPatientsMap");
    Maptrix<HashSet> patientsBeliefMap = new Maptrix<HashSet>(HashSet.class, ThesisWorld.MAP_SIZE.getLeft(), ThesisWorld.MAP_SIZE.getRight());

    LinkedHashMap<String, Location> hospitalLocations = new LinkedHashMap<String, Location>();
    LinkedHashMap<String, TimedValue<Integer>> hospitalCapacities = new LinkedHashMap<String, TimedValue<Integer>>();

    private Location location;

    private enum Direction {NONE, LEFT, RIGHT, UP, DOWN}
    private Direction lastDirection;

    private Patient transportPatient = null;
    private Hospital transportHospital = null;

    public Ambulance(World world, String name) {
        super(world);

        this.name = name;
        this.reset();
    }

    public void setHospitalLocations(HashMap<String, Location> hospitalLocations) {
        this.hospitalLocations.clear();
        this.hospitalCapacities.clear();

        this.hospitalLocations.putAll(hospitalLocations);
        for (String hospitalName : hospitalLocations.keySet())
            this.hospitalCapacities.put(hospitalName, new TimedValue<Integer>(0, -1));
    }

    @Override
    protected void observeEnvironment() {
        // FireFighter observe current location and update already discovered patients
        Set<Patient> localBelief = this.patientsBeliefMap.getValue(this.location);
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
            // Capacity response from hospitals at this location
            if (message.sender.startsWith("Hospital") && message.purpose == Message.Purpose.Response && message.data.containsKey("Capacity")) {
                this.hospitalCapacities.get(message.sender).updateValue((TimedValue) message.data.get("Capacity"));
            }
        }

        for (String hospitalName : this.hospitalCapacities.keySet()) {
            System.out.println(">> " + hospitalName + ": Location " + this.hospitalLocations.get(hospitalName) + ", Capacity " + this.hospitalCapacities.get(hospitalName));
        }
    }

    @Override
    protected void generateActiveImmediateActions() {
        switch ((ThesisScenario.SoSType) this.world.getResources().get("Type")) {
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
//        if (this.transportPatient == null) {
//            // I-RequestHospitalCapacity
//
//            // I-StartTransport
//            switch ((ThesisScenario.SoSType) this.world.getResources().get("Type")) {
//                default:
//                    this.immediateActionList.add(new ABCItem(this.startTransportPatient, 10, 1));
//
//                    Message locationReport = new Message();
//                    locationReport.name = "Request hospital information";
//                    locationReport.sender = this.getName();
//                    locationReport.receiver = "Hospital";
//                    locationReport.purpose = Message.Purpose.ReqInfo;
//                    locationReport.data.put("Location", null);
//                    locationReport.data.put("Capacity", null);
//
//                    this.immediateActionList.add(new ABCItem(new SendMessage(locationReport), 10, 1));
//            }
//
//        } else if (this.transportHospital == null) {
//            // I-FindTargetHospital
//            switch ((ThesisScenario.SoSType) this.world.getResources().get("Type")) {
//                default:
//                    this.immediateActionList.add(new ABCItem(this.findTransportHospital, 9, 1));
//            }
//        }
    }

    @Override
    protected void generatePassiveImmediateActions() {

    }

    @Override
    protected void generateNormalActions() {

    }

    @Override
    public void reset() {
        this.patientsBeliefMap.reset();

        this.location = new Location(ThesisWorld.MAP_SIZE.getLeft() / 2, ThesisWorld.MAP_SIZE.getRight() / 2);
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
        return properties;
    }

//    private final Action startTransportPatient = new Action(0) {
//
//        @Override
//        public void execute() {
//            Set<Patient> discoveredPatients = ((ThesisWorld) Ambulance.this.world).getDiscoveredPatients(Ambulance.this.location);
//
//            if (!discoveredPatients.isEmpty()) {
//                Ambulance.this.transportPatient = ((ThesisWorld) Ambulance.this.world).getDiscoveredPatients(Ambulance.this.location).iterator().next();
//                Ambulance.this.transportHospital = null;
//            } else {
//                // Fail to start transporting a patient
//            }
//        }
//
//        @Override
//        public String getName() {
//            return Ambulance.this.getName() + ": Start transporting a patient";
//        }
//    };

//    private final Action findTransportHospital = new Action(0) {
//
//        @Override
//        public void execute() {
//
//        }
//
//        @Override
//        public String getName() {
//            return null;
//        }
//    };

//    public List sortByDistance(final Map<String, Map> hospitals){
//        List<String> list = new ArrayList();
//        list.addAll(hospitals.keySet());
//
//        Collections.sort(list,new Comparator(){
//
//            public int compare(Object o1,Object o2){
//                int v1 = ((Location) hospitals.get(o1).get("Location")).distanceTo(Ambulance.this.location);
//                int v2 = ((Location) hospitals.get(o2).get("Location")).distanceTo(Ambulance.this.location);
//
//                return v1 - v2;
//            }
//
//        });
//
//        return list;
//    }
}
