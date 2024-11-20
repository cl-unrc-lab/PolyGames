package parser.ast;

import parser.visitor.ASTVisitor;
import prism.PrismLangException;

public class RewardStructWithArrays extends RewardStruct {

	@Override
	public Object accept(ASTVisitor v) throws PrismLangException {
		return v.visit(this);
	}

  public void removeItem(RewardStructItem rsi) {
		items.remove(rsi);
    
		if (rsi.isTransitionReward()) numTransItems--; else numStateItems--;
	}
}
