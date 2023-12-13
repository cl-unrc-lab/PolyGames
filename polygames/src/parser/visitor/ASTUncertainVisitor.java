package parser.visitor;

import parser.ast.*;
import prism.PrismLangException;

public interface ASTUncertainVisitor extends ASTVisitor{
    public Object visit(ExpressionUncertain e) throws PrismLangException;
    public Object visit(UncertainUpdates e) throws PrismLangException;
}
