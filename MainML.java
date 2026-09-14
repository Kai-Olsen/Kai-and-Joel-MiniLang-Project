import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class MainML{
    public static void main(String[] args) throws IOException{
        
        PrintWriter mlFile = new PrintWriter("MiniLang.txt");

        // Testing the PrintWriter object we just made
        mlFile.println("This is a test using PrintWriter");
        mlFile.print("This is printed without using new line");
        mlFile.print("Test");
        mlFile.print("\nThis will be on a new line");

        mlFile.close();
    }
}