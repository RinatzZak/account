package com.example.account.service;

import com.example.account.AccountApplication;
import com.example.account.exception.SumNotExistException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Objects;
import java.util.UUID;

import static java.math.BigDecimal.valueOf;
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
        UUID personId = UUID.randomUUID();
        jdbcTemplate.update("insert into person_account (id, person_id, account_num, balance)"
                + " values(?,?,?,?)", UUID.randomUUID(), personId, "3948509380081", 200000);

        UUID requestId = UUID.randomUUID();
        service.createBalanceRequest(requestId, personId, valueOf(199000));

        var result = jdbcTemplate.queryForMap(" select * from balance_request where id = ?", requestId);

        assertTrue(result.size() > 0);
        assertEquals(personId, result.get("person_id"));
        assertEquals(valueOf(199000), result.get("balance"));
    }

    @Test
    public void createBalanceRequestForOneAccount() throws SumNotExistException {
        UUID personId = UUID.randomUUID();
        jdbcTemplate.update("insert into person_account (id, person_id, account_num, balance)"
                + " values(?,?,?,?)", UUID.randomUUID(), personId, "3948509380081", 200000);

        UUID requestId = UUID.randomUUID();
        service.createBalanceRequest(requestId, personId, valueOf(200000));

        var result = jdbcTemplate.queryForMap(" select * from balance_request where id = ?", requestId);

        assertTrue(result.size() > 0);
        assertEquals(personId, result.get("person_id"));
        assertEquals(valueOf(200000), result.get("balance"));
    }

    @Test
    public void createBalanceRequestForTwoAccount() throws SumNotExistException {
        UUID personId = UUID.randomUUID();
        jdbcTemplate.update("insert into person_account (id, person_id, account_num, balance)"
                + " values(?,?,?,?)", UUID.randomUUID(), personId, "3948509380081", 180000);
        jdbcTemplate.update("insert into person_account (id, person_id, account_num, balance)"
                + " values(?,?,?,?)", UUID.randomUUID(), personId, "3948509380082", 20000);


        UUID requestId = UUID.randomUUID();
        service.createBalanceRequest(requestId, personId, valueOf(200000));

        var result = jdbcTemplate.queryForMap(" select * from balance_request where id = ?", requestId);

        assertTrue(result.size() > 0);
        assertEquals(personId, result.get("person_id"));
        assertEquals(valueOf(200000), result.get("balance"));
    }

    @Test
    public void createBalanceRequestForTwoAccountNegative() {
        UUID personId = UUID.randomUUID();
        jdbcTemplate.update("insert into person_account (id, person_id, account_num, balance)"
                + " values(?,?,?,?)", UUID.randomUUID(), personId, "3948509380081", 180000);
        jdbcTemplate.update("insert into person_account (id, person_id, account_num, balance)"
                + " values(?,?,?,?)", UUID.randomUUID(), personId, "3948509380082", 20000);

        UUID requestId = UUID.randomUUID();
        Throwable throwable = assertThrows(SumNotExistException.class, () -> service.createBalanceRequest(requestId, personId, valueOf(300000)));
        assertTrue(throwable.getMessage().contains("Person " + personId + " doesn't have needed sum " + 300000));
    }

    @Test
    public void createTwoBalanceRequestNegative() {
        UUID personId = UUID.randomUUID();
        jdbcTemplate.update("insert into person_account (id, person_id, account_num, balance)"
                + " values(?,?,?,?)", UUID.randomUUID(), personId, "3948509380082", 2000);
        jdbcTemplate.update("insert into person_account (id, person_id, account_num, balance)"
                + " values(?,?,?,?)", UUID.randomUUID(), personId, "3948509380082", 20000);

        UUID requestId = UUID.randomUUID();
        Throwable throwable = assertThrows(SumNotExistException.class, () -> {
            service.createBalanceRequest(requestId, personId, valueOf(18_000));
            service.createBalanceRequest(requestId, personId, valueOf(18_000));
        });
        assertTrue(throwable.getMessage().contains("Person "));
    }

    @Test
    public void createDuplicateBalanceRequest() throws SumNotExistException {
        UUID personId = UUID.randomUUID();
        jdbcTemplate.update("insert into person_account (id, person_id, account_num, balance)"
                + " values(?,?,?,?)", UUID.randomUUID(), personId, "3948509380081", 200_000);

        UUID requestId = UUID.randomUUID();

        Throwable throwable = Assertions.assertThrows(DuplicateKeyException.class, () ->
        {
            service.createBalanceRequest(requestId, personId, valueOf(2_000));
            service.createBalanceRequest(requestId, personId, valueOf(2_000));
        });
        assertTrue((throwable.getMessage()).contains("Exception"));
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