import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ParserML{
    
    private List<Tokens> tokens;        // The list of tokens we obtained from the file.
    private int pos;                    // The current position in the list.

    // Stores declared variables and their values.
    private Map<String, Object> variables = new HashMap<>();

    // Constructor for our parser.
    public ParserML(List<Tokens> t){
        tokens = t;
        pos = 0;
    }
    
    /**
     * CurrentToken method gives the current token
     * @return Returns the token in the current position
     */
    private Tokens CurrentToken(){
        return tokens.get(pos);
    }

    /**
     * MatchToken method sees if a given token type matches the type of the current token.
     * @param tokenType the given token type we are comparing to.
     * @return true if the given token matches the current token. false otherwise.
     */
    private boolean MatchToken(String tokenType){
        // Checking if the current token type matches with the given token type
        if (CurrentToken().type.equals(tokenType)){
            pos++;
            return true;
        }

        // Returning flase because it didn't match.
        return false;
    }

    // Requires a specific token or reports a syntax error.
    private Tokens Expect(String tokenType, String message) {
        if (CurrentToken().type.equals(tokenType)) {
            return tokens.get(pos++);
        }

        throw new RuntimeException(
            "Syntax Error: " + message +
            " Found '" + CurrentToken().value + "'."
        );
    }

    // Starts parsing the entire program.
    public void ParseProgram() {

        while (!CurrentToken().type.equals("EOF")) {

            int oldPos = pos;

            ParseStatement();

            // Prevents an infinite parsing loop.
            if (pos == oldPos) {
                throw new RuntimeException(
                    "Parser error: Unable to process token " +
                    CurrentToken().value
                );
            }
        }
    }

    // Determines which type of statement is being parsed.
    private void ParseStatement() {

        if (MatchToken("LET")) {
            ParseDeclaration();
        }

        else if (MatchToken("DISPLAY")) {
            ParseDisplay();
        }

        else if (MatchToken("IF")) {
            ParseIf();
        }

        else if (CurrentToken().type.equals("IDENTIFIER")
                && pos + 1 < tokens.size()
                && tokens.get(pos + 1).type.equals("ASSIGN")) {
            ParseAssignment();
        }

        else {
            throw new RuntimeException(
                "Syntax Error: Unexpected token '" +
                CurrentToken().value + "'."
            );
        }
    }

    // let x = 5;
    private void ParseDeclaration() {

        Tokens name = Expect(
            "IDENTIFIER",
            "Expected a variable name after 'let'."
        );

        if (variables.containsKey(name.value)) {
            throw new RuntimeException(
                "Variable '" + name.value +
                "' is already declared."
            );
        }

        Expect("ASSIGN", "Expected '=' after variable name.");

        Object value = ParseExpression();

        Expect("SEMICOLON", "Expected ';' after declaration.");

        variables.put(name.value, value);
    }

    // x = x + 2;
    private void ParseAssignment() {

        Tokens name = Expect(
            "IDENTIFIER",
            "Expected a variable name."
        );

        if (!variables.containsKey(name.value)) {
            throw new RuntimeException(
                "Variable '" + name.value +
                "' has not been declared."
            );
        }

        Expect("ASSIGN", "Expected '=' in assignment.");

        Object value = ParseExpression();

        Expect("SEMICOLON", "Expected ';' after assignment.");

        variables.put(name.value, value);
    }

    // display (expression);
    private void ParseDisplay() {

        Expect("LPAREN", "Expected '(' after display.");

        Object value = ParseExpression();

        Expect("RPAREN", "Expected ')' after display expression.");

        Expect("SEMICOLON", "Expected ';' after display.");

        System.out.println(value);
    }

    // if condition then statement else statement
    // The THEN keyword is optional
    private void ParseIf() {

        Object condition = ParseExpression();

        if (!(condition instanceof Boolean)) {
            throw new RuntimeException(
                "Condition must evaluate to true or false."
            );
        }

        //using THEN is optional
        MatchToken("THEN");

        boolean result = (Boolean) condition;

        if (result) {
            ParseConditionalBranch();
            Expect("ELSE", "Expected 'else' after then branch.");
            SkipConditionalBranch();
        }
        else {
            SkipConditionalBranch();
            Expect("ELSE", "Expected 'else' after then branch.");
            ParseConditionalBranch();
        }
    }

    // Parses a conditional branch.
    // Supports display(...) or a regular expression.
    private void ParseConditionalBranch() {

        if (MatchToken("DISPLAY")) {
            ParseDisplay();
        }
        else {
            Object value = ParseExpression();
            System.out.println("Result: " + value);

            MatchToken("SEMICOLON");
        }
    }

    // Skips the branch that should not execute.
    private void SkipConditionalBranch() {

        // This implementation is intentionally limited to
        // simple display or expression branches.
        if (MatchToken("DISPLAY")) {
            Expect("LPAREN", "Expected '(' after display.");

            ParseExpression();

            Expect("RPAREN", "Expected ')' after display.");

            MatchToken("SEMICOLON");
        }
        else {
            ParseExpression();
            MatchToken("SEMICOLON");
        }
    }

    // Parsing Expressions //

    // OR: ||
    private Object ParseExpression() {
        return ParseOr();
    }

    private Object ParseOr() {

        Object left = ParseAnd();

        while (MatchToken("OR")) {

            Object right = ParseAnd();

            left = toBoolean(left) || toBoolean(right);
        }

        return left;
    }

    // AND: &&
    private Object ParseAnd() {

        Object left = ParseEquality();

        while (MatchToken("AND")) {

            Object right = ParseEquality();

            left = toBoolean(left) && toBoolean(right);
        }

        return left;
    }

    // == and !=
    private Object ParseEquality() {

        Object left = ParseComparison();

        while (true) {

            if (MatchToken("EQ")) {

                Object right = ParseComparison();

                left = equalValues(left, right);
            }

            else if (MatchToken("NEQ")) {

                Object right = ParseComparison();

                left = !equalValues(left, right);
            }

            else {
                break;
            }
        }

        return left;
    }

    // <, >, <=, >=
    private Object ParseComparison() {

        Object left = ParseAddition();

        while (true) {

            if (MatchToken("LT")) {
                Object right = ParseAddition();
                left = toNumber(left) < toNumber(right);
            }

            else if (MatchToken("GT")) {
                Object right = ParseAddition();
                left = toNumber(left) > toNumber(right);
            }

            else if (MatchToken("LTEQ")) {
                Object right = ParseAddition();
                left = toNumber(left) <= toNumber(right);
            }

            else if (MatchToken("GTEQ")) {
                Object right = ParseAddition();
                left = toNumber(left) >= toNumber(right);
            }

            else {
                break;
            }
        }

        return left;
    }

    // + and -
    private Object ParseAddition() {

        Object left = ParseMultiplication();

        while (true) {

            if (MatchToken("PLUS")) {

                if (!(left instanceof Number)) {
                    throw new RuntimeException(
                        "Expected a number before '+'."
                    );
                }

                Object right = ParseMultiplication();

                left = toNumber(left) + toNumber(right);
            }

            else if (MatchToken("MINUS")) {

                Object right = ParseMultiplication();

                left = toNumber(left) - toNumber(right);
            }

            else {
                break;
            }
        }

        return left;
    }

    // *, / and %
    private Object ParseMultiplication() {

        Object left = ParsePower();

        while (true) {

            if (MatchToken("MULT")) {

                Object right = ParsePower();

                left = toNumber(left) * toNumber(right);
            }

            else if (MatchToken("DIV")) {

                Object right = ParsePower();

                double divisor = toNumber(right);

                if (divisor == 0) {
                    throw new RuntimeException(
                        "Division by zero."
                    );
                }

                left = toNumber(left) / divisor;
            }

            else if (MatchToken("MOD")) {

                Object right = ParsePower();

                double divisor = toNumber(right);

                if (divisor == 0) {
                    throw new RuntimeException(
                        "Modulus by zero."
                    );
                }

                left = toNumber(left) % divisor;
            }

            else {
                break;
            }
        }

        return left;
    }

    // Exponentiation: ^
    // Recursive parsing makes exponentiation right-associative.
    private Object ParsePower() {

        Object left = ParseUnary();

        if (MatchToken("EXPONENT")) {

            Object right = ParsePower();

            left = Math.pow(
                toNumber(left),
                toNumber(right)
            );
        }

        return left;
    }

    // Unary +, - and !
    private Object ParseUnary() {

        if (MatchToken("MINUS")) {
            return -toNumber(ParseUnary());
        }

        if (MatchToken("PLUS")) {
            return toNumber(ParseUnary());
        }

        if (MatchToken("NOT")) {
            return !toBoolean(ParseUnary());
        }

        return ParsePrimary();
    }

    // Numbers, strings, variables, functions and parentheses.
    private Object ParsePrimary() {

        if (MatchToken("NUMBER")) {
            return Double.parseDouble(
                tokens.get(pos - 1).value
            );
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
                throw new RuntimeException(
                    "Variable '" + name +
                    "' has not been declared."
                );
            }

            return variables.get(name);
        }

        throw new RuntimeException(
            "Syntax Error: Expected an expression. Found '" +
            CurrentToken().value + "'."
        );
    }

    // Built in Functions //

    private Object ParseFunction(String name) {

        Object first = ParseExpression();

        Object second = null;

        if (MatchToken("COMMA")) {
            second = ParseExpression();
        }

        Expect("RPAREN", "Missing ')' after function arguments.");

        double x = toNumber(first);

        switch (name) {

            case "abs":
                if (second != null)
                    throw new RuntimeException(
                        "abs() requires exactly one argument."
                    );

                return Math.abs(x);

            case "sqrt":
                if (second != null)
                    throw new RuntimeException(
                        "sqrt() requires exactly one argument."
                    );

                if (x < 0)
                    throw new RuntimeException(
                        "sqrt() cannot accept a negative number."
                    );

                return Math.sqrt(x);

            case "pow":
                requireSecondArgument(name, second);

                return Math.pow(x, toNumber(second));

            case "min":
                requireSecondArgument(name, second);

                return Math.min(x, toNumber(second));

            case "max":
                requireSecondArgument(name, second);

                return Math.max(x, toNumber(second));
            //custom function    
            case "average":
                requireSecondArgument(name, second);

                return (x + toNumber(second)) / 2.0;

            default:
                throw new RuntimeException(
                    "Unknown function: " + name
                );
        }
    }

    // Checks that two-argument functions have a second argument.
    private void requireSecondArgument(
            String name, Object second) {

        if (second == null) {
            throw new RuntimeException(
                name + "() requires exactly two arguments."
            );
        }
    }

    // Helper Methods //

    private double toNumber(Object value) {

        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        throw new RuntimeException(
            "Expected a numeric value."
        );
    }

    private boolean toBoolean(Object value) {

        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        throw new RuntimeException(
            "Expected a boolean value."
        );
    }

    private boolean equalValues(Object a, Object b) {

        if (a instanceof Number && b instanceof Number) {
            return toNumber(a) == toNumber(b);
        }

        return a.equals(b);
    }
} 
