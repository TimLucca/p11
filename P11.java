import java.util.Scanner;

public class P11 {

    public static void main(String[] args) throws Exception {

       String name;

       if ( args.length == 1 ) {
          name = args[0];
       }
       else {
          System.out.print("Enter name of P11 program file: ");
          Scanner keys = new Scanner( System.in );
          name = keys.nextLine();
       }

       Lexer lex = new Lexer( name );
       Parser parser = new Parser( lex );

       // start with <statements>
       Node root = parser.parseProgram();

       // display parse tree for debugging/testing:
       TreeViewer viewer = new TreeViewer("Parse Tree", 0, 0, 1700, 900, root );

       // execute the parse tree
       System.out.println("Type in an Expression: ");
       Scanner expString = new Scanner(System.in);
       File expFile =  new File(expString);
       BufferedWriter out = new BufferedReader(new FileWriter('expFile.txt'));
       out.write(expString);
       out.close();
       lex = new Lexer(expFile);
       Parser expParse = new Parser(lex);
       Node expresRoot = expParse.parseExpr();
       expresRoot.execute();

    }// main

 }