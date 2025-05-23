package parser.ast;

import parser.EvaluateContext;
import parser.visitor.ASTVisitor;
import parser.visitor.DeepCopy;
import prism.PrismLangException;

public class ExpressionArrayIndex extends ExpressionIdent {
  private String  name;     // name of the array
  private boolean prime;    // whether this reference is to name' rather than name
  private Expression index; // index expression


  public ExpressionArrayIndex(String name, Expression index) {
    this.name  = name;
    this.prime = false;
    this.index = index;
  }

  public ExpressionArrayIndex(String name, Expression index, boolean prime) {
    this.name  = name;
    this.prime = prime;
    this.index = index;
  }

  public String name() {
    return name;
  }

  public boolean prime() {
    return prime;
  }

  public Expression index() {
    return index;
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
  public Object evaluate() throws PrismLangException {
    return index.evaluateInt();
  }

  @Override
  public Object evaluate(EvaluateContext ec) throws PrismLangException {
    throw new PrismLangException("Could not evaluate identifier", this);
  }

  @Override
  public boolean returnsSingleValue() {
    return false;
  }

  @Override
  public String toString() {
    if (this.prime) {
      return this.name + "'";
    }

    return this.name;
  }

  @Override
	public Object accept(ASTVisitor v) throws PrismLangException {
		return v.visit(this);
	}

  @Override
	public ExpressionIdent deepCopy(DeepCopy copier)
	{
		return this;
	}

	@Override
	public ExpressionIdent clone()
	{
		return this;
	}
}
