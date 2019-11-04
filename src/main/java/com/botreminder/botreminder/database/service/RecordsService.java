package com.botreminder.botreminder.database.service;

import com.botreminder.botreminder.database.entity.Records;
import com.botreminder.botreminder.database.repository.RecordsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class RecordsService {
    private static final Logger logger = LoggerFactory.getLogger(RecordsService.class);
    @Autowired
    private final RecordsRepository recordsRepository;

    public RecordsService(RecordsRepository recordsRepository) {
        this.recordsRepository = recordsRepository;
    }

    //Method that creates records in the database
    public void createRecords (Records records){
        logger.info("records creating");
        recordsRepository.save(records);
    }

    //Method that finds records that match the entered date
    public List<Records> findAllByDate (Timestamp date){

        return recordsRepository.findAllByDate(date);
    }


    //Method that finds records by chatid and data>current data
    public List<Records> findRecordsForNotifications(long chatId){
        return recordsRepository.findRecordsForNotifications(chatId);}

}
