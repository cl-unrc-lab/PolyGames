package parser.ast;

import java.util.ArrayList;

public class UncertainUpdates extends Updates{
    private ArrayList<Expression> uncertains;
    private double[][] coefficients;
    private double[] constants;

    public UncertainUpdates(int coefficients, int constants) {
        super();
        this.uncertains   = new ArrayList<Expression>();
    }

    public void addUpdate(Expression un, Update up) {
        this.uncertains.add(un);
        this.updates.add(up);
        up.setParent(this);
    }

    public void setUncertain(int i, Expression un) {
        this.uncertains.add(i, un);
    }

    public Expression getUncertain(int i) { return this.uncertains.get(i); }
}
