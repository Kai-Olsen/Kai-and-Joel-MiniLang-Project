import java.util.*;

public class TokenizerML{
    private String line;                // The line of the ML file being tokenized.
    private int pos;                    // The position of the pointer
    private List<Character> tokens;     // The list of tokens

    // Constructor for the tokenizer
    public TokenizerML(String line){
        this.line = line;
        this.pos = 0;
        this.tokens = new ArrayList<>();
    }

    // This method allows us to parse through and tokenize the String to help with the evaluation
    public List<Character> tokenizer(){
        // Looping through each character in the given line to see if each piece is correct. (i.e. has no syntax errors)
        while(pos < line.length()){
            char c = line.charAt(pos);

            // Checking for whitespace using the isWhitespace method.
            if (Character.isWhitespace(c)){
                pos++;
                continue;
            }

            

        }
        return tokens;
    }
}