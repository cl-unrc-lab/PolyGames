package parser.ast;

import java.util.ArrayList;

public class UncertainUpdates extends Updates{
    private ArrayList<Expression> uncertains;
    private double[][] coefficients;
    private double[] constants;

    public UncertainUpdates() {
        super();
        this.uncertains   = new ArrayList<Expression>();
    }

    public void addUpdate(Expression un, Update up) {
        if (un == null) { throw new IllegalArgumentException(); }
        if (up == null) { throw new IllegalArgumentException(); }
        this.uncertains.add(un);
        this.updates.add(up);
        up.setParent(this);
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
}
