package parser.visitor.utils;

import java.util.ArrayList;
import java.util.List;
import parser.ast.ASTElement;
import parser.ast.Constant;
import parser.ast.ConstantList;
import parser.ast.Declaration;
import parser.ast.Module;
import parser.ast.ModulesFile;

public class IdentifiersGetter {
  public static List<Declaration> declarations(ModulesFile modulesFile, String name, ComparisonType comparisonType) {
    List<Declaration> declarations = new ArrayList<>();
    Module module = null;

    int n = modulesFile.getNumModules();
    for (int i = 0; i < n; i++) {
      module = modulesFile.getModule(i);

      for (Declaration declaration : module.getDeclarations()) {
        if (matches(declaration.getName(), name, comparisonType)) {
          declarations.add(declaration);
        }
      }
    }

    return declarations;
  }

  public static List<Constant> constants(ModulesFile modulesFile, String name, ComparisonType comparisonType) {
    ConstantList constantList = modulesFile.getConstantList();
    List<Constant> constants  = new ArrayList<>();

    int n = constantList.size();
    for (int i = 0; i < n; i++) {
      String constant = constantList.getConstantName(i);

      if (constant == null)
        continue;

      if (matches(constant, name, comparisonType))
        constants.add(new Constant(constant, constantList.getConstant(i), constantList.getConstantType(i)));
    }

    return constants;
  }

  public static List<ASTElement> identifiers(ModulesFile modulesFile, String name, ComparisonType comparisonType) {
    List<ASTElement> identifiers = new ArrayList<>();

    identifiers.addAll(constants(modulesFile, name, comparisonType));
    identifiers.addAll(declarations(modulesFile, name, comparisonType));

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
