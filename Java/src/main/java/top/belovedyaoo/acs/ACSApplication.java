package top.belovedyaoo.acs;

import com.tangzc.autotable.springboot.EnableAutoTable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 项目启动类
 *
 * @author BelovedYaoo
 * @version 1.0
 */
@EnableAutoTable
@SpringBootApplication
public class ACSApplication {

    public static void main(String[] args) {
        SpringApplication.run(ACSApplication.class, args);
    }

}
