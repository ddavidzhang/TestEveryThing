package jdk;

import java.util.TreeMap;

/**
 * Created by junwei on 12/04/16.
 */
public class TestTreeMap {
    public static void main(String[] args) {
        //red-black tree
        TreeMap<Integer, String> map = new TreeMap<>();
        map.put(3, "1");
        map.put(1, "1");
        map.put(2, "1");
        map.forEach((k, v) -> System.out.println(k));
    }
}
