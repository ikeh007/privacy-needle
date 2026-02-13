package com.jaims.privacyneedle.models;

public class QuizQuestion {

    public String question;
    public String[] options;
    public int correctIndex;

    public QuizQuestion(String question, String[] options, int correctIndex) {
        this.question = question;
        this.options = options;
        this.correctIndex = correctIndex;
    }
}
