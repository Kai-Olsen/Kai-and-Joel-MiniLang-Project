import java.util.List;

public class ParserML{
    
    private List<Tokens> tokens;        // The list of tokens we obtained from the file.
    private int pos;                    // The current position in the list.

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
} 