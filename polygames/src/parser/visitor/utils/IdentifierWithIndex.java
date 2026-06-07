// File: IndexedIdentifier.java
package parser.visitor.utils;

import parser.ast.ASTElement;

public record IdentifierWithIndex(ASTElement identifier, int index) {}
