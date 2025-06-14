package parser.ast;

import parser.type.TypeInt;
import parser.visitor.ASTVisitor;
import parser.visitor.DeepCopy;
import prism.PrismLangException;

public class ExpressionArray extends ExpressionIdent {
  private Expression i = new ExpressionLiteral(TypeInt.getInstance(), 0);
  private Expression j = new ExpressionLiteral(TypeInt.getInstance(), 0);
  private int lineLength;

  public ExpressionArray(String name, Expression i, Expression j, int lineLength) {
    super(name);
    this.i          = i;
    this.j          = j;
    this.lineLength = lineLength;
  }

  public int evalIndex() throws PrismLangException {
    return i.evaluateInt() * lineLength + j.evaluateInt();
  }

  @Override
  public Object accept(ASTVisitor v) throws PrismLangException {
    return v.visit(this);
  }

  @Override
  public ExpressionIdent deepCopy(DeepCopy copier) {
    return this;
  }

  @Override
  public ExpressionIdent clone() {
    return this;
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
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }

    if (! (obj instanceof ExpressionArray)) {
      return false;
    }

    ExpressionArray other = (ExpressionArray) obj;
    try {
      return name.equals(other.getName()) && evalIndex() == other.evalIndex() && prime == other.getPrime();
    } catch (PrismLangException e) {
      e.printStackTrace();
    }

    return false;
  }
}
