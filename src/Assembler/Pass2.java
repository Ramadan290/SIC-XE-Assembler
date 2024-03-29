package Assembler;

import OperandPkg.Literal;
import OperandPkg.Operand;
import OperandPkg.OperandUtility;
import SymbolPkg.Node;
import SymbolPkg.SymbolTable;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 *  Pass2 - This Class
 */
public class Pass2 {
    public static String HRecord = null;
    public static ArrayList<String> DRecordLists = new ArrayList<>();
    public static ArrayList<String> RRecordLists = new ArrayList<>();
    public static TRecordList TRecordLists = new TRecordList();
    public static ArrayList<String> MRecordLists = new ArrayList<>();
    public static String ERecord = null;

    public static boolean useBase = false;
    public static int baseAddress = 0;


    /**
     Loop over all instructions , Symbol Table and Literal Table that was generated in Pass1
     */

    public static void generateObj(String inputFile, SymbolTable symbolTable, LinkedList<Literal> literalTable) throws IOException{
        inputFile = inputFile.substring(0, inputFile.indexOf('.')).concat(".int");
        String txtFile = inputFile.substring(0, inputFile.indexOf('.')).concat(".txt");
        String objFile = inputFile.substring(0, inputFile.indexOf('.')).concat(".o");

        try(
                BufferedReader intReader = new BufferedReader(new FileReader(inputFile));
                PrintWriter txtWriter = new PrintWriter(txtFile, "UTF-8");
                PrintWriter objectWriter = new PrintWriter(objFile, "UTF-8");
        )
        {
            String instruction = intReader.readLine();
            while (instruction != null){
                String objectCode = "";
                String[] fields = getFields(instruction);

// ASSEMBLER starts parsing all instructions ********************************************************

                // START
                if(fields[2].equals("START")){
                    String programName = Utility.padLabel(fields[1]);
                    String startAddress = Utility.padAddress(Pass1.startAddress, 6);
                    String programLength = Utility.padAddress(Pass1.programLength, 6);
                    objectCode = "H^" + programName +"^" + startAddress +"^"+ programLength;

                    txtWriter.println(instruction);
                    HRecord = objectCode;
                    instruction = intReader.readLine();
                    continue;
                }

                // EQU, RESB and RESW
                if(fields[2].equals("EQU") | fields[2].equals("RESB") | fields[2].equals("RESW")){

                    txtWriter.println(instruction);
                    TRecordLists.terminateTRecord();
                    instruction = intReader.readLine();
                    continue;
                }

                // EXTDEF
                if(fields[2].equals("EXTDEF")){
                    objectCode = objectCode.concat("D");

                    StringTokenizer tokenizer = new StringTokenizer(fields[3], ",");
                    while(tokenizer.hasMoreTokens()){
                        String symbolName = tokenizer.nextToken();
                        int symbolValue = symbolTable.search(symbolName).getValue();
                        objectCode = objectCode.concat("^").concat(Utility.padLabel(symbolName)).concat("^").concat(Utility.padAddress(symbolValue, 6));
                    }

                    txtWriter.println(instruction);
                    DRecordLists.add(objectCode);
                    TRecordLists.terminateTRecord();
                    instruction = intReader.readLine();
                    continue;
                }

                // EXTREF
                if(fields[2].equals("EXTREF")){
                    objectCode = objectCode.concat("R");

                    StringTokenizer tokenizer = new StringTokenizer(fields[3], ",");
                    while (tokenizer.hasMoreTokens()){
                        String symbolName = tokenizer.nextToken();

                        Node externalSymbol = new Node(symbolName);
                        externalSymbol.value = 0;
                        externalSymbol.rflag = false;
                        externalSymbol.iflag = false;
                        symbolTable.add(externalSymbol);

                        objectCode = objectCode.concat("^").concat(Utility.padLabel(symbolName));
                    }

                    txtWriter.println(instruction);
                    RRecordLists.add(objectCode);
                    TRecordLists.terminateTRecord();
                    instruction = intReader.readLine();
                    continue;
                }

                // ERROR HANDLING
                String status = OperandUtility.evaluateOperand(symbolTable, literalTable, fields[3]);
                if(status != "valid" && OpcodeConvertor.getFormat(fields[2]) != 2){
                    objectCode = status;
                    txtWriter.printf("%-60s%s\n", instruction, objectCode);
                    instruction = intReader.readLine();
                    continue;
                }
                Operand operand = OperandUtility.operand;
                Literal literal = OperandUtility.literal;

                // BASE
                if(fields[2].equals("BASE")){
                    baseAddress = operand.value;
                    useBase = true;

                    txtWriter.println(instruction);
                    TRecordLists.terminateTRecord();
                    instruction = intReader.readLine();
                    continue;
                }

                // BYTE C'EOF' and BYTE X'0F'
                if(fields[2].equals("BYTE")){
                    if(fields[3].contains("C'")){
                        // convert C'EOF' to EO because assembler takes the first 2 Hexadecimal digits only
                        String charValue = fields[3].substring(fields[3].indexOf("'")+1, fields[3].lastIndexOf("'"));

                        // Convert EO to 4142
                        String hexValue = "";
                        for(int i = 0; i<charValue.length(); i++) {
                            hexValue = hexValue.concat(Integer.toHexString((int) charValue.charAt(i)));
                        }

                        objectCode = hexValue.toUpperCase();
                        txtWriter.printf("%-60s%s\n",instruction, objectCode);
                        TRecordLists.add(objectCode, fields);
                        instruction = intReader.readLine();
                        continue;
                    }

                    // BYTE X'0F'
                    else if(fields[3].contains("X'")){
                        // convert X'1F' to 1F because it contains 2 bytes (this is not specified for X'1F' only i just gave an example here)
                        String hexValue = fields[3].substring(fields[3].indexOf("'")+1, fields[3].lastIndexOf("'"));

                        objectCode = hexValue.toUpperCase();
                        txtWriter.printf("%-60s%s\n",instruction, objectCode);
                        TRecordLists.add(objectCode, fields);
                        instruction = intReader.readLine();
                        continue;
                    }

                }

                // WORD
                if(fields[2].equals("WORD")){
                    // Object code of WORD 97 is 000061
                    objectCode = Utility.padAddress(operand.value, 6);

                    txtWriter.printf("%-60s%s\n",instruction, objectCode);
                    TRecordLists.add(objectCode, fields);
                    MRecordLists.addAll(generateMRecord(fields, symbolTable));
                    instruction = intReader.readLine();
                    continue;
                }

// OPCode conversion for obtaining object code ***************************

                // * C'ABC'
                if(fields[1] != null && fields[1].equals("*")){
                    objectCode = findLiteralValue(literalTable, fields[2]);

                    objectCode = objectCode.toUpperCase();
                    txtWriter.printf("%-60s%s\n",instruction, objectCode);
                    TRecordLists.add(objectCode, fields);
                    instruction = intReader.readLine();
                    continue;
                }


                if(!fields[2].equals("END")){

                    // 3A
                    int opcode = Integer.parseInt(OpcodeConvertor.getHexCode(fields[2]), 16);
                    int addressingMode = getAddressingMode(operand);
                    objectCode = objectCode.concat(Utility.padAddress(opcode + addressingMode, 2));

// format 1 ********************************
                    if(OpcodeConvertor.getFormat(fields[2]) == 1) {
                        txtWriter.printf("%-60s%s\n",instruction, objectCode);
                        TRecordLists.add(objectCode, fields);
                        instruction = intReader.readLine();
                        continue;
                    }

// format 2 ************************************
                    if(OpcodeConvertor.getFormat(fields[2]) == 2){

                        // get the value of the registers
                        StringTokenizer tokenizer = new StringTokenizer(fields[3], ",");
                        while(tokenizer.hasMoreTokens()){
                            String registerName = tokenizer.nextToken();
                            if(registerName != null)
                                objectCode = objectCode.concat(Integer.toString(Utility.getRegisterValue(registerName)));
                        }

                        // Pad the object code to length 4
                        while (objectCode.length()<4) {
                            objectCode = objectCode.concat("0");
                        }

                        txtWriter.printf("%-60s%s\n",instruction, objectCode);
                        TRecordLists.add(objectCode, fields);
                        instruction = intReader.readLine();
                        continue;
                    }

                    // X bit
                    int XBPE = 0;
                    if(operand.Xbit){
                        XBPE += 8;
                    }

// format 3 *********************************************
                    if(OpcodeConvertor.getFormat(fields[2]) == 3){

                        // format 3 with no operand
                        if(fields[3] == null){
                            objectCode = objectCode.concat("0000");

                            txtWriter.printf("%-60s%s\n",instruction, objectCode);
                            TRecordLists.add(objectCode, fields);
                            instruction = intReader.readLine();
                            continue;
                        }

                        if(!operand.relocability & fields[3].charAt(0) != '='){
                            objectCode = objectCode.concat(Utility.padAddress(XBPE, 1)).concat(Utility.padAddress(operand.value, 3));

                            txtWriter.printf("%-60s%s\n",instruction, objectCode);
                            TRecordLists.add(objectCode, fields);
                        }

                        else {
                            int targetAddress, operandValue;

                            if(fields[3].charAt(0) == '='){
                                operandValue = findLiteralAddress(literalTable, fields[3]);
                            } else {
                                operandValue = operand.value;
                            }

                            targetAddress = operandValue - getNextLineCounter(fields);

                            // We need to move forward to get to the destination
                            if(operandValue >= Integer.parseInt(fields[0], 16)){

                                // check P range(positive)
                                if (targetAddress >= 0 && targetAddress <= 2047) {
                                    XBPE += 2;
                                    objectCode = objectCode.concat(Utility.padAddress(XBPE, 1)).concat(Utility.padAddress(targetAddress, 3));

                                    txtWriter.printf("%-60s%s\n",instruction, objectCode);
                                    TRecordLists.add(objectCode, fields);
//                                txtWriter.println(instruction + " " + objectCode + " $Positive address within range of P");
                                    instruction = intReader.readLine();
                                    continue;
                                }

                                // check for B range
                                else {
                                    // use Base register
                                    if(useBase){
                                        // calculate target address using base register
                                        XBPE += 4;
                                        targetAddress = operandValue - baseAddress;

                                        // check for range
                                        if(targetAddress >= 0 && targetAddress <= 4095) { // 2^12 - 1 = 4096 - 1 = 4095
                                            objectCode = objectCode.concat(Utility.padAddress(XBPE, 1)).concat(Utility.padAddress(targetAddress, 3));
                                            txtWriter.printf("%-60s%s\n",instruction, objectCode);
                                            TRecordLists.add(objectCode, fields);
//                                        txtWriter.println(instruction + " " + objectCode + " $Using Base Relative addressing");  // printing objectcode
                                        }

                                        // out of range for Base register
                                        else {
                                            objectCode = "$Error: Base relative out of range.";
                                            txtWriter.printf("%-60s%s\n",instruction, objectCode);
//                                        txtWriter.println(instruction + " $Error : Positive address out of range of B : " + Utility.padAddress(targetAddress, 5)); // printing objectcode
                                        }
                                    }

                                    // Can not use Base register because it's not in use
                                    else {
                                        objectCode = "$Error: (+)PC relative out of range.";
                                        txtWriter.printf("%-60s%s\n",instruction, objectCode);
//                                    txtWriter.println(instruction + " $Error : Positive address out of range of P, try Base addressing. " + Utility.padAddress(targetAddress, 5)); // printing objectcode
                                    }
                                }
                            }

                            // When we need to move backward to get to the destination
                            else if(operandValue < Integer.parseInt(fields[0], 16)){

                                // Check P range (Negative)
                                if(targetAddress <= -1 && targetAddress >= -2048){
                                    XBPE += 2;
                                    objectCode = objectCode.concat(Utility.padAddress(XBPE, 1)).concat(Utility.padAddress(targetAddress, 3));

                                    txtWriter.printf("%-60s%s\n",instruction, objectCode);
                                    TRecordLists.add(objectCode, fields);
//                                txtWriter.println(instruction + " " + objectCode + " $Negative displacement within range of P");    // printing objectcode
                                }

                                // Out or negative P range
                                else {
                                    objectCode = "$Error: (-)PC relative out of range.";
                                    txtWriter.printf("%-60s%s\n",instruction, objectCode);
//                                txtWriter.println(instruction + " $Error : Negative displacement out of range of P :" + Utility.padAddress(targetAddress, 5));   // printing objectcode
                                }
                            }

                        }
                    }

// format 4 ***********************************
                    else if(OpcodeConvertor.getFormat(fields[2]) == 4) {
                        XBPE += 1;

                        int targetAddress;
                        if(fields[3].charAt(0) == '='){
                            targetAddress = findLiteralAddress(literalTable, fields[3]);
                        } else {
                            targetAddress = operand.value;
                        }

                        objectCode = objectCode.concat(Utility.padAddress(XBPE, 1)).concat(Utility.padAddress(targetAddress, 5));

                        txtWriter.printf("%-60s%s\n",instruction, objectCode);
                        TRecordLists.add(objectCode, fields);
                        MRecordLists.addAll(generateMRecord(fields, symbolTable));
                    }

                    // after processing format 3 and format 4 instruction, go to next line
                    instruction = intReader.readLine();
                    continue;
                }

// END directive has been reached   *****************
                else {
                    // END directive with operand
                    if(fields[3] != null) {
                        objectCode = objectCode.concat("E^").concat(Utility.padAddress(operand.value, 6));
                    } else {
                        objectCode = objectCode.concat("E^");
                    }

                    txtWriter.println(instruction);
                    TRecordLists.terminateTRecord();
                    ERecord = objectCode;
                    instruction = intReader.readLine();
                    continue;
                }
            }

            txtWriter.close();
// *****************************************************


            // Write the Object File
            objectWriter.println(HRecord);

            for(String d : DRecordLists)
                objectWriter.println(d);

            for(String r : RRecordLists)
                objectWriter.println(r);

            for(String t : TRecordLists.getAllTRecords())
                objectWriter.println(t);

            for(String m : MRecordLists)
                objectWriter.println(m);

            objectWriter.println(ERecord);

            // close the object file
            //objectWriter.close();
        }
        catch (Exception e){
            System.out.println("Unhandled error occurred during Pass 2.");
            System.exit(1);
        }

    }

    /** Generate The Modification Records from the Symbol Table and all instructions given
     */
    private static ArrayList<String> generateMRecord(String[] fields, SymbolTable symbolTable) {
        ArrayList<String> MRecordList = new ArrayList<>();

        int offset = 0;
        String nibbles;
        if(fields[2].equals("WORD") | fields[2].equals("BYTE")){
            nibbles = "06";
        }
        else {
            nibbles = "05";
            offset = 1;
        }

        // handle literal
        if(fields[3].charAt(0) == '='){
            String genMRec = "M^" + Utility.padAddress(Integer.parseInt(fields[0], 16)+offset, 6) + "^"+ nibbles + "^+" + Utility.padLabel(Pass1.controlSectionName);
            MRecordList.add(genMRec);
        }


        // always M record for external symbol
        for (Node symbol : symbolTable.getAllExternal()) {
            int index = fields[3].indexOf(symbol.getKey()); // find if the symbol exists in the operand

            if (index != -1) {
                char ch = getSignOfSymbol(fields[3], index); // check for sign
                String genMRec = "M^" + Utility.padAddress(Integer.parseInt(fields[0], 16) + offset, 6) + "^" + nibbles + "^" + ch + Utility.padLabel(symbol.getKey());
                MRecordList.add(genMRec);
            }
        }

        // if the operand is relocatable, M record for all relocatable symbols
        if(OperandUtility.operand.relocability) {
            for (Node symbol : symbolTable.getAll()) {
                int index = fields[3].indexOf(symbol.getKey()); // find if the symbol exists in the operand
                String controlSection = (symbol.getIflag() ? Pass1.controlSectionName : symbol.getKey()); // identify control section the symbol belong to

                if (index != -1 && symbol.rflag) {
                    char ch = getSignOfSymbol(fields[3], index); // check for sign
                    String genMRec = "M^" + Utility.padAddress(Integer.parseInt(fields[0], 16) + offset, 6) + "^" + nibbles + "^" + ch + Utility.padLabel(controlSection);
                    MRecordList.add(genMRec);
                    break;
                }
            }
        }

        return MRecordList;
    }

    /**
     * Returns array of strings containing fields of an intermediate instruction.
     * If any field doesn't exists, it's set to null.
     * @param instruction the intermediate instruction to be processed
     * @return an array of string of length 4
     */
    private static String[] getFields(String instruction) {

        String[] fields = new String[4]; // all array elements are initialized to 'null'

        StringTokenizer tokenizer = new StringTokenizer(instruction);

        // get line count
        fields[0] = tokenizer.nextToken();

        // get full length label if exists
        if(!(instruction.charAt(8) <= 32)) {
            fields[1] = tokenizer.nextToken();
        }

        // get opcode
        fields[2] = tokenizer.nextToken();

        // get operand if exists
        if(tokenizer.hasMoreTokens())
            fields[3] = tokenizer.nextToken();

        return fields;
    }

    /**
     * Given an operand, return the addressing mode.
     * return 1 for Intermediate, 2 for Indirect and 3 for Simple/Direct addressing.
     */
    public static int getAddressingMode(Operand operand){
        if(!operand.Nbit && operand.Ibit)
            return 1;
        else if(operand.Nbit && !operand.Ibit)
            return 2;
        else
            return 3;
    }

    /**
     * Check for Literal in literlTable that was generated in Pass1 and provide address and value
     */
    private static int findLiteralAddress(LinkedList<Literal> literalTable, String literalExpression){
        // remove the '=' character since none of the literal on literal table has that character
        literalExpression = literalExpression.substring(1);

        // hex values are changes to upper case
        if(literalExpression.charAt(0) == 'X') literalExpression = literalExpression.toUpperCase();

        for(Literal literal : literalTable){
            if(literal.name.equals(literalExpression))
                return literal.address;
        }

        return -1;
    }
    private static String findLiteralValue(LinkedList<Literal> literalTable, String literalExpression){
        for(Literal literal : literalTable){
            if(literal.name.equals(literalExpression))
                return literal.value;
        }

        return null;
    }

    /**
     * Calculate the value of the next Line Counter and Pass the instruction as an array of String
     * and provide the value of the next value in the location counter to fin the TA later on.
     */
    private static int getNextLineCounter(String[] fields){
        // current list counter
        int currentLineCounter = Integer.parseInt(fields[0], 16);

        // handle format 1/2/3/4 opcode
        int format = OpcodeConvertor.getFormat(fields[2]);
        if(format != 0){
            return currentLineCounter + format;
        }

        // handle BYTE, WORD, RESB, RESW
        else {
            if(fields[2].equals("BYTE")){
                String temp = fields[3].substring(fields[3].indexOf('\'')+1, fields[3].lastIndexOf('\''));
                if(fields[2].contains("C"))
                    return currentLineCounter + temp.length();
                else
                    return currentLineCounter + temp.length() / 2;
            }

            else if(fields[2].equals("WORD")){
                return currentLineCounter + 3;
            }

            else if(fields[2].equals("RESW")){
                return currentLineCounter + 3 * Integer.parseInt(fields[3]);
            }

            else if(fields[2].equals("RESB")){
                return currentLineCounter + Integer.parseInt(fields[3]);
            }
        }

        return currentLineCounter;
    }

    /**
     check Expression to determine relative or absolute Addressing
     */
    //^^^^^^Check this function again it always gives and error in case of relative addressing^^^^^^^^^^
    private static char getSignOfSymbol(String operand, int indexOfSymbol){
        try {
            if (operand.charAt(indexOfSymbol - 1) == '-') {
                return '-';
            }
        } catch(StringIndexOutOfBoundsException e) {
            return '+';
        }

        return '+';
    }


}