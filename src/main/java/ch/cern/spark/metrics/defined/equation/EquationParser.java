package ch.cern.spark.metrics.defined.equation;

import java.text.ParseException;
import java.util.Map;
import java.util.Set;

import ch.cern.properties.ConfigurationException;
import ch.cern.properties.Properties;
import ch.cern.spark.metrics.defined.equation.functions.num.AbsFunc;
import ch.cern.spark.metrics.defined.equation.functions.num.AddFunc;
import ch.cern.spark.metrics.defined.equation.functions.num.CosFunc;
import ch.cern.spark.metrics.defined.equation.functions.num.DivFunc;
import ch.cern.spark.metrics.defined.equation.functions.num.MinusFunc;
import ch.cern.spark.metrics.defined.equation.functions.num.MultiFunc;
import ch.cern.spark.metrics.defined.equation.functions.num.PowFunc;
import ch.cern.spark.metrics.defined.equation.functions.num.SinFunc;
import ch.cern.spark.metrics.defined.equation.functions.num.SqrtFunc;
import ch.cern.spark.metrics.defined.equation.functions.num.SubFunc;
import ch.cern.spark.metrics.defined.equation.functions.num.TanFunc;
import ch.cern.spark.metrics.defined.equation.var.FloatMetricVariable;
import ch.cern.spark.metrics.defined.equation.var.MetricVariable;
import ch.cern.spark.metrics.value.BooleanValue;
import ch.cern.spark.metrics.value.FloatValue;

public class EquationParser {
	
	private int pos = -1, ch;
	
	private String str;

	private Properties variablesProperties;

	private Map<String, MetricVariable> variables;

	private Set<String> variableNames;
	
	public EquationParser() {
	}
	
	private void nextChar() {
        ch = (++pos < str.length()) ? str.charAt(pos) : -1;
    }

	private boolean eat(int charToEat) {
        while (ch == ' ') nextChar();
        if (ch == charToEat) {
            nextChar();
            return true;
        }
        return false;
    }

	public synchronized ValueComputable parse(
			String input, 
			Properties variablesProperties, 
			Map<String, MetricVariable> variables) throws ParseException, ConfigurationException {
		
		this.variablesProperties = variablesProperties;
		this.variableNames = variablesProperties.getUniqueKeyFields();
		this.variables = variables;
		str = input;
		
		pos = -1;
		ch = 0;
		
        nextChar();
        
        ValueComputable x = parseExpression();
        
        if (pos < str.length()) 
        		throw new ParseException("Unexpected: " + (char)ch, pos);
        
        return x;
    }

    // Grammar:
    // expression = term | expression `+` term | expression `-` term
    // term = factor | term `*` factor | term `/` factor
    // factor = `+` factor | `-` factor | `(` expression `)` | number | functionName factor | factor `^` factor

	private ValueComputable parseExpression() throws ParseException, ConfigurationException {
    		ValueComputable x = parseTerm();
        for (;;) {
            if      (eat(AddFunc.REPRESENTATION)) x = new AddFunc(x, parseTerm());
            else if (eat(SubFunc.REPRESENTATION)) x = new SubFunc(x, parseTerm());
            else return x;
        }
    }

	private ValueComputable parseTerm() throws ParseException, ConfigurationException {
    		ValueComputable x = parseFactor(FloatMetricVariable.class);
        for (;;) {
            if      (eat('*')) x = new MultiFunc(x, parseFactor(FloatMetricVariable.class));
            else if (eat('/')) x = new DivFunc(x, parseFactor(FloatMetricVariable.class));
            else return x;
        }
    }

	private ValueComputable parseFactor(Class<? extends MetricVariable> typeIfMetricVariable) 
			throws ParseException, ConfigurationException {
		
        if (eat('+')) 
        		return parseFactor(FloatMetricVariable.class); // unary plus
        if (eat(MinusFunc.REPRESENTATION)) 
        		return new MinusFunc(parseFactor(FloatMetricVariable.class)); // unary minus

        ValueComputable x;
        int startPos = this.pos;
        if (eat('(')) { // parentheses
            x = parseExpression();
            eat(')');
        } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
            while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
            x = FloatValue.from(str.substring(startPos, this.pos));
        } else if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')) { // functions, boolean and variables
            while (ch >= 'a' && ch <= 'z'
            			|| (ch >= 'A' && ch <= 'Z')
            			|| (ch >= '0' && ch <= '9') 
            			|| ch == '-') 
            		nextChar();
            String text = str.substring(startPos, this.pos);
            
            if(ch == '(')
            		x = parseFunction(text);
            else if(text.equals("true") || text.equals("false"))
            		x = BooleanValue.from(text);
            else
            		x = parseVariable(text, typeIfMetricVariable);
        } else {
            throw new ParseException("Unexpected: " + (char)ch, pos);
        }

        if (eat(PowFunc.REPRESENTATION)) 
        		x = new PowFunc(x, parseFactor(FloatMetricVariable.class));

        return x;
    }

	private ValueComputable parseVariable(String text, Class<? extends MetricVariable> typeIfMetricVariable) throws ConfigurationException, ParseException {
		if(variableNames.contains(text)) {
			if(!variables.containsKey(text)) {
				if(typeIfMetricVariable.equals(FloatMetricVariable.class))
					variables.put(text, new FloatMetricVariable(text));
				
				variables.get(text).config(variablesProperties.getSubset(text));
			}
			
			return variables.get(text);
		}else
			throw new ParseException("Unknown variable: " + text, pos);
	}

	private ValueComputable parseFunction(String func) throws ParseException, ConfigurationException {
		ValueComputable x = parseFactor(FloatMetricVariable.class);
		
        if (func.equals(SqrtFunc.REPRESENTATION)) x = new SqrtFunc(x);
        else if (func.equals(SinFunc.REPRESENTATION)) x = new SinFunc(x);
        else if (func.equals(CosFunc.REPRESENTATION)) x = new CosFunc(x);
        else if (func.equals(TanFunc.REPRESENTATION)) x = new TanFunc(x);
        else if (func.equals(AbsFunc.REPRESENTATION)) x = new AbsFunc(x);
        else
        		throw new ParseException("Unknown function: " + func, pos);
        
		return x;
	}

}
