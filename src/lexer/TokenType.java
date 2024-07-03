
package lexer;

public enum TokenType {

    COMMA, DOT,
    LEFT_PAREN, RIGHT_PAREN,
    IDENTIFIER,

    // disjunction
    SEMICOLON,

    // :-
    IMPLIED_BY,
    // =
    EQUAL,

    // \=
    NOT_EQUAL,

    // \+
    NOT,

    EOF,

    // so separate facts and the query
    QUESTION_MARK,

    // for later
    NUMBER
}
