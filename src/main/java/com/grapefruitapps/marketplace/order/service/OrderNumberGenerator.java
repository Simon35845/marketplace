package com.grapefruitapps.marketplace.order.service;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class OrderNumberGenerator {
    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ0123456789";
    private static final int LENGTH = 8;
    private final Random random = new Random();

    public String generate(){
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}
