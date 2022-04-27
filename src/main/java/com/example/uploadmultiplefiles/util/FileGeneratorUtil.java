package com.example.uploadmultiplefiles.util;

import com.example.uploadmultiplefiles.model.OrderSlip;
import com.example.uploadmultiplefiles.model.ProductInfo;
import com.example.uploadmultiplefiles.service.FilesStorageService;
import com.example.uploadmultiplefiles.singleton.OrderIdPdf;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.springframework.beans.factory.annotation.Autowired;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class FileGeneratorUtil {

    private static final int MAX_TEXT_ALLOWED = 30;

    public static void combined(List<OrderSlip> orderList, String fileName) throws IOException, InvalidFormatException {
        String lastOrderId = "";
        XWPFDocument document = new XWPFDocument();
        int count = orderList.size();
        int nextBagNumber = 1;
        for(OrderSlip order : orderList){
            lastOrderId = order.getOrderId();

            //A6 page layout
            CTSectPr sectPr = document.getDocument().getBody().addNewSectPr();
            sectPr.addNewPgSz();
            CTPageSz pageSize = sectPr.getPgSz();
            pageSize.setW(BigInteger.valueOf(5960));
            pageSize.setH(BigInteger.valueOf(8400));

            //margin
            CTPageMar pageMar = sectPr.addNewPgMar();
            pageMar.setLeft(BigInteger.valueOf(100L));
            pageMar.setTop(BigInteger.valueOf(100L));
            pageMar.setRight(BigInteger.valueOf(100L));
            pageMar.setBottom(BigInteger.valueOf(100L));

            //waybill
            XWPFParagraph wayBillParagraph = document.createParagraph();
            wayBillParagraph.setSpacingAfter(0);
            wayBillParagraph.setSpacingBefore(0);

            XWPFRun wayBillImage = wayBillParagraph.createRun();
            wayBillParagraph.setAlignment(ParagraphAlignment.CENTER);
            Path root = Paths.get("uploads");
            String imgFile = root + "/" + nextBagNumber + fileName.replace(".pdf",".jpg");

            FileInputStream is = new FileInputStream(imgFile);
            wayBillImage.addPicture(is, XWPFDocument.PICTURE_TYPE_JPEG, imgFile, 2628000, 3686400);
            is.close();

            //header
            XWPFParagraph headerParagraph = document.createParagraph();
            headerParagraph.setSpacingAfter(0);
            headerParagraph.setSpacingBefore(0);

            XWPFRun bagNoLabel = headerParagraph.createRun();
            bagNoLabel.setFontFamily("Calibri");
            bagNoLabel.setFontSize(7);
            bagNoLabel.setText("NO.");
            XWPFRun bagNum = headerParagraph.createRun();
            bagNum.setFontFamily("Calibri");
            bagNum.setFontSize(7);
            bagNum.setBold(true);
            bagNum.setText(order.getOrderNo() + "");
            bagNum.setText("\t\t\t\t\t");

            XWPFRun orderIdLabel = headerParagraph.createRun();
            orderIdLabel.setFontFamily("Calibri");
            orderIdLabel.setFontSize(7);
            orderIdLabel.setText("ORDER ID:");
            XWPFRun orderId = headerParagraph.createRun();
            orderId.setFontFamily("Calibri");
            orderId.setFontSize(7);
            orderId.setBold(true);
            orderId.setText(order.getOrderId());

            //table
            XWPFTable table = document.createTable();
            table.setWidth("100%");
            table.removeBorders();
            //header
            XWPFTableRow headerRow = table.getRow(0);
            setTableFormat(headerRow.getCell(0), "SKU/NAME", true);
            setTableFormat(headerRow.addNewTableCell(), "VARIATION", true);
            setTableFormat(headerRow.addNewTableCell(), "U PRICE", true);
            setTableFormat(headerRow.addNewTableCell(), "QTY", true);
            for (ProductInfo prod : order.getItems()) {
                //rows
                XWPFTableRow newTableRow = table.createRow();
                setTableFormat(newTableRow.getCell(0), getMaxText(prod.getProductName()), false);
                setTableFormat(newTableRow.getCell(1), getMaxText(prod.getVariationName()), false);
                setTableFormat(newTableRow.getCell(2), "Php" + prod.getPrice(), false);
                setTableFormat(newTableRow.getCell(3), prod.getQuantity(), false);
            }

            String remarks = order.getRemarkFromBuyer();
            if (!remarks.trim().isEmpty()){
                XWPFParagraph buyerCommentParagraph = document.createParagraph();
                XWPFRun buyerCommentLabel = buyerCommentParagraph.createRun();
                buyerCommentLabel.setFontFamily("Calibri");
                buyerCommentLabel.setFontSize(7);
                buyerCommentLabel.setText("BUYER'S COMMENT:");
                buyerCommentLabel.setBold(true);

                XWPFRun buyerComment = buyerCommentParagraph.createRun();
                buyerComment.setFontFamily("Calibri");
                buyerComment.setFontSize(7);
                buyerComment.setItalic(true);
                buyerComment.setText(remarks);
                buyerComment.setBold(true);
            }

            String notes = order.getSellerNote();
            if (!notes.trim().isEmpty()){
                XWPFParagraph sellerNoteParagraph = document.createParagraph();
                XWPFRun sellerNoteLabel = sellerNoteParagraph.createRun();
                sellerNoteLabel.setFontFamily("Calibri");
                sellerNoteLabel.setFontSize(7);
                sellerNoteLabel.setText("SELLER'S NOTE:");
                sellerNoteLabel.setBold(true);

                XWPFRun sellerNote = sellerNoteParagraph.createRun();
                sellerNote.setFontFamily("Calibri");
                sellerNote.setFontSize(7);
                sellerNote.setItalic(true);
                sellerNote.setText(notes);
                sellerNote.setBold(true);
            }

            XWPFParagraph breakerParagraph = document.createParagraph();
            count--;
            nextBagNumber++;
            if (count != 0)
                breakerParagraph.setPageBreak(false);
        }

        saveAsWord(document, fileName);
    }

    private static void saveAsWord(XWPFDocument document, String fileName) {
        Path root = Paths.get("saved");
        try {
            if(!Files.exists(root))
                Files.createDirectory(root);

            FileOutputStream out = new FileOutputStream(new File(root + "/" + fileName.replace(".pdf", ".docx")));

            document.write(out);
            out.close();
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize folder for upload!");
        }
    }

    private static void setTableFormat(XWPFTableCell cell, String Text, boolean isHeader) {
        cell.removeParagraph(0);
        XWPFParagraph paragraph = cell.addParagraph();
        paragraph.setAlignment(ParagraphAlignment.LEFT);
        paragraph.setSpacingAfter(0);
        paragraph.setSpacingBefore(0);
        XWPFRun run = paragraph.createRun();
        run.setBold(true);
        run.setFontSize(7);
        if (isHeader) {
            run.setFontFamily("Arial Black");
            run.setFontSize(6);
        }
        run.setText(Text);
    }

    private static String getMaxText(String str) {
        if(str.length()>MAX_TEXT_ALLOWED){
            return str.substring(0,MAX_TEXT_ALLOWED-3) + "...";
        }
        return str;
    }
}
