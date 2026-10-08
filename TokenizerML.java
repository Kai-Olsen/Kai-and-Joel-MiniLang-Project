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

            // Checking for keyword indicators
            if(Character.isLetter(c)) {
                // Looping through to see if it is a valid keyword
                int start = pos;
                while(pos < line.length() && Character.isLetter(line.charAt(pos)))
                    pos++;
                // Putting the word together
                String word = line.substring(start, pos);
                
                // Seeing if the word is a keyword and adding it to the tokens list
                if (DictionaryML.KEYWORDS.containsKey(word))
                    tokens.add(new Tokens(DictionaryML.KEYWORDS.get(word), word));
                // If the word is one character long, making it a variable token
                else if (word.length() == 1)
                    tokens.add(new Tokens("VARIABLE", word));

                
            }

            // Checking for multi-character operations first, like == or <= 
            if(pos + 1 < line.length()) { // makes sure that the operator isn't the last characters
                String twoChar = line.substring(pos, pos+2); // formining the 
                //DictionaryML.OPERATORS
            }
            

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