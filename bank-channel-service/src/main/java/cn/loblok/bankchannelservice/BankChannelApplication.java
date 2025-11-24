package cn.loblok.bankchannelservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "cn.loblok.common.dao")
@EntityScan(basePackages = "cn.loblok.common.entity") // 如果实体类在 entity 包
public class BankChannelApplication {
    public static void main(String[] args) {
        SpringApplication.run(BankChannelApplication.class,args);
    }
}