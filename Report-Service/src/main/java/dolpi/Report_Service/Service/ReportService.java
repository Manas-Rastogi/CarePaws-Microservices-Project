package dolpi.Report_Service.Service;

import dolpi.Report_Service.Dto.Notificationmessage;
import dolpi.Report_Service.Dto.ReportDTO;
import dolpi.Report_Service.Entity.ReportEntity;
import dolpi.Report_Service.Repository.ReportRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

@Service
@Slf4j
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private NotificationProducer notificationProducer;

    // Docker volume path
    private final String uploadDir = "/app/uploads"; 

  @Autowired
  public ReportService(@Lazy ReportController reportController) {
    this.reportController = reportController;
  }

    public String report(ReportDTO reportDTO, MultipartFile file) {
        try {
            // 1. Directory taiyaar karein
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 2. Unique filename banayein
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);

            // 3. File copy karein (REPLACE_EXISTING ke saath taaki conflict na ho)
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 4. Database mein entry
            ReportEntity report = new ReportEntity();
            report.setUserid(reportDTO.getUserid());
            report.setName(reportDTO.getName());
            report.setLocation_address(reportDTO.getLocation_address());
            report.setCity(reportDTO.getCity());
            report.setDescription(reportDTO.getDescription());
            report.setPincode(reportDTO.getPincode());
            report.setMobilenumber(reportDTO.getMobilenumber());
            report.setImagepath(filePath.toString()); 
            report.setCreatedAt(LocalDateTime.now());

            reportRepository.save(report);

            // 5. RabbitMQ notification
            notificationProducer.send(new Notificationmessage(report.getId(), report.getCity()));
            
            log.info("Successfully saved file at: {}", filePath);
            return "Successfully saved on AWS EC2 Storage.";

        } catch (IOException e) {
            log.error("File upload failed: {}", e.getMessage());
            return "image not upload: " + e.getMessage(); 
        } catch (Exception e) {
            log.error("An unexpected error occurred: {}", e.getMessage());
            return "Error: " + e.getMessage();
        }
    }
}
