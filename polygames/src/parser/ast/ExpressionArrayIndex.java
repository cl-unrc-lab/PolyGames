package parser.ast;

import parser.EvaluateContext;
import parser.type.TypeInt;
import parser.visitor.ASTVisitor;
import parser.visitor.DeepCopy;
import prism.PrismLangException;

public class ExpressionArrayIndex extends ExpressionIdent {
  private String  name; // name of the array
  private Expression i = new ExpressionLiteral(TypeInt.getInstance(), 0);
  private Expression j = new ExpressionLiteral(TypeInt.getInstance(), 0);
  private int lineLength;

  public ExpressionArrayIndex(String name, Expression i, Expression j, int lineLength) {
    this.name       = name;
    this.prime      = false;
    this.i          = i;
    this.j          = j;
    this.lineLength = lineLength;
  }

  public String name() {
    return name;
  }

  public boolean prime() {
    return prime;
  }

  public int index() throws PrismLangException {
    return i.evaluateInt() * lineLength + j.evaluateInt();
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
    return null;
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
    StringBuilder sb = new StringBuilder();
    sb.append(this.name);

    if (this.prime) {
      sb.append("'");
    }

    // Assume `i` and `j` are Expressions representing indices
    if (this.i != null) {
      sb.append("[").append(this.i);
      if (this.j != null) {
        sb.append("][").append(this.j);
      }
      sb.append("]");
    }

    return sb.toString();
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

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }

    if (! (obj instanceof ExpressionArrayIndex)) {
      return false;
    }

    ExpressionArrayIndex other = (ExpressionArrayIndex) obj;
    try {
      return name.equals(other.name()) && index() == other.index() && prime == other.prime();
    } catch (PrismLangException e) {
      e.printStackTrace();
    }

    return false;
  }
}
