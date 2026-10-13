import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class MainML{
    public static void main(String[] args) throws IOException{
        
        System.out.println("Hello world");

        // Declaration to read the files. 
        File inFile = new File("MiniLang.txt");
        Scanner readFile = new Scanner(inFile);

        System.out.println("Hello world");

        //String source = "";
        
        // Break when file is out of lines
        System.out.println("Input: ");
        while (readFile.hasNextLine()){
            // Reading each line from the text file and printing it out for the input
            String line = readFile.nextLine();
            System.out.println(line);

            //source += line + "\n";
        }

        //System.out.println(source);
        readFile.close();
    }
}
