package top.belovedyaoo.acs.core.log;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 注解形式的监听 异步监听日志事件
 *
 * @author BelovedYaoo
 * @version 1.0
 */
@Slf4j
@Component
public class InterfaceLogListener {

    @Async
    @Order
    @EventListener(InterfaceLogEvent.class)
    public void saveInterfaceLog(InterfaceLogEvent event) {
        InterfaceLogPO interfaceLog = (InterfaceLogPO) event.getSource();
        System.out.println(interfaceLog);
    }

}
