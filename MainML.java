import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainML{
    public static void main(String[] args) throws IOException{
        
        // Declaration to read the files. 
        File inFile = new File("MiniLang.txt");
        Scanner readFile = new Scanner(inFile);

        List<Tokens> allTokens = new ArrayList<>();
        
        // Break when file is out of lines
        System.out.println("Input: ");
        while (readFile.hasNextLine()){
            // Reading each line from the text file and printing it out for the input
            String line = readFile.nextLine();
            System.out.println(line);

            // Putting the line through TokenizerML in order to store the tokens to allTokens for our parser
            TokenizerML tokenize = new TokenizerML(line);
            // Catching any errors in the tokenizer
            try{
                allTokens.addAll(tokenize.tokenizer());
            } catch (RuntimeException e) {
                System.err.println(e.getMessage());
                readFile.close();
                return;
            }
        }
        // Adding and end of file token to indicate to our parser that it has reached the end of the file.
        allTokens.add(new Tokens("EOF", ""));

        // Start of the Trace mode
        // Checking for --trace in the consol arguments to turn on trace mode
        boolean traceEnabled = args.length > 0 && args[0].equals("--trace"); 
        if (traceEnabled){
            System.out.println();
            for(Tokens tokens : allTokens){
                System.out.println(tokens);
            }
        }

        //calling parser
        System.out.println("\nOutput:");
        ParserML parser = new ParserML(allTokens, traceEnabled);
        // Catching any errors within the parsing of the code and printing the given message
        try {
            parser.ParseProgram();
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
        }
        readFile.close();
    }
}
