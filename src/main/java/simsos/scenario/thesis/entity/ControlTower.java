package simsos.scenario.thesis.entity;

import simsos.scenario.thesis.ThesisWorld;
import simsos.scenario.thesis.util.*;
import simsos.simulation.component.Action;
import simsos.simulation.component.Agent;
import simsos.simulation.component.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ControlTower extends RationalEconomicCS {

    Maptrix<HashSet> discoveryBeliefMap = new Maptrix<HashSet>(HashSet.class, ThesisWorld.MAP_SIZE.getLeft(), ThesisWorld.MAP_SIZE.getRight());

    private final Message requestFireFighterDiscoveryReport = new Message();

    public ControlTower(World world, String name) {
        super(world);

        this.name = name;

        requestFireFighterDiscoveryReport.name = "Request FireFighter's discovery report";
        requestFireFighterDiscoveryReport.sender = this.getName();
        requestFireFighterDiscoveryReport.receiver = "FireFighter";
        requestFireFighterDiscoveryReport.purpose = Message.Purpose.ReqInfo;
        requestFireFighterDiscoveryReport.data.put("Discovered", null);

        this.reset();
    }

    @Override
    protected void updateBelief() {
        ArrayList<Message> processed = new ArrayList<Message>();

        for (Message message : this.incomingInformation) {
            if (message.sender.startsWith("FireFighter") &&
                    message.purpose == Message.Purpose.Response &&
                    message.data.containsKey("Discovered")) {
                processed.add(message);

                if (message.data.get("Discovered") != null) {
                    Patient newlyDiscovered = (Patient) message.data.get("Discovered");
                    Location discoveredLocation = (Location) message.data.get("Location");

                    this.discoveryBeliefMap.getValue(discoveredLocation.getX(), discoveredLocation.getY()).add(newlyDiscovered);
                }
            }
        }

        this.incomingInformation.removeAll(processed);
    }

    @Override
    protected void observeEnvironment() {

    }

    @Override
    protected void consumeInformation() {

    }

    @Override
    protected void generateActionList() {
        this.normalActionList.clear();

        if (this.phase == Phase.ActiveImmediateStep) {
            this.immediateActionList.add(new ABCItem(new SendMessage(this.requestFireFighterDiscoveryReport), 0, 0));

            this.phase = Phase.NormalStep;
        } else {
            this.normalActionList.add(new ABCItem(Action.getNullAction(1, "Control Tower: null"), 0, 0));

            this.phase = Phase.ActiveImmediateStep;
        }
    }

    @Override
    protected void generateActiveImmediateActions() {

    }

    @Override
    protected void generatePassiveImmediateActions() {

    }

    @Override
    protected void generateNormalActions() {

    }

    @Override
    public void reset() {

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
        properties.put("DiscoveryBeliefMap", this.discoveryBeliefMap);
        return properties;
    }
}
