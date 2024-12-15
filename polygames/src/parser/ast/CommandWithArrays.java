package parser.ast;

import parser.visitor.ASTVisitor;
import prism.PrismLangException;

public class CommandWithArrays extends Command {

  public CommandWithArrays() {
    super();
  }

  @Override
  public Object accept(ASTVisitor v) throws PrismLangException {
		return v.visit(this);
	}
}
