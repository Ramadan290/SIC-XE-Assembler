package Assembler;

import OperandPkg.*;
import SymbolPkg.*;

import java.io.*;
import java.util.LinkedList;

/**
 * Assembler will generate
 * Pass 1-
 * Pass 2-
 */
public class Main {
    public static void main(String[] args) throws IOException{
        SymbolTable symbolTable = new SymbolTable();
        LinkedList<Literal> literalTable = new LinkedList<>();

        // set input files
        String inputFile;
        if(args.length < 1){
            System.out.println("Waiting for File");

            // Take input SIC/XE File
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Enter SIC Assembly Source file : ");
            inputFile = reader.readLine();
        } else {
            inputFile = args[0];
        }

        System.out.println("Reading from File : " + inputFile );

        // ********************** PASS 1 **********************

        // This Function creates a file that will include - input file / Symbol Table / Literal Table(if it exists)
        //those Functions are taken from another Java class file in the same Package
        Pass1.generateIntermediate(inputFile, symbolTable, literalTable);

        // Create and Print the .inc file (will be used later in LinkingLoader)
        System.out.println("\n*********** PASS 1 ***********");
        String incFileNmae = inputFile.substring(0, inputFile.indexOf('.')).concat(".int");
        System.out.println("\n> Generated Intermediate File : " + incFileNmae);
        Utility.printFile(incFileNmae);

        // printing symbol table
        System.out.println("\n> Symbol Table\nSymbol\tValue\trflag\tiflag\tmflag");
        int pass1symbolTableSize = symbolTable.size();
        if(pass1symbolTableSize != 0)
            symbolTable.view();
        else
            System.out.println("(Symbol Table is Empty)");

        // printing literal table
        System.out.println("\n> Literal Table\nLiteral\t\t\tValue\t\tlength\taddress");
        if(literalTable.size() != 0) {
            for (Literal literal : literalTable)
                System.out.println(literal);
        } else {
            System.out.println("(Literal Table is Empty)");
        }

        // ********************** PASS 2 **********************

        System.out.println("\nContinue to see the output of Pass 2.");
        Utility.enterToContinue();

        // This function creates an updates intermediate file in .txt extension, and object file in .o extension
        Pass2.generateObj(inputFile, symbolTable, literalTable);

        // print the updated intermediate code
        System.out.println("\n\n*********** PASS 2 ***********");
        String txtfileName = inputFile.substring(0, inputFile.indexOf('.')).concat(".txt");
        System.out.println("\n> Adding object code to Intermediate File : " + txtfileName);
        Utility.printFile(txtfileName);

        System.out.println("\nContinue to see the Updated Symbol table and Object Code");
        Utility.enterToContinue();

        // Printing the symbol table
        if(symbolTable.size() != pass1symbolTableSize) {
            System.out.println("\n> Updated Symbol Table\nSymbol\tValue\trflag\tiflag\tmflag");
            symbolTable.view();
        }
        else
            System.out.println("\nNo external symbol was added during Pass 2.");

        // Printing the generated object code(will be used later in LinkingLoader)
        String objFileName = inputFile.substring(0, inputFile.indexOf('.')).concat(".o");
        System.out.println("\n> Generated Object Code : " + objFileName);
        Utility.printFile(objFileName);
    }
}