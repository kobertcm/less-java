package com.github.lessjava.visitor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.github.lessjava.ast.ASTBinaryExpr;
import com.github.lessjava.ast.ASTBlock;
import com.github.lessjava.ast.ASTExpression;
import com.github.lessjava.ast.ASTFunction;
import com.github.lessjava.ast.ASTFunctionCall;
import com.github.lessjava.ast.ASTNode.DataType;
import com.github.lessjava.ast.ASTProgram;
import com.github.lessjava.ast.ASTUnaryExpr;
import com.github.lessjava.ast.ASTVariable;
import com.github.lessjava.types.Symbol;
import com.github.lessjava.types.SymbolTable;
import com.github.lessjava.visitor.impl.LJBaseASTVisitor;

public abstract class LJAbstractAssignTypes extends LJBaseASTVisitor implements LJAssignTypes
{
    protected Map<String, ASTFunction> nameFunctionMap = new HashMap<>();
    protected Deque<SymbolTable>       scopes          = new ArrayDeque<>();

    @Override
    public void preVisit(ASTProgram node)
    {
        for (ASTFunction function : node.functions) {
            nameFunctionMap.put(function.name, function);
        }

        scopes.push((SymbolTable) node.attributes.get("symbolTable"));
    }

    @Override
    public void postVisit(ASTProgram node)
    {
        scopes.pop();
    }
    
    @Override
    public void preVisit(ASTFunction node) {
        scopes.push((SymbolTable) node.attributes.get("symbolTable"));
    }

    @Override
    public void postVisit(ASTFunction node) {
        scopes.pop();
    }

    @Override
    public void preVisit(ASTBlock node)
    {
        scopes.push((SymbolTable) node.attributes.get("symbolTable"));
    }

    @Override
    public void postVisit(ASTBlock node)
    {
        scopes.pop();
    }

    public DataType evalExprType(ASTExpression expr)
    {
        DataType type;

        if (typeIsKnown(expr.type)) {
            type = expr.type;
        } else if (expr instanceof ASTBinaryExpr) {
            type = evalExprType((ASTBinaryExpr) expr);
        } else if (expr instanceof ASTUnaryExpr) {
            type = evalExprType((ASTUnaryExpr) expr);
        } else if (expr instanceof ASTFunctionCall) {
            type = evalExprType((ASTFunctionCall) expr);
        } else if (expr instanceof ASTVariable) {
            type = evalExprType((ASTVariable) expr);
        } else {
            type = expr.type;
        }

        return type;
    }

    public DataType evalExprType(ASTBinaryExpr expr)
    {
        return evalExprType(expr.leftChild);
    }

    public DataType evalExprType(ASTUnaryExpr expr)
    {
        return evalExprType(expr.child);
    }

    public DataType evalExprType(ASTFunctionCall expr)
    {
        return nameFunctionMap.get(expr.name).returnType;
    }

    public DataType evalExprType(ASTVariable expr)
    {
        try {
            
            Iterator<SymbolTable> scopeIterator = scopes.iterator();
            
            // Iterate over the current active scopes for the symbol
            while (scopeIterator.hasNext()) {
                SymbolTable scope = scopeIterator.next();

                Symbol symbol = scope.lookup(expr.name);
                if (symbol != null && typeIsKnown(symbol.type)) {
                    return symbol.type;
                }
            }

        } catch (Exception e) {
        }

        return DataType.UNKNOWN;
    }

    public boolean typeIsKnown(DataType type)
    {
        return type != null && !type.equals(DataType.UNKNOWN);
    }

}