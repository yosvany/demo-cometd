package com.demo.cometd;

import com.demo.cometd.dao.TestDAO;
import com.demo.cometd.reposiroty.DBRepository;
import org.springframework.beans.factory.annotation.Autowired;



public class MyDBService {

    @Autowired
    DBRepository repository;

    public void save(TestDAO dao){
        this.repository.save(dao);
    }
}
