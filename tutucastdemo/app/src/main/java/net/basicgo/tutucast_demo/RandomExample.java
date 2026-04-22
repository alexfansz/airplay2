package net.basicgo.tutucast_demo;

import java.util.Random;

public class RandomExample {

    private final Random random = new Random();   // 建议作为成员变量，避免频繁创建

    // 获取 int 类型随机数（0 到 int 最大值）
    public int getRandomInt() {
        return random.nextInt();
    }

    // 获取指定范围的随机数：例如 0 ~ 99
    public int getRandomInt(int max) {
        return random.nextInt(max);           // 结果：0 <= x < max
    }

    // 获取指定范围的随机数：例如 10 ~ 50（包含10和50）
    public int getRandomInt(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

    // 获取 double 类型随机数（0.0 <= x < 1.0）
    public double getRandomDouble() {
        return random.nextDouble();
    }

    // 获取 boolean 随机值（true 或 false）
    public boolean getRandomBoolean() {
        return random.nextBoolean();
    }
}
