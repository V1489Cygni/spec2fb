package ru.ifmo.optimization.instance.multimaskefsm.task;

import ru.ifmo.optimization.instance.multimaskefsm.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Test {
    public static void main(String[] args) throws IOException {
        MultiMaskEfsmSkeleton.STATE_COUNT = 11;
        MultiMaskEfsmSkeleton.PREDICATE_COUNT = 10;
        MultiMaskEfsmSkeleton.INPUT_EVENT_COUNT = 2;
        MultiMaskEfsmSkeleton.MEANINGFUL_PREDICATES_COUNT = 10;
        MultiMaskEfsmSkeleton.TRANSITION_GROUPS_COUNT = 1;
        MultiMaskEfsmSkeleton.INPUT_EVENTS = new HashMap<>();
        MultiMaskEfsmSkeleton.INPUT_EVENTS.put("INIT", 0);
        MultiMaskEfsmSkeleton.INPUT_EVENTS.put("REQ", 1);
        MultiMaskEfsmSkeleton.PREDICATE_NAMES = new ArrayList<>();
        Collections.addAll(MultiMaskEfsmSkeleton.PREDICATE_NAMES, "c1Home", "c1End", "c2Home", "c2End",
                "vcHome", "vcEnd", "pp1", "pp2", "pp3", "vac");
        Map<String, Integer> p = new HashMap<>();
        for (int i = 0; i < MultiMaskEfsmSkeleton.PREDICATE_NAMES.size(); i++) {
            p.put(MultiMaskEfsmSkeleton.PREDICATE_NAMES.get(i), i);
        }
        MultiMaskEfsm efsm = new MultiMaskEfsm(new MultiMaskEfsmSkeleton(new State[]{
                new State(), new State(), new State(), new State(), new State(), new State(),
                new State(), new State(), new State(), new State(), new State()
        }), new OutputAction[]{
                new OutputAction("xxxxxxx", ""),
                new OutputAction("0000000", "INITO"),
                new OutputAction("xxxx1xx", "CNF"),
                new OutputAction("0000000", "CNF"),
                new OutputAction("1xxxxxx", "CNF"),
                new OutputAction("xx1xxxx", "CNF"),
                new OutputAction("1x1xxxx", "CNF"),
                new OutputAction("xxxxx10", "CNF"),
                new OutputAction("xxxx0xx", "CNF"),
                new OutputAction("xxxxx01", "CNF"),
                new OutputAction("0101xxx", "CNF")
        });

        State state = efsm.getState(0);
        TransitionGroup group = new TransitionGroup(0);
        group.setNewState(0, 1);
        state.addTransitionGroup("INIT", group);

        state = efsm.getState(1);
        group = new TransitionGroup(0);
        group.setNewState(0, 3);
        state.addTransitionGroup("INIT", group);
        state.addTransitionGroup("CNF", group);

        state = efsm.getState(2);
        group = new TransitionGroup(3);
        group.setMaskElement(p.get("c1Home"), true);
        group.setMaskElement(p.get("c2Home"), true);
        group.setMaskElement(p.get("vcEnd"), true);
        //group.setNewState();

        Writer writer = new FileWriter(new File("result.smv"));
        writer.write(TLFitness.getSMV(efsm));
        writer.close();
    }
}
