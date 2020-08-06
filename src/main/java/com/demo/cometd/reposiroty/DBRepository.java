package com.demo.cometd.reposiroty;

import com.demo.cometd.dao.TestDAO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DBRepository extends JpaRepository<TestDAO, Long> {
}

