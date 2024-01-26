package Assembler;

import OperandPkg.Literal;
import OperandPkg.OperandUtility;
import SymbolPkg.Node;
import SymbolPkg.SymbolTable;

import java.io.*;
import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 * Pass1 - This class takes the assembly input file, and Print output File
 */
public class Pass1 {
    public static int startAddress = 0;
    public static int LocationCounter = startAddress;   // both startAddress and LineCounter will change if START directive is found
    public static int programLength = 0;
    private static int endofLocationCounter = 0;
    private static String endSymbol = null;
    public static String controlSectionName = null; //not used yet will be used later in LinkingLoader

    public static void generateIntermediate(
            String assemblyFileName, SymbolTable symbolTable, LinkedList<Literal> literalTable)
            throws IOException {

        // set the output file name, and set an output writer to write on that file (Has to be asm File) i cant use tokens and parse in .txt it worked with c langauge in sic but doesnt work here
        String intFile = assemblyFileName.substring(0, assemblyFileName.indexOf('.')).concat(".int");
        PrintWriter intWriter = new PrintWriter(intFile, "UTF-8");

        // Take input and process SIC Instruction
        BufferedReader asmReader = new BufferedReader(new FileReader(assemblyFileName));
        String instruction = asmReader.readLine();

        String label, opcode, operand;
        int format = 0;

        while (instruction != null) {
            // data flow issue without null initialization
            label = null;
            opcode = null;
            operand = null;
            format = 0;

            // Remove {read from subroutine......}
            instruction = Utility.removeComment(instruction);

            // After removal if there is no token, read the next line
            StringTokenizer tokenizer = new StringTokenizer(instruction);
            if (tokenizer.countTokens() == 0) {
                instruction = asmReader.readLine();
                continue;
            }


// *********************Handling all Types of Instructions***************************************

            // read label
            Node symbol = null;
            // if first character is a white space then there is no label
            if (!(instruction.charAt(0) <= 32)) {
                label = tokenizer.nextToken();

                // populate the symbol table using the label
                symbol = new Node(label);
                symbolTable.add(symbol);
                symbol.value = LocationCounter;
            }

            // read opcode
            if (tokenizer.hasMoreTokens())
                opcode = tokenizer.nextToken();
            if (opcode != null) {
                // update the rflag of the symbol if opcode is EQU
                if (opcode.equals("EQU") && symbol != null)
                    symbol.rflag = false;

                // format 3 or format 4 opcode
                format = OpcodeConvertor.getFormat(opcode);
                LocationCounter += format;
            }

            // read operand
            if (tokenizer.hasMoreTokens())
                operand = tokenizer.nextToken();
            if (operand != null) {
                // if instruction has START assembler directive
                if (opcode.equals("START") && Utility.isInteger(operand)) {
                    startAddress = Integer.parseInt(operand);
                    LocationCounter = startAddress;
                    controlSectionName = label;
                }

                //
                if (operand.equals("*") && symbol != null) {
                    // update the rflag and value of the symbol if opcode is EQU and operand is *
                    symbol.value = LocationCounter;
                    symbol.rflag = true;
                } else {
                    // Evaluate the operand
                    OperandUtility.evaluateOperand(symbolTable, literalTable, operand);
                }
            }

            // We need the line counter of END directive later
            if (opcode.equals("END") && operand != null) {
                endofLocationCounter = LocationCounter;
                endSymbol = operand;
            }

// ************************************************************

            // Handles BYTE operand
            if (opcode.equals("BYTE")) {
                String temp = operand.substring(operand.indexOf('\'') + 1, operand.lastIndexOf('\''));
                if (operand.contains("C"))
                    format = temp.length();
                else
                    format = temp.length() / 2;

                LocationCounter += format;
            }

            // Handles WORD operand
            if (opcode.equals("WORD")) {
                format = 3;
                LocationCounter += format;
            }

            // Handles RESW operand
            if (opcode.equals("RESW")) {
                format = 3 * Integer.parseInt(operand);
                LocationCounter += format;
            }

            // Handles RESB operand
            if (opcode.equals("RESB")) {
                format = Integer.parseInt(operand);
                LocationCounter += format;
            }

            // generate the instructions and Checking null values
             String intermediateInstruction = String.format("%-8s%-12s%-20s%-20s",
                    Utility.padAddress(LocationCounter - format, 5),
                    ((label == null) ? " " : label),
                    ((opcode == null) ? " " : opcode),
                    ((operand == null) ? " " : operand));

            // print the intermediate instruction to terminal and file
            intWriter.println(intermediateInstruction);

            // print the intermediate instruction to terminal
            instruction = asmReader.readLine();
        }

        // add the literal lists at the end of the program and update the address of the literals
        for (Literal literal : literalTable) {
            String intermediateInstruction = String.format("%-8s*           %-20s", Utility.padAddress(LocationCounter - format, 5), literal.name);

            intWriter.println(intermediateInstruction);

            literal.address = LocationCounter - format;
            LocationCounter += literal.length;
        }

        // Print the program length while the LocationCounter is always updated
        System.out.println("Program Length : " + Integer.toHexString(LocationCounter).toUpperCase());
        int sizeOfLiteralTable = LocationCounter - endofLocationCounter;
        if (endSymbol == null)
            programLength = LocationCounter;
        else
            programLength = (endofLocationCounter - symbolTable.search(endSymbol).getValue()) + sizeOfLiteralTable;

        // close the intermediate file
        intWriter.close();

    }
}
