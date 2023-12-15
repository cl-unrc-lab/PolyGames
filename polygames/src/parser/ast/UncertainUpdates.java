package parser.ast;

import java.util.ArrayList;

import parser.visitor.ASTVisitor;
import prism.PrismLangException;

public class UncertainUpdates extends Updates{
    private ArrayList<Expression> uncertains;
    private double[][] coefficients;
    private double[] constants;

    public UncertainUpdates() {
        super();
        this.uncertains   = new ArrayList<Expression>();
        this.coefficients = new double[9][9]; // we have to do a resize of arrays
        this.constants = new double[9];
    }

    public void addUpdate(Expression un, Update up) {
        if (un == null) { throw new IllegalArgumentException(); }
        if (up == null) { throw new IllegalArgumentException(); }
        this.uncertains.add(un);
        //this.updates.add(up);
        super.addUpdate(un, up); // we call the super version
        //up.setParent(this);
    }

    public void setUncertain(int i, Expression un) {
        if (un == null) { throw new IllegalArgumentException(); }
        this.uncertains.add(i, un);
    }

    public Expression getUncertain(int i) { return this.uncertains.get(i); }

    public void addCoefficient(double c, int i, int j)
    {
        this.coefficients[i][j] = c;
    }

    public void addConstant(double c, int i) {
        this.constants[i] = c;
    }
    
    /**
	 * Visitor method.
	 */
	public Object accept(ASTVisitor v) throws PrismLangException
	{
		return v.visit(this);
	}
}
