package jdk.nio;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by fsdevops on 6/2/16.
 */
public class TestStream {
    public static void main(String[] args) {
        List<Integer> l = Arrays.asList(1, 1, 2, 2, 3, 3);
        List<Integer>ll=l.stream().collect(Collectors.toSet()).stream().collect(Collectors.toList());
        System.out.println(ll);
    }
}
