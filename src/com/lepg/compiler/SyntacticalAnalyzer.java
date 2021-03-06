package com.lepg.compiler;

import static java.lang.Math.pow;
import java.util.ArrayList;

/**
 * @author Ptthappy
 */


public class SyntacticalAnalyzer {
    protected int inLength;
    protected String opFound = "";
    protected boolean error = false;
    protected int par;
    
    public String analyze(String input) {
        error = false;
        this.par = Compiler.par;
        String[] in = input.trim().split(" ");
        inLength = in.length;
        ArrayList<String> var = new ArrayList<>();
        String varName = "";
        Integer result = null;
        
        switch(Compiler.statementType) {
            case 0:
                varName = in[1];
                varName = Compiler.table.get(varName);
                if (!search(varName).equals("null")) { //Revisa que la variable no haya sido creada
                    System.out.println("Variable is already defined");
                    return "";
                }
                
                var.add(Compiler.table.get(in[0]));
                var.add(Compiler.table.get(in[1]));
                
                input = input.substring(input.indexOf('=') + 1).trim();
                input = input.substring(0, input.length() - 1).trim();
                result = calculate(input);
                if (result == null)
                    return "";
                
                var.add(result.toString());
                Compiler.variables.add(var);
                System.out.println(result);
                return result.toString();
                
            case 1:
                varName = in[1];
                varName = Compiler.table.get(varName);
                if (!search(varName).equals("null"))  //Revisa que la variable no haya sido creada
                    return "";
                
                var.add(Compiler.table.get(in[0]));
                var.add(Compiler.table.get(in[1]));
                var.add("");
                Compiler.variables.add(var);
                System.out.println("Variable added and not initializated");
                return in[1];
                
            case 2:
                varName = in[0];
                varName = Compiler.table.get(varName);
                if (search(varName).equals("null"))  { //Revisa que la variable haya sido creada 
                    return "";
                }
                
                ArrayList<String> x = Compiler.variables.get(getVarIndex(varName));
                input = input.substring(input.indexOf('=') + 1).trim();
                input = input.substring(0, input.length() - 1).trim();
                result = calculate(input);
                if (result == null)
                    return "";
                
                x.set(2, result.toString());
                Compiler.variables.remove(getVarIndex(varName));
                Compiler.variables.add(x);
                System.out.println(result);
                return result.toString();
                
            case 3:
                result = calculate(input);
                if (result == null)
                    return "";
                System.out.println(result);
                return result.toString();
                
            default:
                throw new RuntimeException();
        }
    }
    
    private int getVarIndex(String varName) {
        for (int i = 0; i < Compiler.variables.size(); i++) {
            ArrayList<String> x = Compiler.variables.get(i);
            
            if(varName.equals(x.get(1)))
                return i;
        }
        return -1;
    }
    
    private Integer calculate(String in) {
        boolean isLexem = true;
        Integer actualResult = 0;
        int next = 0;
        boolean par = false;
        String lastOperator = "";
        boolean isNeg = false;
        
        if (this.par > 0) {
            par = true;
            this.par--;
            String in2 = in.substring(in.indexOf(Compiler.Symbol.get(3)) + 1, in.lastIndexOf(Compiler.Symbol.get(4))).trim();
            String in3 = in.substring(in.lastIndexOf(Compiler.Symbol.get(4)) + 1);
            in = in.substring(0, in.indexOf(Compiler.Symbol.get(3)));
            in += calculate(in2).toString() + in3;
        }
        
        String[] lexems = in.split(" ");
        if (lexems[0].equals(Compiler.Operator.get(1))) {
            String[] y = new String[lexems.length - 1];
            isNeg = true;
            for (int i = 1; i < lexems.length; i++)
                y[i - 1] = lexems[i];
            lexems = y;
        }
            
        int x = 0;
        while((x = check(lexems)) != -1) {
            for (int i = 0; i < lexems.length; i++) {
                System.out.print(lexems[i]);
            }
            System.out.println();
            lexems = compress(lexems, x, opFound);
        }
        
        if (lexems.length == 1) {
            lexems[0] = process(lexems[0]);
            if (lexems[0].equals(""))
                return null;
            else {
                if (isNeg)
                    return Integer.parseInt(lexems[0]) * -1;
                return Integer.parseInt(lexems[0]);
            }
                
        } else {
            
            for (int i = 0; i < lexems.length; i++) {  //lastOperator
                if (isLexem) {
                    lexems[i] = process(lexems[i]);
                    if (lexems[i].equals(""))
                        return null;
                    
                    if(lastOperator.equals("")) {
                        actualResult += Integer.parseInt(lexems[i]);
                    }
                    else {
                        actualResult = solve(actualResult, Integer.parseInt(lexems[i]), lastOperator);
                    }
                    
                    isLexem = false;
                } else {
                    lastOperator = lexems[i];
                    isLexem = true;
                }
            }
        }
        if (isNeg)
            actualResult *= -1;
        
        return actualResult;
    }
    
    
    private String[] compress(String[] shit, int index, String op) {
        int length = shit.length;
        String[] output = new String[length - 2];
        shit[index - 1] = process(shit[index - 1]);
        shit[index + 1] = process(shit[index + 1]);
        
        if (shit[index - 1].equals("") || shit[index + 1].equals(""))
            return null;
        
        solve(Integer.parseInt(shit[index - 1]), Integer.parseInt(shit[index + 1]), op);
        
        if (index >= 3) {
            for (int i = 0; i < index - 1; i++) {
                output[i] = shit[i];
            }
            
            output[index - 1] = solve(Integer.parseInt(shit[index - 1]), 
                Integer.parseInt(shit[index + 1]), op).toString();
        } else {
            Integer i = solve(Integer.parseInt(shit[index - 1]), 
                Integer.parseInt(shit[index + 1]), op);
            output[0] = i + "";
        }
        
        int i = index + 2;
        while(i < length) {
            output[i - 2] = shit[i];
            i++;
        }
        return output;
    }
    
    private String process(String input) {
        String a = input.charAt(0) + "";
        if (!Compiler.Number.contains(a))
            input = Compiler.table.get(input);
        
        if (input == null)
            return "";
        
        if (isVar(input)) {         //Si es variable
            input = search(input);  //Busca la variable
            if (input.equals("null"))   //Si no la encuentra
                return "";
            else {                  //Si la encuentra
                return input;
            }
        } else {                    //Si no es variable
            return input;
        }
    }
    
    private int check(String[] in) {
        int index = 0;
        for (String x : in) {
            if (x.equals("**")) {
                opFound = x;
                return index;
            }
            index++;
        }
        
        index = 0;
        for (String x : in) {
            if (x.equals("*") || x.equals("/") || x.equals("%")) {
                opFound = x;
                return index;
            }
            index++;
        }
        
        return -1;
    }
    
    private String search(String varName) {
        for (int i = 0; i < Compiler.variables.size(); i++) {
            ArrayList<String> x = Compiler.variables.get(i);
            
            if(varName.equals(x.get(1)))
                return x.get(2);
        }
        return "null";
    }
    
    protected boolean isVar(String shit) {
        Character c = shit.charAt(0);
        if(Compiler.Letter.contains(c.toString()))
            return true;
        return false;
    }
    
    private Integer solve(int value1, int value2, String op) {
        switch(op) {
            case "+":
                return value1 + value2;
                
            case "-":
                return value1 - value2;
                
            case "*":
                return value1 * value2;
                
            case "/":
                return value1 / value2;
                
            case "**":
                return (int)pow(value1, value2);
                
            case "%":
                return value1 % value2;
                
            default:
                return null;
                
        }
    }
    
    private boolean checkPrivateWord(String[] words) {
        for (String word : words) {
            if(Compiler.PrivateWord.contains(word))
                return true;
        }
        
        return false;
    }
    
}
