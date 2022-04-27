package com.example.uploadmultiplefiles.singleton;

import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

public class UserOrdersFile {
    private static Map<String, byte[]> map = new HashMap<String, byte[]>();
    private static UserOrdersFile ourInstance = new UserOrdersFile();

    public static UserOrdersFile getInstance() {
        return ourInstance;
    }

    private UserOrdersFile() {
    }

    public void put(String userId, byte[] orderSlip) {
        map.put(userId,orderSlip);
    }

    public byte[] get(String userId){
        return map.get(userId);
    }

    public void remove(String userId){
        map.remove(userId);
    }
}
