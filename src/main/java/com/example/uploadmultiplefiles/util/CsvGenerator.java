package com.example.uploadmultiplefiles.util;

import com.example.uploadmultiplefiles.model.OrderSlip;
import com.example.uploadmultiplefiles.model.ProductInfo;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CsvGenerator {

    public static void generate(List<OrderSlip> orderList, String fileName){
        Path root = Paths.get("saved");
        String lastOrderId = "";
        String csvFileName = root + "/" + fileName.replace(".pdf", ".csv");
        try{
            FileWriter writer = new FileWriter(csvFileName);
            writer.write(insertCsvHeader());
            writer.write("\n");
            for(OrderSlip order: orderList){
                lastOrderId = order.getOrderId();
                for(ProductInfo prod:order.getItems()){
                    List<String> orderString = new ArrayList<String>();
                    orderString.add(order.getOrderNo()+"");
                    orderString.add(order.getTrackingNo());
                    orderString.add(order.getOrderId());
                    orderString.add(prod.getProductName().replace(',', ':'));
                    orderString.add(prod.getVariationName().replace(',', ':'));
                    orderString.add(prod.getPrice());
                    orderString.add(prod.getQuantity());
                    String collect = orderString.stream().collect(Collectors.joining(","));
                    writer.write(collect);
                    writer.write("\n");
                }
            }

            writer.close();
            System.out.println(csvFileName + " generated successully");
        } catch (IOException e) {
            System.out.println("Failing OrderId:" + lastOrderId);
            e.printStackTrace();
        }
    }

    private static String insertCsvHeader() {
        List<String> header = new ArrayList<String>();
        header.add("OrderNumber");
        header.add("OrderId");
        header.add("TrackingNo");
        header.add("ProductName");
        header.add("VariationName");
        header.add("Price");
        header.add("Quantity");
        return header.stream().collect(Collectors.joining(","));
    }
}
