package com.example.wtow_transfer.aspect;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Aspect
@Component
public class RlsAspect {

    @PersistenceContext(unitName = "accountManager")
    EntityManager accountManagerEntityManager;

    @Before("@annotation(com.example.wtow_transfer.annotation.RlsProtected) && @annotation(transactional)")
    public void setCurrentUserIdLocalToTransaction(Transactional transactional) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UUID userId) {
            EntityManager entityManager = resolveEntityManager(transactional.transactionManager());
            entityManager.createNativeQuery("SELECT set_config('app.current_user_id', :uid, true)")
                    .setParameter("uid", userId.toString())
                    .getSingleResult();
        }
    }

    private EntityManager resolveEntityManager(String transactionManagerName) {
        if (transactionManagerName.equals("accountManagerTransactionManager")) {
            return accountManagerEntityManager;
        }

        throw new IllegalStateException("No EntityManager mapping for " + transactionManagerName);
    }
}
