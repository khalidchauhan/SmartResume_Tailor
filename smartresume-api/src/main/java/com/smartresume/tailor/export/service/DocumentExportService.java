package com.smartresume.tailor.export.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.ListItem;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.smartresume.tailor.domain.entity.TailoredResume;
import com.smartresume.tailor.domain.model.ParsedResumeDto;
import com.smartresume.tailor.domain.model.TailoredResumeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentExportService {

    private final ObjectMapper objectMapper;

    public byte[] exportToPdf(TailoredResume tailoredResume) {
        try {
            TailoredResumeDto dto = objectMapper.readValue(tailoredResume.getTailoredJson(), TailoredResumeDto.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            Document document = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter.getInstance(document, out);
            document.open();

            // Font definitions
            Font nameFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font headlineFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Font.ITALIC);
            Font sectionTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            // 1. Header
            Paragraph name = new Paragraph(dto.getCandidateName(), nameFont);
            name.setAlignment(Element.ALIGN_LEFT);
            document.add(name);

            Paragraph contact = new Paragraph(String.format("%s | %s | %s", dto.getEmail(), dto.getPhone(), dto.getLocation()), bodyFont);
            document.add(contact);

            Paragraph headline = new Paragraph(dto.getTailoredHeadline(), headlineFont);
            headline.setSpacingAfter(10);
            document.add(headline);

            // 2. Professional Summary
            addSectionHeader(document, "PROFESSIONAL SUMMARY", sectionTitleFont);
            Paragraph summary = new Paragraph(dto.getTailoredSummary(), bodyFont);
            summary.setSpacingAfter(12);
            document.add(summary);

            // 3. Technical Skills
            addSectionHeader(document, "TECHNICAL SKILLS", sectionTitleFont);
            if (dto.getSkillsSection() != null) {
                for (Map.Entry<String, List<String>> entry : dto.getSkillsSection().entrySet()) {
                    Paragraph skillLine = new Paragraph();
                    skillLine.add(new Chunk(entry.getKey() + ": ", boldFont));
                    skillLine.add(new Chunk(String.join(", ", entry.getValue()), bodyFont));
                    document.add(skillLine);
                }
            }
            document.add(new Paragraph(" ", bodyFont)); // Spacing

            // 4. Professional Experience
            addSectionHeader(document, "PROFESSIONAL EXPERIENCE", sectionTitleFont);
            if (dto.getWorkExperience() != null) {
                for (ParsedResumeDto.ExperienceItem exp : dto.getWorkExperience()) {
                    Paragraph expTitle = new Paragraph();
                    expTitle.add(new Chunk(exp.getRole() + " — " + exp.getCompany(), boldFont));
                    expTitle.add(new Chunk(" (" + exp.getStartDate() + " to " + (exp.getEndDate() != null ? exp.getEndDate() : "Present") + ")", bodyFont));
                    document.add(expTitle);

                    com.lowagie.text.List bulletList = new com.lowagie.text.List(com.lowagie.text.List.UNORDERED, 10);
                    bulletList.setListSymbol("• ");
                    for (String bullet : exp.getBullets()) {
                        bulletList.add(new ListItem(bullet, bodyFont));
                    }
                    document.add(bulletList);
                    document.add(new Paragraph(" ", bodyFont));
                }
            }

            // 5. Education
            addSectionHeader(document, "EDUCATION", sectionTitleFont);
            if (dto.getEducation() != null) {
                for (ParsedResumeDto.EducationItem edu : dto.getEducation()) {
                    Paragraph eduLine = new Paragraph(edu.getDegree() + " — " + edu.getInstitution() + " (" + edu.getGraduationYear() + ")", bodyFont);
                    document.add(eduLine);
                }
            }

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate PDF export: {}", e.getMessage(), e);
            throw new RuntimeException("PDF generation failed: " + e.getMessage(), e);
        }
    }

    public byte[] exportToDocx(TailoredResume tailoredResume) {
        try {
            TailoredResumeDto dto = objectMapper.readValue(tailoredResume.getTailoredJson(), TailoredResumeDto.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            XWPFDocument doc = new XWPFDocument();

            // Name
            XWPFParagraph namePara = doc.createParagraph();
            XWPFRun nameRun = namePara.createRun();
            nameRun.setText(dto.getCandidateName());
            nameRun.setBold(true);
            nameRun.setFontSize(16);

            // Contact
            XWPFParagraph contactPara = doc.createParagraph();
            XWPFRun contactRun = contactPara.createRun();
            contactRun.setText(dto.getEmail() + " | " + dto.getPhone() + " | " + dto.getLocation());
            contactRun.setFontSize(10);

            // Headline
            XWPFParagraph headPara = doc.createParagraph();
            XWPFRun headRun = headPara.createRun();
            headRun.setText(dto.getTailoredHeadline());
            headRun.setItalic(true);
            headRun.setFontSize(11);

            // Summary
            addDocxSection(doc, "PROFESSIONAL SUMMARY");
            XWPFParagraph sumPara = doc.createParagraph();
            sumPara.createRun().setText(dto.getTailoredSummary());

            // Skills
            addDocxSection(doc, "TECHNICAL SKILLS");
            if (dto.getSkillsSection() != null) {
                for (Map.Entry<String, List<String>> entry : dto.getSkillsSection().entrySet()) {
                    XWPFParagraph sp = doc.createParagraph();
                    XWPFRun srKey = sp.createRun();
                    srKey.setBold(true);
                    srKey.setText(entry.getKey() + ": ");
                    XWPFRun srVal = sp.createRun();
                    srVal.setText(String.join(", ", entry.getValue()));
                }
            }

            // Experience
            addDocxSection(doc, "PROFESSIONAL EXPERIENCE");
            if (dto.getWorkExperience() != null) {
                for (ParsedResumeDto.ExperienceItem exp : dto.getWorkExperience()) {
                    XWPFParagraph ep = doc.createParagraph();
                    XWPFRun er = ep.createRun();
                    er.setBold(true);
                    er.setText(exp.getRole() + " — " + exp.getCompany() + " (" + exp.getStartDate() + " - " + (exp.getEndDate() != null ? exp.getEndDate() : "Present") + ")");

                    for (String bullet : exp.getBullets()) {
                        XWPFParagraph bp = doc.createParagraph();
                        bp.setIndentationLeft(360);
                        bp.createRun().setText("• " + bullet);
                    }
                }
            }

            doc.write(out);
            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate DOCX export: {}", e.getMessage(), e);
            throw new RuntimeException("DOCX generation failed: " + e.getMessage(), e);
        }
    }

    private void addSectionHeader(Document doc, String title, Font font) throws DocumentException {
        Paragraph p = new Paragraph(title, font);
        p.setSpacingBefore(8);
        p.setSpacingAfter(4);
        doc.add(p);
    }

    private void addDocxSection(XWPFDocument doc, String title) {
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingBefore(200);
        p.setSpacingAfter(100);
        XWPFRun r = p.createRun();
        r.setBold(true);
        r.setFontSize(12);
        r.setText(title);
    }
}
