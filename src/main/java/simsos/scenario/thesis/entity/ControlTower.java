package simsos.scenario.thesis.entity;

import com.sun.org.apache.xpath.internal.operations.Bool;
import simsos.scenario.thesis.ThesisScenario;
import simsos.scenario.thesis.ThesisWorld;
import simsos.scenario.thesis.util.*;
import simsos.simulation.component.Action;
import simsos.simulation.component.Agent;
import simsos.simulation.component.World;

import java.util.*;

public class ControlTower extends RationalEconomicCS {

    Maptrix<Integer> expectedPatientsMap = (Maptrix<Integer>) this.world.getResources().get("ExpectedPatientsMap");
    Maptrix<Boolean> pulloutBeliefMap = new Maptrix<Boolean>(Boolean.TYPE, ThesisWorld.MAP_SIZE.getLeft(), ThesisWorld.MAP_SIZE.getRight());
    ArrayList<FireFighter> fireFighters = new ArrayList<FireFighter>();

    public ControlTower(World world, String name) {
        super(world);

        this.name = name;

        ArrayList<Agent> agents = this.world.getAgents();
        for (Agent agent : agents)
            if (agent instanceof FireFighter)
                this.fireFighters.add((FireFighter) agent);

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
//                for (int x = 0; x < ThesisWorld.MAP_SIZE.getLeft(); x++)
//                    for (int y = 0; y < ThesisWorld.MAP_SIZE.getRight(); y++) {
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
        switch ((ThesisScenario.SoSType) this.world.getResources().get("Type")) {
            case Directed:
            case Acknowledged:
            case Collaborative:
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
        switch ((ThesisScenario.SoSType) this.world.getResources().get("Type")) {
            case Directed:
            case Acknowledged:
                Collections.shuffle(this.fireFighters, this.world.random);
                int mapX = ((ThesisWorld) this.world).MAP_SIZE.getLeft();
                int mapY = ((ThesisWorld) this.world).MAP_SIZE.getRight();

                for (FireFighter fireFighter : this.fireFighters) {
                    Location fLocation = (Location) fireFighter.getProperties().get("Location");
                    ArrayList<Location> targetLocations = new ArrayList<Location>();
                    for (int x = 0; x < mapX; x++)
                        for (int y = 0; y < mapY; y++)
                            if (!this.pulloutBeliefMap.getValue(x, y))
                                targetLocations.add(new Location(x, y));

                    Collections.shuffle(targetLocations, this.world.random);
                    targetLocations.sort(new Comparator<Location>() {

                        @Override
                        public int compare(Location o1, Location o2) {
                            int v1 = calculateMoveCost(fLocation, o1);
                            int v2 = calculateMoveCost(fLocation, o2);

                            return v1 - v2;
                        }
                    });

                    Message direction = new Message();
                    direction.name = "Direct heading location";
                    direction.sender = this.getName();
                    direction.receiver = fireFighter.getName();
                    if (this.world.getResources().get("Type") == ThesisScenario.SoSType.Directed)
                        direction.purpose = Message.Purpose.Order;
                    else if (this.world.getResources().get("Type") == ThesisScenario.SoSType.Acknowledged) {
                        direction.purpose = Message.Purpose.ReqAction;
                        direction.data.put("AdditionalBenefit", (mapX + mapY) / 4);
                    }

                    direction.data.put("HeadingLocation", targetLocations.get(0));

                    this.immediateActionList.add(new ABCItem(new SendMessage(direction), 0, 1));
                }
        }
    }

    public int calculateMoveCost(Location currentLocation, Location headingLocation) {
        int totalCost = 0;
        // Distance cost
        totalCost += currentLocation.distanceTo(headingLocation);
        // Belief cost
        totalCost -= this.expectedPatientsMap.getValue(headingLocation.getX(), headingLocation.getY()) * 4;

        return totalCost;
    }

    @Override
    protected void generatePassiveImmediateActions() {

    }

    @Override
    protected void generateNormalActions() {

    }

    @Override
    public void reset() {
        this.pulloutBeliefMap.reset();
        for (int x = 0; x < ThesisWorld.MAP_SIZE.getLeft(); x++)
            for (int y = 0; y < ThesisWorld.MAP_SIZE.getRight(); y++)
                this.pulloutBeliefMap.setValue(x, y, false);
    }

    @Override
    public String getName() {
        return this.name;
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
