package com.perago.techtest.test;

import java.io.Serializable;

/**
 * Created by paballo on 2017/08/21.
 */
public class TestClass implements Serializable {

    private static final long serialVersionUID = -4309657210316182163L;

    private String name;
    private int score;

    public TestClass(){}



    public void setName(String name) {
        this.name = name;
    }

    public void setScore(int score) {
        this.score = score;

    }

    public String getName() {

        return name;
    }

    public int getScore() {
        return score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestClass)) return false;

        TestClass testClass = (TestClass) o;

        if (getScore() != testClass.getScore()) return false;
        return getName().equals(testClass.getName());
    }

    @Override
    public int hashCode() {
        int result = getName().hashCode();
        result = 31 * result + getScore();
        return result;
    }
}
