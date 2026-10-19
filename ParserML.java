import java.util.List;

public class ParserML{
    
    private List<Tokens> tokens;        // The list of tokens we obtained from the file.
    private int pos;                    // The current position in the list.

    // Constructor for our parser.
    public ParserML(List<Tokens> t){
        tokens = t;
        pos = 0;
    }
} 