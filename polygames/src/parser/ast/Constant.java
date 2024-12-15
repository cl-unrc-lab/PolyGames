package parser.ast;

import parser.type.Type;
import prism.PrismLangException;

public class Constant {
  private String name;
  private Expression expression;
  private Type type;

  public Constant(String name, Expression expression, Type type) {
    this.name = name;
    this.expression = expression;
    this.type = type;
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

  public Double evaluate() throws PrismLangException {
    return expression.evaluateDouble();
  }

  @Override
  public String toString() {
    return name;
  }
}
