package com.example.demo;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    @PostMapping("/add-image")
    public ResponseEntity<ByteArrayResource> addImageToPdf(
            @RequestParam("pdfFile") MultipartFile pdfFile,
            @RequestParam("imageFile") MultipartFile imageFile,
            @RequestParam("x") float x,
            @RequestParam("y") float y,
            @RequestParam("pageNumber") int pageNumber) {
        try {
            // Load the PDF document
            PDDocument document = PDDocument.load(pdfFile.getInputStream());

            // Validate the page number
            if (pageNumber < 1 || pageNumber > document.getNumberOfPages()) {
                return ResponseEntity.badRequest().body(null);
            }

            // Get the specified page (1-based index, so subtract 1 for 0-based index)
            PDPage page = document.getPage(pageNumber - 1);

            // Load the image
            PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, imageFile.getBytes(), imageFile.getOriginalFilename());

            // Add the image to the specified page at the given coordinates
            var contentStream = new org.apache.pdfbox.pdmodel.PDPageContentStream(document, page, org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode.APPEND, true, true);
            contentStream.drawImage(pdImage, x, y, pdImage.getWidth(), pdImage.getHeight());
            contentStream.close();

            // Write the updated PDF to a ByteArrayOutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            document.close();

            // Prepare the output as a ByteArrayResource
            ByteArrayResource resource = new ByteArrayResource(outputStream.toByteArray());

            // Return the updated PDF in the response
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=modified.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(resource.contentLength())
                    .body(resource);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
}
