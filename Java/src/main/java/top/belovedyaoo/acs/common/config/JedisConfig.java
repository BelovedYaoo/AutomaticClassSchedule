package top.belovedyaoo.acs.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.annotation.PostConstruct;
import java.time.Duration;

/**
 * Jedis配置
 *
 * @author BelovedYaoo
 * @version 1.1
 */
@Component
public class JedisConfig {

    /**
     * Redis服务器地址
     */
    @Value("${spring.redis.host}")
    private String host;

    private static String hostStatic;

    /**
     * Redis服务器连接端口
     */
    @Value("${spring.redis.port}")
    private int port;

    private static int portStatic;

    /**
     * Redis服务器连接密码
     */
    @Value("${spring.redis.password}")
    private String password;

    private static String passwordStatic;

    /**
     * 连接超时时间
     */
    @Value("${spring.redis.timeout}")
    private int timeout;

    private static int timeoutStatic;

    /**
     * 连接池最大连接数
     */
    @Value("${spring.redis.lettuce.pool.max-active}")
    private int maxTotal;

    private static int maxTotalStatic;

    /**
     * 连接池最大阻塞等待时间
     */
    @Value("${spring.redis.lettuce.pool.max-wait}")
    private int maxWait;

    private static int maxWaitStatic;

    /**
     * 连接池中的最大空闲连接
     */
    @Value("${spring.redis.lettuce.pool.max-idle}")
    private int maxIdle;

    private static int maxIdleStatic;

    /**
     * 连接池中的最小空闲连接
     */
    @Value("${spring.redis.lettuce.pool.min-idle}")
    private int minIdle;

    private static int minIdleStatic;

    @PostConstruct
    public void init() {
        hostStatic = host;
        portStatic = port;
        passwordStatic = password;
        timeoutStatic = timeout;
        maxTotalStatic = maxTotal;
        maxWaitStatic = maxWait;
        maxIdleStatic = maxIdle;
        minIdleStatic = minIdle;
    }

    /**
     * 获取Jedis连接池对象
     *
     * @return Jedis连接池
     */
    public static JedisPool getJedisPool() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(maxTotalStatic);
        jedisPoolConfig.setMaxIdle(maxIdleStatic);
        jedisPoolConfig.setMinIdle(minIdleStatic);
        jedisPoolConfig.setMaxWait(Duration.ofMillis(maxWaitStatic));

        if (passwordStatic == null) {
            passwordStatic = "";
        }
        return new JedisPool(jedisPoolConfig, hostStatic, portStatic, timeoutStatic, passwordStatic);
    }

}
