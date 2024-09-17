package top.belovedyaoo.acs.common.toolkit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisDataException;
import top.belovedyaoo.acs.common.config.JedisConfig;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Jedis连接池工具类
 *
 * @author BelovedYaoo
 * @version 1.2
 */
@Component
public class JedisOperateUtil {

    /**
     * 默认分隔符
     */
    public static final String REDIS_SEPARATOR = ":";

    /**
     * 默认成功返回值
     */
    private static final String OK = "ok";

    /**
     * 默认超时时间
     */
    private static final int DEFAULT_SETEX_TIMEOUT = 60 * 60;

    /**
     * 从配置文件中获取Jedis连接池
     */
    private static final JedisPool JEDIS_POOL = JedisConfig.getJedisPool();

    /**
     * Jackson对象映射器
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 从连接池中获取一个Jedis实例
     *
     * @return Jedis实例
     */
    public static Jedis getJedis() {
        return JEDIS_POOL.getResource();
    }

    /**
     * 将使用完毕的Jedis实例返回到连接池
     *
     * @param jedis 使用完毕的Jedis实例
     */
    public static void returnJedis(Jedis jedis) {
        JEDIS_POOL.returnResource(jedis);
    }

    /**
     * 添加一个字符串值到Redis中，如果添加成功则返回1，添加失败则返回0（例如键或值为null）。
     *
     * @param key   添加的键，类型为String。
     * @param value 添加的值，类型为String。
     *
     * @return 添加结果，成功返回1，失败返回0。
     */
    public static int set(String key, String value) {
        if (isValueNull(key, value)) {
            return 0;
        }
        Jedis jedis = null;
        try {
            jedis = getJedis();
            if (jedis.set(key, value).equalsIgnoreCase(OK)) {
                return 1;
            } else {
                return 0;
            }
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * 缓存一个字符串值,成功返回1,失败返回0,默认缓存时间为1小时,以本类的常量DEFAULT_SETEX_TIMEOUT为准
     *
     * @param key   缓存的键，类型为String。
     * @param value 缓存的值，类型为String。
     *
     * @return int 返回操作的结果，成功返回1，失败返回0。
     */
    public static int setEx(String key, String value) {
        return setEx(key, value, DEFAULT_SETEX_TIMEOUT);
    }

    /**
     * 缓存一个字符串值到Redis缓存中，设定指定的过期时间。
     *
     * @param value   缓存的值，类型为String。
     * @param timeout 缓存的过期时间，单位为秒。
     * @param keys    缓存的键列表，类型为String。
     *
     * @return int 返回操作的结果，成功返回1，失败返回0。
     */
    public static int setEx(String value, int timeout, String... keys) {
        StringBuilder keyBuilder = new StringBuilder();
        for (String key : keys) {
            if (!keyBuilder.isEmpty()) {
                keyBuilder.append(REDIS_SEPARATOR);
            }
            keyBuilder.append(key);
        }
        return setEx(keyBuilder.toString(), value, timeout);
    }

    /**
     * 缓存一个字符串值到Redis缓存中，设定指定的过期时间。
     * 成功缓存返回1，失败返回0。
     * 缓存时间以timeout为准，单位为秒。
     *
     * @param key     缓存的键，类型为String。
     * @param value   缓存的值，类型为String。
     * @param timeout 缓存的过期时间，单位为秒。
     *
     * @return int 返回操作的结果，成功返回1，失败返回0。
     */
    public static int setEx(String key, String value, int timeout) {
        if (isValueNull(key, value)) {
            return 0;
        }
        Jedis jedis = null;
        try {
            jedis = getJedis();
            if (jedis.setex(key, timeout, value).equalsIgnoreCase(OK)) {
                return 1;
            } else {
                return 0;
            }
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * 添加一个指定类型的对象到存储系统中，成功返回1，失败返回0。
     * 首先检查键值是否为null，若为null则不进行存储操作。
     * 使用Jedis客户端将对象序列化后存储到指定的键中。
     *
     * @param key   用于存储对象的键，类型为String。
     * @param value 需要存储的对象，类型为泛型T。
     *
     * @return int 返回操作结果，成功存储返回1，失败返回0。
     */
    public static <T> int set(String key, T value) {
        if (isValueNull(key, value)) {
            return 0;
        }
        Jedis jedis = null;
        try {
            jedis = getJedis();
            // 序列化对象为字节数组
            byte[] data = enSeri(value);
            // 尝试将序列化后的对象存储到指定的键
            if (jedis.set(key.getBytes(), data).equalsIgnoreCase(OK)) {
                return 1;
            } else {
                return 0;
            }
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * 缓存一个指定类型的对象到Redis中,成功返回1,失败返回0。
     * 默认缓存时间为1小时,使用本类中定义的常量DEFAULT_SETEX_TIMEOUT作为超时时间。
     *
     * @param key   缓存对象的键，类型为String。
     * @param value 缓存对象的值，类型为泛型T。
     *
     * @return 若缓存操作成功返回1，失败返回0。
     */
    public static <T> int setEx(String key, T value) {
        return setEx(key, value, DEFAULT_SETEX_TIMEOUT);
    }

    /**
     * 缓存一个指定类型的对象到Redis中，成功返回1，失败返回0。缓存时间以timeout为准，单位为秒。
     *
     * @param key     缓存对象的键，类型为String。
     * @param value   缓存对象的值，类型为泛型T。
     * @param timeout 缓存对象的过期时间，单位为秒。
     *
     * @return 若缓存操作成功返回1，失败返回0。
     */
    public static <T> int setEx(String key, T value, int timeout) {
        if (isValueNull(key, value)) {
            return 0;
        }
        Jedis jedis = null;
        try {
            jedis = getJedis();
            byte[] data = enSeri(value);
            if (jedis.setex(key.getBytes(), timeout, data).equalsIgnoreCase(OK)) {
                return 1;
            } else {
                return 0;
            }
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * 将Redis中指定key的值加1，并返回加1后的结果。
     * 如果key不存在或值不是数字，会抛出JedisDataException异常。
     * 如果key的值为null，则函数返回null。
     *
     * @param key Redis中要进行加1操作的key。
     *
     * @return 加1后的值，如果key的值为null则返回null。
     *
     * @exception JedisDataException 如果操作失败，例如key的值不是数字。
     */
    public static Long incr(String key) throws JedisDataException {
        if (isValueNull(key)) {
            return null;
        }
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.incr(key);
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * 将Redis中指定key的值减1，并返回减1后的结果。
     * 如果key不存在或值不是数字，会抛出JedisDataException异常。
     * 如果key的值为null，则函数返回null。
     *
     * @param key Redis中要进行减1操作的key。
     *
     * @return 减1后的值，如果key的值为null则返回null。
     *
     * @exception JedisDataException 如果操作失败，例如key的值不是数字。
     */
    public static Long decr(String key) throws JedisDataException {
        if (isValueNull(key)) {
            return null;
        }
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.decr(key);
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * 添加一个或多个字符串值到指定的list中。如果操作成功（即值被添加），返回1；如果操作失败（如键或值为null），返回0。
     *
     * @param key   指定的list的键，不能为空。
     * @param value 要添加到list中的一个或多个字符串值，可以为空，但不能全部为空。
     *
     * @return 如果成功添加值到list中，返回1；如果因为键或值为null导致添加失败，返回0。
     */
    public static int setList(String key, String... value) {
        if (isValueNull(key, value)) {
            return 0;
        }
        Jedis jedis = null;
        try {
            jedis = getJedis();
            long result = jedis.rpush(key, value);
            if (result != 0) {
                return 1;
            } else {
                return 0;
            }
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * 向指定key的list中添加一个或多个<T>类型对象值。
     * 如果添加成功（即至少添加了一个值），返回1。
     * 如果添加失败（没有添加任何值），返回0。
     *
     * @param key   要添加数据的list的键，类型为String。
     * @param value 要添加到list中的一个或多个对象，类型为泛型T。可以使用变长参数（varargs）的形式传入。
     *
     * @return 如果至少成功添加了一个对象到list中，则返回1；如果未能添加任何对象，则返回0。
     */
    @SafeVarargs
    public static <T> int setList(String key, T... value) {
        if (isValueNull(key, value)) {
            return 0;
        }
        Jedis jedis = null;
        try {
            jedis = getJedis();
            int res = 0;
            for (T t : value) {
                byte[] data = enSeri(t);
                long result = jedis.rpush(key.getBytes(), data);
                if (result != 0) {
                    res++;
                }
            }
            if (res != 0) {
                return 1;
            } else {
                return 0;
            }
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * 缓存一个字符串值到list中,全部list的key默认缓存时间为1小时,成功返回1,失败返回0。
     * 用于将一个或多个字符串值添加到列表的末尾(尾插法)。如果操作成功，返回1；如果失败，返回0。
     * 注意，如果传入的key或value为null，将直接返回0，表示操作失败。
     *
     * @param key   列表的键，类型为String。该键不能为空。
     * @param value 要添加到列表中的一个或多个值，类型为String...。值可以为空，但至少要有一个非null的值。
     *
     * @return 操作结果，如果成功将值添加到列表中返回1，如果失败返回0。
     */
    public static int setExList(String key, String... value) {
        return setExList(key, DEFAULT_SETEX_TIMEOUT, value);
    }

    /**
     * 将字符串值缓存到List中，并设定整个List的缓存时间。
     * 成功缓存返回1，失败返回0。
     *
     * @param key     缓存的键名。
     * @param timeout 缓存的超时时间，单位为秒。
     * @param value   要缓存的字符串值，可以是多个。
     *
     * @return 缓存成功返回1，失败返回0。
     */
    public static int setExList(String key, int timeout, String... value) {
        if (isValueNull(key, value)) {
            return 0;
        }
        Jedis jedis = null;
        try {
            jedis = getJedis();
            long result = jedis.rpush(key, value);
            jedis.expire(key, timeout);
            if (result != 0) {
                return 1;
            } else {
                return 0;
            }

        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * 缓存一个<T>类型对象值到list中,全部list的key默认缓存时间为1小时,成功返回1,失败返回0。
     * 此方法会将给定的值追加到Redis的列表缓存中，如果缓存操作成功，则对于每个成功添加的值返回1，否则返回0。
     *
     * @param key   缓存的键，类型为String。
     * @param value 要缓存的对象，可以是多个。类型为<T>。
     *
     * @return 如果缓存操作成功（至少有一个值被成功添加到列表中），则返回1；如果所有值都添加失败或输入参数为null，则返回0。
     */
    @SafeVarargs
    public static <T> int setExList(String key, T... value) {
        return setExList(key, DEFAULT_SETEX_TIMEOUT, value);
    }

    /**
     * 缓存一个或多个<T>类型对象值到Redis列表中，指定列表的缓存时间。成功缓存所有值返回1，任何缓存失败返回0。
     *
     * @param key     缓存列表的键名。
     * @param timeout 缓存的超时时间，单位为秒。
     * @param value   要缓存到列表中的对象，可以是多个。
     *
     * @return 如果所有对象都成功缓存到列表中，则返回1；如果有任何对象缓存失败，则返回0。
     */
    @SafeVarargs
    public static <T> int setExList(String key, int timeout, T... value) {
        if (isValueNull(key, value)) {
            return 0;
        }
        Jedis jedis = null;
        try {
            jedis = getJedis();
            int res = 0;
            for (T t : value) {
                byte[] data = enSeri(t);
                long result = jedis.rpush(key.getBytes(), data);
                if (result != 0) {
                    res++;
                }
            }
            jedis.expire(key, timeout);
            if (res != 0) {
                return 1;
            } else {
                return 0;
            }
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * 添加一个List集合到存储系统中，如果添加成功返回1，失败则返回0。
     * 使用Jedis客户端将List序列化后存储，支持泛型类型。
     *
     * @param key   用于存储List的键，类型为String。
     * @param value 要存储的List集合，类型为泛型T。
     *
     * @return 添加成功返回1，添加失败返回0。
     *
     * @exception IOException      IO异常。
     * @exception RuntimeException 运行时异常。
     */
    public static <T> int setList(String key, List<T> value) throws RuntimeException, IOException {
        if (isValueNull(key, value)) {
            return 0;
        }
        Jedis jedis = null;
        try {
            jedis = getJedis();
            byte[] data = enSeriList(value);
            if (jedis.set(key.getBytes(), data).equalsIgnoreCase(OK)) {
                return 1;
            } else {
                return 0;
            }
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * 缓存一个List<T>集合到Redis中，成功返回1，失败返回0。默认缓存时间为1小时，使用本类中定义的常量DEFAULT_SETEX_TIMEOUT作为缓存时间。
     *
     * @param key   用于在Redis中标识缓存数据的键。
     * @param value 需要缓存的List<T>集合。
     *
     * @return 缓存成功返回1，失败返回0。
     *
     * @exception IOException      IO异常。
     * @exception RuntimeException 运行时异常。
     */
    public static <T> int setExList(String key, List<T> value) throws RuntimeException, IOException {
        if (isValueNull(key, value)) {
            return 0;
        }
        Jedis jedis = null;
        try {
            jedis = getJedis();
            byte[] data = enSeriList(value);
            if (jedis.setex(key.getBytes(), DEFAULT_SETEX_TIMEOUT, data).equalsIgnoreCase(OK)) {
                return 1;
            } else {
                return 0;
            }
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * 将一个List<T>类型的集合缓存起来，成功缓存返回1，失败返回0。缓存时间由timeout参数指定，单位为秒。
     * 使用了Redis作为缓存工具，通过将集合序列化为字节数组进行存储。
     *
     * @param key     缓存数据的键，类型为String。
     * @param value   需要缓存的List<T>类型的集合。
     * @param timeout 缓存数据的有效期，单位为秒。
     *
     * @return 缓存成功返回1，失败返回0。
     *
     * @exception IOException      IO异常。
     * @exception RuntimeException 运行时异常。
     */
    public static <T> int setExList(String key, List<T> value, int timeout) throws RuntimeException, IOException {
        if (isValueNull(key, value)) {
            return 0;
        }
        Jedis jedis = null;
        try {
            jedis = getJedis();
            byte[] data = enSeriList(value);
            if (jedis.setex(key.getBytes(), timeout, data).equalsIgnoreCase(OK)) {
                return 1;
            } else {
                return 0;
            }
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * 添加一个或多个字符串到Redis Set中。如果键不存在，则创建一个新的Set；如果键存在，将新字符串追加到该Set中。
     * 成功添加（即键存在且至少添加了一个新值，或键不存在但成功创建并添加了值）返回1，否则返回0。
     *
     * @param key   Redis中Set的键。
     * @param value 要添加到Set的一个或多个字符串值。
     *
     * @return 添加成功且有值被添加返回1，否则返回0。
     */
    public static int setSet(String key, String... value) {
        if (isValueNull(key, value)) {
            return 0;
        }
        Jedis jedis = null;
        try {
            jedis = getJedis();
            long result = jedis.sadd(key, value);
            if (result != 0) {
                return 1;
            } else {
                return 0;
            }
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * 添加一个字符串到Set中，并设置过期时间。如果Key已存在，则在原有Set基础上追加新字符串；如果Key不存在，则创建新的Set。
     * 整个Set的Key默认一个小时后过期。如果添加操作成功且有影响（即添加了新元素），则返回1；否则返回0。
     *
     * @param key   要操作的Set的Key。
     * @param value 要添加到Set中的一个或多个字符串。
     *
     * @return 添加结果。成功且有影响返回1，失败或无影响返回0。
     */
    public static int setExSet(String key, String... value) {
        return setExSet(key, DEFAULT_SETEX_TIMEOUT, value);
    }

    /**
     * 添加一个字符串到Set中，并设置Set的过期时间。如果键已存在，追加字符串到该Set中；如果键不存在，则创建新的Set。
     * 成功添加（或有影响）返回1，失败或无影响返回0。
     *
     * @param key     Set的键，类型为String。
     * @param timeOut Set中元素的有效期，单位为秒。
     * @param value   要添加到Set中的一个或多个字符串。
     *
     * @return int    添加结果：1表示成功添加或有影响，0表示失败或无影响。
     */
    public static int setExSet(String key, int timeOut, String... value) {
        if (isValueNull(key, value)) {
            return 0;
        }
        Jedis jedis = null;
        try {
            jedis = getJedis();
            long result = jedis.sadd(key, value);
            jedis.expire(key, timeOut);
            if (result != 0) {
                return 1;
            } else {
                return 0;
            }
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * 向指定的Redis Set中添加一个或多个元素，如果添加成功（即至少有一个元素被成功添加），则返回1，否则返回0。
     *
     * @param key   Redis中Set的键名。
     * @param value 要添加到Set的一个或多个元素。
     *
     * @return 如果至少有一个元素被成功添加到Set，则返回1；如果没有任何元素被添加或操作失败，则返回0。
     */
    @SafeVarargs
    public static <T> int setSet(String key, T... value) {
        if (isValueNull(key, value)) {
            return 0;
        }
        Jedis jedis = null;
        try {
            jedis = getJedis();
            int res = 0;
            for (T t : value) {
                byte[] data = enSeri(t);
                long result = jedis.sadd(key.getBytes(), data);
                if (result != 0) {
                    res++;
                }
            }
            if (res != 0) {
                return 1;
            } else {
                return 0;
            }
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * 缓存一个或多个<T>类型到set集合,如果key存在就在后面追加,整个set的key默认有效时间为1小时。成功缓存至少一个元素返回1，失败或没有影响返回0。
     *
     * @param key   用于标识缓存数据的键，类型为String。
     * @param value 要缓存的值，可以是单个值或多个值的可变参数。类型为泛型<T>。
     *
     * @return int 返回值为1表示至少有一个值成功缓存到set中，返回0表示缓存操作失败或没有影响。
     */
    @SafeVarargs
    public static <T> int setExSet(String key, T... value) {
        if (isValueNull(key, value)) {
            return 0;
        }
        Jedis jedis = null;
        try {
            jedis = getJedis();
            int res = 0;
            for (T t : value) {
                byte[] data = enSeri(t);
                long result = jedis.sadd(key.getBytes(), data);
                if (result != 0) {
                    res++;
                }
            }
            jedis.expire(key, DEFAULT_SETEX_TIMEOUT);
            if (res != 0) {
                return 1;
            } else {
                return 0;
            }
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * 缓存一个或多个<T>类型到set集合,如果key存在就在后面追加,整个set的key有效时间为timeOut秒。成功返回1，失败或没有影响返回0。
     * 注意：首先检查key和value是否为null，任何为null的情况都会直接返回0。
     * 对每个值进行序列化后添加到set中，并设置key的过期时间。
     *
     * @param key     缓存的键，类型为String。
     * @param timeout 缓存的键的有效时间，单位为秒。
     * @param value   要缓存的值，可以是多个<T>类型的值。
     *
     * @return 如果缓存操作成功且有值被添加，则返回1；如果操作失败或没有值被添加，则返回0。
     */
    @SafeVarargs
    public static <T> int setExSet(String key, int timeout, T... value) {
        if (isValueNull(key, value)) {
            return 0;
        }
        Jedis jedis = null;
        try {
            jedis = getJedis();
            int res = 0;
            for (T t : value) {
                byte[] data = enSeri(t);
                long result = jedis.sadd(key.getBytes(), data);
                if (result != 0) {
                    res++;
                }
            }
            jedis.expire(key, timeout);
            if (res != 0) {
                return 1;
            } else {
                return 0;
            }
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * 将一个Map<K, V>集合缓存到Redis中，指定缓存时间。成功缓存返回1，失败返回0。
     * 默认缓存时间为1小时，使用本类中定义的常量DEFAULT_SETEX_TIMEOUT作为缓存时间。
     *
     * @param key   缓存数据的键，类型为String。键不能为空或空字符串。
     * @param value 要缓存的数据，是一个Map<K, V>集合。value不能为null。
     *
     * @return 缓存成功返回1，失败返回0。
     */
    public static <K, V> int setExMap(String key, Map<K, V> value) {
        return setExMap(key, value, DEFAULT_SETEX_TIMEOUT);
    }

    /**
     * 将一个Map<K, V>集合缓存起来，成功返回1，失败返回0。缓存时间以timeout为准，单位为秒。
     * 使用JSON序列化将Map对象转换为字符串，然后存储在缓存中。
     *
     * @param key     缓存数据的键，必须不为null且非空字符串。
     * @param value   要缓存的数据，是一个Map对象。如果为null，则不进行缓存操作。
     * @param timeout 缓存数据的有效期，单位为秒。timeout大于0时表示缓存时间，等于0或小于0时表示不设置缓存时间。
     *
     * @return 操作成功返回1，失败返回0。
     */
    public static <K, V> int setExMap(String key, Map<K, V> value, int timeout) {
        if (value == null || key == null || key.isEmpty()) {
            return 0;
        }
        Jedis jedis = null;
        try {
            jedis = getJedis();
            String data = OBJECT_MAPPER.writeValueAsString(value);
            if (jedis.setex(key, timeout, data).equalsIgnoreCase(OK)) {
                return 1;
            } else {
                return 0;
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * 将多个key的值拼接并从Redis中获取
     *
     * @param keys key列表
     *
     * @return 如果key存在且对应的value不为空，则返回该value的字符串形式；如果key不存在或者value为空，则返回null。
     */
    public static String get(String... keys) {
        StringBuilder keyBuilder = new StringBuilder();
        for (String key : keys) {
            if (!keyBuilder.isEmpty()) {
                keyBuilder.append(REDIS_SEPARATOR);
            }
            keyBuilder.append(key);
        }
        return get(keyBuilder.toString());
    }

    /**
     * 从Redis获取指定key的字符串值
     *
     * @param key 需要获取值的键，类型为String
     *
     * @return 如果key存在且对应的value不为空，则返回该value的字符串形式；如果key不存在或者value为空，则返回null。
     */
    public static String get(String key) {
        if (isValueNull(key)) {
            return null;
        }
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.get(key);
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * 获得一个指定类型的对象
     *
     * @param key   从Redis中获取数据的键
     * @param clazz 需要返回的对象的类型
     *
     * @return 返回与给定类型相匹配的对象。如果key对应的数据不存在或发生异常，则返回null。
     */
    public static <T> T get(String key, Class<T> clazz) {
        if (isValueNull(key)) {
            return null;
        }
        Jedis jedis = null;
        try {
            jedis = getJedis();

            byte[] data = jedis.get(key.getBytes());
            return deSeri(data, clazz);
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * 获得一个字符串集合,区间以偏移量 START 和 END 指定。
     * 其中 0 表示列表的第一个元素， 1表示列表的第二个元素，以此类推。
     * 也可以使用负数下标，以 -1 表示列表的最后一个元素， -2 表示列表的倒数第二个元素，以此类推。
     *
     * @param key   列表的键名。
     * @param start 获取子集的起始偏移量。
     * @param end   获取子集的结束偏移量。
     *
     * @return 如果指定的Key存在且为列表类型，则返回该列表子集的字符串列表；如果Key不存在或类型不匹配，则返回null。
     */
    public static List<String> getList(String key, long start, long end) {
        if (isValueNull(key)) {
            return null;
        }
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.lrange(key, start, end);
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * 获得一个<T>类型的对象集合,区间以偏移量 START 和 END 指定。 其中 0 表示列表的第一个元素， 1 表示列表的第二个元素，以此类推。
     * 你也可以使用负数下标，以 -1 表示列表的最后一个元素， -2 表示列表的倒数第二个元素，以此类推。
     *
     * @param key   指定的Key，在Redis中作为列表的标识。
     * @param start 获取列表的起始下标，支持正向和负向索引。
     * @param end   获取列表的结束下标，支持正向和负向索引。
     * @param clazz 需要转换成的对象类型，指定返回的集合元素类型。
     *
     * @return 返回一个类型为<T>的列表对象，包含指定区间内的元素。如果指定的key不存在或区间为空，则返回null。
     */
    public static <T> List<T> getList(String key, long start, long end, Class<T> clazz) {
        if (isValueNull(key)) {
            return null;
        }
        Jedis jedis = null;
        try {
            jedis = getJedis();
            List<byte[]> listRange = jedis.lrange(key.getBytes(), start, end);
            List<T> result = null;
            if (listRange != null) {
                for (byte[] data : listRange) {
                    if (result == null) {
                        result = new ArrayList<>();
                    }
                    result.add(deSeri(data, clazz));
                }
            }
            return result;
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * 获取指定Redis列表的长度。
     *
     * @param key Redis中列表的键名。
     *
     * @return 如果指定键存在且为列表类型，则返回列表的元素数量；如果键不存在或类型不匹配，返回0。
     */
    public static long getListCount(String key) {
        if (isValueNull(key)) {
            return 0;
        }
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.llen(key);
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * 获得一个指定类型的List集合。
     * 通过给定的键值从Redis中获取数据，然后将其反序列化为指定的类型T的List集合。
     *
     * @param key   用于在Redis中获取数据的键。
     * @param clazz 指定返回集合中元素的类型。
     * @param <T>   泛型参数，表示返回集合中元素的类型。
     *
     * @return 根据给定键从Redis获取到的，类型为T的List集合。如果给定的键不存在或者值为null，则返回null。
     */
    public static <T> List<T> getList(String key, Class<T> clazz) {
        if (isValueNull(key)) {
            return null;
        }
        Jedis jedis = null;
        try {
            jedis = getJedis();
            byte[] data = jedis.get(key.getBytes());
            return deSeriList(data, clazz);
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * 获得一个与指定键相关联的字符串set集合。
     *
     * @param key 用于获取其关联set集合的键。
     *
     * @return 如果找到相关联的set集合，则返回它；如果key为null或不存在，则返回null。
     */
    public static Set<String> getSet(String key) {
        if (isValueNull(key)) {
            return null;
        }
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.smembers(key);
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * 根据指定的key从Redis获取一个字符串类型的set集合。
     *
     * @param key   Redis中存储的set的键。
     * @param clazz 需要转换成的Java对象的类型。
     *
     * @return 返回一个转换后的对象类型的set集合，如果key不存在或为null，则返回null。
     */
    public static <T> Set<T> getSet(String key, Class<T> clazz) {
        if (isValueNull(key)) {
            return null;
        }
        Jedis jedis = null;
        try {
            jedis = getJedis();
            Set<byte[]> smembers = jedis.smembers(key.getBytes());
            Set<T> result = null;
            if (smembers != null) {
                for (byte[] data : smembers) {
                    if (result == null) {
                        result = new HashSet<>();
                    }
                    result.add(deSeri(data, clazz));
                }
            }
            return result;
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * 获取Redis集合中值的数量
     *
     * @param key Redis中集合的键名
     *
     * @return 集合中值的数量。如果键不存在或值为null，则返回0。
     */
    public static long getSetCount(String key) {
        if (isValueNull(key)) {
            return 0;
        }
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.scard(key);
        } finally {
            returnJedis(jedis);
        }
    }


    // --------------------------公用方法区------------------------------------

    /**
     * 根据给定的key从Redis获取一个Map对象。首先检查key的有效性，然后使用Jedis客户端从Redis中获取对应key的值。
     * 如果key不存在或为空，方法将返回null。获取到的值将尝试通过JSON解析为Map类型，并且该Map的键值对类型由参数k和v指定。
     *
     * @param key 用于在Redis中标识数据的键，类型为String。
     * @param k   指定Map键的类型。
     * @param v   指定Map值的类型。
     *
     * @return 从Redis中解析出的Map对象，如果key不存在或为空，则返回null。返回的Map对象的键和值类型为参数k和v指定的类型。
     */
    public static <K, V> Map<K, V> getMap(String key, Class<K> k, Class<V> v) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        Jedis jedis = null;
        try {
            jedis = getJedis();
            String data = jedis.get(key);
            return OBJECT_MAPPER.readValue(data, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * 删除一个或多个键对应的值
     *
     * @param key 提供一个或多个要删除的键，类型为String的可变参数。
     *            每个键代表要从Redis中删除的数据。
     */
    public static void del(String... key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            for (int i = 0; i < key.length; i++) {
                jedis.del(key);
            }
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * 检查传入的值是否为null或空字符串。接受一个可变参数，对每个参数进行检查，
     * 如果任何一个参数为null或空字符串，则返回true，否则返回false。
     *
     * @param obj 可变参数，代表需要检查的值。
     *
     * @return 返回一个布尔值，如果任何一个值为null或空字符串，则返回true，否则返回false。
     */
    private static boolean isValueNull(Object... obj) {
        for (Object o : obj) {
            if (o == null || "".equals(o)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 将元素添加到REDIS列表的头部（即在列表的前面插入元素）。
     * 适用于需要顺序存储的场景。
     *
     * @param <T> 元素的类型。
     * @param obj 要添加到列表头部的元素。
     */
    public static <T> void rpush(String k, T obj) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            byte[] key = k.getBytes();
            byte[] value = enSeri(obj);
            jedis.lpush(key, value);
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * 从REDIS队列中取出最后一条元素
     *
     * @param <T> 泛型参数，表示返回值的类型
     * @param cls 类型参数的Class对象，用于反序列化Redis返回的二进制数据
     *
     * @return 返回队列尾部的元素，如果队列为空，则返回null。返回的元素类型为泛型T。
     */
    public static <T> T rpop(String k, Class<T> cls) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            byte[] key = k.getBytes();
            byte[] rpop = jedis.rpop(key);
            if (rpop == null || rpop.length == 0) {
                return null;
            } else {
                return deSeri(rpop, cls);
            }
        } finally {
            // 返还到连接池
            returnJedis(jedis);
        }
    }

    /**
     * 获取列表的长度。
     *
     * @param k 指定列表的键，类型为String。
     *
     * @return 返回列表的长度，类型为Long。
     */
    public static Long llen(String k) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            byte[] key = k.getBytes();
            return jedis.llen(key);
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * 往redis中添加map，默认缓存时间为1小时，使用本类中定义的常量DEFAULT_SETEX_TIMEOUT作为缓存时间。
     * 用于向Redis的哈希表中添加一个键值对。如果指定的键不存在，则会创建一个新的哈希表。
     *
     * @param key   Redis中键的名称。该键用于标识存储在Redis中的哈希表。
     * @param field Map中字段的名称。在哈希表中，字段相当于键。
     * @param value 字段对应的值。该值会与指定的字段一起存储在哈希表中。
     */
    public static void hset(String key, String field, String value) {
        hset(key, field, value, DEFAULT_SETEX_TIMEOUT);
    }

    /**
     * 往redis中添加map
     * 用于向Redis的哈希表中添加一个键值对。如果指定的键不存在，则会创建一个新的哈希表。
     * 通过指定超时时间，可以为键设置过期时间。
     *
     * @param key     Redis中键的名称。该键用于标识存储在Redis中的哈希表。
     * @param field   Map中字段的名称。在哈希表中，字段相当于键。
     * @param value   字段对应的值。该值会与指定的字段一起存储在哈希表中。
     * @param timeout 键的过期时间，单位为秒。设置为0表示永不过期。
     */
    public static void hset(String key, String field, String value, int timeout) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.hset(key, field, value);
            jedis.expire(key, timeout);
        } finally {
            // 返还到连接池
            returnJedis(jedis);
        }
    }

    /**
     * 往redis中添加map，默认缓存时间为1小时，使用本类中定义的常量DEFAULT_SETEX_TIMEOUT作为缓存时间。
     * 用于向Redis的哈希表中添加一个键值对。如果指定的键不存在，则会创建一个新的哈希表。
     * 通过指定超时时间，可以为键设置过期时间。
     *
     * @param key  Redis中键的名称。该键用于标识存储在Redis中的哈希表。
     * @param hash Map中字段的名称和值组成的键值对。在哈希表中，字段相当于键。
     */
    public static void hset(String key, Map<String, String> hash) {
        hset(key, hash, DEFAULT_SETEX_TIMEOUT);
    }

    /**
     * 往redis中添加map
     * 用于向Redis的哈希表中添加一个键值对。如果指定的键不存在，则会创建一个新的哈希表。
     * 通过指定超时时间，可以为键设置过期时间。
     *
     * @param key     Redis中键的名称。该键用于标识存储在Redis中的哈希表。
     * @param hash    Map中字段的名称和值组成的键值对。在哈希表中，字段相当于键。
     * @param timeout 键的过期时间，单位为秒。设置为0表示永不过期。
     */
    public static void hset(String key, Map<String, String> hash, int timeout) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.hset(key, hash);
            jedis.expire(key, timeout);
        } finally {
            // 返还到连接池
            returnJedis(jedis);
        }
    }

    /**
     * 从Redis中获取哈希表中指定字段的值。
     *
     * @param key   指定的key，对应Redis中的键。
     * @param field 指定的字段，对应Redis哈希表中的字段。
     *
     * @return 返回字段对应的值，如果不存在，则返回null。
     */
    public static String hget(String key, String field) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.hget(key, field);
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * 从Redis的哈希表中删除指定字段。
     *
     * @param key    指定的Redis键。
     * @param fields 要删除的一个或多个字段。
     *               注意：此方法不返回任何值，而是通过操作Redis来影响数据。
     */
    public static void hdel(String key, String... fields) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.hdel(key, fields);
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * 将消息推送到Redis消息通道
     * 此方法用于向指定的Redis通道发布消息。消息发布后，订阅了该通道的客户端将会收到消息。
     *
     * @param channel String类型，消息将要推送到的Redis通道的名称。
     * @param message String类型，要推送的消息内容。
     */
    public static void publish(String channel, String message) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.publish(channel, message);
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * 监听消息通道
     * 通过JedisPubSub实现对Redis消息通道的订阅。一旦订阅成功，任何在这些通道上发布的消息都会被此监听任务捕获。
     *
     * @param jedisPubSub 监听任务，负责处理订阅过程中发生的各种事件，如消息接收。
     * @param channels    要监听的消息通道，可以是一个或多个通道。
     */
    public static void subscribe(JedisPubSub jedisPubSub, String... channels) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.subscribe(jedisPubSub, channels);
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * 序列化一个对象，将其转换为字节数组。
     *
     * @param obj 需要被序列化的对象。该对象必须实现Serializable接口。
     * @param <T> 被序列化对象的类型。
     *
     * @return 如果对象成功序列化，则返回包含序列化对象的字节数组；如果序列化过程中发生异常，则返回null。
     */
    public static <T> byte[] enSeri(T obj) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(obj);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 反序列化一个对象
     * 用于将字节流数据（data）反序列化成指定类型的对象（cls）。
     * 如果反序列化过程中出现异常，则返回null。
     *
     * @param data 要被反序列化的字节流数据。
     * @param cls  指定反序列化后的对象类型。
     *
     * @return 反序列化后的对象，如果反序列化过程中出现异常则返回null。
     */
    public static <T> T deSeri(byte[] data, Class<T> cls) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
             ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            return cls.cast(objectInputStream.readObject());
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * 序列化 List 集合为字节数组。
     * 将给定的 List 对象序列化为字节数组，以便于存储或网络传输。
     *
     * @param list 需要被序列化的 List 对象。不可为 null 且不可为空。
     *
     * @return 返回序列化后的字节数组。
     *
     * @exception RuntimeException 如果传入的 list 为 null 或空，抛出此异常。
     * @exception IOException      在序列化过程出现错误时抛出。
     */
    private static <T> byte[] enSeriList(List<T> list) throws RuntimeException, IOException {
        if (list == null || list.isEmpty()) {
            throw new RuntimeException("集合不能为空!");
        }
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(list);
            return byteArrayOutputStream.toByteArray();
        }
    }

    /**
     * 反序列化 List 集合
     * 本方法用于将序列化的数据恢复为 List 对象。如果输入的序列化数据为 null 或空，则返回一个空的 List，而不是 null。
     * 注意，反序列化的对象必须是 List 类型，并且列表中的每个元素都必须是指定的 clazz 类型。
     *
     * @param data  序列化数据，即待反序列化的字节数组。
     * @param clazz 对象类型，指定反序列化列表中元素的类型。
     * @param <T>   泛型，表示列表中元素的类型。
     *
     * @return 反序列化后的 List 对象。如果输入为 null 或空，则返回空列表。
     *
     * @exception RuntimeException 如果反序列化过程中发生错误（如数据格式不正确或类找不到）。
     */
    private static <T> List<T> deSeriList(byte[] data, Class<T> clazz) {
        // 检查输入数据是否为空，若为空则直接返回空列表
        if (data == null || data.length == 0) {
            return new ArrayList<>();
        }
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
             ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            // 反序列化对象
            Object obj = objectInputStream.readObject();
            // 验证反序列化的对象是否为 List 类型
            if (obj instanceof List<?> list) {
                List<T> result = new ArrayList<>(list.size());
                // 遍历列表，类型转换，并添加到结果列表中
                for (Object item : list) {
                    result.add(clazz.cast(item));
                }
                return result;
            }
        } catch (IOException | ClassNotFoundException e) {
            // 反序列化异常处理，抛出运行时异常
            throw new RuntimeException("反序列化过程中发生错误。", e);
        }
        // 如果反序列化失败或对象不是预期的 List 类型，则返回空列表
        return new ArrayList<>();
    }

}
