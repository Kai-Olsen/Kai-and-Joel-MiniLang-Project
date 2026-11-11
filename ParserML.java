import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParserML {
    
    private final List<Tokens> tokens;  // The list of tokens we obtained from the file.
    private int pos;                    // The current position in the list.
    private boolean trace;              // The indicator that trace mode is active or not.

    // Stores declared variables and their values.
    private final Map<String, Object> variables = new HashMap<>();

    // Constructor for our parser.
    public ParserML(List<Tokens> tokens, boolean traceEnabled) {
        this.tokens = tokens;
        trace = traceEnabled;
    }
    
    /**
     * CurrentToken method gives the current token
     * @return Returns the token in the current position
     */
    private Tokens CurrentToken() {
        return tokens.get(pos);
    }

    /**
     * MatchToken method sees if a given token type matches the type of the current token.
     * @param tokenType the given token type we are comparing to.
     * @return true if the given token matches the current token. false otherwise.
     */
    private boolean MatchToken(String tokenType) {
        // Checking if the current token type matches with the given token type
        if (!CurrentToken().type.equals(tokenType)) {
            // Returning false because it didn't match.
            return false;
        }
        pos++;
        return true;
    }

    // Requires a specific token or reports a syntax error.
    private Tokens Expect(String tokenType, String message) {
        if (CurrentToken().type.equals(tokenType)) {
            return tokens.get(pos++);
        }

        throw new RuntimeException("Syntax Error: " + message + " Found '" + CurrentToken().value + "' instead.");
    }

    // Starts parsing the entire program.
    public void ParseProgram() {
        while (!CurrentToken().type.equals("EOF")) {
            int oldPos = pos;
            ParseStatement();
            // Prevents an infinite parsing loop.
            if (pos == oldPos) {
                throw new RuntimeException("Parser error: Unable to process token " + CurrentToken().value);
            }
        }
    }

    // Determines which type of statement is being parsed. And tracing the parser with the trace mode
    private void ParseStatement() {
        traceMode("PARSE", "Statement at " + CurrentToken().type);
        
        if (MatchToken("LET")) {
            ParseDeclaration();
        } else if (MatchToken("DISPLAY")) {
            ParseDisplay(true);
        } else if (MatchToken("IF")) {
            ParseIf();
        } else if (CurrentToken().type.equals("IDENTIFIER")
                && pos + 1 < tokens.size()
                && tokens.get(pos + 1).type.equals("ASSIGN")) {
            ParseAssignment();
        } else {
            throw new RuntimeException("Syntax Error: Unexpected token '" + CurrentToken().value + "'. Expected a keyword (i.e. display, let, if)");
        }
    }

    // let x = 5;
    private void ParseDeclaration() {
        ParseVariableUpdate(true);
    }

    // x = x + 2;
    private void ParseAssignment() {
        ParseVariableUpdate(false);
    }

    private void ParseVariableUpdate(boolean declaration) {
        Tokens name = Expect("IDENTIFIER", "Expected a variable name"
            + (declaration ? " after 'let'." : "."));
        boolean alreadyDeclared = variables.containsKey(name.value);
        if (alreadyDeclared == declaration) {
            throw new RuntimeException(declaration
                ? "Error: Variable '" + name.value + "' is already declared."
                : "Error: Variable '" + name.value + "' has not been declared.");
        }

        Expect("ASSIGN", declaration
            ? "Expected '=' after variable name."
            : "Expected '=' in assignment.");
        Object value = ParseExpression();
        Expect("SEMICOLON", declaration
            ? "Expected ';' after declaration."
            : "Expected ';' after assignment.");
        // Capturing the old value, updating the map, and tracing the change.
        boolean existed = variables.containsKey(name.value);
        Object oldValue = variables.get(name.value);

        variables.put(name.value, value);

        traceMode("ENV", "\t" + name.value + ": " + (existed ? formatValue(oldValue) : "<undefined>") + " -> " + formatValue(value));
    }

    // display (expression);
    // RequireSemicolon determines if a display method needs to have a semi-colon. Used mainly in the case of if statements.
    private void ParseDisplay(boolean requireSemicolon) {
        Expect("LPAREN", "Expected '(' after display.");
        Object value = ParseExpression();
        Expect("RPAREN", "Expected ')' after display expression.");
        if (requireSemicolon) {
            Expect("SEMICOLON", "Expected ';' after display.");
        } else {
            MatchToken("SEMICOLON");
        }
        System.out.println("\nOutput:\nResult: " + formatValue(value));
    }

    // if condition then statement else statement
    // The THEN keyword is optional
    private void ParseIf() {
        Object condition = ParseExpression();
        if (!(condition instanceof Boolean)) {
            throw new RuntimeException("Error: Condition must evaluate to true or false.");
        }

        // using THEN is optional
        MatchToken("THEN");
        boolean result = (Boolean) condition;
        if (result) ParseConditionalBranch();
        else SkipConditionalBranch();
        Expect("ELSE", "Expected 'else' after then branch.");
        if (result) SkipConditionalBranch();
        else ParseConditionalBranch();
    }

    // Parses a conditional branch.
    // Supports display(...) or a regular expression.
    private void ParseConditionalBranch() {
        if (MatchToken("DISPLAY")) {
            ParseDisplay(false);
        } else {
            Object value = ParseExpression();
            System.out.println("\nOutput:\nResult: " + formatValue(value));
            MatchToken("SEMICOLON");
        }
    }

    // Skips the branch that should not execute.
    private void SkipConditionalBranch() {
        if (MatchToken("DISPLAY")) {
            Expect("LPAREN", "Expected '(' after display.");
            int depth = 1;
            while (depth > 0 && !CurrentToken().type.equals("EOF")) {
                if (MatchToken("LPAREN")) {
                    depth++;
                } else if (MatchToken("RPAREN")) {
                    depth--;
                } else {
                    pos++;
                }
            }
            if (depth > 0) {
                throw new RuntimeException("Syntax Error: Expected ')' after display.");
            }
            MatchToken("SEMICOLON");
        } else {
            ParseExpression();
            MatchToken("SEMICOLON");
        }
    }

    // Parsing Expressions //

    // OR: ||
    private Object ParseExpression() {
        traceMode("PARSE", "Expression");
        return ParseOr();
    }

    private Object ParseOr() {
        Object left = ParseAnd();
        while (MatchToken("OR")) {
            Object right = ParseAnd();
            // Calculating the result and getting the formatting for tracing.
            Object result = toBoolean(left) || toBoolean(right);
            
            traceMode("EVAL", "\t" + formatValue(left) + " || " + formatValue(right) + " = " + formatValue(result));
            left = result;
        }
        return left;
    }

    // AND: &&
    private Object ParseAnd() {
        Object left = ParseEquality();
        while (MatchToken("AND")) {
            Object right = ParseEquality();
            // Calculating the result and getting the formatting for tracing.
            Object result = toBoolean(left) && toBoolean(right);
            
            traceMode("EVAL", "\t" + formatValue(left) + " && " + formatValue(right) + " = " + formatValue(result));
            left = result;
        }
        return left;
    }

    // == and !=
    private Object ParseEquality() {
        Object left = ParseComparison();
        while (CurrentToken().type.equals("EQ") || CurrentToken().type.equals("NEQ")) {
            boolean equal = MatchToken("EQ");
            if (!equal) MatchToken("NEQ");
            Object right = ParseComparison();
            // Calculating the result and getting the formatting for tracing.
            Object result = equal == equalValues(left, right);
            String symbol = equal ? "==" : "!=";

            traceMode("EVAL", "\t" + formatValue(left) + " " + symbol + " " + formatValue(right) + " = " + formatValue(result));

            left = result;
        }
        return left;
    }

    // <, >, <=, >=
    private Object ParseComparison() {
        Object left = ParseAddition();
        while (true) {
            String operator = CurrentToken().type;
            if (!operator.equals("LT") && !operator.equals("GT")
                    && !operator.equals("LTEQ") && !operator.equals("GTEQ")) 
                break;
            
            pos++;
            Object right = ParseAddition();
            double leftNumber = toNumber(left);
            double rightNumber = toNumber(right);

            Object result;
            String symbol;

            // Calculating the result and getting the formatting for tracing.
            // Using a switch case to determine what comparison is being done.
            switch (operator) {
                case "LT": result = leftNumber < rightNumber; symbol = "<"; break;
                case "GT": result = leftNumber > rightNumber; symbol = ">"; break;
                case "LTEQ": result = leftNumber <= rightNumber; symbol = "<="; break;
                default: result = leftNumber >= rightNumber; symbol = ">="; break;
            }

            traceMode("EVAL", "\t" + formatValue(left) + " " + symbol + " " + formatValue(right) + " = " + formatValue(result));
            left = result;
        }
        return left;
    }

    // + and -
    private Object ParseAddition() {
        Object left = ParseMultiplication();
        while (true) {
            String operator = CurrentToken().type;
            if (operator.equals("PLUS")) {
                if (!(left instanceof Number)) {
                    throw new RuntimeException("Syntax Error: Expected a number before '+'.");
                }
            } else if (!operator.equals("MINUS")) {
                break;
            }
            pos++;
            Object right = ParseMultiplication();
            
            // Determining if we are adding or subtracting
            Object result = operator.equals("PLUS")
                ? toNumber(left) + toNumber(right)
                : toNumber(left) - toNumber(right);
            
            String symbol = operator.equals("PLUS") ? "+" : "-";
            traceMode("EVAL", formatValue(left) + " " + symbol + " " + formatValue(right) + " = " + formatValue(result));

            left = result;
        }
        return left;
    }

    // *, / and %
    private Object ParseMultiplication() {
        Object left = ParsePower();
        Object result;
        String symbol;
        while (true) {
            String operator = CurrentToken().type;
            if (!operator.equals("MULT") && !operator.equals("DIV")
                    && !operator.equals("MOD")) {
                break;
            }
            pos++;
            Object right = ParsePower();
            // Calculating for multiplication
            if (operator.equals("MULT")) {
                result = toNumber(left) * toNumber(right);
                symbol = "*";
            } else {
                double divisor = toNumber(right);
                if (divisor == 0) { // Divide/mod by 0 check
                    throw new RuntimeException(operator.equals("DIV")
                        ? "Error: " + "Division by zero." : "Modulus by zero.");
                }
                // Calculating for division or modulus
                result = operator.equals("DIV")
                    ? toNumber(left) / divisor
                    : toNumber(left) % divisor;
                symbol = operator.equals("DIV") ? "/" : "%";
            }
            traceMode("EVAL", formatValue(left) + " " + symbol + " " + formatValue(right) + " = " + formatValue(result));
            left = result;
        }
        return left;
    }

    // Exponentiation: ^
    // Recursive parsing makes exponentiation right-associative.
    private Object ParsePower() {
        Object left = ParseUnary();
        Object result;
        // Calculating exponents
        if (MatchToken("EXPONENT")) {
            Object right = ParsePower();
            result = Math.pow(toNumber(left), toNumber(right));
            traceMode("EVAL", formatValue(left) + " ^ " + formatValue(right) + " = " + formatValue(result));
            left = result;
        }
        return left;
    }

    // Unary +, - and !
    private Object ParseUnary() {
        if (MatchToken("MINUS")) { // -
            Object operand = ParseUnary();
            Object result = -toNumber(operand);
            traceMode("EVAL", "\t-" + formatValue(operand) + " = " + formatValue(result));
            return result;
        }

        if (MatchToken("PLUS")) { // +
            Object operand = ParseUnary();
            Object result = toNumber(operand);
            traceMode("EVAL", "\t+" + formatValue(operand) + " = " + formatValue(result));
            return result;
        }

        if (MatchToken("NOT")) { // !
            Object operand = ParseUnary();
            boolean value = toBoolean(operand);
            traceMode("EVAL", "\t!" + formatValue(operand) + " = " + !value);
            return !value;
        }

        return ParsePrimary();
    }

    // Numbers, strings, variables, functions and parentheses.
    private Object ParsePrimary() {
        if (MatchToken("NUMBER")) {
            return Double.parseDouble(tokens.get(pos - 1).value);
        }

        if (MatchToken("STRING")) {
            return tokens.get(pos - 1).value;
        }

        if (MatchToken("LPAREN")) {
            Object value = ParseExpression();
            Expect("RPAREN", "Missing closing parenthesis.");
            return value;
        }

        if (CurrentToken().type.equals("IDENTIFIER")) {

            String name = CurrentToken().value;
            pos++;

            // Function call
            if (MatchToken("LPAREN")) {
                return ParseFunction(name);
            }

            // Variable lookup
            if (!variables.containsKey(name)) {
                throw new RuntimeException("Error: Variable '" + name + "' has not been declared.");
            }

            return variables.get(name);
        }

        throw new RuntimeException("Syntax Error: Expected an expression after '" + tokens.get(pos-1).value + "'. Found '" + CurrentToken().value + "' instead.");
    }

    // Built in Functions //
    private Object ParseFunction(String name) {
        Object first = ParseExpression();
        Object second = null;
        if (MatchToken("COMMA")) second = ParseExpression();
        Expect("RPAREN", "Missing ')' after function arguments.");
        double x = toNumber(first);

        switch (name) {
            case "abs":
                requireNoSecondArgument(name, second);
                return Math.abs(x);
            case "sqrt":
                requireNoSecondArgument(name, second);
                if (x < 0) {
                    throw new RuntimeException("Error: sqrt() cannot accept a negative number.");
                }
                return Math.sqrt(x);
            case "pow":
                return Math.pow(x, toNumber(requireSecondArgument(name, second)));
            case "min":
                return Math.min(x, toNumber(requireSecondArgument(name, second)));
            case "max":
                return Math.max(x, toNumber(requireSecondArgument(name, second)));
            // custom function
            case "average":
                return (x + toNumber(requireSecondArgument(name, second))) / 2.0;
            default:
                throw new RuntimeException("Error: Unknown function: " + name);
        }
    }

    // Checks that two-argument functions have a second argument.
    private Object requireSecondArgument(String name, Object second) {
        if (second == null) {
            throw new RuntimeException("Syntax Error: " + name + "() requires exactly two arguments.");
        }
        return second;
    }

    private void requireNoSecondArgument(String name, Object second) {
        if (second != null) {
            throw new RuntimeException("Error: " + name + "() requires exactly one argument.");
        }
    }

    // Helper Methods //
    // Formats integers to have no decimal places
    private String formatValue(Object value) {
        if (value instanceof Number) {
            double number = ((Number) value).doubleValue();
            if (number == Math.rint(number)) {
                return String.valueOf((long) number);
            }
        }
        return String.valueOf(value);
    }

    // Trace mode method
    private void traceMode (String category, String message) {
        if (trace)
            System.out.println("[" + category + "] \t" + message);
    }

    private double toNumber(Object value) {
        if (value instanceof Number) return ((Number) value).doubleValue();
        throw new RuntimeException("Error: Expected a numeric value.");
    }

    private boolean toBoolean(Object value) {
        if (value instanceof Boolean) return (Boolean) value;
        throw new RuntimeException("Error: Expected a boolean value.");
    }

    private boolean equalValues(Object a, Object b) {
        return a instanceof Number && b instanceof Number
            ? toNumber(a) == toNumber(b)
            : a.equals(b);
    }
} 
