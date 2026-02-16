// package dolpi.Report_Service.Service;

// import com.rabbitmq.client.Channel;
// import dolpi.Report_Service.Dto.Notificationmessage;
// import dolpi.Report_Service.Dto.RegisterEntity;
// import dolpi.Report_Service.Dto.RegisterNgo;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.amqp.core.Message;
// import org.springframework.amqp.core.MessageProperties;

// import java.util.List;

// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyBoolean;
// import static org.mockito.ArgumentMatchers.anyLong;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// public class NotificationConsumerTest {
//     @InjectMocks
//     private NotificationConsumer notificationConsumer;

//     @Mock
//     private NgoFeignService ngoFeignService;

//     @Mock
//     private MuncipalFeignService muncipalFeignService;

//     @Mock
//     private NotificationFeignService notificationFeignService;

//     @Mock
//     private Channel channel;

//     @Mock
//     private Message message;

//     @Test
//     void consume_success_ackCalled() throws Exception {

//         Notificationmessage msg = new Notificationmessage();
//         msg.setCity("Delhi");
//         msg.setReported_id("123");

//         RegisterNgo ngo = new RegisterNgo();
//         ngo.setId("N1");

//         RegisterEntity entity = new RegisterEntity();
//         entity.setId("M1");

//         when(ngoFeignService.findNGO("Delhi"))
//                 .thenReturn(List.of(ngo));

//         when(muncipalFeignService.findMunicipal("Delhi"))
//                 .thenReturn(List.of(entity));

//         MessageProperties props = new MessageProperties();
//         props.setDeliveryTag(10L);
//         when(message.getMessageProperties()).thenReturn(props);

//         notificationConsumer.consume(msg, message, channel);

//         verify(notificationFeignService, times(2))
//                 .savenotifiaction(any());

//         verify(channel).basicAck(10L, false);
//         verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
//     }
// }
