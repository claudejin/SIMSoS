package simvasos.scenario.mciresponse.entity;

import simvasos.modelparsing.modeling.ABCPlus.ABCItem;
import simvasos.modelparsing.modeling.ABCPlus.ABCPlusCS;
import simvasos.scenario.mciresponse.MCIResponseScenario;
import simvasos.scenario.mciresponse.MCIResponseWorld;
import simvasos.simulation.component.Agent;
import simvasos.simulation.component.Message;
import simvasos.simulation.component.Scenario;
import simvasos.simulation.component.World;
import simvasos.simulation.util.Location;
import simvasos.simulation.util.Maptrix;

import java.util.*;

public class ControlTower extends ABCPlusCS {

    Maptrix<Integer> expectedPatientsMap = (Maptrix<Integer>) this.world.getResources().get("ExpectedPatientsMap");
    Maptrix<Boolean> pulloutBeliefMap = new Maptrix<Boolean>(Boolean.TYPE, ((MCIResponseWorld) this.world).MAP_SIZE.getLeft(), ((MCIResponseWorld) this.world).MAP_SIZE.getRight());
    ArrayList<FireFighter> fireFighters = new ArrayList<FireFighter>();

    public ControlTower(World world, String name) {
        super(world);

        this.name = name;

        this.reset();
    }

    @Override
    protected void observeEnvironment() {
        // It cannot observe environment because ControlTower has no physical existence
    }

    @Override
    protected void consumeInformation() {
        for (Message message : this.incomingInformation) {
            // Pullout report from FireFighters
            if (message.sender.startsWith("FireFighter") && message.purpose == Message.Purpose.Delivery && message.data.containsKey("PulloutLocation")) {
                Location pulloutLocation = (Location) message.data.get("PulloutLocation");
                this.pulloutBeliefMap.setValue(pulloutLocation, true);
//                Maptrix<Boolean> othersBeliefMap = (Maptrix<Boolean>) message.data.get("PulloutBelief");
//
//                boolean localBelief = false;
//                for (int x = 0; x < MCIResponseWorld.MAP_SIZE.getLeft(); x++)
//                    for (int y = 0; y < MCIResponseWorld.MAP_SIZE.getRight(); y++) {
//                        localBelief = this.pulloutBeliefMap.getValue(x, y);
//                        localBelief = localBelief || othersBeliefMap.getValue(x, y);
//                        this.pulloutBeliefMap.setValue(x, y, localBelief);
//                    }
            } else if (message.sender.startsWith("FireFighter") && message.purpose == Message.Purpose.Response && message.data.containsKey("Accepted")) {
                boolean accepted = (boolean) message.data.get("Accepted");

                if (accepted) {
                    Location markedLocation = (Location) message.data.get("HeadingLocation");
                    this.pulloutBeliefMap.setValue(markedLocation, true);
                }
            }
        }
    }

    @Override
    protected void generateActiveImmediateActions() {
        // Share pullout belief
        switch (this.sosType) {
            case Acknowledged:
            case Directed:
                Message beliefShare = new Message();
                beliefShare.name = "Share Pullout belief";
                beliefShare.sender = this.getName();
                beliefShare.receiver = "FireFighter";
//                beliefShare.location = this.location;
                beliefShare.purpose = Message.Purpose.Delivery;
                beliefShare.data.put("PulloutBelief", this.pulloutBeliefMap);

                this.immediateActionList.add(new ABCItem(new SendMessage(beliefShare), 5, 1));
        }

        // Direct
        switch (this.sosType) {
            case Directed:
            case Acknowledged:
                Collections.shuffle(this.fireFighters, this.world.random);
                int mapX = ((MCIResponseWorld) this.world).MAP_SIZE.getLeft();
                int mapY = ((MCIResponseWorld) this.world).MAP_SIZE.getRight();

                for (FireFighter fireFighter : this.fireFighters) {
                    if (fireFighter.getSoSType() == Scenario.SoSType.Virtual || fireFighter.getSoSType() == Scenario.SoSType.Collaborative)
                        continue;

                    Location fLocation = (Location) fireFighter.getProperties().get("Location");
                    Location startingLocation = (Location) fireFighter.getStartingLocation();

                    PriorityQueue<Location> targetLocations = new PriorityQueue<Location>(mapX * mapY, new Comparator<Location>() {

                        @Override
                        public int compare(Location o1, Location o2) {
                            int v1 = fLocation.distanceTo(o1);
                            int v2 = fLocation.distanceTo(o2);

                            if (v1 == v2)
                                return startingLocation.distanceTo(o1) - startingLocation.distanceTo(o2);

                            return v1 - v2;
                        }
                    });

                    for (int x = 0; x < mapX; x++)
                        for (int y = 0; y < mapY; y++)
                            if (!this.pulloutBeliefMap.getValue(x, y))
                                targetLocations.offer(new Location(x, y));

                    if (targetLocations.size() == 0)
                        break;

                    Message direction = new Message();
                    direction.name = "Direct heading location";
                    direction.sender = this.getName();
                    direction.receiver = fireFighter.getName();
                    if (fireFighter.getSoSType() == MCIResponseScenario.SoSType.Directed)
                        direction.purpose = Message.Purpose.Order;
                    else if (fireFighter.getSoSType() == MCIResponseScenario.SoSType.Acknowledged) {
                        direction.purpose = Message.Purpose.ReqAction;
                        direction.data.put("AdditionalBenefit", (mapX + mapY) / 4);
                    }

                    direction.data.put("HeadingLocation", targetLocations.poll());

                    this.immediateActionList.add(new ABCItem(new SendMessage(direction), 0, 1));
                }
        }
    }

    @Override
    protected void generatePassiveImmediateActions() {

    }

    @Override
    protected void generateNormalActions() {

    }

    @Override
    public void reset() {
        ArrayList<Agent> agents = this.world.getAgents();
        this.fireFighters.clear();
        for (Agent agent : agents)
            if (agent instanceof FireFighter)
                this.fireFighters.add((FireFighter) agent);

        this.pulloutBeliefMap.reset();
        for (int x = 0; x < ((MCIResponseWorld) this.world).MAP_SIZE.getLeft(); x++)
            for (int y = 0; y < ((MCIResponseWorld) this.world).MAP_SIZE.getRight(); y++)
                this.pulloutBeliefMap.setValue(x, y, false);
        this.sosType = Scenario.SoSType.Acknowledged;
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
        return "C";
    }

    @Override
    public HashMap<String, Object> getProperties() {
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put("PulloutBeliefMap", this.pulloutBeliefMap);
        return properties;
    }
}
