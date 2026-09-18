import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class MainML{
    public static void main(String[] args) throws IOException{
        
        // Declaration to read the files. 
        File inFile = new File("MiniLang.txt");
        Scanner readFile = new Scanner(inFile);
        
        // Break when file is out of lines
        while (readFile.hasNextLine()){
            String line = readFile.nextLine();
            System.out.println(line);

        readFile.close();
    }
}
