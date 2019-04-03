/*
    This class provides a recursive descent parser 
    for P11,
    creating a parse tree which can be interpreted
    to simulate execution of a P11 program
*/

import java.util.*;
import java.io.*;

public class Parser {

   private Lexer lex;

   public Parser( Lexer lexer ) {
      lex = lexer;
   }

   public Node parseProgram() {
      System.out.println("-----> parsing <program>:");
      Node classes = parseDefs();
      return new Node("program", defs, null, null);
   }


   public Node parseDefs() {
      Node def = paseDef();
      Token token = lex.getNextToken();
      if ( token.isKind("eof")) {
         return new Node("defs", def, null, null);
      }
      lex.putBackToken(token);
      Node defs = parseDefs();
      return new Node("defs", def, defs, null);
   }

   public Node parseDef() {
      Token token = lex.getNextToken();
      errorCheck(token, "lparen");
      
      token = lex.getNextToken();
      errorCheck(token, "word", "DEFINE");

      token = lex.getNextToken();
      errorCheck(token, "lparen");

      Token name = lex.getNextToken();
      errorCheck(token, "word");

      token = lex.getNextToken();
      if(!token.isKind("rparen")){
         Node params = parseParams();
      }
      else{
         Node params = null;
      }
      errorCheck(token, "rparen");

      Node expr = parseExpr();

      token = lex.getNextToken();
      errorCheck(token, "rparen");

      return new Node(name.getDetails(), params, expr, null);
   }

   public Node parseParams() {
      Token name = lex.getNextToken();
      //fin
   }











   public Node parseClasses() {
      System.out.println("-----> parsing <classes>:");

      Node clas = parseClass();

      // look ahead to see if there are more funcDef's
      Token token = lex.getNextToken();

      if ( token.isKind("eof") ) {
         return new Node( "classes", clas, null, null );
      }
      else {
         lex.putBackToken( token );
         Node second = parseClasses();
         return new Node( "classes", clas, classes, null );
      }
   }

   public Node parseClass() {
      System.out.println("-----> parsing <class>:");

      Token token = lex.getNextToken();
      errorCheck( token, "var", "class" );

      Token name = lex.getNextToken();  // the class name
      errorCheck( name, "class" );

      token = lex.getNextToken();
      errorCheck( token, "single", "{" );

      Node members = parseMembers();

      token = lex.getNextToken();
      errorCheck( token, "single", "}" );
      
      token = lex.getNextToken();


      return new Node("class", name.getDetails(), members, null, null );

      
   }
   
   
   private Node parseMembers() {
      System.out.println("-----> parsing <members>:");
 
      Node first = parseMember();

      if ( token.matches( "single", "}" ) ) {// no more params
         lex.putBackToken( token );  // handles the } for the end of the body
         return new Node( "members", first, null, null );
      }
      else { 
         Node second = parseMembers();
         return new Node( "members", first, second, null );
      }
   }




   private Node parseMember() {
      System.out.println("----> parsing <member>");

      token = lex.getNextToken();
      
      if(token.isKind( "class" )){
         Node first = parseConstructor();
         return new Node("member", first, null, null);
      }

      else if(token.matches("var", "static")){
         token1 = lex.getNextToken();
         token2 = lex.getNextToken();
         if(token2.matches("single", "(")){
            lex.putBackToken(token2);
            lex.putBackToken(token1);
            Node first = parseStaticMethod();
            return new Node("member", first, null, null);
         }
         else{
            lex.putBackToken(token2);
            lex.putBackToken(token1);
            Node first = parseStaticField();
            return new Node("member", first, null, null);
         }
      }

      else {
         token1 = lex.getNextToken();
         token2 = lex.getNextToken();
         if(token2.matches("single", "(")){
            lex.putBackToken(token2);
            lex.putBackToken(token1);
            lex.putBackToken(token);
            Node first = parseInstanceMethod();
            return new Node("member", first, null, null);
         }
         else{
            lex.putBackToken(token2);
            lex.putBackToken(token1);
            lex.putBackToken(token);
            Node first = parseInstanceField();
            return new Node("member", first, null, null);
         }
      
      }
   }


   private Node parseStaticField() {
      token = lex.getNextToken();
      
      ErrorCheck(token, "var");

      token1 = lex.getNextToken();
      
      if(token1.matches("single", "=")){
         Node first = parseStatement();
         return new Node("staticField", token.getDetails(), first, null, null);
      }
      
      else{
         lex.putBackToken(token1);
         return new Node("staticField", token.getDetails(), null, null, null);
      }
   }

   private Node parseStaticMethod() {
      token = lex.getNextToken();
      Node first = parseRestOfMethod();
      return new Node("staticMethod", token.getDetails(), first, null, null);
   }

   private Node parseInstanceField(){
      token = lex.getNextToken();
      ErrorCheck(token, "var");
      return new Node("instanceField", token.getDetails(), null, null, null);
   }

   private Node parseInstanceMethod(){
      token = lex.getNextToken();
      Node first = parseRestOfMethod();
      return new Node("instanceMethod", token.getDetails(), first, null, null);
   }



























   private Node parseStatements() {
      System.out.println("-----> parsing <statements>:");
 
      Node first = parseStatement();
 
      // look ahead to see if there are more statement's
      Token token = lex.getNextToken();
 
      if ( token.isKind("eof") ) {
         return new Node( "stmts", first, null, null );
      }
      else if ( token.isKind("end") || 
                token.isKind("else")
              ) {
         lex.putBackToken( token );
         return new Node( "stmts", first, null, null );
      }
      else {
         lex.putBackToken( token );
         Node second = parseStatements();
         return new Node( "stmts", first, second, null );
      }
   }// <statements>

   private Node parseFuncCall() {
      System.out.println("-----> parsing <funcCall>:");

      Token name = lex.getNextToken(); // function name
      errorCheck( name, "var" );

      Token token = lex.getNextToken();
      errorCheck( token, "single", "(" );

      token = lex.getNextToken();
      
      if ( token.matches( "single", ")" ) ) {// no args
         return new Node( "funcCall", name.getDetails(), null, null, null );
      }
      else {// have args
         lex.putBackToken( token );
         Node first = parseArgs();
         return new Node( "funcCall", name.getDetails(), first, null, null );
      }

   }// <funcCall>

   private Node parseArgs() {
      System.out.println("-----> parsing <args>:");

      Node first = parseExpr();

      Token token = lex.getNextToken();

      if ( token.matches( "single", ")" ) ) {// no more args
         return new Node( "args", first, null, null );
      }
      else if ( token.matches( "single", "," ) ) {// have more args
         Node second = parseArgs();
         return new Node( "args", first, second, null );
      }
      else {// error
         System.out.println("expected , or ) and saw " + token );
         System.exit(1);
         return null;
      }

   }// <args>

   private Node parseStatement() {
      System.out.println("-----> parsing <statement>:");
 
      Token token = lex.getNextToken();
 
      // --------------->>>  <str>
      if ( token.isKind("string") ) {
         return new Node( "str", token.getDetails(),
                          null, null, null );
      }
      // --------------->>>   <var> = <expr> or funcCall
      else if ( token.isKind("var") ) {
         String varName = token.getDetails();
         token = lex.getNextToken();
 
         if ( token.matches("single","=") ) {// assignment
            Node first = parseExpr();
            return new Node( "sto", varName, first, null, null );
         }
         else if ( token.matches("single","(")) {// funcCall
            lex.putBackToken( token );
            lex.putBackToken( new Token("var",varName) );
            Node first = parseFuncCall();
            return first;
         }
         else {
            System.out.println("<var> must be followed by = or (, "
                  + " not " + token );
            System.exit(1);
            return null;
         }
      }
      // --------------->>>   if ...
      else if ( token.isKind("if") ) {
         Node first = parseExpr();

         token = lex.getNextToken();
         
         if ( token.isKind( "else" ) ) {// no statements for true case
            token = lex.getNextToken();
            if ( token.isKind( "end" ) ) {// no statements for false case
               return new Node( "if", first, null, null );
            }
            else {// have statements for false case
               lex.putBackToken( token );
               Node third = parseStatements();
               token = lex.getNextToken();
               errorCheck( token, "end" );
               return new Node( "if", first, null, third );               
            }
         }
         else {// have statements for true case
            lex.putBackToken( token );
            Node second = parseStatements();

            token = lex.getNextToken();
            errorCheck( token, "else" );

            token = lex.getNextToken();
            
            if ( token.isKind( "end" ) ) {// no statements for false case
               return new Node( "if", first, second, null );
            }
            else {// have statements for false case
               lex.putBackToken( token );
               Node third = parseStatements();
               token = lex.getNextToken();
               errorCheck( token, "end" );
               return new Node( "if", first, second, third );
            }
         }

      }// if ... 

      else if ( token.isKind( "return" ) ) {
         Node first = parseExpr();
         return new Node( "return", first, null, null );
      }// return

      else {
         System.out.println("Token " + token + 
                             " can't begin a statement");
         System.exit(1);
         return null;
      }
 
   }// <statement>

   private Node parseExpr() {
      System.out.println("-----> parsing <expr>");

      Node first = parseTerm();

      // look ahead to see if there's an addop
      Token token = lex.getNextToken();
 
      if ( token.matches("single", "+") ||
           token.matches("single", "-") 
         ) {
         Node second = parseExpr();
         return new Node( token.getDetails(), first, second, null );
      }
      else {// is just one term
         lex.putBackToken( token );
         return first;
      }

   }// <expr>

   private Node parseTerm() {
      System.out.println("-----> parsing <term>");

      Node first = parseFactor();

      // look ahead to see if there's a multop
      Token token = lex.getNextToken();
 
      if ( token.matches("single", "*") ||
           token.matches("single", "/") 
         ) {
         Node second = parseTerm();
         return new Node( token.getDetails(), first, second, null );
      }
      else {// is just one factor
         lex.putBackToken( token );
         return first;
      }
      
   }// <term>

   private Node parseFactor() {
      System.out.println("-----> parsing <factor>");

      Token token = lex.getNextToken();

      if ( token.isKind("num") ) {
         return new Node("num", token.getDetails(), null, null, null );
      }
      else if ( token.isKind("var") ) {
         // could be simply a variable or could be a function call
         String name = token.getDetails();

         token = lex.getNextToken();

         if ( token.matches( "single", "(" ) ) {// is a funcCall
            lex.putBackToken( new Token( "single", "(") );  // put back the (
            lex.putBackToken( new Token( "var", name ) );  // put back name 
            Node first = parseFuncCall();
            return first;
         }
         else {// is just a <var>
            lex.putBackToken( token );  // put back the non-( token
            return new Node("var", name, null, null, null );
         }
      }
      else if ( token.matches("single","(") ) {
         Node first = parseExpr();
         token = lex.getNextToken();
         errorCheck( token, "single", ")" );
         return first;
      }
      else if ( token.matches("single","-") ) {
         Node first = parseFactor();
         return new Node("opp", first, null, null );
      }
      else {
         System.out.println("Can't have a factor starting with " + token );
         System.exit(1);
         return null;
      }
      
   }// <factor>

  // check whether token is correct kind
  private void errorCheck( Token token, String kind ) {
    if( ! token.isKind( kind ) ) {
      System.out.println("Error:  expected " + token + 
                         " to be of kind " + kind );
      System.exit(1);
    }
  }

  // check whether token is correct kind and details
  private void errorCheck( Token token, String kind, String details ) {
    if( ! token.isKind( kind ) || 
        ! token.getDetails().equals( details ) ) {
      System.out.println("Error:  expected " + token + 
                          " to be kind= " + kind + 
                          " and details= " + details );
      System.exit(1);
    }
  }

}