package parser.ast;

import parser.visitor.ASTVisitor;
import parser.visitor.DeepCopy;
import prism.PrismLangException;

public class RewardStructWithArrays extends RewardStruct {

	public void removeItem(RewardStructItem rewardStructItem) {
		items.remove(rewardStructItem);
    
		if (rewardStructItem.isTransitionReward()) numTransItems--; else numStateItems--;
	}

	@Override
	public Object accept(ASTVisitor v) throws PrismLangException {
		return v.visit(this);
	}

	@Override
	public RewardStruct deepCopy(DeepCopy copier) throws PrismLangException {
		RewardStruct rewardStruct = new RewardStruct();
    
		for (RewardStructItem rewardStructItem : copier.copyAll(items))
			rewardStruct.addItem(rewardStructItem);

		return rewardStruct;
	}
}
