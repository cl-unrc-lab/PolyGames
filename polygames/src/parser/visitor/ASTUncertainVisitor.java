package parser.visitor;

import parser.ast.*;
import parser.ast.Module;
import prism.PrismLangException;

public interface ASTUncertainVisitor extends ASTVisitor{
    public Object visit(UncertainExpression e) throws PrismLangException;
    public Object visit(UncertainUpdates e) throws PrismLangException;
}
