package parser.ast;

import parser.type.Type;
import parser.visitor.ASTVisitor;
import parser.visitor.DeepCopy;
import prism.PrismLangException;

public class Constant extends ASTElement {
  private String name;
  private Expression expression;
  private Type type;

  public Constant(String name, Expression expression, Type type) {
    this.name       = name;
    this.expression = expression;
    this.type       = type;
  }

  public String name() {
    return name;
  }

  public Expression expression() {
    return expression;
  }

  public Type type() {
    return type;
  }

  public Object evaluate() throws PrismLangException {
    return expression.evaluate();
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public Object accept(ASTVisitor v) throws PrismLangException {
    // This method shouldn't be called
    throw new UnsupportedOperationException("Unimplemented method 'accept'");
  }

  @Override
  public ASTElement deepCopy(DeepCopy copier) throws PrismLangException {
    // This method shouldn't be called
    throw new UnsupportedOperationException("Unimplemented method 'deepCopy'");
  }
}
