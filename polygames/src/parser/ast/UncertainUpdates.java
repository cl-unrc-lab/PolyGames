package parser.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.logging.log4j.core.pattern.ThreadIdPatternConverter;

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

	//private ArrayList<Expression> uncertains; // the list of uncertains
	//private HashMap<String, HashMap<Integer, Expression>> coefficients; 
	// coefficients contains, for each uncertain, the corresponding column of coefficients
    // for instance, coefficients.get(uncertain).get(i) returns the coefficient corresponding to row i, null if none
	//private ArrayList<Expression> constants; // constains the columns of constants in the equations
	//private List<Relation_Symbol> relationSymbols;
	//private boolean converted=false;
	//int div = 1; 	     // the divisor allows us to move the decimal point, PPL only allows for integers.
	//int precision = 6; // this is the precision, after that we truncate the number	
	private EquationSystem eqs; // the equations corresponding to the update
	private String equationSystemReference; // the equation system could be an instance of one globally defined
	private ArrayList<Expression> actualParameters; // in the last case we have a list of actual parameters 

	/**
	 * Basic constructor, it initializes the object, all the coefficients and constants are initialized to zero,
	 * we assume that, when a uncertain is added, the coefficient is set to a number different from 0
	 */
	public UncertainUpdates() {
		super();
		//this.uncertains      = new ArrayList<Expression>();
		//this.coefficients    = new HashMap<String, HashMap<Integer, Expression>>();
		//this.constants       = new ArrayList<Expression>();
		//this.relationSymbols = new ArrayList<Relation_Symbol>();
		//this.converted		 = false;
		this.eqs = new EquationSystem();
		this.actualParameters = new ArrayList<Expression>();
	}
	
	public void setEquationSystemReference(String name) {
		this.equationSystemReference = name;
	}

	public void addActualParameter(Expression par) {
		this.actualParameters.add(par);
	}
	
	public HashMap<String, HashMap<Integer, Expression>> coefficients() {
		return this.eqs.coefficients();
	}
	
	public void setEquationSystem(EquationSystem eqs) {
		this.eqs = eqs;
	}
	
	public EquationSystem getEquationSystem() {
		return this.eqs;
	}

	public void setCoefficient(String uncertain, int row, Expression coefficient) {
		//if (this.coefficients.keySet().contains(uncertain))
		//	this.coefficients.get(uncertain).put(row, coefficient);
		//else {
		//	this.coefficients.put(uncertain, new HashMap<Integer,Expression>());
		//	this.coefficients.get(uncertain).put(row, coefficient);
		//}
		this.eqs.setCoefficient(uncertain, row, coefficient);
	}

	public List<Relation_Symbol> getRelations(){
		//return this.relationSymbols;
		return this.eqs.getRelations();
	}
	
	public List<Expression> constants() {
		//return constants;
		return this.eqs.constants();
	}

	public void setConstant(int row, Expression constant) {
		//this.constants.set(row, constant);
		this.eqs.setConstant(row, constant);
	}

	public Expression constant(int row) {
		//return this.constants.get(row);
		return this.eqs.constant(row);
	}

	/**
	* @param Expression
	* @param Up
	*/
	public void addUpdate(Expression un, Update up) {
		if (un == null) { throw new IllegalArgumentException(); }
		if (up == null) { throw new IllegalArgumentException(); }
		
		this.eqs.getUncertains().add(un);
		super.addUpdate(un, up); // we call the super version
	}

	/**
	 * 
	 * @param i
	 * @param un
	 */
	public void setUncertain(int i, Expression un) {
		if (un == null) { throw new IllegalArgumentException(); }
		this.eqs.getUncertains().set(i, un);
	}

	/**
	 * 
	 * @param i
	 * @return
	 */
	public Expression getUncertain(int i) { 
		return this.eqs.getUncertains().get(i); 
	}

	public HashMap<String, HashMap<Integer, Expression>> getCoefficients(){
		return this.eqs.coefficients();
	}
	
	public ArrayList<Expression> getUncertains(){
		return this.eqs.getUncertains();
	}
	/**
	 * 
	 * @return
	 */
	public int getNumberUncertains() {
		return this.eqs.getUncertains().size();
	}

	
	/**
	 * 
	 * @return
	 */
	public int getNumberConstants() {
		return this.eqs.constants().size();
	}
	
	
	public Set<String> getUncertainNames(){
		return this.eqs.coefficients().keySet();
	}
	
	public Expression getCoefficient(String uncertain, int row) throws PrismLangException {
		Expression result = this.eqs.coefficients().get(uncertain).get(row);
		//if (result == null) {
		//	throw new PrismLangException("Error: null coefficient.");
		//}
		return result;
	}
	
	/**
	 * 
	 * @return The divisor indicating the number of decimals that one needs to shift the numbers
	 */
	public int getDivisor() {
		return eqs.getDivisor();
	}
	
	/**
	 * 
	 * @return the precision of the constants and coefficients
	 */
	public int getPrecision() {
		return eqs.getPrecision();
	}
	
	/**
	 * 
	 * @param coefficient
	 * @param i
	 * @param uncertain
	 * @param isInLeftSide
	 */
	public void addCoefficient(Expression coefficient, int row, UncertainExpression uncertainExpression, boolean isInLeftSide) {
		
		this.eqs.addCoefficient(coefficient, row, uncertainExpression, isInLeftSide);
		
		//String uncertain = uncertainExpression.getName();

		//if (Objects.isNull(coefficients.get(uncertain))) {
		//	coefficients.put(uncertain, new HashMap<Integer, Expression>());
		//}
		
		//if (!isInLeftSide) {
		//	coefficient = new ExpressionUnaryOp(ExpressionUnaryOp.MINUS, coefficient);
		//}

		//Expression previousCoefficient = coefficients.get(uncertain).get(row);

		//if (Objects.isNull(previousCoefficient)) {
		//	coefficients.get(uncertain).put(row, coefficient);
		//} else {
		//	coefficients.get(uncertain).put(
		//		row, new ExpressionUnaryOp(
		//			ExpressionUnaryOp.PARENTH, new ExpressionBinaryOp(ExpressionBinaryOp.PLUS, coefficient, previousCoefficient)
		//		)
		//	);
		//}
	}
	
	/**
	 * Sets the coefficient for an uncertain
	 */
	public void setCoefficient(UncertainExpression uncertainExpression, int row, Expression coefficient) throws PrismLangException {
		
		//if (coefficients.containsKey(uncertainExpression.getName())) {
		//	coefficients.get(uncertainExpression.getName()).put(row, coefficient);
		//}
		this.eqs.setCoefficient(uncertainExpression, row, coefficient);
	}

	/**
	 * 
	 * @param constant
	 * @param i
	 * @param isInLeftSide
	 */
	public void addConstant(Expression constant, int row, boolean isInLeftSide) {
		this.eqs.addConstant(constant, row, isInLeftSide);
		//if (isInLeftSide) {
		//	constant = new ExpressionUnaryOp(ExpressionUnaryOp.MINUS, constant);
		//}
		//
		//if ( row < constants.size() ) { // if there is already a constant for the i-th row then
		//	constants.set(row, new ExpressionBinaryOp(ExpressionBinaryOp.PLUS, constant, this.constants.get(row)));
		//} else {
		//	constants.add(row, constant);
		//}
	}

	public void setRelationSymbol(String relationSymbol) {
		//load_PPL();
		//	
		//switch (relationSymbol) {
		//	case "=":
		//		this.relationSymbols.add(Relation_Symbol.EQUAL);
		//		break;
		//	case "<=":
		//		this.relationSymbols.add(Relation_Symbol.LESS_OR_EQUAL);
		//		break;
		//	case ">=":
		//		this.relationSymbols.add(Relation_Symbol.GREATER_OR_EQUAL);
		//		break;
		//	default:
		//		throw new IllegalArgumentException("Invalid relation symbol: " + relationSymbol);
		//}
		this.eqs.setRelationSymbol(relationSymbol);
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
		result += super.toString()+"\n";
		result += eqs.toString();
		//result += "{";
		//for (int row = 0; row < constants.size(); row++) {
		//	for (int col = 0; col < uncertains.size(); col++) {
		//		if (uncertains.get(col) != null ) {
		//			String uncertain = ((UncertainExpression) uncertains.get(col)).getName();
		//			result += uncertain + " * " + coefficients.get(uncertain).get(row);
		//		}
//
//				if (col < uncertains.size() - 1) {
//					result += " + ";
//				}
//			}
//
//			result += " " + relationSymbols.get(row) + " " + constants.get(row) + "\n";
//		}
//		result += "}";
		return result;
	}
	
	@Override
	public UncertainUpdates deepCopy(DeepCopy copier) throws PrismLangException
	{
		UncertainUpdates result = new UncertainUpdates();
		super.deepCopy(copier);
		
		//ArrayList<Expression> newuncertains = (ArrayList<Expression>) copier.copyAll(this.uncertains);
		//ArrayList<Expression> newconstants  = (ArrayList<Expression>) copier.copyAll(this.constants);
		
		//for (Expression e : newconstants) {
		//	Expression ecopy = copier.copy(e);
		//	result.constants().add(ecopy);
		//}
		
		//for (Expression e : newuncertains) {
		//	Expression ecopy = copier.copy(e);
		//	result.getUncertains().add(ecopy);
		//}
		
		//for (String key : this.coefficients.keySet()) { // for all uncertain names
		//	HashMap<Integer, Expression> uncertain_coeffs = coefficients.get(key);
		//	for (Integer i : uncertain_coeffs.keySet()) {
				//new_uncertain_coeffs.put(i, (Expression) uncertain_coeffs.get(i).accept(copier));
				//new_uncertain_coeffs.put(i, (Expression) copier.copy(uncertain_coeffs.get(i)));
		//		result.setCoefficient(key, i, (Expression) copier.copy(uncertain_coeffs.get(i)));
		//	}
				
			//coefficients_new.put(key, new_uncertain_coeffs);
		//}
		//this.coefficients = coefficients_new;
		//result.getRelations().addAll(this.getRelations());
		result.eqs = this.eqs.deepCopy(copier);
		result.getUpdates().addAll(copier.copyAll(this.updates));
		result.getProbabilities().addAll(copier.copyAll(this.getProbabilities()));
		return result;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public UncertainUpdates clone() {
		UncertainUpdates clone = (UncertainUpdates) super.clone();
		clone.eqs = this.eqs.clone();
		//clone.uncertains   = (ArrayList<Expression>) uncertains.clone();
		//clone.coefficients = (HashMap<String, HashMap<Integer, Expression>>) coefficients.clone();
		//clone.constants    = (ArrayList<Expression>) constants.clone();

		return clone;
	}
	
	/**
	 * @return The PPL constrain system corresponding to the equations in the uncertain system
	 * @throws PrismLangException 
	 */
	public Constraint_System getPPLConstraintSystem() throws PrismLangException{
		/*
		 * load_PPL();
		 * 
		 * Constraint_System constraint_System = new Constraint_System();
		 * 
		 * // Each variable corresponds to an uncertain. The uncertains are mapped using
		 * their index in {@code uncertains}. ArrayList<Variable> vars = new
		 * ArrayList<Variable>(); for (int i = 0; i < this.uncertains.size() ; i++) {
		 * vars.add(new Variable(i)); }
		 * 
		 * // linear_Expressions[i] represents the left linear expression in the i-th
		 * row of the constraint system Linear_Expression[] linear_Expressions = new
		 * Linear_Expression[this.constants.size()];
		 * 
		 * for (int i = 0; i < this.constants.size(); i++) { Linear_Expression
		 * linear_Expression = new Linear_Expression_Coefficient(new Coefficient(0));
		 * 
		 * for (int j = 0; j < this.uncertains.size(); j++) { String uncertain =
		 * ((UncertainExpression) this.uncertains.get(j)).getName();
		 * 
		 * if (this.coefficients.get(uncertain).get(i) == null) continue ;
		 * 
		 * Integer coefficient = ((Double)
		 * this.coefficients.get(uncertain).get(i).evaluate()).intValue();
		 * Linear_Expression times = new Linear_Expression_Times(new
		 * Coefficient(coefficient), vars.get(j)); linear_Expression = new
		 * Linear_Expression_Sum(linear_Expression, times); }
		 * 
		 * linear_Expressions[i] = linear_Expression; }
		 * 
		 * for (int i = 0; i < this.constants.size(); i++) { constraint_System.add( new
		 * Constraint( linear_Expressions[i], this.relationSymbols.get(i), new
		 * Linear_Expression_Coefficient(new Coefficient(((Double)
		 * constants.get(i).evaluate()).intValue())) ) ); }
		 * 
		 * // Structural constraints // Each p_i must lie within the interval [0, 1].
		 * for (int i = 0; i < vars.size(); i++) { constraint_System.add( new
		 * Constraint(new Linear_Expression_Variable(vars.get(i)),
		 * Relation_Symbol.LESS_OR_EQUAL, new Linear_Expression_Coefficient(new
		 * Coefficient(1))) );
		 * 
		 * constraint_System.add( new Constraint(new
		 * Linear_Expression_Variable(vars.get(i)), Relation_Symbol.GREATER_OR_EQUAL,
		 * new Linear_Expression_Coefficient(new Coefficient(0))) ); }
		 * 
		 * // The sum of all the p_i must equal 1. Linear_Expression sum = new
		 * Linear_Expression_Coefficient(new Coefficient(0)); for (int i = 0; i <
		 * vars.size(); i++) { sum = new Linear_Expression_Sum(sum, new
		 * Linear_Expression_Variable(vars.get(i))); }
		 * 
		 * constraint_System.add(new Constraint(sum, Relation_Symbol.EQUAL, new
		 * Linear_Expression_Coefficient(new Coefficient(1))));
		 * 
		 * return constraint_System;
		 */
		return eqs.getPPLConstraintSystem();
	}
	
	/**
	 * Converts all the coefficients to integers. This conversion is required for PPL (Parma Polyhedra Library) operations.
	 */
	public void convertToInt() throws PrismLangException{
		/*
		 * if (!converted) { // if it was not converted before for (int i = 0; i <
		 * this.uncertains.size(); i++) { String uncertainName = ((UncertainExpression)
		 * this.uncertains.get(i)).getName(); for (int j = 0; j < this.constants.size();
		 * j++) { if (this.coefficients.get(uncertainName).get(j) != null) {
		 * this.coefficients.get(uncertainName).put( j, new
		 * ExpressionLiteral(TypeDouble.getInstance(),
		 * Math.floor(this.coefficients.get(uncertainName).get(j).evaluateDouble() *
		 * Math.pow(10, precision))) ); } } }
		 * 
		 * for (int i = 0; i < this.constants.size(); i++) { this.constants.set( i, new
		 * ExpressionLiteral(TypeDouble.getInstance(),
		 * Math.floor(this.constants.get(i).evaluateDouble() * Math.pow(10, precision)))
		 * ); } converted = true; }
		 */
		this.eqs.convertToInt();
	}

	/**
	 * initializes a constraint system with all zeros
	 */
	public void initializeConstraintSystem() {
		/*
		 * Expression ZERO = new ExpressionLiteral(TypeDouble.getInstance(), 0.0); for
		 * (int i = 0; i < this.uncertains.size(); i++) { UncertainExpression uncertain
		 * = ((UncertainExpression) this.uncertains.get(i)); for (int j = 0; j <
		 * this.uncertains.size(); j++) { addCoefficient( ZERO.clone().deepCopy(), j,
		 * uncertain, true ); } }
		 */
		this.eqs.initializeConstraintSystem();
	}

	/**
	 * Instanciates a equation system replacing formal parameters by actual ones
	 * @param mf
	 */
	public void instantiateEquationSystem(ModulesFile mf) throws PrismLangException {
		if (this.equationSystemReference != null) {
			EquationSystem eqs = mf.getEquationSystemByName(this.equationSystemReference);
			this.eqs = eqs.instantiateSystem(this.actualParameters);
		}
	}
	
	
	private void load_PPL() {
		// Initialize PPL (Parma Polyhedra Library)
		 try { PPLSupport.initPPL(); } 
		 catch (PrismException e) {
			 System.err.println("Error loading Parma Polyhedra Library:"); 
		 }
	}
}
