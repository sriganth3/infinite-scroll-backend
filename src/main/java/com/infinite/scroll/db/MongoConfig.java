package com.infinite.scroll.db;


import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfig {
	

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database}")
    private String mongoDB;

    @Bean
    MongoDatabase mongoDatabase() {
        MongoClient mongoClient = MongoClients.create(mongoUri);
        return mongoClient.getDatabase(mongoDB);  
    }
}
