package parser.visitor.utils;

import parser.ast.*;
import parser.type.TypeInt;
import prism.PrismLangException;

public class RewardStructItemWithArraysReplacer extends ASTElementReplacer {
  public RewardStructItemWithArraysReplacer() {}

  @Override
  public ASTElement replace(ASTElement astElement, ExpressionArrayIndex expressionArrayIndex, Expression expression, int index)
      throws PrismLangException {
        
    setExpression(expression);
    setExpressionArrayIndex(expressionArrayIndex);

    RewardStructItem rewardStructItem = (RewardStructItem) astElement;
    Expression guard =
      ExpressionBinaryOp.And(
        (Expression) rewardStructItem.getStates().clone().accept(this), new ExpressionBinaryOp(
          5, expressionArrayIndex.index(), new ExpressionLiteral(TypeInt.getInstance(), index)
        )
      );

    RewardStructItem result = new RewardStructItem(rewardStructItem.getSynchs(), guard, (Expression) rewardStructItem.getReward().accept(this));
    
    return result;
  }
}
