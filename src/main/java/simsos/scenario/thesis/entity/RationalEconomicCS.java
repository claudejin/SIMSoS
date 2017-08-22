package simsos.scenario.thesis.entity;

import simsos.scenario.thesis.util.ABCItem;
import simsos.simulation.component.Action;
import simsos.simulation.component.Agent;
import simsos.simulation.component.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public abstract class RationalEconomicCS extends Agent {

    protected ArrayList<ABCItem> immediateActionList = new ArrayList<ABCItem>();
    protected ArrayList<ABCItem> normalActionList = new ArrayList<ABCItem>();

    private Comparator<ABCItem> utilityComparator = new Comparator<ABCItem>() {
        @Override
        public int compare(ABCItem o1, ABCItem o2) {
            return o2.utility() - o1.utility();
        }
    };

    public RationalEconomicCS(World world) {
        super(world);
    }

    protected abstract void updateBelief();

    protected abstract void generateActionList();

    protected Action selectBestAction() {
        Action res = null;

        if (immediateActionList.size() > 0) {
            Collections.shuffle(immediateActionList);
            Collections.sort(immediateActionList, utilityComparator);
            res = immediateActionList.remove(0).action;
        } else if (normalActionList.size() > 0) {
            Collections.shuffle(normalActionList);
            Collections.sort(normalActionList, utilityComparator);
            res = normalActionList.remove(0).action;
        } else {
            res = Action.getNullAction(1, this.getName() + ": Null action");
        }

        return res;
    }

    @Override
    public Action step() {
        updateBelief();
        generateActionList();

        return selectBestAction();
    }

    public abstract void reset();
    public abstract String getName();

    public abstract HashMap<String, Object> getProperties();
}
