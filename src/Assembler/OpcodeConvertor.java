package Assembler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

/** This Class creates methods to read opcode from instructions file and convert it to it's opcode
 * Only if opcode is found ofc (will be used throughout the whole program)
 * */

public class OpcodeConvertor {

    //*********First Method to Get Format**********//
    public static int getFormat(String opcode){
        //Checking Format 4 (Doesn't always work if file is not supported)
        boolean format4 = false;
        if(opcode.contains("+")) {
            format4 = true;
            opcode = opcode.substring(1);
        }

        try(BufferedReader inputReader = new BufferedReader(new FileReader("C:\\Users\\Asus\\OneDrive - Arab Academy for Science and Technology\\TERM 5\\System Programming\\SIC-XE Assembler\\src\\instructions.txt"))){
            //instructions.txt will not work unless it is read from absolute path (no idea why)
            //Tokens used to parse the file as in column by column similar to the idea of PDA and CFG in Theory
            String instruction = inputReader.readLine();
            StringTokenizer tokenizer;
            while(instruction != null){
                tokenizer = new StringTokenizer(instruction, " ");
                if(tokenizer.hasMoreTokens()){
                    if(tokenizer.nextToken().equals(opcode)){
                        // if opcode found fetch the second column or return 4 if format4 is true
                        if(format4)
                            return 4;
                        else
                            return Integer.parseInt(tokenizer.nextToken());
                    }
                }
                instruction = inputReader.readLine();
            }
        } catch (IOException e){
            System.out.println("Problem opening instructions file.");
        }


        return -1;
    }

    //*********Second Method to Get the HEXADECIMAL CODE (iff the opcode is found in instructions.txt)**********//

    public static String getHexCode(String opcode){
        if(opcode.contains("+"))
            opcode = opcode.substring(1);

        try(BufferedReader inputReader = new BufferedReader(new FileReader("C:\\Users\\Asus\\OneDrive - Arab Academy for Science and Technology\\TERM 5\\System Programming\\SIC-XE Assembler\\src\\instructions.txt"))){
            String instruction = inputReader.readLine();
            StringTokenizer tokenizer;
            while(instruction != null){
                tokenizer = new StringTokenizer(instruction, " ");
                if(tokenizer.hasMoreTokens()){
                    if(tokenizer.nextToken().equals(opcode)){
                        // if opcode found fetch the third column
                        tokenizer.nextToken();
                        return tokenizer.nextToken();
                    }
                }
                instruction = inputReader.readLine();
            }
        } catch (IOException e){
            System.out.println("Problem opening instructions file.");
        }

        return null;
    }
}