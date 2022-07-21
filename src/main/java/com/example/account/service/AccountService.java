package com.example.account.service;

import com.example.account.exception.SumNotExistException;
import com.example.account.model.BalanceRequest;
import com.example.account.model.PersonAccount;
import lombok.SneakyThrows;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AccountService {

    private static final String SELECT_ACC = "select id, person_id, account_num, balance" +
            "from person_account where person_id = :personId for update";

    private static final String SELECT_BALANCE_REQUEST =
            "select * from balance_request 1s where 1s.person_id = :personId for update";

    private static final String INSERT_REQUEST =
            "insert into balance_request (id, person_id, balance) values (:id, :personId, :balance)";

    private static final String UPDATE_ACC =
            "update person_account set balance = :balance where id = :id";

    private static final String DELETE_REQUEST =
            "delete from balance_request where id = :requestId";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public AccountService(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<PersonAccount> getPersonAccount(UUID personId) {
        return jdbcTemplate.query(SELECT_ACC, Map.of("personId", personId),
                new BeanPropertyRowMapper<>(PersonAccount.class));
    }

    public List<BalanceRequest> getBalanceRequest(UUID personId) {
        return jdbcTemplate.query(SELECT_BALANCE_REQUEST, Map.of("personId", personId),
                new BeanPropertyRowMapper<>(BalanceRequest.class));
    }

    @SneakyThrows
    @Transactional(rollbackFor = Exception.class,
    isolation = Isolation.REPEATABLE_READ)
    public void createBalanceRequest(UUID requestId, UUID personId, BigDecimal requiredSum) {
        var exitSum = getPersonAccount(personId)
                .stream()
                .map(PersonAccount::getBalance)
                .reduce(BigDecimal::add);

        if (exitSum.isEmpty() || exitSum.get().compareTo(requiredSum) < 0) {
            throw new SumNotExistException(personId, requiredSum);
        }

        var blockedSum = getBalanceRequest(personId)
                .stream()
                .map(BalanceRequest::getBalance)
                .reduce(BigDecimal::add);

        if (blockedSum.isPresent() && exitSum.get().negate().compareTo(requiredSum) < 0) {
            throw new SumNotExistException(personId, requiredSum);
        }

        jdbcTemplate.update(INSERT_REQUEST, Map.of("id", requestId, "personId", personId, "balance", requiredSum));
    }

    public void takeSumFromPersonAccount(PersonAccount account, BigDecimal spentSum) {
        jdbcTemplate.update(UPDATE_ACC, Map.of("id", account.getId(), "balance", account.getBalance().add(spentSum.negate())));
    }

    public void cancelBalanceRequest(UUID requestId) {
        jdbcTemplate.update(DELETE_REQUEST, Map.of("requestId", requestId));
    }

    @SneakyThrows
    @Transactional(rollbackFor = Exception.class,
    isolation = Isolation.REPEATABLE_READ)
    public void executeBalanceRequest(UUID requestId, UUID personId, BigDecimal spentSum) {
        var accounts = getPersonAccount(personId);
        BigDecimal requiredSum = spentSum;
        for (PersonAccount account : accounts) {
            if (account.getBalance().compareTo(requiredSum) >= 0) {
                takeSumFromPersonAccount(account, requiredSum);
                break;
            }
            takeSumFromPersonAccount(account, account.getBalance());
            requiredSum = requiredSum.add(account.getBalance().negate());
        }
        jdbcTemplate.update(DELETE_REQUEST, Map.of("requestId", requestId));
    }
}
