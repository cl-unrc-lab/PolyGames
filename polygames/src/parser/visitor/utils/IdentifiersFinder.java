package parser.visitor.utils;

import java.util.ArrayList;
import java.util.List;
import parser.ast.ASTElement;
import parser.ast.ConstantList;
import parser.ast.Declaration;
import parser.ast.ExpressionConstant;
import parser.ast.Module;
import parser.ast.ModulesFile;

public class IdentifiersFinder {
  public static Declaration lookUpDeclarationByName(ModulesFile modulesFile, String name) {
    Module module = null;

    int n = modulesFile.getNumModules();

    for (int i = 0; i < n; i++) {
      module = modulesFile.getModule(i);

      if (module == null)
        continue ;

      for (Declaration declaration : module.getDeclarations()) {
        if (declaration == null)
          continue ;

        if (matches(declaration.getName(), name, ComparisonType.EQUALS))
          return declaration;
      }
    }

    return null;
  }

  public static List<Declaration> lookUpDeclarationsByName(ModulesFile modulesFile, String name, ComparisonType comparisonType) {
    List<Declaration> declarations = new ArrayList<>();
    Module module = null;

    int n = modulesFile.getNumModules();
    for (int i = 0; i < n; i++) {
      module = modulesFile.getModule(i);

      if (module == null)
        continue ;

      for (Declaration declaration : module.getDeclarations()) {
        if (declaration == null)
          continue ;

        if (matches(declaration.getName(), name, comparisonType))
          declarations.add(declaration);
      }
    }

    return declarations;
  }

  public static ExpressionConstant lookUpConstantByName(ModulesFile modulesFile, String name) {
    ConstantList constantList = modulesFile.getConstantList();

    int n = constantList.size();
    for (int i = 0; i < n; i++) {
      String constant = constantList.getConstantName(i);

      if (constant == null)
        continue;

      if (matches(constant, name, ComparisonType.EQUALS))
        return new ExpressionConstant(constant, constantList.getConstantType(i));
    }

    return null;
  }

  public static List<ExpressionConstant> lookUpConstantsByName(ModulesFile modulesFile, String name, ComparisonType comparisonType) {
    ConstantList constantList           = modulesFile.getConstantList();
    List<ExpressionConstant> constants  = new ArrayList<>();

    int n = constantList.size();
    for (int i = 0; i < n; i++) {
      String constant = constantList.getConstantName(i);

      if (constant == null)
        continue;

      if (matches(constant, name, comparisonType))
        constants.add(new ExpressionConstant(constant, constantList.getConstantType(i)));
    }

    return constants;
  }

  public static List<ASTElement> identifiers(ModulesFile modulesFile, String name, ComparisonType comparisonType) {
    List<ASTElement> identifiers = new ArrayList<>();

    identifiers.addAll(lookUpConstantsByName(modulesFile, name, comparisonType));
    identifiers.addAll(lookUpDeclarationsByName(modulesFile, name, comparisonType));

    return identifiers;
  }

  private static boolean matches(String s1, String s2, ComparisonType comparisonType) {
    switch (comparisonType) {
      case STARTS_WITH:
        return s1.startsWith(s2);
      default:
        return s1.equals(s2);
    }
  }
}
