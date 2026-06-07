package parser.visitor.utils;

import parser.ast.*;
import parser.visitor.DeepCopy;
import prism.PrismLangException;

public class FormulaWithArraysReplacer extends ASTElementReplacer {
  public FormulaWithArraysReplacer() {}

  @Override
  public ASTElement replace(ASTElement astElement, ExpressionArray expressionArrayIndex, Expression expression, int index)
      throws PrismLangException {
    setExpression(expression);
    setExpressionArrayIndex(expressionArrayIndex);

    Expression formula = (Expression) astElement.clone().deepCopy(new DeepCopy()); // astElement.accept(new DeepCopy());
    formula = (Expression) formula.accept(this);

    return formula;
  }
}
