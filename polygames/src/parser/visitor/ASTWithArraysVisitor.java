package parser.visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

import parser.ast.Command;
import parser.ast.CommandWithArrays;
import parser.ast.ConstantList;
import parser.ast.Declaration;
import parser.ast.Expression;
import parser.ast.ExpressionArrayIndexing;
import parser.ast.ExpressionBinaryOp;
import parser.ast.ExpressionIdent;
import parser.ast.ExpressionLiteral;
import parser.ast.FormulaList;
import parser.ast.LabelList;
import parser.ast.Module;
import parser.ast.ModulesFile;
import parser.ast.Observable;
import parser.ast.ObservableVars;
import parser.ast.Player;
import parser.ast.RewardStruct;
import parser.ast.RewardStructItem;
import parser.ast.RewardStructWithArrays;
import parser.ast.SystemDefn;
import parser.ast.UncertainUpdates;
import parser.ast.Update;
import parser.ast.UpdateElement;
import parser.ast.Updates;
import prism.PrismLangException;

public class ASTWithArraysVisitor extends ASTTraverseModify {
  private ModulesFile modulesFile;
  private RewardStructWithArrays rewardStruct;
  private ExpressionArrayIndexing replaced;
  private ExpressionIdent replacer;

  @Override
  public Object visit(ModulesFile e) throws PrismLangException {
    this.modulesFile = e;

		int i, n;
		if (e.getFormulaList() != null) e.setFormulaList((FormulaList)(e.getFormulaList().accept(this)));
		if (e.getLabelList() != null) e.setLabelList((LabelList)(e.getLabelList().accept(this)));
		if (e.getConstantList() != null) e.setConstantList((ConstantList)(e.getConstantList().accept(this)));
		n = e.getNumGlobals();
		for (i = 0; i < n; i++) {
			if (e.getGlobal(i) != null) e.setGlobal(i, (Declaration)(e.getGlobal(i).accept(this)));
		}
		n = e.getNumModules();
		for (i = 0; i < n; i++) {
			if (e.getModule(i) != null) e.setModule(i, (parser.ast.Module)(e.getModule(i).accept(this)));
		}
		n = e.getNumSystemDefns();
		for (i = 0; i < n; i++) {
			if (e.getSystemDefn(i) != null) e.setSystemDefn(i, (SystemDefn)(e.getSystemDefn(i).accept(this)), e.getSystemDefnName(i));
		}
		n = e.getNumRewardStructs();
		for (i = 0; i < n; i++) {
			if (e.getRewardStruct(i) != null) e.setRewardStruct(i, (RewardStruct)(e.getRewardStruct(i).accept(this)));
		}
		if (e.getInitialStates() != null) e.setInitialStates((Expression)(e.getInitialStates().accept(this)));
		n = e.getNumObservableVarLists();
		for (i = 0; i < n; i++) {
			if (e.getObservableVarList(i) != null) e.setObservableVarList(i, (ObservableVars) e.getObservableVarList(i).accept(this));
		}
		n = e.getNumObservableDefinitions();
		for (i = 0; i < n; i++) {
			if (e.getObservableDefinition(i) != null) e.setObservableDefinition(i, (Observable) e.getObservableDefinition(i).accept(this));
		}
		n = e.getNumPlayers();
		for (i = 0; i < n; i++) {
			if (e.getPlayer(i) != null) e.setPlayer(i, (Player)(e.getPlayer(i).accept(this)));
		}
    
		return e;
	}

  @Override
  public Object visit(RewardStructWithArrays e) throws PrismLangException {
    this.rewardStruct = e;

		int i, n;
		n = e.getNumItems();
		for (i = 0; i < n; i++) {
      RewardStructItem rewardStructItem = e.getRewardStructItem(0);
			if (rewardStructItem != null) {
        rewardStructItem.accept(this);
      }
		}

    RewardStruct rewardStruct = new RewardStruct();
    rewardStruct.setName(e.getName());
		n = e.getNumItems();
    for (i = 0; i < n; i++) {
			if (e.getRewardStructItem(i) != null) {
        rewardStruct.addItem(e.getRewardStructItem(i));
      }
		}

		return rewardStruct;
	}

  @SuppressWarnings("unchecked")
  @Override
  public Object visit(RewardStructItem e) throws PrismLangException {
    this.rewardStruct.removeItem(e);

    ASTExpressionArrayIndexingVisitor checker = new ASTExpressionArrayIndexingVisitor();
    List<ExpressionArrayIndexing> arrayIndexingExpressions = (List<ExpressionArrayIndexing>) checker.visit(e);

    if (!arrayIndexingExpressions.isEmpty()) {
      List<RewardStructItem> rewardStructItems = new ArrayList<>();
      rewardStructItems.add(e);

      for (ExpressionArrayIndexing expression : arrayIndexingExpressions) {
        List<Declaration> matchingDeclarations  = matchingDeclarations(expression);
        ListIterator<RewardStructItem> iterator = rewardStructItems.listIterator();

        try {
          // try to evaluate the Expression array indexing
          int index = (int) expression.evaluate();
          RewardStructItem rewardStructItem = iterator.next();
          iterator.remove();
          iterator.add(
            replaceArrayIndexingExpressions(
              matchingDeclarations.stream().filter(declaration -> declaration.getName().equals(expression.name() + index)).findFirst().orElse(null), expression, index, rewardStructItem
            )
          );
        } catch (PrismLangException exception) {
          while(iterator.hasNext()) {
            RewardStructItem rewardStructItem = iterator.next();
            iterator.remove();
  
            int position = 0;
            for (Declaration declaration : matchingDeclarations) {
              RewardStructItem rsi = replaceArrayIndexingExpressions(declaration, expression, position, rewardStructItem);
              iterator.add(rsi);
  
              position++;
            }
          }
        }
      }

      for (RewardStructItem rewardStructItem : rewardStructItems)
        rewardStruct.addItem(rewardStructItem);

    } else {
      e.setStates((Expression)(e.getStates().accept(this)));
      e.setReward((Expression)(e.getReward().accept(this)));
      rewardStruct.addItem(e);
    }

    // TODO: remove the RewardStructItem from the RewardStruct and replace it with the new RewardStructItems
    return null;
	}

  @Override
  public Object visit(parser.ast.Module e) throws PrismLangException {
		int i, n;
		n = e.getNumDeclarations();
		for (i = 0; i < n; i++) {
			if (e.getDeclaration(i) != null) e.setDeclaration(i, (Declaration) (e.getDeclaration(i).accept(this)));
		}

		if (e.getInvariant() != null)
			e.setInvariant((Expression) (e.getInvariant().accept(this)));

    n = e.getNumCommands();
		for (i = 0; i < n; i++) {
      Command command = e.getCommand(0);

			if (command != null) {
        command.accept(this);

        e.removeCommand(command);
      }
		}

		return e;
	}
        
  @SuppressWarnings("unchecked")
  @Override
  public Object visit(CommandWithArrays e) throws PrismLangException {
    Module module = e.getParent();

    ASTExpressionArrayIndexingVisitor checker = new ASTExpressionArrayIndexingVisitor();

    // A CommandWithArray may have many ExpressionArrayIndexing
    List<ExpressionArrayIndexing> arrayIndexingExpressions = (List<ExpressionArrayIndexing>) checker.visit(e);
    if ( !arrayIndexingExpressions.isEmpty() ) { // Does it contain array indexing expressions?
      // If so, we need to replace this CommandWithArray with a set of Commands.

      List<Command> commands = new ArrayList<>();
      commands.add(e);

      for (ExpressionArrayIndexing expression : arrayIndexingExpressions) {
        List<Declaration> matchingDeclarations = matchingDeclarations(expression);
        ListIterator<Command> iterator         = commands.listIterator();

        try {
          // try to evaluate the Expression array indexing
          int index = (int) expression.evaluate();
          Command command = iterator.next();
          iterator.remove();
          iterator.add(
            replaceArrayIndexingExpressions(
              matchingDeclarations.stream().filter(declaration -> declaration.getName().equals(expression.name() + index))
                                           .findFirst()
                                           .orElse(null), expression, index, command
            )
          );
        } catch (PrismLangException exception) {
          while(iterator.hasNext()) {
            Command command = iterator.next();
            iterator.remove();
  
            int position = 0;
            for (Declaration declaration : matchingDeclarations) {
              iterator.add(
                replaceArrayIndexingExpressions(declaration, expression, position, command)
              );
  
              position++;
            }
          }
        }
      }

      for (Command command : commands) {
        module.addCommand(command);
      }

    } else {
      // Otherwise, we need to replace this CommandWithArray with Command
      Command command = new Command();

      command.setGuard(e.getGuard());
      command.setSynchs(e.getSynchs());
      command.setUpdates(e.getUpdates());

      module.addCommand(command);
    }

    return null;
  }

  @Override
  public Object visit(UncertainUpdates e) throws PrismLangException {
		int i, n;
		n = e.getNumUpdates();

    Expression probability = null;
    Update update = null;

		for (i = 0; i < n; i++) {
      probability = e.getProbability(i);
			if (probability != null) {
        e.setProbability(i, (Expression) (probability.accept(this)));
      }

      update = e.getUpdate(i);
			if (update != null) {
        e.setUpdate(i, (Update) (update.accept(this)));
      }
		}

		return e;
	}

  @Override
  public Object visit(UpdateElement e) throws PrismLangException {
    ExpressionIdent assigned = e.getVarIdent();
    if (assigned != null) {
      e.setVarIdent((ExpressionIdent) assigned.accept(this));
    }

    Expression expression = e.getExpression();
    if (expression != null) {
      e.setExpression((Expression) expression.accept(this));;
    }

    return e;
  }

  @Override
  public Object visit(ExpressionArrayIndexing e) throws PrismLangException {
    if (e == replaced) {
      return replacer;
    } else {
      return e;
    }
  }

  private Command replaceArrayIndexingExpressions(Declaration declaration, ExpressionArrayIndexing expr, int position, Command command) throws PrismLangException {
    this.replaced = expr;
    this.replacer = new ExpressionIdent(declaration.getName());

    Command c = new Command();

    c.setGuard(
      // guard & index = position
      ExpressionBinaryOp.And(
        (Expression) command.getGuard().clone().accept(this), // this replaces the expression array indexing in the guard
        new ExpressionBinaryOp(5, expr.index(), new ExpressionLiteral(declaration.getType(), position))
      )
    );

    c.setUpdates((Updates) command.getUpdates().clone().accept(this)); // this replaces the expression array indexing in the updates

    c.setSynchs(command.getSynchs());

    // return a command without the expression array indexing
    return c;
  }

  private RewardStructItem replaceArrayIndexingExpressions(Declaration declaration, ExpressionArrayIndexing expr, int position, RewardStructItem rewardStructItem) throws PrismLangException {
    this.replaced = expr;
    this.replacer = new ExpressionIdent(declaration.getName());

    Expression guard =
      ExpressionBinaryOp.And(
        (Expression) rewardStructItem.getStates().clone().accept(this), new ExpressionBinaryOp(5, expr.index(), new ExpressionLiteral(declaration.getType(), position))
      );

    RewardStructItem rsi =
      new RewardStructItem(
        rewardStructItem.getSynchs(), guard, (Expression) rewardStructItem.getReward().accept(this)
      );

    return rsi;
  }

  private List<Declaration> matchingDeclarations(ExpressionArrayIndexing expr) {
    List<Declaration> matchingDeclarations = new ArrayList<>();

    int n = this.modulesFile.getNumModules();
    for (int i = 0; i < n; i++) {
      Module module = this.modulesFile.getModule(i);
      matchingDeclarations.addAll(
        module.getDeclarations().stream()
                                .filter(declaration -> declaration.getName()
                                .startsWith(expr.name()))
                                .collect(Collectors.toList())
      );
    }

    return matchingDeclarations;
  }
}
