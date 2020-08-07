package com.demo.cometd.service;

import com.demo.cometd.dao.TestDAO;
import com.demo.cometd.reposiroty.DBRepository;
import org.springframework.beans.factory.annotation.Autowired;



public class MyDBService {

    @Autowired
    private DBRepository repository;

    public TestDAO save(TestDAO dao){
       return  this.repository.save(dao);
    }
}
