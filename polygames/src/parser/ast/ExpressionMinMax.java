package parser.ast;

import parser.EvaluateContext;
import parser.visitor.ASTVisitor;
import parser.visitor.DeepCopy;
import prism.PrismLangException;

public class ExpressionMinMax extends Expression {

  private int function;
  private Expression left;
  private Expression right;

  public static final int MIN = 1;
	public static final int MAX = 2;

  public ExpressionMinMax() {}

  public void setFunction(int function) {
    this.function = function;
  }

  public int function() {
    return function;
  }

  public void setLeft(Expression left) {
    this.left = left;
  }

  public void setRight(Expression right) {
    this.right = right;
  }

  public Expression left() {
    return left;
  }

  public Expression right() {
    return right;
  }

  @Override
  public Object accept(ASTVisitor v) throws PrismLangException {
    return v.visit(this);
  }

  @Override
  public String toString() {
    String functionString = function == ExpressionMinMax.MAX ? "max" : "min";

    return functionString + "(" + left + ", " + right + ")";
  }

  @Override
  public boolean isConstant() {
    return false;
  }

  @Override
  public boolean isProposition() {
    return false;
  }

  @Override
  public Object evaluate(EvaluateContext ec) throws PrismLangException {
    throw new UnsupportedOperationException("Unimplemented method 'evaluate'");
  }

  @Override
  public boolean returnsSingleValue() {
    return false;
  }

  @Override
  public Expression deepCopy(DeepCopy copier) throws PrismLangException {
    return this;
  }
  
}