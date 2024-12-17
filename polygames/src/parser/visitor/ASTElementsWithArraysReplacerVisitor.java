package parser.visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import parser.ast.*;
import parser.visitor.utils.*;
import prism.PrismLangException;

public class ASTElementsWithArraysReplacerVisitor extends ASTTraverseModify {
  private ModulesFile modulesFile;

  private enum EvaluationType { EXACTLY, COMBINATION };

  public ASTElementsWithArraysReplacerVisitor(ModulesFile modulesFile) {
    this.modulesFile = modulesFile;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object visit(parser.ast.Module e) throws PrismLangException {
    List<Command> commands = null;
		int i, n;
		n = e.getNumDeclarations();
		for (i = 0; i < n; i++) {
			if (e.getDeclaration(i) != null) {
        e.setDeclaration(i, (Declaration) (e.getDeclaration(i).accept(this)));
      }
		}

		if (e.getInvariant() != null)
			e.setInvariant((Expression) (e.getInvariant().accept(this)));

    n = e.getNumCommands();
		for (i = 0; i < n; i++) {
      Command command = e.getCommand(0);

      e.removeCommand(command);

			if (command != null) {
        commands = (List<Command>) command.accept(this);

        for (Command c : commands) {
          e.addCommand(c);
        }
      }
		}

		return e;
	}

  @SuppressWarnings("unchecked")
  @Override
  public Object visit(CommandWithArrays e) throws PrismLangException {
    List<ASTElement> commands = new ArrayList<>();
    ASTVisitor searcher   = new ASTElementSearcherVisitor(ExpressionArrayIndex.class);
    List<ExpressionArrayIndex> expressionArrayIndexList = (List<ExpressionArrayIndex>) searcher.visit(e);

    if (!expressionArrayIndexList.isEmpty()) {
      commands.add(e);

      for (ExpressionArrayIndex expressionArrayIndex : expressionArrayIndexList) {
        try {
          commands =
            createASTElementsWithoutExpressionArrayIndex(
              commands, expressionArrayIndex, IdentifiersGetter.identifiers(
                this.modulesFile, expressionArrayIndex.name() + expressionArrayIndex.index().evaluateInt(), ComparisonType.EQUALS
                ), EvaluationType.EXACTLY, new CommandWithArraysReplacer()
            );
        } catch (Exception exception) {
          commands =
          createASTElementsWithoutExpressionArrayIndex(
            commands, expressionArrayIndex, IdentifiersGetter.identifiers(
              this.modulesFile, expressionArrayIndex.name(), ComparisonType.STARTS_WITH
              ), EvaluationType.COMBINATION, new CommandWithArraysReplacer()
          );
        }
      }

    } else {
      Command command = new Command();

      command.setGuard(e.getGuard());
      command.setSynchs(e.getSynchs());
      command.setUpdates(e.getUpdates());

      commands.add(command);
    }

    return commands;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object visit(FormulaList e) throws PrismLangException {
    // For the moment we only allow ExpressionArrayIndex that can be evaluated without context.
    List<ASTElement> formulaes = new ArrayList<>();

    ASTVisitor searcher = new ASTElementSearcherVisitor(ExpressionArrayIndex.class);
    List<ExpressionArrayIndex> expressionArrayIndexList = (List<ExpressionArrayIndex>) searcher.visit(e);

    if (!expressionArrayIndexList.isEmpty()) {
      for (ExpressionArrayIndex expressionArrayIndex : expressionArrayIndexList) {
        int i, n;
        n = e.size();
        for (i = 0; i < n; i++) {
          Expression formula = e.getFormula(i);
  
          if (formula != null) {
            formulaes.clear();
            formulaes.add(formula);

            try {
              formulaes =
                createASTElementsWithoutExpressionArrayIndex(
                  formulaes, expressionArrayIndex, IdentifiersGetter.identifiers(
                    this.modulesFile, expressionArrayIndex.name() + expressionArrayIndex.index().evaluateInt(), ComparisonType.EQUALS
                  ), EvaluationType.EXACTLY, new FormulaWithArraysReplacer()
                );
              
              e.setFormula(i, (Expression) formulaes.get(0));
            } catch (Exception exception) {
              exception.printStackTrace();
            }
          }
        }
      }
    } else {

      int i, n;
      n = e.size();
      for (i = 0; i < n; i++) {
        if (e.getFormula(i) != null) {
          e.setFormula(i, (Expression)(e.getFormula(i).accept(this)));
        }
      }
    }

    return e;
	}

  @SuppressWarnings("unchecked")
  @Override
  public Object visit(RewardStructWithArrays e) throws PrismLangException {
    RewardStruct rewardStruct = new RewardStruct();
    rewardStruct.setName(e.getName());

    List<RewardStructItem> rewardStructItems = new ArrayList<>();
		int i, n;
		n = e.getNumItems();
		for (i = 0; i < n; i++) {
      RewardStructItem rewardStructItem = e.getRewardStructItem(0);
      e.removeItem(rewardStructItem);
      
			if (rewardStructItem != null) {
        rewardStructItems.addAll(
          (List<RewardStructItem>) rewardStructItem.accept(this)
        );
      }
		}

    for (RewardStructItem rsi : rewardStructItems) {
      rewardStruct.addItem(rsi);
    }

		return rewardStruct;
	}

  @SuppressWarnings("unchecked")
  @Override
  public Object visit(RewardStructItem e) throws PrismLangException {
    List<ASTElement> rewardStructItems = new ArrayList<>();
    ASTVisitor searcher = new ASTElementSearcherVisitor(ExpressionArrayIndex.class);
    List<ExpressionArrayIndex> expressionArrayIndexList = (List<ExpressionArrayIndex>) searcher.visit(e);

    if (!expressionArrayIndexList.isEmpty()) {
      rewardStructItems.add(e);

      for (ExpressionArrayIndex expressionArrayIndex : expressionArrayIndexList) {
        try {
          rewardStructItems =
            createASTElementsWithoutExpressionArrayIndex(
              rewardStructItems, expressionArrayIndex, IdentifiersGetter.identifiers(
                this.modulesFile, expressionArrayIndex.name() + expressionArrayIndex.index().evaluateInt(), ComparisonType.EQUALS
              ), EvaluationType.EXACTLY, new RewardStructItemWithArraysReplacer()
            );
        } catch (Exception exception) {
          rewardStructItems =
            createASTElementsWithoutExpressionArrayIndex(
              rewardStructItems, expressionArrayIndex, IdentifiersGetter.identifiers(
                this.modulesFile, expressionArrayIndex.name(), ComparisonType.STARTS_WITH
              ), EvaluationType.COMBINATION, new RewardStructItemWithArraysReplacer()
            );
        }
      }

    } else {
      e.setStates((Expression)(e.getStates().accept(this)));
      e.setReward((Expression)(e.getReward().accept(this)));
      rewardStructItems.add(e);
    }

    return rewardStructItems;
	}

  private List<ASTElement> createASTElementsWithoutExpressionArrayIndex(
      List<ASTElement> astElements, ExpressionArrayIndex expressionArrayIndex, List<ASTElement> identifiers, EvaluationType evaluationType, ASTElementReplacer replacer
    ) throws PrismLangException {

    ListIterator<ASTElement> iterator = astElements.listIterator();

    while(iterator.hasNext()) {
      ASTElement astElement = iterator.next();

      if ( !identifiers.isEmpty() )
        iterator.remove();

      int index = 0;
      if (evaluationType.equals(EvaluationType.EXACTLY) && identifiers.size() == 1) {
        index = expressionArrayIndex.index().evaluateInt();
      }

      for (ASTElement identifier : identifiers) {
        iterator.add(replacer.replace(astElement, expressionArrayIndex, replacement(identifier, expressionArrayIndex), index));

        index++;
      }
    }

    return astElements;
  }

  private Expression replacement(ASTElement identifier, ExpressionArrayIndex expression) throws PrismLangException {
    if (identifier instanceof Declaration declaration) {
      ExpressionIdent result = new ExpressionIdent(declaration.getName());
      result.setPrime(expression.prime());
      return result;
    }
    
    if (identifier instanceof Constant constant) {
      return new ExpressionLiteral(constant.type(), constant.evaluate());
    }
    
    throw new PrismLangException("Error: Invalid identifier type: " + identifier.getClass().getSimpleName());
  }
}
