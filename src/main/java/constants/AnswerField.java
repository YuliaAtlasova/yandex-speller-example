package constants;

public enum AnswerField {
    CODE("code"),
    WORD("word"),
    SUGGEST("s");

    public String name;

    AnswerField(String name) {
        this.name = name;
    };
}
