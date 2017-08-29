package simsos.scenario.thesis.entity;

import simsos.scenario.thesis.ThesisScenario;
import simsos.scenario.thesis.ThesisWorld;
import simsos.scenario.thesis.util.ABCItem;
import simsos.scenario.thesis.util.Location;
import simsos.scenario.thesis.util.Message;
import simsos.scenario.thesis.util.TimedValue;
import simsos.simulation.component.Action;
import simsos.simulation.component.Agent;
import simsos.simulation.component.World;

import java.util.HashMap;

// Treat patients to recover

// Directed
// HS-Directed:
// HS-Report: Report the number of currently available beds

// Acknowledged
// HS-Directed:
// HS-Auton:
// HS-Share: Share the number of currently available beds to AMs
// HS-Report

// Collaborative
// HS-Auton
// HS-Share

// Virtual
// HS-Auton

public class Hospital extends RationalEconomicCS {

    private Location location;

    private final int maxCapacity;
    private int capacity;

    public Hospital(World world, String name, Location location, int maxCapacity) {
        super(world);

        this.name = name;
        this.location = location;
        this.maxCapacity = maxCapacity;

        this.reset();
    }

    @Override
    protected void observeEnvironment() {

    }

    @Override
    protected void consumeInformation() {

    }

    @Override
    protected void generateActiveImmediateActions() {

    }

    @Override
    protected void generatePassiveImmediateActions() {
        for (Message message : this.incomingRequests) {
            // I-ReportLocation
            if (message.sender.startsWith("Ambulance") && message.purpose == Message.Purpose.ReqInfo && message.data.containsKey("Capacity")) {
                switch ((ThesisScenario.SoSType) this.world.getResources().get("Type")) {
                    default:
                        Message locationReport = new Message();
                        locationReport.name = "Respond hospital capacity";
                        locationReport.sender = this.getName();
                        locationReport.receiver = message.sender;
                        locationReport.purpose = Message.Purpose.Response;
                        locationReport.data.put("Capacity", new TimedValue<Integer>(this.world.getTime(), this.capacity));

                        this.immediateActionList.add(new ABCItem(new SendMessage(locationReport), 0, 0));
                        break;
                }
            }
        }
    }

    @Override
    protected void generateNormalActions() {

    }

    @Override
    public void reset() {
        this.capacity = this.maxCapacity;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getSymbol() {
        return this.name.replace("Hospital", "H");
    }

    @Override
    public HashMap<String, Object> getProperties() {
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put("Capacity", this.capacity);
        properties.put("Location", this.location);
        return properties;
    }
}
