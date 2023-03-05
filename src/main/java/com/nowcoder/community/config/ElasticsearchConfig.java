package com.nowcoder.community.config;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;

import java.time.Duration;

/**
 * @Projectname: community
 * @Filename: ElasticsearchConfig
 * @Author: yunqi
 * @Date: 2023/3/3 20:19
 * @Description: TODO
 */

@Configuration
//public class ElasticsearchConfig extends AbElasticsearchConfiguration {
//    @Bean
//    @Override
//    public ClientConfiguration clientConfiguration() {
//        return ClientConfiguration
//                .builder()
//                .connectedTo("127.0.0.1:9200")
//                .withConnectTimeout(Duration.ofSeconds(10))
//                .withSocketTimeout(Duration.ofSeconds(30))
//                .build();
//    }
//}

public class ElasticsearchConfig extends AbstractElasticsearchConfiguration {
    @Bean

    @Override
    public RestHighLevelClient elasticsearchClient() {
        ClientConfiguration clientConfiguration = ClientConfiguration
                .builder()
                .connectedTo("127.0.0.1:9200")
                .withConnectTimeout(Duration.ofSeconds(10))
                .withSocketTimeout(Duration.ofSeconds(30))
                .build();
        return RestClients.create(clientConfiguration).rest();
    }
}

