package com.botreminder.botreminder.database.service;

import com.botreminder.botreminder.core.Bot;
import com.botreminder.botreminder.database.entity.Records;
import com.botreminder.botreminder.database.repository.RecordsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.List;

@Service
public class RecordsService {
    private static final Logger logger = LoggerFactory.getLogger(RecordsService.class);
    @Autowired
    private final RecordsRepository recordsRepository;

    public RecordsService(RecordsRepository recordsRepository) {
        this.recordsRepository = recordsRepository;
    }

    public void createRecords (Records records){
        logger.info("records creating");
        recordsRepository.save(records);
    }
    public void deleteRecords (Records records){
        recordsRepository.delete(records);
    }

    public List<Records> findAllByDate (Date date){
        return recordsRepository.findAllByDate(date);
    }

}
