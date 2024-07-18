package dev.codescreen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class BankLedgerApplication {
	 public static void main(String[] args) {
	        SpringApplication.run(BankLedgerApplication.class, args);
	        System.out.println("service is running on localhost try our rest api service ");
	    }
}
