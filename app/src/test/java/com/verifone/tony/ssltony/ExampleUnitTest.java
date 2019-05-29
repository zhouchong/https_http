package com.verifone.tony.ssltony;

import android.support.v4.util.Pair;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);

        Pair<Integer, byte[]> pair;
        byte[] retData = null;

        pair = getValue1();

        int ret = pair.first;
        if (pair.second != null)
            retData = pair.second;

        System.out.println("1: " + ret + "\n");
        if (retData != null)
            System.out.println("2: " + new String(retData) + "\n");

    }

    private Pair<Integer, byte[]> getValue1() {
        return Pair.create(9, "Hello World".getBytes());
    }
}