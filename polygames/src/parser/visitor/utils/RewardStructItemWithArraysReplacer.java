package parser.visitor.utils;

import parser.ast.*;
import parser.type.TypeInt;
import parser.visitor.DeepCopy;
import prism.PrismLangException;

public class RewardStructItemWithArraysReplacer extends ASTElementReplacer {
  public RewardStructItemWithArraysReplacer() {}

  @Override
  public ASTElement replace(ASTElement astElement, ExpressionArrayIndex expressionArrayIndex, Expression expression, int index)
      throws PrismLangException {
        
    setExpression(expression);
    setExpressionArrayIndex(expressionArrayIndex);

    RewardStructItem rewardStructItem = (RewardStructItem) astElement.clone().deepCopy(new DeepCopy()); // astElement.accept(new DeepCopy());
    Expression guard =
      ExpressionBinaryOp.And(
        (Expression) rewardStructItem.getStates().accept(this), new ExpressionBinaryOp(
          ExpressionBinaryOp.EQ,
          new ExpressionLiteral(TypeInt.getInstance(), expressionArrayIndex.index()),
          new ExpressionLiteral(TypeInt.getInstance(), index)
        )
      );

    RewardStructItem result = new RewardStructItem(rewardStructItem.getSynchs(), guard, (Expression) rewardStructItem.getReward().accept(this));
    
    return result;
  }
}
