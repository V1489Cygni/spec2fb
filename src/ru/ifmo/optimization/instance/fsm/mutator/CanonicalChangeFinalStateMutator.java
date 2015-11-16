package ru.ifmo.optimization.instance.fsm.mutator;

import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.mutation.FsmMutation;

public class CanonicalChangeFinalStateMutator extends ChangeFinalStateMutator {
	
	public CanonicalChangeFinalStateMutator() {
		super();
	}
	
	@Override
	public MutatedInstanceMetaData<FSM, FsmMutation> apply(FSM individual) {
		MutatedInstanceMetaData<FSM, FsmMutation> mutatedInstanceMetaData = super.apply(individual);
		FSM canonicalMutatedFSM = mutatedInstanceMetaData.getInstance().getCanonicalFSM();
		MutatedInstanceMetaData<FSM, FsmMutation> result = new MutatedInstanceMetaData<FSM, FsmMutation>(
				canonicalMutatedFSM, individual.getMutations(canonicalMutatedFSM));
		return result;
	}
	
	@Override
	public FSM applySimple(FSM individual) {
		return super.applySimple(individual).getCanonicalFSM();
	}
}
