package parser.visitor;

import parser.ast.*;
import parser.ast.Module;
import prism.PrismLangException;

public class ASTUncertainTraverse implements ASTUncertainVisitor{

    private ASTTraverse astTraverse;

    public ASTUncertainTraverse() {
        this.astTraverse = new ASTTraverse();
    }

    public void defaultVisitPre(ASTElement e) throws PrismLangException {}
    public void defaultVisitPost(ASTElement e) throws PrismLangException {}

    public void visitPre(ExpressionUncertain e) throws PrismLangException {
        defaultVisitPre(e);
    }

    @Override
    public Object visit(ExpressionUncertain e) throws PrismLangException {
        visitPre(e);
        visitPost(e);
        return null;
    }

    public void visitPost(ExpressionUncertain e) throws PrismLangException {
        defaultVisitPost(e);
    }

    public void visitPre(UncertainUpdates e) throws PrismLangException {
        defaultVisitPre(e);
    }

    @Override
    public Object visit(UncertainUpdates e) throws PrismLangException {
        visitPre(e);
        int i, n;
        n = e.getNumUpdates();
        for (i = 0; i < n; i++) {
            e.getUncertain(i).accept(this);
            e.getUpdate(i).accept(this);
        }
        visitPost(e);
        return null;
    }

    public void visitPost(UncertainUpdates e) throws PrismLangException {
        defaultVisitPost(e);
    }

    @Override
    public Object visit(ModulesFile e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(PropertiesFile e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(Property e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(FormulaList e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(LabelList e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(ConstantList e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(Declaration e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(DeclarationInt e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(DeclarationBool e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(DeclarationArray e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(DeclarationClock e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(DeclarationIntUnbounded e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(Module e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(Command e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(Updates e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(Update e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(UpdateElement e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(RenamedModule e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(RewardStruct e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(RewardStructItem e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(ObservableVars e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(Observable e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(Player e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(SystemInterleaved e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(SystemFullParallel e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(SystemParallel e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(SystemHide e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(SystemRename e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(SystemModule e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(SystemBrackets e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(SystemReference e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(ExpressionTemporal e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(ExpressionITE e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(ExpressionBinaryOp e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(ExpressionUnaryOp e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(ExpressionFunc e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(ExpressionIdent e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(ExpressionLiteral e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(ExpressionConstant e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(ExpressionFormula e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(ExpressionVar e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(ExpressionInterval e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(ExpressionProb e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(ExpressionReward e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(ExpressionMultiNash e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(ExpressionMultiNashProb e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(ExpressionMultiNashReward e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(ExpressionSS e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(ExpressionExists e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(ExpressionForAll e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(ExpressionStrategy e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(ExpressionLabel e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(ExpressionObs e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(ExpressionProp e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(ExpressionFilter e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(Filter e) throws PrismLangException {
        return astTraverse.visit(e);
    }

    @Override
    public Object visit(ForLoop e) throws PrismLangException {
        return astTraverse.visit(e);
    }
}
