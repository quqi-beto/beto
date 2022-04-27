package com.example.uploadmultiplefiles.util;

import com.example.uploadmultiplefiles.model.OrderSlip;
import com.example.uploadmultiplefiles.model.ProductInfo;
import com.example.uploadmultiplefiles.service.FileService;
import com.example.uploadmultiplefiles.singleton.OrderIdPdf;
import com.example.uploadmultiplefiles.singleton.UserOrdersFile;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AppUtil {
    @Autowired
    static FileService fileService;

    private static List<ProductInfo> toItems(Cell cell) {
        List items = new ArrayList();
        String rawProd = (cell + "");
        String[] products = rawProd.split("\n");

        for (String item : products) {
            ProductInfo prod = new ProductInfo();
            String[] itemDetails = item.split(";");
            prod.setProductName(itemDetails[0].replace("Product Name:", "").trim());
            prod.setVariationName(itemDetails[1].replace("Variation Name:", "").trim());
            String price = itemDetails[2].replace("Price:", "").trim();
            prod.setPrice(price.replaceAll("[^a-zA-Z0-9]", ""));
            prod.setQuantity(itemDetails[3].replace("Quantity:", "").trim());
            items.add(prod);
        }
        return items;
    }

    private static String blankIfNull(Cell cell) {
        if (cell != null)
            return cell + "";
        return "";
    }

    public static List<PDDocument> splitPdf(MultipartFile file) throws IOException {
        long startPdf = System.nanoTime();
        Splitter splitter = new Splitter();

        //pdf to images
        pdfToImages(file,1);

        List<PDDocument> pdfList = splitter.split(PDDocument.load(file.getInputStream()));
        long stopPdf = System.nanoTime();
        System.out.println("Pdf time:" + (stopPdf - startPdf));
        return pdfList;
    }

    private static void pdfToImages(MultipartFile file, int nextBagNumber) {
        Path root = Paths.get("uploads");
        PDDocument document = null;
        int startPage = 1;
        int endPage = 2147483647;
        boolean subsampling = false;
        ImageType imageType = ImageType.RGB;
        int dpi = 96;
        float quality = 1.0F;
        String imageFormat = "jpg";
        try {
            document = PDDocument.load((file.getInputStream()));

            boolean success = true;

            endPage = Math.min(endPage, document.getNumberOfPages());
            PDFRenderer renderer = new PDFRenderer(document);
            renderer.setSubsamplingAllowed(subsampling);
            for (int i = startPage - 1; i < endPage; i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, dpi, imageType);
                String fileName = new StringBuilder().append(root).append("/").append(i+1).append(file.getOriginalFilename().replace("pdf",imageFormat)).toString();
                success &= ImageIOUtil.writeImage(image, fileName, dpi, quality);
            }

            if (!success) {
                System.err.println(new StringBuilder().append("Error: no writer found for image format '").append(imageFormat).append("'").toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (document != null) {
                try {
                    document.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static List<OrderSlip> getOrderList(InputStream file, int nextBagNumber) throws IOException {
        long start = System.nanoTime();
        Workbook workbook = WorkbookFactory.create(file);
        Sheet sheet = workbook.getSheetAt(0);

        Iterator rowIterator = sheet.iterator();
        List<OrderSlip> orderList = new ArrayList();
        rowIterator.next();
        int orderNo = nextBagNumber;

        while (rowIterator.hasNext()) {
            Row row = (Row)rowIterator.next();
            OrderSlip order = new OrderSlip();
            order.setOrderNo(orderNo);
            if(blankIfNull(row.getCell(0)).equals(""))
                break;
            order.setTrackingNo(blankIfNull(row.getCell(0)));
            order.setOrderId(blankIfNull(row.getCell(1)));
            order.setItems(toItems(row.getCell(2)));
            order.setRemarkFromBuyer(blankIfNull(row.getCell(3)));
            order.setSellerNote(blankIfNull(row.getCell(4)));
            orderList.add(order);
            orderNo++;
        }
        long stop = System.nanoTime();
        System.out.println("converting file to OrderSlip:" + (stop-start));
        return orderList;
    }

    public static String getTextFromPdf(PDDocument document) throws IOException {
        PDFTextStripper pdfStripper = new PDFTextStripper();
        String text = pdfStripper.getText(document);
        return text;
    }

    public static boolean validate(List<OrderSlip> orderList, List<PDDocument> pdDocumentList) throws IOException {
        boolean result = false;
        long startTime = System.nanoTime();
        if(orderList.size()==pdDocumentList.size()) {
            int matchedCount = 0;
            for (PDDocument pdf : pdDocumentList) {
                String pdfText = getTextFromPdf(pdf);
                for (OrderSlip order : orderList) {
                    if (pdfText.contains(order.getOrderId())) {
//                        PDFRenderer pdfRenderer = new PDFRenderer(pdf);
//                        BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 300, ImageType.RGB);
//                        Path root = Paths.get("uploads");
//                        String fileName = new StringBuilder().append(root).append("/").append(order.getOrderId()).append(".jpg").toString();
//                        ImageIOUtil.writeImage(bim, fileName, 300, 1.0F);
//                        OrderIdPdf.getInstance().put(order.getOrderId(),bim);
                        matchedCount++;
                        break;
                    }
                }
            }
            if(matchedCount==orderList.size()){
                result = true;
            }
        }
        long stopTime = System.nanoTime();
        System.out.println("Validation time: " + (stopTime-startTime));
        return result;
    }
}
