package com.fkt.network.repositories;

import com.fkt.network.models.NetworkRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NetworkRecordRepository extends MongoRepository<NetworkRecord,String> {
    List<NetworkRecord> findByFullNetworkRecordIs(String fullName);
}
