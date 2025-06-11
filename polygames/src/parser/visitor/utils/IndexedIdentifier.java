// File: IndexedIdentifier.java
package parser.visitor.utils;

import parser.ast.ASTElement;

public record IndexedIdentifier(ASTElement identifier, int index) {}
