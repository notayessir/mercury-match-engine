
package com.notayessir;

import com.notayessir.config.AppConfiguration;
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
		String raftGroupList = appConfiguration.getRaftGroupList();
		String raftServerList = appConfiguration.getRaftServerList();
		String[] serverConfigStr = raftServerList.split("\\|");
		String[] groupArr = raftGroupList.split("\\|");
		// check param
		if (groupArr.length != serverConfigStr.length){
			throw new RuntimeException("raftGroupList and raftServerList are not configured correctly.");
		}
		for (String serverConfigs : serverConfigStr) {
			String[] serverConfig = serverConfigs.split(";");
			if (serverConfig.length != 2){
				throw new RuntimeException("server config should be 2, but found " + serverConfig.length);
			}
			String[] serverArr = serverConfig[0].split(",");
			if (serverArr.length < 3){
				throw new RuntimeException("server config should greater or equal to 3, but found " + serverArr.length);
			}
		}

		// start server
		String raftServerMode = appConfiguration.getRaftServerMode();
		if (StringUtils.equalsIgnoreCase(raftServerMode, EnumServerMode.SINGLE.name())){
			for (int i = 0; i < groupArr.length; i++) {
				String groupId = groupArr[i];
				String[] serverConfig = serverConfigStr[i].split(";");
				String[] serverArr = serverConfig[0].split(",");
				for (int j = 0; j < serverArr.length; j++) {
					List<String> addresses = Arrays.asList(serverArr);
					MatchServerConfig config = MatchServerConfig.builder()
							.addresses(addresses).dirname(appConfiguration.getRaftStorageDir())
							.groupId(groupId).index(j)
							.publishers(publishers)
							.build();
					MatchServer matchServer = new MatchServer(config);
					matchServer.start();
				}
			}
		} else {
			// GROUP mode
			for (int i = 0; i < groupArr.length; i++) {
				String groupId = groupArr[i];
				String[] serverConfig = serverConfigStr[i].split(";");
				String[] serverArr = serverConfig[0].split(",");
				List<String> addresses = Arrays.asList(serverArr);
				MatchServerConfig config = MatchServerConfig.builder()
						.addresses(addresses).dirname(appConfiguration.getRaftStorageDir())
						.groupId(groupId).index(i)
						.publishers(publishers)
						.build();
				MatchServer matchServer = new MatchServer(config);
				matchServer.start();
			}
		}

	}


}
