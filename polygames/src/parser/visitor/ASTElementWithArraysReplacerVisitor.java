package parser.visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import parser.ast.*;
import parser.type.TypeInt;
import parser.visitor.utils.*;
import prism.PrismLangException;

/**
 * This visitor class is responsible for replacing all occurrences of {@link ExpressionArray} in the Abstract Syntax Tree (AST).
 * For example, a command like:
 * 
 * <code>[] guard -> array'[i] = 1;</code>
 * 
 * will be replaced by:
 * <pre>
 * {@code 
 * [] guard & i=0 -> array0' = 1;
 * [] guard & i=1 -> array1' = 1;
 * [] guard & i=2 -> array2' = 1;
 * ...
 * [] guard & i=N-1 -> arrayN-1' = 1;
 * }
 * </pre>
 * 
 * assuming the array is defined as <code>int array[N];</code>.
 * 
 * This is achieved by visiting various AST nodes and transforming {@link ExpressionArray} into their equivalent expressions
 * based on the provided array and index expressions.
 */
public class ASTElementWithArraysReplacerVisitor extends ASTTraverseModify {
  private ModulesFile modulesFile;

  @Override
  public Object visit(ModulesFile e) throws PrismLangException {
    this.modulesFile = e;

    return super.visit(e);
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

		if (e.getInvariant() != null) {
      e.setInvariant((Expression) (e.getInvariant().accept(this)));
    }

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

  @Override
  public Object visit(CommandWithArrays e) throws PrismLangException {
    ASTElementReplacer astElementReplacer = new CommandWithArraysReplacer();
    List<ASTElement> commands             = new ArrayList<>();

    commands.add(e.clone().deepCopy(new DeepCopy()));

    for (ExpressionArray expression : getExpressionArrays(e)) {
      List<IdentifierWithIndex> identifierWithIndexs = getIdentifierWithIndexs(expression);

      try {
        int i                = expression.evalIndex();
        identifierWithIndexs = List.of(identifierWithIndexs.get(i));
      } finally {
        commands = createASTElementsWithoutExpressionArrayIndex(commands, expression, identifierWithIndexs, astElementReplacer);
      }
    }

    return commands;
  }

  @Override
  public Object visit(FormulaList e) throws PrismLangException {
    ASTElementReplacer astElementReplacer = new FormulaWithArraysReplacer();
    int i, n;
    n = e.size();
    for (i = 0; i < n; i++) {
      Expression formula = (Expression) e.getFormula(i);
      Expression result  = formula.clone().deepCopy(new DeepCopy());

      List<ExpressionArray> expressionArrayIndexList = getExpressionArrays(formula);

      for (ExpressionArray expression : expressionArrayIndexList) {
        List<IdentifierWithIndex> identifierWithIndexs = getIdentifierWithIndexs(expression);

        try {
          int index            = expression.evalIndex();
          identifierWithIndexs = List.of(identifierWithIndexs.get(index));
        } catch (Exception exception) {
          // Nothing to do
        } finally {
          for (IdentifierWithIndex identifierWithIndex : identifierWithIndexs) {
            ASTElement identifier = identifierWithIndex.identifier();
            Integer indexValue    = identifierWithIndex.index();

            if (indexValue < identifierWithIndexs.size() - 1) {
              Expression guard = new ExpressionBinaryOp(
                ExpressionBinaryOp.EQ,
                new ExpressionLiteral(TypeInt.getInstance(), expression.evalIndex()),
                new ExpressionLiteral(TypeInt.getInstance(), indexValue)
              ); // index =? value
              Expression eThen = (Expression) astElementReplacer.replace(
                formula, expression, resolve(identifier, expression), indexValue
              );
              Expression eElse = result.clone().deepCopy(new DeepCopy());

              result = new ExpressionITE(guard, eThen, eElse);
            } else {
              result = (Expression) astElementReplacer.replace(
                result, expression, resolve(identifier, expression), indexValue
              );
            }
          }
        }

        formula = result;
      }

      if ( !expressionArrayIndexList.isEmpty() ) {
        e.setFormula(i, result);
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
        rewardStructItems.addAll((List<RewardStructItem>) rewardStructItem.accept(this));
      }
		}

    for (RewardStructItem rsi : rewardStructItems) {
      rewardStruct.addItem(rsi);
    }

		return rewardStruct;
	}

  @Override
  public Object visit(RewardStructItem e) throws PrismLangException {
    ASTElementReplacer astElementReplacer = new RewardStructItemWithArraysReplacer();
    List<ASTElement> rewardStructItems    = new ArrayList<>();

    rewardStructItems.add(e.clone().deepCopy(new DeepCopy()));

    for (ExpressionArray expression : getExpressionArrays(e)) {
      List<IdentifierWithIndex> identifierWithIndexs = getIdentifierWithIndexs(expression);

      try {
        int index            = expression.evalIndex();
        identifierWithIndexs = List.of(identifierWithIndexs.get(index));
      } catch (Exception exception) {
        // Nothing to do
      } finally {
        rewardStructItems = createASTElementsWithoutExpressionArrayIndex(
          rewardStructItems, expression, identifierWithIndexs, astElementReplacer
        );
      }
    }

    return rewardStructItems;
	}

  private List<ASTElement> createASTElementsWithoutExpressionArrayIndex(
      List<ASTElement>          astElements,
      ExpressionArray           expressionArrayIndex,
      List<IdentifierWithIndex> indexedIdentifiers,
      ASTElementReplacer        astElementReplacer
    ) throws PrismLangException {

    ListIterator<ASTElement> iterator = astElements.listIterator();

    while(iterator.hasNext()) {
      ASTElement astElement = iterator.next();

      iterator.remove();

      for (IdentifierWithIndex indexedIdentifier : indexedIdentifiers) {
        ASTElement identifier = indexedIdentifier.identifier();
        int index             = indexedIdentifier.index();

        iterator.add(astElementReplacer.replace(astElement, expressionArrayIndex, resolve(identifier, expressionArrayIndex), index));
      }
    }

    return astElements;
  }

  private Expression resolve(ASTElement identifier, ExpressionArray expression) throws PrismLangException {
    if (identifier instanceof Declaration) {
      Declaration declaration = (Declaration) identifier;

      ExpressionIdent result = new ExpressionIdent(declaration.getName());
      result.setPrime(expression.getPrime());

      return result;
    } else if (identifier instanceof ExpressionConstant) {
      ExpressionConstant constant = (ExpressionConstant) identifier;

      ConstantList constantList = modulesFile.getConstantList();
      int indexOfConstant       = constantList.getConstantIndex(constant.getName());

      ExpressionLiteral result = new ExpressionLiteral(
        constantList.getConstantType(indexOfConstant), constantList.getConstant(indexOfConstant).evaluate()
      );

      return result;
    }

    return null;
  }

  @SuppressWarnings("unchecked")
  private List<ExpressionArray> getExpressionArrays(ASTElement e) throws PrismLangException {
    ASTVisitor searcher              = new ASTElementSearcherVisitor(ExpressionArray.class);
    List<ExpressionArray> allMatches = (List<ExpressionArray>) e.accept(searcher);

    return allMatches;
  }

  private List<IdentifierWithIndex> getIdentifierWithIndexs(ExpressionIdent expressionIdent) {
    String prefix                = expressionIdent.getName();
    List<ASTElement> identifiers = IdentifiersFinder.identifiers(modulesFile, prefix, ComparisonType.STARTS_WITH);

    List<IdentifierWithIndex> identifierWithIndexs = IntStream.range(
      0, identifiers.size()).mapToObj(i -> new IdentifierWithIndex(identifiers.get(i), i)
    ).collect(Collectors.toList());

    return identifierWithIndexs;
  }
}
