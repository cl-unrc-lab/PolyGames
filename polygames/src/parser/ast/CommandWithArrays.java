package parser.ast;

import java.util.ArrayList;
import java.util.List;

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

  public Command ConvertToCommand() {
    Command command = new Command();

    command.setSynchs(getSynchs());
    command.setGuard(getGuard());
    command.setUpdates(getUpdates());

    return command;
  }

  public List<ExpressionArrayIndexing> arrayIndexingExpressions() {
    return new ArrayList<ExpressionArrayIndexing>();
  }

}
