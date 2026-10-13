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
            allTokens.addAll(tokenize.tokenizer());
        }
        // Adding and end of file token to indicate to our parser that it has reached the end of the file.
        allTokens.add(new Tokens("EOF", ""));

        // Start of the Trace mode
        for(Tokens tokens : allTokens){
            System.out.println(tokens);
        }

        readFile.close();
    }
}
