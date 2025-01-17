package parser.ast;

import parser.visitor.ASTVisitor;
import parser.visitor.DeepCopy;
import prism.PrismLangException;

public class CommandWithArrays extends Command {

  public CommandWithArrays() {
    super();
  }

  @Override
  public Object accept(ASTVisitor v) throws PrismLangException {
		return v.visit(this);
	}

  @Override
	public Command deepCopy(DeepCopy copier) throws PrismLangException {
    Command command = new Command();
    command.setSynchs(
      getSynchs()
    );

    command.setGuard(
      copier.copy(getGuard())
    );

    command.setUpdates(
      copier.copy(getUpdates())
    );

		return command;
	}
}
