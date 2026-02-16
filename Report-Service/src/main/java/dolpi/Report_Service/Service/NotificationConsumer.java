package dolpi.Report_Service.Service;

import dolpi.Report_Service.Configuration.RabbitMQConfig;
import dolpi.Report_Service.Dto.*;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
            log.error("Received null message from RabbitMQ");
            channel.basicAck(deliveryTag, false);
            return;
        }

        try {
            String city = notificationmessage.getCity();
            log.info("Processing city: {}", city);

            // 1. Fetch NGOs Safely
            List<RegisterNgo> ngos = new ArrayList<>();
            try {
                ngos = ngoFeignService.findNGO(city);
            } catch (Exception e) {
                log.error("NGO Service error: {}", e.getMessage());
            }

            // 2. Fetch Municipals Safely
            List<RegisterEntity> municipals = new ArrayList<>();
            try {
                municipals = muncipalFeignService.findMunicipal(city);
            } catch (Exception e) {
                log.error("Municipal Service error (Circuit Breaker OPEN or Service Down): {}", e.getMessage());
            }

            // 3. Logic: No Targets Found -> Ack and Return (STOPS INFINITE LOOP)
            if ((ngos == null || ngos.isEmpty()) && (municipals == null || municipals.isEmpty())) {
                log.warn("No NGO or Municipal found for city: {}. Cleaning from queue.", city);
                channel.basicAck(deliveryTag, false);
                return;
            }

            // 4. Save Notifications for NGOs
            if (ngos != null) {
                for (RegisterNgo ngo : ngos) {
                    processSave(notificationmessage.getReported_id(), ngo.getId());
                }
            }

            // 5. Save Notifications for Municipals
            if (municipals != null) {
                for (RegisterEntity entity : municipals) {
                    processSave(notificationmessage.getReported_id(), entity.getId());
                }
            }

            // Success ACK
            channel.basicAck(deliveryTag, false);
            log.info("Message processed successfully for city: {}", city);

        } catch (Exception ex) {
            log.error("Fatal processing error: {}", ex.getMessage());
            // REQUEUE = FALSE (System ko crash hone se bachane ke liye)
            channel.basicNack(deliveryTag, false, false);
        }
    }

    private void processSave(String reportId, String targetId) {
        try {
            Notifiaction n = new Notifiaction();
            n.setSubmissionId(reportId);
            n.setNgoanmcplId(targetId);
            notificationFeignService.savenotifiaction(n);
        } catch (Exception e) {
            log.error("Failed to save notification for target {}: {}", targetId, e.getMessage());
        }
    }
}
