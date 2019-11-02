package com.botreminder.botreminder.database.repository;

import com.botreminder.botreminder.database.entity.Records;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

@Repository
public interface RecordsRepository extends JpaRepository<Records, Long> {

    List<Records> findAllByDate(Timestamp date);
    List<Records> findAllByNotifiedIsNull();


}
