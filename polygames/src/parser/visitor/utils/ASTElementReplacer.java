package parser.visitor.utils;

import parser.ast.*;
import parser.visitor.ASTTraverseModify;
import prism.PrismLangException;

/**
 * This visitor replaces all instances of `expressionArrayIndex` within a single `astElement`
 * with a specified `expression`.
 */
public abstract class ASTElementReplacer extends ASTTraverseModify {
  private Expression           expression;
  private ExpressionArray expressionArrayIndex;
  /**
   * Creates a new ASTElement by copying the given ASTElement and omitting the 'expressionArrayIndex' value. 
   * The new ASTElement will use the provided index as the value for the 'expressionArrayIndex'.
   * @param element The ASTElement to be copied. It serves as the base element for the new ASTElement.
   * @param expressionArrayIndex The ExpressionArrayIndex that should be excluded from the new ASTElement.
   * @param expression The Expression that will replace the ExpressionArrayIndex.
   * @param index The index to be used as the new value for 'expressionArrayIndex' in the resulting ASTElement.
   * @return A new ASTElement with the same properties as the original, except for the 'expressionArrayIndex', 
   *         which is replaced by the provided index.
   */
  public abstract ASTElement replace(
    ASTElement astElement, ExpressionArray expressionArrayIndex, Expression expression, int index
  ) throws PrismLangException;

  public void setExpression(Expression expression) {
    this.expression = expression;
  }

  public void setExpressionArrayIndex(ExpressionArray expressionArrayIndex) {
    this.expressionArrayIndex = expressionArrayIndex;
  }

  @Override
  public Object visit(ExpressionArray e) throws PrismLangException {
    if (e == expressionArrayIndex) {
      return expression;
    } else {
      return e;
    }
  }
}
