package parser.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import parser.ast.utils.relOps;
import parser.type.TypeDouble;
import parser.visitor.ASTVisitor;
import parser.visitor.DeepCopy;
import prism.PrismLangException;
import prism.PrismException;

import parma_polyhedra_library.Coefficient;
import parma_polyhedra_library.Linear_Expression_Variable;
import parma_polyhedra_library.Variable;
import parma_polyhedra_library.Constraint;
import parma_polyhedra_library.Linear_Expression;
import parma_polyhedra_library.Linear_Expression_Sum;
import parma_polyhedra_library.Linear_Expression_Times;
import parma_polyhedra_library.Linear_Expression_Coefficient;
import parma_polyhedra_library.Constraint_System;

import parma_polyhedra_library.Relation_Symbol;
import explicit.PPLSupport;

public class UncertainUpdates extends Updates {

	private ArrayList<Expression> uncertains; // the list of uncertains
	private HashMap<String, HashMap<Integer, Expression>> coefficients; // coefficients contains for each uncertain the corresponding column of coefficients
																																	    // for instance, coefficients.get(uncertain).get(i) returns the coefficiente corresponding to row i, null if none
	private ArrayList<Expression> constants; // constains the columns of constants in the equations
	private ArrayList<relOps> ineqs; // the ineqs in the constraints: Relation_Symbol.LESS_OR_EQUAL, or Relation_Symbol.G
	int div = 1; 	     // the divisor allows us to move the decimal point, PPL only allows for integers.
	int precision = 6; // this is the precision, after that we truncate the number	

	/**
	 * Basic constructor, it initializes the object, all the coefficients and constants are initialized to zero,
	 * we assume that, when a uncertain is added, the coefficient is set to a number different from 0
	 */
	public UncertainUpdates() {
		super();
		this.uncertains   = new ArrayList<Expression>();
		this.coefficients = new HashMap<String, HashMap<Integer, Expression>>();
		this.constants    = new ArrayList<Expression>();
		this.ineqs        = new ArrayList<relOps>();
	}

	public HashMap<String, HashMap<Integer, Expression>> coefficients() {
		return coefficients;
	}

	public void setCoefficient(String uncertain, int row, Expression coefficient) {
		this.coefficients.get(uncertain).put(row, coefficient);
	}

	public List<Expression> constants() {
		return constants;
	}

	public void setConstant(int row, Expression constant) {
		this.constants.set(row, constant);
	}

	public Expression constant(int row) {
		return this.constants.get(row);
	}

	public List<Expression> uncertains() {
		return uncertains;
	}

	public List<relOps> ineqs() {
		return ineqs;
	}

	/**
	* @param Expression
	* @param Up
	*/
	public void addUpdate(Expression un, Update up) {
		if (un == null) { throw new IllegalArgumentException(); }
		if (up == null) { throw new IllegalArgumentException(); }
		
		this.uncertains.add(un);
		super.addUpdate(un, up); // we call the super version
	}

	/**
	 * 
	 * @param i
	 * @param un
	 */
	public void setUncertain(int i, Expression un) {
		if (un == null) { throw new IllegalArgumentException(); }
		this.uncertains.set(i, un);
	}

	/**
	 * 
	 * @param i
	 * @return
	 */
	public Expression getUncertain(int i) { return this.uncertains.get(i); }

	/**
	 * 
	 * @return
	 */
	public int getNumberUncertains() {
		return this.uncertains.size();
	}

	/**
	 * 
	 * @return The divisor indicating the number of decimals that one needs to shift the numbers
	 */
	public int getDivisor() {
		return div;
	}
	
	/**
	 * 
	 * @return the precision of the constants and coefficients
	 */
	public int getPrecision() {
		return precision;
	}
	/**
	 * @param c
	 * @param i			the row of the coefficient
	 * @param uncertain	the uncertain to which the coefficient applies
	 */
	public void addCoefficient(Expression coefficient, int i, UncertainExpression uncertain, boolean isInLeftSide) {
		
		String uncertainName = uncertain.getName();
		
		// we check if the row for the uncertain was initialised
		if (this.coefficients.get(uncertainName) == null)
			this.coefficients.put(uncertainName, new HashMap<Integer, Expression>());
		
		if (!isInLeftSide)
			coefficient = new ExpressionUnaryOp(2, coefficient);

		if (this.coefficients.get(uncertainName).get(i) == null) { // if the uncertain hasn't a mapped coefficient
			this.coefficients.get(uncertainName).put(i, coefficient);
		} else { // if the uncertain has a mapped coefficient
			this.coefficients.get(uncertainName).put(i, new ExpressionBinaryOp(11, coefficient, this.coefficients.get(uncertainName).get(i)));
		}
	}

	public void addConstant(Expression constant, int i, boolean isInLeftSide) {
		if (isInLeftSide)
			constant = new ExpressionUnaryOp(2, constant);
		
		if ( i < this.constants.size() ) { // if there is already a constant for the i-th row then
			this.constants.set(i, new ExpressionBinaryOp(11, constant, this.constants.get(i)));
		} else {
			this.constants.add(i, constant);
		}
	}

	public void setInequalitySymbol(String inequalitySymbol) {
		switch (inequalitySymbol) {
			case "<=":
				this.ineqs.add(relOps.LE);
				break;
			case ">=":
				this.ineqs.add(relOps.GE);
				break;
			default:
				throw new IllegalArgumentException("Invalid inequality symbol: " + inequalitySymbol);
		}
	}
    
	/**
	 * Visitor method.
	 */
	public Object accept(ASTVisitor v) throws PrismLangException
	{
		return v.visit(this);
	}
	
	@Override
	public String toString() {
		String result = "";
		
		for (int j = 0; j < this.constants.size(); j++) {
			for (int i = 0; i < this.uncertains.size(); i++) {
				String uncertain = ((UncertainExpression) this.uncertains.get(i)).getName();
				result += this.coefficients.get(uncertain).get(j) + uncertain;
			}

			result += (ineqs.get(j) == relOps.LE) ? "<=" + constants.get(j) : ">=" + constants.get(j) ;
		}
		
		return result;
	}
	
	@Override
	public UncertainUpdates deepCopy(DeepCopy copier) throws PrismLangException
	{
		super.deepCopy(copier);
		return this;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public UncertainUpdates clone() {
		UncertainUpdates clone = (UncertainUpdates) super.clone();
		clone.uncertains   = (ArrayList<Expression>) uncertains.clone();
		clone.coefficients = (HashMap<String, HashMap<Integer, Expression>>) coefficients.clone();
		clone.constants    = (ArrayList<Expression>) constants.clone();

		return clone;
	}
	
	/**
	 * @return The PPL constrain system corresponding to the equations in the uncertain system
	 * @throws PrismLangException 
	 */
	public Constraint_System getPPLConstraintSystem() throws PrismLangException{
		Constraint_System constraint_System = new Constraint_System();
		
		// Initialize PPL (Parma Polyhedra Library)
		try {
			PPLSupport.initPPL();
		} catch (PrismException e) {
			System.err.println("Error loading Parma Polyhedra Library:");
		}
		
		// Each variable corresponds to an uncertain. The uncertains are mapped using their index in {@code uncertains}.
		ArrayList<Variable> vars = new ArrayList<Variable>();
		for (int i = 0; i < this.uncertains.size() ; i++) {
			vars.add(new Variable(i));
		}

		// linear_Expressions[i] represents the left linear expression in the i-th row of the constraint system
		Linear_Expression[] linear_Expressions = new Linear_Expression[this.constants.size()];

		for (int i = 0; i < this.constants.size(); i++) {
			Linear_Expression linear_Expression = new Linear_Expression_Coefficient(new Coefficient(0));

			for (int j = 0; j < this.uncertains.size(); j++) {
				String uncertain = ((UncertainExpression) this.uncertains.get(j)).getName();

				if (this.coefficients.get(uncertain).get(i) == null)
					continue ;

				Integer coefficient = ((Double) this.coefficients.get(uncertain).get(i).evaluate()).intValue();
				Linear_Expression times = new Linear_Expression_Times(new Coefficient(coefficient), vars.get(j));
				linear_Expression = new Linear_Expression_Sum(linear_Expression, times);
			}

			linear_Expressions[i] = linear_Expression;
		}

		for (int i = 0; i < this.constants.size(); i++) {
			constraint_System.add(
				new Constraint(linear_Expressions[i], this.ineqs.get(i) == relOps.LE ? Relation_Symbol.LESS_OR_EQUAL : Relation_Symbol.GREATER_OR_EQUAL, new Linear_Expression_Coefficient(new Coefficient(((Double) constants.get(i).evaluate()).intValue())))
			);
		}

		// Structural constraints
		// Each p_i must lie within the interval [0, 1].
		for (int i = 0; i < vars.size(); i++) {
			constraint_System.add(
				new Constraint(new Linear_Expression_Variable(vars.get(i)), Relation_Symbol.LESS_OR_EQUAL, new Linear_Expression_Coefficient(new Coefficient(1)))
			);

			constraint_System.add(
				new Constraint(new Linear_Expression_Variable(vars.get(i)), Relation_Symbol.GREATER_OR_EQUAL, new Linear_Expression_Coefficient(new Coefficient(0)))
			);
		}

		// The sum of all the p_i must equal 1.
		Linear_Expression sum = new Linear_Expression_Coefficient(new Coefficient(0));
		for (int i = 0; i < vars.size(); i++) {
			sum = new Linear_Expression_Sum(sum, new Linear_Expression_Variable(vars.get(i)));
		}

		constraint_System.add(new Constraint(sum, Relation_Symbol.EQUAL, new Linear_Expression_Coefficient(new Coefficient(1))));

		return constraint_System;
	}
	
	/**
	 * Converts all the coefficients to integers. This conversion is required for PPL (Parma Polyhedra Library) operations.
	 */
	public void convertToInt() throws PrismLangException{
		for (int i = 0; i < this.uncertains.size(); i++) {
			String uncertainName = ((UncertainExpression) this.uncertains.get(i)).getName();
			for (int j = 0; j < this.constants.size(); j++) {
				if (this.coefficients.get(uncertainName).get(j) != null) {
					this.coefficients.get(uncertainName).put(
						j, new ExpressionLiteral(TypeDouble.getInstance(), Math.floor(this.coefficients.get(uncertainName).get(j).evaluateDouble() * Math.pow(10, precision)))
					);
				}
			}
		}
		
		for (int i = 0; i < this.constants.size(); i++) {
			this.constants.set(
				i, new ExpressionLiteral(TypeDouble.getInstance(), Math.floor(this.constants.get(i).evaluateDouble() * Math.pow(10, precision)))
			);
		}
	}
}
