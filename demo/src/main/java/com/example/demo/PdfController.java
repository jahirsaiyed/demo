package com.example.demo;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    @PostMapping("/add-image")
    public ResponseEntity<String> addImageToPdf(
            @RequestParam("pdfFile") MultipartFile pdfFile,
            @RequestParam("imageFile") MultipartFile imageFile,
            @RequestParam("x") float x,
            @RequestParam("pageNo") int pageNo,
            @RequestParam("y") float y,
            @RequestParam("outputFileName") String outputFileName) {
        try {
            // Load the PDF document
            PDDocument document = PDDocument.load(pdfFile.getInputStream());

            // Assuming we add to the first page
            PDPage page = document.getPage(pageNo - 1);

            // Load the image
            PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, imageFile.getBytes(), imageFile.getOriginalFilename());

            // Add the image to the PDF at specified coordinates
            var contentStream = new org.apache.pdfbox.pdmodel.PDPageContentStream(document, page, org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode.APPEND, true, true);
            contentStream.drawImage(pdImage, x, y, pdImage.getWidth(), pdImage.getHeight());
            contentStream.close();

            // Save the modified PDF
            File outputFile = new File(outputFileName);
            document.save(outputFile);
            document.close();

            return ResponseEntity.ok("PDF updated successfully. Saved at: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating PDF: " + e.getMessage());
        }
    }
}
