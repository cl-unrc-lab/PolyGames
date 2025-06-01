package parser.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

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
	private List<Relation_Symbol> relationSymbols;
	int div = 1; 	     // the divisor allows us to move the decimal point, PPL only allows for integers.
	int precision = 6; // this is the precision, after that we truncate the number	

	/**
	 * Basic constructor, it initializes the object, all the coefficients and constants are initialized to zero,
	 * we assume that, when a uncertain is added, the coefficient is set to a number different from 0
	 */
	public UncertainUpdates() {
		super();
		this.uncertains      = new ArrayList<Expression>();
		this.coefficients    = new HashMap<String, HashMap<Integer, Expression>>();
		this.constants       = new ArrayList<Expression>();
		this.relationSymbols = new ArrayList<Relation_Symbol>();
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
	 * 
	 * @param coefficient
	 * @param i
	 * @param uncertain
	 * @param isInLeftSide
	 */
	public void addCoefficient(Expression coefficient, int row, UncertainExpression uncertainExpression, boolean isInLeftSide) {
		String uncertain = uncertainExpression.getName();

		if (Objects.isNull(coefficients.get(uncertain))) {
			coefficients.put(uncertain, new HashMap<Integer, Expression>());
		}
		
		if (!isInLeftSide) {
			coefficient = new ExpressionUnaryOp(ExpressionUnaryOp.MINUS, coefficient);
		}

		Expression previousCoefficient = coefficients.get(uncertain).get(row);

		if (Objects.isNull(previousCoefficient)) {
			coefficients.get(uncertain).put(row, coefficient);
		} else {
			coefficients.get(uncertain).put(
				row, new ExpressionUnaryOp(
					ExpressionUnaryOp.PARENTH, new ExpressionBinaryOp(ExpressionBinaryOp.PLUS, coefficient, previousCoefficient)
				)
			);
		}
	}

	/**
	 * 
	 * @param constant
	 * @param i
	 * @param isInLeftSide
	 */
	public void addConstant(Expression constant, int row, boolean isInLeftSide) {
		if (isInLeftSide) {
			constant = new ExpressionUnaryOp(ExpressionUnaryOp.MINUS, constant);
		}
		
		if ( row < constants.size() ) { // if there is already a constant for the i-th row then
			constants.set(row, new ExpressionBinaryOp(ExpressionBinaryOp.PLUS, constant, this.constants.get(row)));
		} else {
			constants.add(row, constant);
		}
	}

	public void setRelationSymbol(String relationSymbol) {
		load_PPL();

		switch (relationSymbol) {
			case "=":
				this.relationSymbols.add(Relation_Symbol.EQUAL);
				break;
			case "<=":
				this.relationSymbols.add(Relation_Symbol.LESS_OR_EQUAL);
				break;
			case ">=":
				this.relationSymbols.add(Relation_Symbol.GREATER_OR_EQUAL);
				break;
			default:
				throw new IllegalArgumentException("Invalid relation symbol: " + relationSymbol);
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

		for (int row = 0; row < constants.size(); row++) {
			for (int col = 0; col < uncertains.size(); col++) {
				String uncertain = ((UncertainExpression) uncertains.get(col)).getName();
				result += uncertain + " * " + coefficients.get(uncertain).get(row);

				if (col < uncertains.size() - 1) {
					result += " + ";
				}
			}

			result += " " + relationSymbols.get(row) + " " + constants.get(row) + "\n";
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
		load_PPL();

		Constraint_System constraint_System = new Constraint_System();
		
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
				new Constraint(
					linear_Expressions[i], this.relationSymbols.get(i), new Linear_Expression_Coefficient(new Coefficient(((Double) constants.get(i).evaluate()).intValue()))
				)
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

	public void initializeConstraintSystem() {
		Expression ZERO = new ExpressionLiteral(TypeDouble.getInstance(), 0.0);
		for (int i = 0; i < this.uncertains.size(); i++) {
			UncertainExpression uncertain = ((UncertainExpression) this.uncertains.get(i));
			for (int j = 0; j < this.uncertains.size(); j++) {
				addCoefficient(
					ZERO.clone().deepCopy(), j, uncertain, true
				);
			}
		}
	}

	private void load_PPL() {
		// Initialize PPL (Parma Polyhedra Library)
		try {
			PPLSupport.initPPL();
		} catch (PrismException e) {
			System.err.println("Error loading Parma Polyhedra Library:");
		}
	}
}
