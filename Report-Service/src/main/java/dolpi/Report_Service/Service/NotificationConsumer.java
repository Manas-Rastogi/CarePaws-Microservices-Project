package dolpi.Report_Service.Service;

import dolpi.Report_Service.Configuration.RabbitMQConfig;
import dolpi.Report_Service.Dto.Notifiaction;
import dolpi.Report_Service.Dto.Notificationmessage;
import dolpi.Report_Service.Dto.RegisterEntity;
import dolpi.Report_Service.Dto.RegisterNgo;
import dolpi.Report_Service.Interface.NotificationFeign;
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

        // 1. Null Message Check
        if (notificationmessage == null) {
            log.error("Received null message from RabbitMQ. Acknowledging to remove from queue.");
            channel.basicAck(deliveryTag, false);
            return;
        }

        try {
            String city = notificationmessage.getCity();
            log.info("Processing notification for city: {}", city);

            // 2. Fetch NGOs (Wrapped in try-catch to prevent Feign/CircuitBreaker crash)
            List<RegisterNgo> ngos = new ArrayList<>();
            try {
                ngos = ngoFeignService.findNGO(city);
            } catch (Exception e) {
                log.error("NGO Service call failed for city {}: {}", city, e.getMessage());
            }

            // 3. Fetch Municipals (Wrapped in try-catch)
            List<RegisterEntity> municipals = new ArrayList<>();
            try {
                municipals = muncipalFeignService.findMunicipal(city);
            } catch (Exception e) {
                log.error("Municipal Service call failed for city {}: {}", city, e.getMessage());
            }

            // 4. Data Validation (Log instead of throwing Exception to avoid loop)
            boolean hasNgos = (ngos != null && !ngos.isEmpty());
            boolean hasMunicipals = (municipals != null && !municipals.isEmpty());

            if (!hasNgos && !hasMunicipals) {
                log.warn("No NGO or Municipal found for city: {}. Acknowledging message as 'processed' (no targets found).", city);
                channel.basicAck(deliveryTag, false);
                return;
            }

            // 5. Process NGOs
            if (hasNgos) {
                for (RegisterNgo ngo : ngos) {
                    processNotification(notificationmessage.getReported_id(), ngo.getId());
                }
            }

            // 6. Process Municipals
            if (hasMunicipals) {
                for (RegisterEntity entity : municipals) {
                    processNotification(notificationmessage.getReported_id(), entity.getId());
                }
            }

            // SUCCESS: Message successfully handled
            channel.basicAck(deliveryTag, false);
            log.info("Completed processing for city: {}", city);

        } catch (Exception ex) {
            log.error("Critical error in consumer logic: {}", ex.getMessage());
            
            // IMPORTANT: Setting requeue to 'false' to prevent infinite loops.
            // If you have a Dead Letter Queue (DLQ) configured, it will go there.
            channel.basicNack(deliveryTag, false, false);
        }
    }

    /**
     * Helper method to handle individual notification saves
     */
    private void processNotification(String reportedId, String targetId) {
        try {
            Notifiaction n = new Notifiaction();
            n.setSubmissionId(reportedId);
            n.setNgoanmcplId(targetId);
            notificationFeignService.savenotifiaction(n);
        } catch (Exception e) {
            log.error("Failed to save notification for TargetID {}: {}", targetId, e.getMessage());
        }
    }
}
