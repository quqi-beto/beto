package com.example.uploadmultiplefiles.singleton;

import org.apache.pdfbox.pdmodel.PDDocument;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class OrderIdPdf {
    private Map<String, BufferedImage> map = new HashMap<String, BufferedImage>();
    private static OrderIdPdf ourInstance = new OrderIdPdf();

    public static OrderIdPdf getInstance() {
        return ourInstance;
    }

    private OrderIdPdf() {
    }

    public void put(String orderId, BufferedImage pdf) {
        map.put(orderId,pdf);
    }

    public BufferedImage get(String orderId){
        return map.get(orderId);
    }

    public void remove(String orderId){
        map.remove(orderId);
    }
}
