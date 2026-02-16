package dolpi.Report_Service.Service;

import dolpi.Report_Service.Configuration.RabbitMQConfig;
import dolpi.Report_Service.Dto.Notifiaction;
import dolpi.Report_Service.Dto.Notificationmessage;
import dolpi.Report_Service.Dto.RegisterEntity;
import dolpi.Report_Service.Dto.RegisterNgo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class NotificationConsumer {

    @Autowired
    private NgoFeignService ngoFeignService;

    @Autowired
    private MuncipalFeignService muncipalFeignService;

    @Autowired
    private NotificationFeignService notificationFeignService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE, ackMode = "MANUAL")
    public void consume(Notificationmessage notificationmessage, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        if (notificationmessage == null) {
            log.error("RabbitMQ message is null. Dropping message.");
            channel.basicAck(deliveryTag, false);
            return;
        }

        try {
            String city = notificationmessage.getCity();
            log.info("Starting processing for city: {}", city);

            // 1. NGO Fetch (Isolated in try-catch to keep processing alive)
            List<RegisterNgo> ngos = new ArrayList<>();
            try {
                ngos = ngoFeignService.findNGO(city);
            } catch (Exception e) {
                log.error("NGO Service unavailable for city {}: {}", city, e.getMessage());
            }

            // 2. Municipal Fetch (Isolated in try-catch)
            List<RegisterEntity> municipals = new ArrayList<>();
            try {
                municipals = muncipalFeignService.findMunicipal(city);
            } catch (Exception e) {
                log.error("Municipal Service unavailable for city {}: {}", city, e.getMessage());
            }

            // 3. Logic: Agar data nahi hai, toh exception throw mat karo (loop rokne ke liye)
            if ((ngos == null || ngos.isEmpty()) && (municipals == null || municipals.isEmpty())) {
                log.warn("No NGO or Municipal found for city: {}. Marking as processed.", city);
                channel.basicAck(deliveryTag, false);
                return;
            }

            // 4. Process valid NGOs
            if (ngos != null) {
                for (RegisterNgo ngo : ngos) {
                    saveToNotificationService(notificationmessage.getReported_id(), ngo.getId());
                }
            }

            // 5. Process valid Municipals
            if (municipals != null) {
                for (RegisterEntity entity : municipals) {
                    saveToNotificationService(notificationmessage.getReported_id(), entity.getId());
                }
            }

            // SUCCESS: Message acknowledge karein
            channel.basicAck(deliveryTag, false);
            log.info("Successfully finished processing for city: {}", city);

        } catch (Exception ex) {
            log.error("Unexpected error in consumer: {}", ex.getMessage());
            // Fatal error par requeue=false karein taaki loop na bane
            channel.basicNack(deliveryTag, false, false);
        }
    }

    private void saveToNotificationService(String reportedId, String targetId) {
        try {
            Notifiaction n = new Notifiaction();
            n.setSubmissionId(reportedId);
            n.setNgoanmcplId(targetId);
            notificationFeignService.savenotifiaction(n);
        } catch (Exception e) {
            log.error("Failed to save notification for target {}: {}", targetId, e.getMessage());
        }
    }
}
