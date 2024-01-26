package LinkingLoader;

import java.io.*;
import java.util.Scanner;


public class LinkingLoader {
    public static void main(String[] args) {
        Estab[] es = new Estab[20];
        String input, name;
        String symbol;
        int count = 0, progaddr, csaddr, add, len = 0;
        File fp1, fp2;

        Scanner scan = new Scanner(System.in);
        System.out.println("Enter The File to Link");
        String LinkingFile = scan.next();

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the location where the program has to be loaded : ");
        progaddr = scanner.nextInt();
        csaddr = progaddr;

        try {
            fp1 = new File(LinkingFile);
            fp2 = new File("Link.txt");
            FileWriter writer = new FileWriter(fp2);

            writer.write("CS_NAME\t  EXT_SYM_NAME  \t  ADDRESS  \t  LENGTH  \n");
            writer.write("--------------------------------------\n");

            scanner = new Scanner(fp1);
            input = scanner.next();

            while (!input.equals("END")) {
                if (input.equals("H")) {
                    name = scanner.next();
                    es[count] = new Estab();
                    es[count].csname = name;
                    es[count].extsym = "**";
                    add = scanner.nextInt();
                    es[count].address = add + csaddr;
                    len = scanner.nextInt();
                    es[count].length = len;
                    writer.write(es[count].csname + "\t" + es[count].extsym + "\t\t" + es[count].address + "\t" + es[count].length + "\n");
                    count++;
                } else if (input.equals("D")) {
                    input = scanner.next();
                    while (!input.equals("R")) {
                        es[count] = new Estab();
                        es[count].csname = "**";
                        es[count].extsym = input;
                        add = scanner.nextInt();
                        es[count].address = add + csaddr;
                        es[count].length = 0;
                        writer.write(es[count].csname + "\t" + es[count].extsym + "\t\t" + es[count].address + "\t" + es[count].length + "\n");
                        count++;
                        input = scanner.next();
                    }
                    csaddr = csaddr + len;
                } else if (input.equals("T")) {
                    while (!input.equals("E")) {
                        input = scanner.next();
                    }
                }
                input = scanner.next();
            }
            writer.write("---------------------");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (BufferedReader br = new BufferedReader(new FileReader("Link.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}