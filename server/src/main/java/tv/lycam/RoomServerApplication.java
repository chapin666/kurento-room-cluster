package tv.lycam;

import com.google.gson.JsonArray;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.kurento.commons.ConfigFileManager;
import org.kurento.jsonrpc.JsonUtils;
import org.kurento.jsonrpc.internal.server.config.JsonRpcConfiguration;
import org.kurento.jsonrpc.server.JsonRpcConfigurer;
import org.kurento.jsonrpc.server.JsonRpcHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import tv.lycam.api.KurentoClientProvider;
import tv.lycam.kms.FixedNKmsManager;
import tv.lycam.rpc.JsonRpcNotificationService;
import tv.lycam.rpc.JsonRpcUserControl;

import java.util.List;

import static org.kurento.commons.PropertiesManager.getPropertyJson;


/**
 *
 * 应用入口
 *
 */
@Import(JsonRpcConfiguration.class)
@SpringBootApplication
public class RoomServerApplication implements JsonRpcConfigurer {

    // Logger
    private  static final Logger log = LoggerFactory.getLogger(RoomServerApplication.class);


    private final static String ROOM_CFG_FILENAME = "room-config.json";


    private final String KMS_AUTO_DISCOVERY_MODEL = "autodiscovery";
    // KMS 地址列表
    public static final String KMSS_URIS_PROPERTY = "kms.uris";
    // KMS 默认地址
    public static final String KMSS_URIS_DEFAUKT = "[\"ws://localhost:8888/kurento\"]";


    static {
        ConfigFileManager.loadConfigFile(ROOM_CFG_FILENAME);
    }


    /**
     * KMS Manager
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public KurentoClientProvider kmsManager() {

        // 加载kms配置列表
        JsonArray kmsUris = getPropertyJson(KMSS_URIS_PROPERTY, KMSS_URIS_DEFAUKT, JsonArray.class);
        List<String> kmsWsUris = JsonUtils.toStringList(kmsUris);

        if (kmsWsUris.isEmpty()) {
            throw new IllegalArgumentException(KMSS_URIS_PROPERTY + " 配置至少应该有一个url");
        }

        FixedNKmsManager fixedNKmsManager = new FixedNKmsManager(kmsWsUris);
        //fixedNKmsManager.setAuthRegex("");
        return fixedNKmsManager;

       /* String firstKmsWsUri = kmsWsUris.get(0);

        if (KMS_AUTO_DISCOVERY_MODEL.equals(firstKmsWsUri)) {
            log.info("Using autodiscovery rules to locate KMS on every pipeline");
            return new AutodiscoveryKurentoClientProvider();
        } else {
            log.info("Configuring Kurento Room Server to use first of the following kmss: " + kmsUris);
            return new FixedOneKmsManager(firstKmsWsUri);
        }*/

    }


    @Bean
    @ConditionalOnMissingBean
    public JsonRpcNotificationService notificationService() {
        return new JsonRpcNotificationService();
    }

    @Bean
    @ConditionalOnMissingBean
    public NotificationRoomManager roomManager() {
        return new NotificationRoomManager(notificationService(), kmsManager());
    }


    @Bean
    @ConditionalOnMissingBean
    public JsonRpcUserControl userControl() {
        return new JsonRpcUserControl(roomManager());
    }


    @Bean
    @ConditionalOnMissingBean
    public RoomJsonRpcHandler roomHandler() {
        return new RoomJsonRpcHandler(userControl(), notificationService());
    }


    @Override
    public void registerJsonRpcHandlers(JsonRpcHandlerRegistry jsonRpcHandlerRegistry) {
        jsonRpcHandlerRegistry.addHandler(roomHandler().withPingWatchdog(true), "/room");
    }

    /**
     * 设置并启动
     *
     * @param args
     * @return
     */
    public static ConfigurableApplicationContext start(String []args) {
        log.info("Using /dev/urandom for secure random generation");
        System.setProperty("java.security.egd", "file:/dev/./urandom");
        return SpringApplication.run(RoomServerApplication.class, args);
    }


    public static void main(String[] args) {
		start(args);
	}

}
