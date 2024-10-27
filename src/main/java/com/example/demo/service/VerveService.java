package com.example.demo.service;

import java.util.Map;
import java.util.logging.Logger;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
public class VerveService {

    @Autowired
    private RedisTemplate<String, Integer> redisTemplate;
    
    private final RestTemplate restTemplate = new RestTemplate();

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private Logger logger = Logger.getLogger(VerveService.class.getName());

    
    public boolean process(int id, String endpoint) {
        if (endpoint == null || endpoint.isEmpty()) {
            return true;
        }

        final long nb = registerCall(id);
        logger.info(endpoint + " " + nb);
        
        final int code = doPostRequest(endpoint, nb);
        if (code >= 200 && code < 400) {
            return true;
        }

        return false;
    }
    
    private int doPostRequest(String url, long uniqueCount) {        
        Map<String, Object> payload = new HashMap<>();
        payload.put("uniqueCount", uniqueCount);

        if (url != null && !url.isEmpty()) {
            try {
                ResponseEntity<String> response = restTemplate.postForEntity(
                    url, 
                    payload, 
                    String.class
                );
                logger.info("POST request status code: " + response.getStatusCode());
                return response.getStatusCode().value();
            } catch (Exception e) {
                logger.severe(url + " is not reachable" + " " + e.getMessage());
            }
        }
        
        return -1;
    }

    private long registerCall(int id) {
        final String keyTime = LocalDateTime.now().format(FORMATTER);
        final String hashKey = keyTime + "_" + id;
        return redisTemplate.opsForValue().increment(hashKey, 1);
    }


    // Problem 1: This method is not thread-safe
    // Problem 2: This method is not efficient because it is called every minute
    // Problem 3: This method is not scalable because each pods will perform the same operation
    // Solution1: Use another service responsible reading Redis and sending the data to Kafka
    // Solution2: Every time a call is being made, send the data to Kafka, and use a separate service to aggregate the data
    @Scheduled(cron = "0 * * * * *") // Runs at the start of every minute
    private void sendUniqueCallsCountToKafka(Set<String> uniqueCount) {
        final Set<String> keys = flushOldKeysFromRedis();

        if (keys != null && !keys.isEmpty()) {
            for (String key : keys) {
                final String[] parts = key.split("_");
                final int id = Integer.parseInt(parts[1]);
                final long count = redisTemplate.opsForValue().get(key);
                logger.info("Sending to Kafka: " + id + " " + count);
                // TODO: Send to Kafka
            }
        }
    }

    private Set<String> flushOldKeysFromRedis() {

        final String previousMinute = LocalDateTime.now().minusMinutes(1).format(FORMATTER);

        Set<String> keysToDelete = redisTemplate.keys(previousMinute + "*");

        if (keysToDelete != null && !keysToDelete.isEmpty()) {
            long count = redisTemplate.delete(keysToDelete);
            logger.info("Deleted keys: " + keysToDelete + " count " + count);
        }

        return keysToDelete;
    }
}
