package simsos.scenario.thesis.entity;

import simsos.scenario.thesis.ThesisScenario;
import simsos.scenario.thesis.ThesisWorld;
import simsos.scenario.thesis.util.*;
import simsos.simulation.component.Action;
import simsos.simulation.component.Agent;
import simsos.simulation.component.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ControlTower extends RationalEconomicCS {

    Maptrix<HashSet> pulloutBeliefMap = new Maptrix<HashSet>(HashSet.class, ThesisWorld.MAP_SIZE.getLeft(), ThesisWorld.MAP_SIZE.getRight());

    private final Message requestFireFighterPulloutReport = new Message();

    public ControlTower(World world, String name) {
        super(world);

        this.name = name;

        requestFireFighterPulloutReport.name = "Request FireFighter's Pullout report";
        requestFireFighterPulloutReport.sender = this.getName();
        requestFireFighterPulloutReport.receiver = "FireFighter";
        requestFireFighterPulloutReport.purpose = Message.Purpose.ReqInfo;
        requestFireFighterPulloutReport.data.put("Pulledout", null);

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
            if (message.sender.startsWith("FireFighter") && message.purpose == Message.Purpose.Response && message.data.containsKey("Pulledout")) {
                if (message.data.get("Pulledout") != null) {
                    Patient pulledoutPatient = (Patient) message.data.get("Pulledout");
                    Location pulledoutLocation = (Location) message.data.get("Location");

                    this.pulloutBeliefMap.getValue(pulledoutLocation).add(pulledoutPatient);
            }
            }
        }
    }

    @Override
    protected void generateActiveImmediateActions() {
        if (this.world.getResources().get("Type") == ThesisScenario.SoSType.Directed) {

        }

        if (this.world.getResources().get("Type") == ThesisScenario.SoSType.Acknowledged) {
            this.immediateActionList.add(new ABCItem(new SendMessage(this.requestFireFighterPulloutReport), 0, 0));
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
