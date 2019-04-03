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

      return new Node("def", name.getDetails(), params, expr);
   }

   public Node parseParams() {
      Token name = lex.getNextToken();
      errorCheck(token, "word");
      
      Token token = lex.getNextToken();
      if(token.isKind("word")) {
         lex.putBackToken(token);
         Node params = parseParams();
         return new Node("params", name.getDetails(), params, null);
      }

      lex.putBackToken(token);
      return new Node("params", name.getDetails(), null, null);
   }

   public Node parseExpr() {
      Token token = lex.getNextToken();

      if(token.isKind("num")) {
         return new Node("number", token.getDetails(), null, null);
      }
      else {
         lex.putBackToken(token);
         Node lst = parseList();
         return new Node("expr", lst, null, null);
      }
   }

   public Node parseList() {
      Node items = null;
      Token token = lex.getNextToken();
      errorCheck(token, "lparen");
      token = lex.getNextToken();
      if(!token.isKind("rparen")) {
         lex.putBackToken(token);
         items = parseItems();
         token = lex.getNextToken();
      }
      errorCheck(token, "rparen");
      return new Node("list", items, null, null);
   }

   public Node parseItems() {
      Node expr = parseExpr();
      Token token = lex.getNextToken();
      if(token.isKind("rparen")) {
         lex.putBackToken(token);
         return new Node("expr", expr, null, null);
      }
      Node items = parseItems();
      return new Node("items", expr, items, null);
   }

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