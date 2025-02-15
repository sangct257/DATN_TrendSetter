package com.example.datn_trendsetter.Service;

import com.example.datn_trendsetter.Entity.HoaDon;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.*;

@Service
public class PdfService {
    private final TemplateEngine templateEngine;

    public PdfService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public File generateInvoicePdf(HoaDon hoaDon) throws Exception {
        // Chỉ tạo file PDF nếu là hóa đơn "Tại Quầy"
        if (!"Tại Quầy".equals(hoaDon.getLoaiHoaDon())) {
            return null;
        }

        // Tạo nội dung HTML từ template Thymeleaf
        Context context = new Context();
        context.setVariable("hoaDon", hoaDon);
        String htmlContent = templateEngine.process("invoice-template", context);

        // Đường dẫn lưu file PDF
        String pdfPath = "invoices/hoa_don_" + hoaDon.getId() + ".pdf";
        File file = new File(pdfPath);
        file.getParentFile().mkdirs();

        // Chuyển đổi HTML thành PDF
        try (OutputStream outputStream = new FileOutputStream(file)) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            renderer.createPDF(outputStream);
        }

        return file;
    }
}
