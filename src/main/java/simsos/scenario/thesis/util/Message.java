package simsos.scenario.thesis.util;

import java.util.HashMap;

public class Message {
    public enum Purpose {ReqInfo, ReqAction, Response, Delivery, Order}

    public String name;

    public String sender;
    public String receiver;
    public Purpose purpose;
    public HashMap<String, Object> data = new HashMap<String, Object>();
    public int additionalBenefit;
    public int reducedCost;
    public int timestamp;

    public String context;
    public int trust;

    public String getName() {
        return this.name;
    }
}
