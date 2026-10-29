package bg.fmi.uni.boomvox;

import org.springframework.boot.SpringApplication;

public class TestBoomvoxApplication {

	public static void main(String[] args) {
		SpringApplication.from(BoomvoxApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
