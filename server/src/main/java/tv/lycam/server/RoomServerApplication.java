package tv.lycam.server;

import com.cloopen.rest.sdk.CCPRestSmsSDK;
import com.google.gson.JsonArray;
import org.kurento.commons.ConfigFileManager;
import org.kurento.jsonrpc.JsonUtils;
import org.kurento.jsonrpc.internal.server.config.JsonRpcConfiguration;
import org.kurento.jsonrpc.server.JsonRpcConfigurer;
import org.kurento.jsonrpc.server.JsonRpcHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import tv.lycam.sdk.NotificationRoomManager;
import tv.lycam.sdk.api.KurentoClientProvider;
import tv.lycam.server.api.config.JwtFilter;
import tv.lycam.server.api.storage.StorageProperties;
import tv.lycam.server.api.storage.StorageService;
import tv.lycam.server.kms.FixedNKmsManager;
import tv.lycam.server.rpc.JsonRpcNotificationService;
import tv.lycam.server.rpc.JsonRpcUserControl;

import java.util.List;

import static org.kurento.commons.PropertiesManager.getPropertyJson;


/**
 *
 * 应用入口
 *
 */
@Import(JsonRpcConfiguration.class)
@EnableConfigurationProperties(StorageProperties.class)
@SpringBootApplication
public class RoomServerApplication implements JsonRpcConfigurer {

    // Logger
    private  static final Logger log = LoggerFactory.getLogger(RoomServerApplication.class);


    private final static String ROOM_CFG_FILENAME = "room-config.json";


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
            throw new IllegalArgumentException(KMSS_URIS_PROPERTY + " At least one kms uri in file " + ROOM_CFG_FILENAME);
        }

        FixedNKmsManager fixedNKmsManager = new FixedNKmsManager(kmsWsUris);

        return fixedNKmsManager;
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
        // random number (unlocked)
        System.setProperty("java.security.egd", "file:/dev/./urandom");
        return SpringApplication.run(RoomServerApplication.class, args);
    }


    public static void main(String[] args) {
		start(args);
	}



	@Bean
    public CCPRestSmsSDK ccpRestSmsSDK() {
        CCPRestSmsSDK ccpRestSmsSDK = new CCPRestSmsSDK();
        ccpRestSmsSDK.init("sandboxapp.cloopen.com", "8883");
        ccpRestSmsSDK.setAccount("aaf98f8952305ced015230f21a4001ca", "b4cf071f95c442daab536985b590e1bb");
        ccpRestSmsSDK.setAppId("aaf98f8952305ced015230f7237b0203");
        return ccpRestSmsSDK;
    }


    @Bean
    CommandLineRunner init(StorageService storageService) {
        return (args) -> {

            // File init
            //storageService.deleteAll();
            storageService.init();
        };
    }


    @Bean
    public FilterRegistrationBean jwtFilter() {
        final FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(new JwtFilter());
        registrationBean.addUrlPatterns("/test/*");
        return registrationBean;
    }



}
