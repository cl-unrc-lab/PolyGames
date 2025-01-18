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
 * This visitor class is responsible for replacing all occurrences of {@link ExpressionArrayIndex} in the Abstract Syntax Tree (AST).
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
 * This is achieved by visiting various AST nodes and transforming {@link ExpressionArrayIndex} into their equivalent expressions
 * based on the provided array and index expressions.
 */
public class ASTElementsWithArraysReplacerVisitor extends ASTTraverseModify {
  private ModulesFile modulesFile;

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
    ASTElementReplacer replacer = new CommandWithArraysReplacer();
    List<ASTElement> commands   = new ArrayList<>();
    ASTVisitor searcher         = new ASTElementSearcherVisitor(ExpressionArrayIndex.class);
    List<ExpressionArrayIndex> expressionArrayIndexList = (List<ExpressionArrayIndex>) e.accept(searcher);

    commands.add(e.clone().deepCopy(new DeepCopy()));

    for (ExpressionArrayIndex expressionArrayIndex : expressionArrayIndexList) {
      String prefix = expressionArrayIndex.name();
      List<ASTElement> identifiers = IdentifiersGetter.identifiers(modulesFile, prefix, ComparisonType.STARTS_WITH);
      List<Pair<ASTElement, Integer>> pairs = IntStream.range(
          0, identifiers.size()).mapToObj(i -> new Pair<>(identifiers.get(i), i)
        ).collect(Collectors.toList()); // [(p0, 0), (p1, 1), ..., (pn, n)]

      try {
        pairs = List.of(pairs.get(expressionArrayIndex.index().evaluateInt()));
      } catch (Exception exception) {
        // Nothing to do
      } finally {
        commands = createASTElementsWithoutExpressionArrayIndex(commands, expressionArrayIndex, pairs, replacer);
      }
    }

    return commands;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object visit(FormulaList e) throws PrismLangException {
    ASTElementReplacer replacer = new FormulaWithArraysReplacer();
    ASTVisitor searcher;

    int i, n;
    n = e.size();
    for (i = 0; i < n; i++) {
      Expression formula = (Expression) e.getFormula(i);
      Expression result  = new ExpressionUnaryOp(ExpressionUnaryOp.PARENTH, formula.clone().deepCopy(new DeepCopy()));

      searcher = new ASTElementSearcherVisitor(ExpressionArrayIndex.class);
      List<ExpressionArrayIndex> expressionArrayIndexList = (List<ExpressionArrayIndex>) formula.accept(searcher);

      for (ExpressionArrayIndex expressionArrayIndex : expressionArrayIndexList) {
        String prefix = expressionArrayIndex.name();
        List<ASTElement> identifiers = IdentifiersGetter.identifiers(modulesFile, prefix, ComparisonType.STARTS_WITH);
        List<Pair<ASTElement, Integer>> pairs = IntStream.range(
            0, identifiers.size()).mapToObj(j -> new Pair<>(identifiers.get(j), j)
          ).collect(Collectors.toList()); // [(p0, 0), (p1, 1), ..., (pn, n)]

        try {
          pairs = List.of(pairs.get(expressionArrayIndex.evaluateInt()));
        } catch (Exception exception) {
          for (Pair<ASTElement, Integer> pair : pairs) {
            ASTElement identifier = pair.fst();
            Integer indexValue    = pair.snd();

            if (indexValue < identifiers.size() - 1) {
              Expression guard = new ExpressionBinaryOp(5, expressionArrayIndex.index(), new ExpressionLiteral(TypeInt.getInstance(), indexValue)); // index =? value
              result = new ExpressionUnaryOp(
                ExpressionUnaryOp.PARENTH, new ExpressionITE(
                    guard, (Expression) replacer.replace(formula, expressionArrayIndex, replacement(identifier, expressionArrayIndex), indexValue), result.clone().deepCopy(new DeepCopy())
                  )
              );
            } else {
              result = (Expression) replacer.replace(
                  result, expressionArrayIndex, replacement(identifier, expressionArrayIndex), indexValue
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

  @SuppressWarnings("unchecked")
  @Override
  public Object visit(RewardStructItem e) throws PrismLangException {
    ASTElementReplacer replacer        = new RewardStructItemWithArraysReplacer();
    List<ASTElement> rewardStructItems = new ArrayList<>();
    ASTVisitor searcher                = new ASTElementSearcherVisitor(ExpressionArrayIndex.class);
    List<ExpressionArrayIndex> expressionArrayIndexList = (List<ExpressionArrayIndex>) e.accept(searcher);

    rewardStructItems.add(e.clone().deepCopy(new DeepCopy()));

    for (ExpressionArrayIndex expressionArrayIndex : expressionArrayIndexList) {
      String prefix = expressionArrayIndex.name();
      List<ASTElement> identifiers = IdentifiersGetter.identifiers(modulesFile, prefix, ComparisonType.STARTS_WITH);
      List<Pair<ASTElement, Integer>> pairs = IntStream.range(
          0, identifiers.size()).mapToObj(i -> new Pair<>(identifiers.get(i), i)
        ).collect(Collectors.toList()); // [(p0, 0), (p1, 1), ..., (pn, n)]

      try {
        pairs = List.of(pairs.get(expressionArrayIndex.index().evaluateInt()));
      } catch (Exception exception) {
        // Nothing to do
      } finally {
        rewardStructItems = createASTElementsWithoutExpressionArrayIndex(rewardStructItems, expressionArrayIndex, pairs, replacer);
      }
    }

    return rewardStructItems;
	}

  private List<ASTElement> createASTElementsWithoutExpressionArrayIndex(
      List<ASTElement> astElements, ExpressionArrayIndex expressionArrayIndex, List<Pair<ASTElement, Integer>> pairs, ASTElementReplacer replacer 
    ) throws PrismLangException {

    ListIterator<ASTElement> iterator = astElements.listIterator();

    while(iterator.hasNext()) {
      ASTElement astElement = iterator.next();

      iterator.remove();

      for (Pair<ASTElement, Integer> pair : pairs) {
        ASTElement identifier = pair.fst();
        Integer index        = pair.snd();

        iterator.add(replacer.replace(astElement, expressionArrayIndex, replacement(identifier, expressionArrayIndex), index));
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
