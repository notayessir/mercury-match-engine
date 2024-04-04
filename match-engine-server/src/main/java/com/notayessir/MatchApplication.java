
package com.notayessir;

import com.notayessir.config.AppConfiguration;
import com.notayessir.config.RaftConfig;
import com.notayessir.config.RaftServerConfiguration;
import com.notayessir.constant.EnumServerMode;
import com.notayessir.match.engine.MatchServer;
import com.notayessir.match.engine.MatchServerConfig;
import com.notayessir.match.engine.publisher.Publisher;
import com.notayessir.match.engine.publisher.impl.KafkaPublisher;
import com.notayessir.match.engine.publisher.impl.LogPublisher;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class MatchApplication implements CommandLineRunner {



	public static void main(String[] args) {

		SpringApplication.run(MatchApplication.class, args);
	}


	@Autowired
	private AppConfiguration appConfiguration;

	@Autowired
	private RaftServerConfiguration raftServerConfiguration;

	@Autowired
	private KafkaPublisher kafkaPublisher;

	@Autowired
	private LogPublisher logPublisher;

	@Override
	public void run(String... args) throws Exception {
		List<Publisher> publishers = new ArrayList<>();
		if (appConfiguration.isKafkaEnable()){
			publishers.add(kafkaPublisher);
		}
		if (appConfiguration.isLogEnable()){
			publishers.add(logPublisher);
		}
		List<RaftConfig> configs = raftServerConfiguration.getConfigs();
		for (RaftConfig config : configs) {
			if (config.getAddresses().size() < 3){
				throw new RuntimeException("server config should greater or equal to 3, but found " + config.getAddresses().size());
			}
		}

		for (RaftConfig config : configs) {
			if (StringUtils.equalsIgnoreCase(config.getMode(), EnumServerMode.SINGLE.name())){
				for (int j = 0; j < config.getAddresses().size(); j++) {
					MatchServerConfig serverConfig = MatchServerConfig.builder()
							.addresses(config.getAddresses()).dirname(config.getStorage())
							.groupId(config.getGroupId()).index(j)
							.publishers(publishers)
							.build();
					MatchServer matchServer = new MatchServer(serverConfig);
					matchServer.start();
				}
			}else {
				// GROUP mode
				MatchServerConfig serverConfig = MatchServerConfig.builder()
						.addresses(config.getAddresses()).dirname(config.getStorage())
						.groupId(config.getGroupId()).index(config.getIndex())
						.publishers(publishers)
						.build();
				MatchServer matchServer = new MatchServer(serverConfig);
				matchServer.start();
			}
		}

	}


}
