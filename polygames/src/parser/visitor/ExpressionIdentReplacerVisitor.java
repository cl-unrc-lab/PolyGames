package parser.visitor;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashMap;

import parser.ast.*;
import parser.type.TypeInt;
import parser.visitor.utils.IdentifiersFinder;
import prism.PrismLangException;

public class ExpressionIdentReplacerVisitor extends ASTTraverseModify {
  private ExpressionIdent expressionIdent;
  private ExpressionLiteral value;

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

  @SuppressWarnings("unchecked")
  @Override
  public Object visit(Command e) throws PrismLangException {
    List<Command> commands = new ArrayList<>();

    ASTVisitor searcher = new ASTElementSearcherVisitor(ExpressionIdent.class);
    List<ExpressionIdent> expressionIdentList = (List<ExpressionIdent>) e.getUpdates().accept(searcher);
    expressionIdentList = expressionIdentList.stream()
    .filter(expressionIdent -> !expressionIdent.getPrime())
    .collect(Collectors.collectingAndThen(
        Collectors.toMap(
            ExpressionIdent::getName,    // clave: nombre del identificador
            Function.identity(),         // valor: el propio objeto
            (existing, replacement) -> existing // en caso de duplicado, conservar el primero
        ),
        map -> new ArrayList<>(map.values())
    ));

    commands.add(e.clone().deepCopy(new DeepCopy()));

    for (ExpressionIdent expression : expressionIdentList) {
      String name = expression.getName();

      Declaration declaration = IdentifiersFinder.lookUpDeclarationByName(modulesFile, name);

      if (declaration == null) {
        continue;
      }

      if (declaration.getDeclType() instanceof DeclarationInt) {
        ListIterator<Command> iterator = commands.listIterator();

        while (iterator.hasNext()) {
          Command command = iterator.next();

          iterator.remove();

          DeclarationInt declarationType = (DeclarationInt) declaration.getDeclType();

          int low  = declarationType.getLow().evaluateInt();
          int high = declarationType.getHigh().evaluateInt();

          for (int v = low; v <= high; v++) {
            iterator.add(replaceInCommand(command, expression, new ExpressionLiteral(TypeInt.getInstance(), v)));
          }
        }
      }
    }

    return commands;
  }

  @Override
  public Object visit(UncertainUpdates e) throws PrismLangException {
    int i, n;
    String uncertain;
    Expression coefficient;
    int index;

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

    for (Map.Entry<String, HashMap<Integer, Expression>> entry : e.coefficients().entrySet()) {
      uncertain = entry.getKey();

      for (Map.Entry<Integer, Expression> row : entry.getValue().entrySet()) {
        index = row.getKey();
        coefficient = row.getValue();

        e.setCoefficient(uncertain, index, (Expression) coefficient.accept(this));
      }
    }

    for (i = 0; i < e.constants().size(); i++) {
      Expression constant = e.constant(i);

      e.setConstant(i, (Expression) constant.accept(this));
    }

    return e;
  }

  @Override
  public Object visit(UpdateElement e) throws PrismLangException {
    ExpressionIdent identifier = e.getVarIdent();
    if (identifier != null) {
      e.setVarIdent((ExpressionIdent) identifier.accept(this));
    }

    Expression expression = e.getExpression();
    if (expression != null) {
      e.setExpression((Expression) expression.accept(this));
      ;
    }

    return e;
  }

  @Override
  public Object visit(ExpressionIdent e) throws PrismLangException {
    if (expressionIdent != null && (e.getName().equals(expressionIdent.getName()) && !e.getPrime())) {
      return value;
    } else {
      return e;
    }
  }

  private Command replaceInCommand(Command command, ExpressionIdent expression, ExpressionLiteral value)
      throws PrismLangException {
    this.expressionIdent = expression;
    this.value = value;

    Command result = command.clone().deepCopy(new DeepCopy());

    result.setGuard(
        ExpressionBinaryOp.And(
            (Expression) result.getGuard(), new ExpressionBinaryOp(ExpressionBinaryOp.EQ, expression, value)));

    result.setUpdates((Updates) result.getUpdates().accept(this));

    return result;
  }
}