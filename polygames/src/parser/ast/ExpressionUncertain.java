package parser.ast;

import parser.EvaluateContext;
import parser.visitor.ASTUncertainVisitor;
import parser.visitor.ASTVisitor;
import parser.visitor.DeepCopy;
import prism.PrismLangException;

public class ExpressionUncertain extends Expression{

    private String name;

    public ExpressionUncertain(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public Object accept(ASTVisitor v) throws PrismLangException {
        ASTUncertainVisitor visitor = (ASTUncertainVisitor) v;
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return null;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public boolean isProposition() {
        return false;
    }

    @Override
    public Object evaluate(EvaluateContext ec) throws PrismLangException {
        return null;
    }

    @Override
    public boolean returnsSingleValue() {
        return false;
    }

    @Override
    public Expression deepCopy(DeepCopy copier) throws PrismLangException {
        return null;
    }
}
