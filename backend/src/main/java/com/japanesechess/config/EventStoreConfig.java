package com.japanesechess.config;

import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.EventStoreDBClientSettings;
import com.eventstore.dbclient.EventStoreDBConnectionString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventStoreConfig {

    @Value("${eventstore.connection-string}")
    private String connectionString;

    @Bean
    public EventStoreDBClient eventStoreDBClient() throws Exception {
        EventStoreDBClientSettings settings = EventStoreDBConnectionString.parseOrThrow(connectionString);
        return EventStoreDBClient.create(settings);
    }
}
