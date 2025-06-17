package Rent;

import com.toedter.calendar.JDateChooser;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import javax.swing.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.PDFont;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;


public class Report extends javax.swing.JFrame {

    private JDateChooser dateChooser;
    private JButton exportButton;

    public Report() {
        initComponents();
    }

    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Report");

        JLabel dateLabel = new JLabel("Select Date:");
        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");

        exportButton = new JButton("Export to PDF");
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });

        JPanel panel = new JPanel();
        panel.add(dateLabel);
        panel.add(dateChooser);
        panel.add(exportButton);

        add(panel);

        pack();
        setLocationRelativeTo(null);
    }

    private void exportButtonActionPerformed(ActionEvent evt) {
        if (dateChooser.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Please select a date.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String selectedDate = sdf.format(dateChooser.getDate());

        // Fetch data from the database
        String data = fetchData(selectedDate);

        if (data.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No data found for the selected date.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Export the fetched data to a PDF
        boolean success = exportToPDF(selectedDate, data);
        if (success) {
            JOptionPane.showMessageDialog(this, "Report exported successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to export the report.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String fetchData(String date) {
    StringBuilder data = new StringBuilder();
    String url = "jdbc:mysql://localhost:3306/rentcar"; // Replace with your DB details
    String user = "root"; // Replace with your DB username
    String password = ""; // Replace with your DB password

    String rentalQuery = "SELECT r.car_id, c.name AS customer_name, r.date, r.due, r.fee, cr.make " +
                         "FROM rental r " +
                         "JOIN customer c ON r.cust_id = c.id " +
                         "JOIN carregistration cr ON r.car_id = cr.car_no " +
                         "WHERE DATE(r.date) = ?";

    double totalFee = 0.0; // Initialize total fee

    try (Connection conn = DriverManager.getConnection(url, user, password);
         PreparedStatement rentalStmt = conn.prepareStatement(rentalQuery)) {

        // Fetch rental records
        rentalStmt.setString(1, date);
        ResultSet rentalRs = rentalStmt.executeQuery();
        while (rentalRs.next()) {
            double fee = rentalRs.getDouble("fee");
            totalFee += fee; // Sum up the fees

            data.append(rentalRs.getInt("car_id")).append(", ")
                .append(rentalRs.getString("customer_name")).append(", ")
                .append(rentalRs.getString("date")).append(", ")
                .append(rentalRs.getString("due")).append(", ")
                .append(rentalRs.getString("make")).append(", ")
                .append(fee)
                .append("\n");
        }
    } catch (Exception e) {
        e.printStackTrace();
    }

    // Append the total fee to the end of the data
    data.append("TOTAL, , , , , ").append(totalFee).append("\n");
    return data.toString();
}



private boolean exportToPDF(String date, String data) {
    String pdfFileName = "Report_" + date + ".pdf";

    try (PDDocument document = new PDDocument()) {
        PDPage page = new PDPage();
        document.addPage(page);

        float margin = 50; // Page margin
        float tableStartY = 650; // Adjusted table start position to leave space for the title and logo
        float rowHeight = 20;
        float tableWidth = 500; // Adjusted total table width to fit the page
        float[] columnWidths = {60, 120, 120, 120, 80, 100}; // Adjusted column widths to fit table
        String[] headers = {"Car ID", "Customer", "Rental Start", "Rental End", "Make", "Fee"}; // Headers

        // Get current time for the export timestamp
        String exportTime = new SimpleDateFormat("HH:mm:ss").format(new java.util.Date());

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            // Add Logo
            PDImageXObject logo = PDImageXObject.createFromFile("E:\\University Work\\ACP JAVA Course\\Java Project\\Car-Rental-Java\\src\\Rent\\images\\malik_travel.png", document);
            contentStream.drawImage(logo, margin, tableStartY + 50, 100, 50); // Adjust position and size

            // Add Company Name
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 20);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin + 120, tableStartY + 80); // Adjust position
            contentStream.showText("Malik Travels");
            contentStream.endText();

            // Add Date and Export Time
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, tableStartY + 30);
            contentStream.showText("Report Date: " + date + "  |  Exported at: " + exportTime);
            contentStream.endText();

            // Draw Table Headers
            float nextX = margin;
            float nextY = tableStartY;
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);

            for (int i = 0; i < headers.length; i++) {
                // Draw cell background for headers
                contentStream.setNonStrokingColor(220, 220, 220); // Light gray background
                contentStream.addRect(nextX, nextY - rowHeight, columnWidths[i], rowHeight);
                contentStream.fill();

                // Reset to black for text
                contentStream.setNonStrokingColor(0, 0, 0);

                // Write header text
                contentStream.beginText();
                contentStream.newLineAtOffset(nextX + 5, nextY - 15);
                contentStream.showText(headers[i]);
                contentStream.endText();

                nextX += columnWidths[i];
            }

            // Draw table header borders
            contentStream.setLineWidth(1);
            contentStream.moveTo(margin, nextY);
            contentStream.lineTo(margin + tableWidth, nextY);
            contentStream.stroke();

            nextY -= rowHeight;

            // Add Data Rows
            contentStream.setFont(PDType1Font.HELVETICA, 10);
            String[] rows = data.split("\n");

            for (String row : rows) {
                nextX = margin;
                String[] values = row.split(", ");

                for (int i = 0; i < headers.length; i++) { // Limit data to number of headers
                    // Write data cell text
                    contentStream.beginText();
                    contentStream.newLineAtOffset(nextX + 5, nextY - 15);
                    if (i < values.length) {
                        contentStream.showText(values[i]);
                    }
                    contentStream.endText();

                    // Draw vertical line for each cell
                    contentStream.moveTo(nextX, nextY);
                    contentStream.lineTo(nextX, nextY - rowHeight);
                    contentStream.stroke();

                    nextX += columnWidths[i];
                }

                nextY -= rowHeight;

                // Draw horizontal line after each row
                contentStream.moveTo(margin, nextY);
                contentStream.lineTo(margin + tableWidth, nextY);
                contentStream.stroke();
            }

            // Final right border
            contentStream.moveTo(margin + tableWidth, tableStartY);
            contentStream.lineTo(margin + tableWidth, nextY);
            contentStream.stroke();
        }

        document.save(pdfFileName);
        return true;

    } catch (IOException e) {
        e.printStackTrace();
        return false;
    }
}


    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Report().setVisible(true);
            }
        });
    }
}
