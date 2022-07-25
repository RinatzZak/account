package com.example.account.service;

import com.example.account.AccountApplication;
import com.example.account.exception.SumNotExistException;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;


import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AccountApplication.class)
class AccountServiceTest {

    @Autowired
    private AccountService service;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void createBalanceRequestLessThanAccount() throws SumNotExistException {
        UUID personId =  UUID.randomUUID();
        jdbcTemplate.update("insert into person_account (id, person_id, account_num, balance)"
                + " values(?,?,?,?)", UUID.randomUUID(), personId, "3948509380081", 200000);

        UUID requestId =  UUID.randomUUID();
        service.createBalanceRequest(requestId, personId, BigDecimal.valueOf(199000));

        var result = jdbcTemplate.queryForMap(" select * from balance_request where id = ?", requestId);

        assertTrue(result.size() > 0);
        assertEquals(personId, result.get("person_id"));
        assertEquals(BigDecimal.valueOf(199000), result.get("balance"));
    }

    @Test
    public void createBalanceRequestForOneAccount() throws SumNotExistException {
        UUID personId =  UUID.randomUUID();
        jdbcTemplate.update("insert into person_account (id, person_id, account_num, balance)"
                + " values(?,?,?,?)", UUID.randomUUID(), personId, "3948509380081", 200000);

        UUID requestId =  UUID.randomUUID();
        service.createBalanceRequest(requestId, personId, BigDecimal.valueOf(200000));

        var result = jdbcTemplate.queryForMap(" select * from balance_request where id = ?", requestId);

        assertTrue(result.size() > 0);
        assertEquals(personId, result.get("person_id"));
        assertEquals(BigDecimal.valueOf(200000), result.get("balance"));
    }

    @Test
    public void createBalanceRequestForTwoAccount() throws SumNotExistException {
        UUID personId =  UUID.randomUUID();
        jdbcTemplate.update("insert into person_account (id, person_id, account_num, balance)"
                + " values(?,?,?,?)", UUID.randomUUID(), personId, "3948509380081", 180000);
        jdbcTemplate.update("insert into person_account (id, person_id, account_num, balance)"
                + " values(?,?,?,?)", UUID.randomUUID(), personId, "3948509380082", 20000);


        UUID requestId =  UUID.randomUUID();
        service.createBalanceRequest(requestId, personId, BigDecimal.valueOf(200000));

        var result = jdbcTemplate.queryForMap(" select * from balance_request where id = ?", requestId);

        assertTrue(result.size() > 0);
        assertEquals(personId, result.get("person_id"));
        assertEquals(BigDecimal.valueOf(200000), result.get("balance"));
    }

    @Test
    public void createBalanceRequestForTwoAccountNegative() throws SumNotExistException {
        UUID personId = UUID.randomUUID();
        jdbcTemplate.update("insert into person_account (id, person_id, account_num, balance)"
                + " values(?,?,?,?)", UUID.randomUUID(), personId, "3948509380081", 180000);
        jdbcTemplate.update("insert into person_account (id, person_id, account_num, balance)"
                + " values(?,?,?,?)", UUID.randomUUID(), personId, "3948509380082", 20000);

        UUID requestId = UUID.randomUUID();
        Throwable throwable = assertThrows(SumNotExistException.class, () -> service.createBalanceRequest(requestId, personId, BigDecimal.valueOf(300000)));
        assertTrue(throwable.getMessage().contains("Person " + personId + " doesn't have needed sum " + 300000));
    }

    @Test
    void takeSumFromPersonAccount() {
    }

    @Test
    void cancelBalanceRequest() {
    }

    @Test
    void executeBalanceRequest() {
    }
}