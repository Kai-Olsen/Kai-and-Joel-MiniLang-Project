import java.util.*;

public class TokenizerML{
    private String line;                // The line of the ML file being tokenized.
    private int pos;                    // The position of the pointer
    private List<Tokens> tokens;     // The list of tokens

    // Constructor for the tokenizer
    public TokenizerML(String line){
        this.line = line;
        this.pos = 0;
        this.tokens = new ArrayList<>();
    }

    // This method allows us to parse through and tokenize the String to help with the evaluation
    public List<Tokens> tokenizer(){
        // Looping through each character in the given line to see if each piece is correct. (i.e. has no syntax errors)
        while(pos < line.length()){
            char c = line.charAt(pos);

            // Checking for whitespace using the isWhitespace method.
            if (Character.isWhitespace(c)){
                pos++;
                continue;
            }

            // Comment check to ignore the line if it is a comment line
            if(c == '/' && pos + 1 < line.length() && line.charAt(pos+1) == '/')
                break; // breaking because we don't have to check this line anymore

            // Checking for keywords or identifiers
            if(Character.isLetter(c)) {
                // Looping through to take each character of the word
                int start = pos;
                while(pos < line.length() && Character.isLetterOrDigit(line.charAt(pos)))
                    pos++;
                // Putting the word together
                String word = line.substring(start, pos);
                
                // Seeing if the word is a keyword and adding it to the tokens list
                if (DictionaryML.KEYWORDS.containsKey(word))
                    tokens.add(new Tokens(DictionaryML.KEYWORDS.get(word), word));
                // Otherwise making it an identifier token
                else
                    tokens.add(new Tokens("IDENTIFIER", word));
                continue;
            }

            // Checking for numbers
            if(Character.isDigit(c)){
                int start = pos;
                // Integer numbers
                while(pos < line.length() && Character.isDigit(line.charAt(pos)))
                    pos++;

                // Decimal numbers
                if (pos < line.length() && line.charAt(pos) == '.'){
                    pos++;

                    // Error recognition for a decimal point with no digits following
                    if (pos >= line.length() || !Character.isDigit(line.charAt(pos)))
                        throw new RuntimeException("Expected a digit after the decimal point.");
                    // Numbers after the decimal point.
                    while(pos < line.length() && Character.isDigit(line.charAt(pos)))
                        pos++;
                }
                // Putting the number together and putting it into a token
                tokens.add(new Tokens("NUMBER", line.substring(start, pos)));
                continue;
            }

            // Checking for multi-character operations first, like == or <= 
            boolean isOperator = false;

            if(pos + 1 < line.length()) { // makes sure that the operator isn't the last characters
                String twoChars = line.substring(pos, pos+2); // formining the two character string 
                // Checking to see if it is a multi-character operation
                if (DictionaryML.OPERATORS.containsKey(twoChars)){
                    tokens.add(new Tokens(DictionaryML.OPERATORS.get(twoChars), twoChars));
                    pos += 2;
                    // We found an operator, so set isOperator to true
                    isOperator = true;
                }
            }
            
            // If not a multi-character operation, checking if it is a single-line operator.
            if (!isOperator && DictionaryML.OPERATORS.containsKey(String.valueOf(c))){
                tokens.add(new Tokens(DictionaryML.OPERATORS.get(String.valueOf(c)), String.valueOf(c)));
                pos++;
                // We found an operator, so set isOperator to true
                isOperator = true;
            }

            if (isOperator)
                continue;

            // Checking for Strings
            if (c == '"'){
                pos++;
                int start = pos;
                // Looping through while it is still a String
                while (pos < line.length() && line.charAt(pos) != '"')
                    pos++;

                // If there is no end quotation, throw an error.
                if (pos >= line.length())
                    throw new RuntimeException("Missing end quotation.");

                tokens.add(new Tokens("STRING", line.substring(start, pos)));
                pos++;
                continue;
            }

            // Error - invalid character
            throw new RuntimeException("Invalid character: " + c);
        }
        return tokens;
    }

    // Creating a Tokens class in order to each token type with its value.
    public class Tokens {
        public String type;
        public String value;

        // Constructor for Tokens 
        public Tokens(String t, String v){
            type = t;
            value = v;            
        }
    }
}