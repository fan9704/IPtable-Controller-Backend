package com.fkt.network.services;

import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class IPValidateService {
    private static final Pattern IPv4_PATTERN = Pattern.compile(
            "^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$");

    public boolean isValidIPv4(String ipAddress) {
        return IPv4_PATTERN.matcher(ipAddress).matches();
    }
}
