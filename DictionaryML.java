import java.util.Map;
import java.util.Hashtable;

public class DictionaryML{
    // Using the Map class because it allows us to use the containsKey method for tokenizing
    // Creating a dictionary for the keywords using the Map class
    final static Map<String, String> KEYWORDS = new Hashtable<>();

    // Creating a dictionary for the operators using the Map class
    final static Map<String, String> OPERATORS = new Hashtable<>();

    public DictionaryML() {
        // Initializing the keyphrases that we will be looking for to read MiniLang programs
        KEYWORDS.put("let", "LET");
        KEYWORDS.put("if", "IF");
        KEYWORDS.put("then", "THEN");
        KEYWORDS.put("else", "ELSE");
        KEYWORDS.put("display", "DISPLAY");

        // Initializing the operators that we will be looking for to evaluate MiniLang programs
        OPERATORS.put("+", "PLUS");
        OPERATORS.put("-", "MINUS");
        OPERATORS.put("*", "MULT");
        OPERATORS.put("/", "DIV");
        OPERATORS.put("%", "MOD");
        OPERATORS.put("^", "EXPONENT");
        OPERATORS.put("=", "ASSIGN");
        OPERATORS.put(";", "END");
        OPERATORS.put("(", "LPAREN");
        OPERATORS.put(")", "RPAREN");
        OPERATORS.put("==", "EQ");
        OPERATORS.put("!=", "NEQ");
        OPERATORS.put("<", "LT");
        OPERATORS.put(">", "GT");
        OPERATORS.put("<=", "LTEQ");
        OPERATORS.put(">=", "GTEQ");
        OPERATORS.put("&&", "AND");
        OPERATORS.put("||", "OR");
        OPERATORS.put("!", "NOT");
    }
}
